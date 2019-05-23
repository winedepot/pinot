/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.integration.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.pinot.common.utils.CommonConstants;
import org.apache.pinot.common.utils.JsonUtils;
import org.apache.pinot.common.utils.ServiceStatus;
import org.apache.pinot.core.indexsegment.generator.SegmentVersion;
import org.apache.pinot.core.plan.SelectionPlanNode;
import org.apache.pinot.util.TestUtils;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


/**
 * Integration test that converts Avro data for 12 segments and runs queries against it.
 */
public class OfflineClusterIntegrationTest extends BaseClusterIntegrationTestSet {
  private static final int NUM_BROKERS = 1;
  private static final int NUM_SERVERS = 1;
  private static final int NUM_SEGMENTS = 12;

  // For inverted index triggering test
  private static final List<String> UPDATED_INVERTED_INDEX_COLUMNS =
      Arrays.asList("FlightNum", "Origin", "Quarter", "DivActualElapsedTime");
  private static final String TEST_UPDATED_INVERTED_INDEX_QUERY =
      "SELECT COUNT(*) FROM mytable WHERE DivActualElapsedTime = 305";

  private static final List<String> UPDATED_BLOOM_FLITER_COLUMNS = Arrays.asList("Carrier");
  private static final String TEST_UPDATED_BLOOM_FILTER_QUERY = "SELECT COUNT(*) FROM mytable WHERE Carrier = 'CA'";

  // For default columns test
  private static final String SCHEMA_WITH_EXTRA_COLUMNS =
      "On_Time_On_Time_Performance_2014_100k_subset_nonulls_default_column_test_extra_columns.schema";
  private static final String SCHEMA_WITH_MISSING_COLUMNS =
      "On_Time_On_Time_Performance_2014_100k_subset_nonulls_default_column_test_missing_columns.schema";
  private static final String TEST_DEFAULT_COLUMNS_QUERY =
      "SELECT COUNT(*) FROM mytable WHERE NewAddedIntDimension < 0";
  private static final String SELECT_STAR_QUERY = "SELECT * FROM mytable";

  private final List<ServiceStatus.ServiceStatusCallback> _serviceStatusCallbacks =
      new ArrayList<>(getNumBrokers() + getNumServers());

  protected int getNumBrokers() {
    return NUM_BROKERS;
  }

  protected int getNumServers() {
    return NUM_SERVERS;
  }

  @BeforeClass
  public void setUp()
      throws Exception {
    TestUtils.ensureDirectoriesExistAndEmpty(_tempDir, _segmentDir, _tarDir);

    // Start the Pinot cluster
    startZk();
    startController();
    startBrokers(getNumBrokers());
    startServers(getNumServers());

    // Set up service status callbacks
    List<String> instances = _helixAdmin.getInstancesInCluster(_clusterName);
    for (String instance : instances) {
      if (instance.startsWith(CommonConstants.Helix.PREFIX_OF_BROKER_INSTANCE)) {
        _serviceStatusCallbacks.add(
            new ServiceStatus.IdealStateAndExternalViewMatchServiceStatusCallback(_helixManager, _clusterName, instance,
                Collections.singletonList(CommonConstants.Helix.BROKER_RESOURCE_INSTANCE)));
      }
      if (instance.startsWith(CommonConstants.Helix.PREFIX_OF_SERVER_INSTANCE)) {
        _serviceStatusCallbacks.add(new ServiceStatus.MultipleCallbackServiceStatusCallback(ImmutableList
            .of(new ServiceStatus.IdealStateAndCurrentStateMatchServiceStatusCallback(_helixManager, _clusterName,
                    instance),
                new ServiceStatus.IdealStateAndExternalViewMatchServiceStatusCallback(_helixManager, _clusterName,
                    instance))));
      }
    }

    // Unpack the Avro files
    List<File> avroFiles = unpackAvroData(_tempDir);

    ExecutorService executor = Executors.newCachedThreadPool();

    // Create segments from Avro data
    ClusterIntegrationTestUtils
        .buildSegmentsFromAvro(avroFiles, 0, _segmentDir, _tarDir, getTableName(), false, null, getRawIndexColumns(),
            null, executor);

    // Load data into H2
    setUpH2Connection(avroFiles, executor);

    // Initialize query generator
    setUpQueryGenerator(avroFiles, executor);

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.MINUTES);

    // Create the table
    addOfflineTable(getTableName(), null, null, null, null, getLoadMode(), SegmentVersion.v1, getInvertedIndexColumns(),
        getBloomFilterIndexColumns(), getTaskConfig());

    completeTableConfiguration();

    // Upload all segments
    uploadSegments(_tarDir);

    // Wait for all documents loaded
    waitForAllDocsLoaded(600_000L);
  }

  @Test
  public void testInstancesStarted() {
    assertEquals(_serviceStatusCallbacks.size(), getNumBrokers() + getNumServers());
    for (ServiceStatus.ServiceStatusCallback serviceStatusCallback : _serviceStatusCallbacks) {
      assertEquals(serviceStatusCallback.getServiceStatus(), ServiceStatus.Status.GOOD);
    }
  }

  @Test
  public void testInvertedIndexTriggering()
      throws Exception {
    final long numTotalDocs = getCountStarResult();

    JsonNode queryResponse = postQuery(TEST_UPDATED_INVERTED_INDEX_QUERY);
    assertEquals(queryResponse.get("numEntriesScannedInFilter").asLong(), numTotalDocs);

    // Update table config and trigger reload
    updateOfflineTable(getTableName(), null, null, null, null, getLoadMode(), SegmentVersion.v1,
        UPDATED_INVERTED_INDEX_COLUMNS, null, getTaskConfig());

    updateTableConfiguration();

    sendPostRequest(_controllerBaseApiUrl + "/tables/mytable/segments/reload?type=offline", null);

    TestUtils.waitForCondition(new Function<Void, Boolean>() {
      @Override
      public Boolean apply(@Nullable Void aVoid) {
        try {
          JsonNode queryResponse = postQuery(TEST_UPDATED_INVERTED_INDEX_QUERY);
          // Total docs should not change during reload
          assertEquals(queryResponse.get("totalDocs").asLong(), numTotalDocs);
          return queryResponse.get("numEntriesScannedInFilter").asLong() == 0L;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }, 600_000L, "Failed to generate inverted index");
  }

  @Test
  public void testBloomFilterTriggering()
      throws Exception {
    final long numTotalDocs = getCountStarResult();
    JsonNode queryResponse = postQuery(TEST_UPDATED_BLOOM_FILTER_QUERY);
    assertEquals(queryResponse.get("numSegmentsProcessed").asLong(), NUM_SEGMENTS);

    // Update table config and trigger reload
    updateOfflineTable(getTableName(), null, null, null, null, getLoadMode(), SegmentVersion.v1, null,
        UPDATED_BLOOM_FLITER_COLUMNS, getTaskConfig());

    updateTableConfiguration();

    sendPostRequest(_controllerBaseApiUrl + "/tables/mytable/segments/reload?type=offline", null);

    TestUtils.waitForCondition(new Function<Void, Boolean>() {
      @Override
      public Boolean apply(@Nullable Void aVoid) {
        try {
          JsonNode queryResponse = postQuery(TEST_UPDATED_BLOOM_FILTER_QUERY);
          // Total docs should not change during reload
          assertEquals(queryResponse.get("totalDocs").asLong(), numTotalDocs);
          return queryResponse.get("numSegmentsProcessed").asLong() == 0L;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }, 600_000L, "Failed to generate inverted index");
  }

  /**
   * We will add extra new columns to the schema to test adding new columns with default value to the offline segments.
   * <p>New columns are: (name, field type, data type, single/multi value, default null value)
   * <ul>
   *   <li>"newAddedIntMetric", METRIC, INT, single-value, 1</li>
   *   <li>"newAddedLongMetric", METRIC, LONG, single-value, 1</li>
   *   <li>"newAddedFloatMetric", METRIC, FLOAT, single-value, default (0.0)</li>
   *   <li>"newAddedDoubleMetric", METRIC, DOUBLE, single-value, default (0.0)</li>
   *   <li>"newAddedIntDimension", DIMENSION, INT, single-value, default (Integer.MIN_VALUE)</li>
   *   <li>"newAddedLongDimension", DIMENSION, LONG, single-value, default (Long.MIN_VALUE)</li>
   *   <li>"newAddedFloatDimension", DIMENSION, FLOAT, single-value, default (Float.NEGATIVE_INFINITY)</li>
   *   <li>"newAddedDoubleDimension", DIMENSION, DOUBLE, single-value, default (Double.NEGATIVE_INFINITY)</li>
   *   <li>"newAddedSVStringDimension", DIMENSION, STRING, single-value, default ("null")</li>
   *   <li>"newAddedMVStringDimension", DIMENSION, STRING, multi-value, ""</li>
   * </ul>
   */
  @Test
  public void testDefaultColumns()
      throws Exception {
    long numTotalDocs = getCountStarResult();

    reloadDefaultColumns(true);
    JsonNode queryResponse = postQuery(SELECT_STAR_QUERY);
    assertEquals(queryResponse.get("totalDocs").asLong(), numTotalDocs);
    assertEquals(queryResponse.get("selectionResults").get("columns").size(), 89);

    testNewAddedColumns();

    reloadDefaultColumns(false);
    queryResponse = postQuery(SELECT_STAR_QUERY);
    assertEquals(queryResponse.get("totalDocs").asLong(), numTotalDocs);
    assertEquals(queryResponse.get("selectionResults").get("columns").size(), 79);
  }

  private void reloadDefaultColumns(final boolean withExtraColumns)
      throws Exception {
    final long numTotalDocs = getCountStarResult();

    if (withExtraColumns) {
      sendSchema(SCHEMA_WITH_EXTRA_COLUMNS);
    } else {
      sendSchema(SCHEMA_WITH_MISSING_COLUMNS);
    }

    updateTableConfiguration();

    // Trigger reload
    sendPostRequest(_controllerBaseApiUrl + "/tables/mytable/segments/reload?type=offline", null);

    String errorMessage;
    if (withExtraColumns) {
      errorMessage = "Failed to add default columns";
    } else {
      errorMessage = "Failed to remove default columns";
    }

    TestUtils.waitForCondition(new Function<Void, Boolean>() {
      @Override
      public Boolean apply(@Nullable Void aVoid) {
        try {
          JsonNode queryResponse = postQuery(TEST_DEFAULT_COLUMNS_QUERY);
          // Total docs should not change during reload
          assertEquals(queryResponse.get("totalDocs").asLong(), numTotalDocs);
          long count = queryResponse.get("aggregationResults").get(0).get("value").asLong();
          if (withExtraColumns) {
            return count == numTotalDocs;
          } else {
            return count == 0;
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }, 600_000L, errorMessage);
  }

  private void sendSchema(String resourceName)
      throws Exception {
    URL resource = OfflineClusterIntegrationTest.class.getClassLoader().getResource(resourceName);
    assertNotNull(resource);
    File schemaFile = new File(resource.getFile());
    addSchema(schemaFile, getTableName());
  }

  private void testNewAddedColumns()
      throws Exception {
    long numTotalDocs = getCountStarResult();
    double numTotalDocsInDouble = (double) numTotalDocs;

    String pqlQuery;
    String sqlQuery;

    // Test queries with each new added columns
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedIntMetric = 1";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedLongMetric = 1";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedFloatMetric = 0.0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedDoubleMetric = 0.0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedIntDimension < 0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedLongDimension < 0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedFloatDimension < 0.0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedDoubleDimension < 0.0";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedSVStringDimension = 'null'";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT COUNT(*) FROM mytable WHERE NewAddedMVStringDimension = ''";
    sqlQuery = "SELECT COUNT(*) FROM mytable";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));

    // Test queries with new added metric column in aggregation function
    pqlQuery = "SELECT SUM(NewAddedIntMetric) FROM mytable WHERE DaysSinceEpoch <= 16312";
    sqlQuery = "SELECT COUNT(*) FROM mytable WHERE DaysSinceEpoch <= 16312";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT SUM(NewAddedIntMetric) FROM mytable WHERE DaysSinceEpoch > 16312";
    sqlQuery = "SELECT COUNT(*) FROM mytable WHERE DaysSinceEpoch > 16312";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT SUM(NewAddedLongMetric) FROM mytable WHERE DaysSinceEpoch <= 16312";
    sqlQuery = "SELECT COUNT(*) FROM mytable WHERE DaysSinceEpoch <= 16312";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));
    pqlQuery = "SELECT SUM(NewAddedLongMetric) FROM mytable WHERE DaysSinceEpoch > 16312";
    sqlQuery = "SELECT COUNT(*) FROM mytable WHERE DaysSinceEpoch > 16312";
    testQuery(pqlQuery, Collections.singletonList(sqlQuery));

    // Test other query forms with new added columns
    JsonNode response;
    JsonNode groupByResult;
    pqlQuery = "SELECT SUM(NewAddedFloatMetric) FROM mytable GROUP BY NewAddedSVStringDimension";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), 0.0);
    assertEquals(groupByResult.get("group").get(0).asText(), "null");
    pqlQuery = "SELECT SUM(NewAddedDoubleMetric) FROM mytable GROUP BY NewAddedIntDimension";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), 0.0);
    assertEquals(groupByResult.get("group").get(0).asInt(), Integer.MIN_VALUE);
    pqlQuery = "SELECT SUM(NewAddedIntMetric) FROM mytable GROUP BY NewAddedLongDimension";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), numTotalDocsInDouble);
    assertEquals(groupByResult.get("group").get(0).asLong(), Long.MIN_VALUE);
    pqlQuery =
        "SELECT SUM(NewAddedIntMetric), SUM(NewAddedLongMetric), SUM(NewAddedFloatMetric), SUM(NewAddedDoubleMetric) "
            + "FROM mytable GROUP BY NewAddedIntDimension, NewAddedLongDimension, NewAddedFloatDimension, "
            + "NewAddedDoubleDimension, NewAddedSVStringDimension, NewAddedMVStringDimension";
    response = postQuery(pqlQuery);
    JsonNode groupByResultArray = response.get("aggregationResults");
    groupByResult = groupByResultArray.get(0).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), numTotalDocsInDouble);
    assertEquals(groupByResult.get("group").get(0).asInt(), Integer.MIN_VALUE);
    assertEquals(groupByResult.get("group").get(1).asLong(), Long.MIN_VALUE);
    assertEquals((float) groupByResult.get("group").get(2).asDouble(), Float.NEGATIVE_INFINITY);
    assertEquals(groupByResult.get("group").get(3).asDouble(), Double.NEGATIVE_INFINITY);
    groupByResult = groupByResultArray.get(1).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), numTotalDocsInDouble);
    assertEquals(groupByResult.get("group").get(0).asInt(), Integer.MIN_VALUE);
    assertEquals(groupByResult.get("group").get(1).asLong(), Long.MIN_VALUE);
    assertEquals((float) groupByResult.get("group").get(2).asDouble(), Float.NEGATIVE_INFINITY);
    assertEquals(groupByResult.get("group").get(3).asDouble(), Double.NEGATIVE_INFINITY);
    groupByResult = groupByResultArray.get(2).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), 0.0);
    assertEquals(groupByResult.get("group").get(0).asInt(), Integer.MIN_VALUE);
    assertEquals(groupByResult.get("group").get(1).asLong(), Long.MIN_VALUE);
    assertEquals((float) groupByResult.get("group").get(2).asDouble(), Float.NEGATIVE_INFINITY);
    assertEquals(groupByResult.get("group").get(3).asDouble(), Double.NEGATIVE_INFINITY);
    groupByResult = groupByResultArray.get(3).get("groupByResult").get(0);
    assertEquals(groupByResult.get("value").asDouble(), 0.0);
    assertEquals(groupByResult.get("group").get(0).asInt(), Integer.MIN_VALUE);
    assertEquals(groupByResult.get("group").get(1).asLong(), Long.MIN_VALUE);
    assertEquals((float) groupByResult.get("group").get(2).asDouble(), Float.NEGATIVE_INFINITY);
    assertEquals(groupByResult.get("group").get(3).asDouble(), Double.NEGATIVE_INFINITY);
  }

  @Test
  @Override
  public void testBrokerResponseMetadata()
      throws Exception {
    super.testBrokerResponseMetadata();
  }

  @Test
  public void testGroupByUDF()
      throws Exception {
    String pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY timeConvert(DaysSinceEpoch,'DAYS','SECONDS')";
    JsonNode response = postQuery(pqlQuery);
    JsonNode groupByResult = response.get("aggregationResults").get(0);
    JsonNode groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asInt(), 16138 * 24 * 3600);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "timeconvert(DaysSinceEpoch,'DAYS','SECONDS')");

    pqlQuery =
        "SELECT COUNT(*) FROM mytable GROUP BY dateTimeConvert(DaysSinceEpoch,'1:DAYS:EPOCH','1:HOURS:EPOCH','1:HOURS')";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asInt(), 16138 * 24);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(),
        "datetimeconvert(DaysSinceEpoch,'1:DAYS:EPOCH','1:HOURS:EPOCH','1:HOURS')");

    pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY add(DaysSinceEpoch,DaysSinceEpoch,15)";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asDouble(), 16138.0 + 16138 + 15);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "add(DaysSinceEpoch,DaysSinceEpoch,'15')");

    pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY sub(DaysSinceEpoch,25)";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asDouble(), 16138.0 - 25);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "sub(DaysSinceEpoch,'25')");

    pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY mult(DaysSinceEpoch,24,3600)";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asDouble(), 16138.0 * 24 * 3600);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "mult(DaysSinceEpoch,'24','3600')");

    pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY div(DaysSinceEpoch,2)";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 605.0);
    assertEquals(groupByEntry.get("group").get(0).asDouble(), 16138.0 / 2);
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "div(DaysSinceEpoch,'2')");

    pqlQuery = "SELECT COUNT(*) FROM mytable GROUP BY valueIn(DivAirports,'DFW','ORD')";
    response = postQuery(pqlQuery);
    groupByResult = response.get("aggregationResults").get(0);
    groupByEntry = groupByResult.get("groupByResult").get(0);
    assertEquals(groupByEntry.get("value").asDouble(), 336.0);
    assertEquals(groupByEntry.get("group").get(0).asText(), "ORD");
    assertEquals(groupByResult.get("groupByColumns").get(0).asText(), "valuein(DivAirports,'DFW','ORD')");

    pqlQuery = "SELECT MAX(timeConvert(DaysSinceEpoch,'DAYS','SECONDS')) FROM mytable";
    response = postQuery(pqlQuery);
    JsonNode aggregationResult = response.get("aggregationResults").get(0);
    assertEquals(aggregationResult.get("function").asText(), "max_timeconvert(DaysSinceEpoch,'DAYS','SECONDS')");
    assertEquals(aggregationResult.get("value").asDouble(), 16435.0 * 24 * 3600);

    pqlQuery = "SELECT MIN(div(DaysSinceEpoch,2)) FROM mytable";
    response = postQuery(pqlQuery);
    aggregationResult = response.get("aggregationResults").get(0);
    assertEquals(aggregationResult.get("function").asText(), "min_div(DaysSinceEpoch,'2')");
    assertEquals(aggregationResult.get("value").asDouble(), 16071.0 / 2);
  }

  @Test
  public void testAggregationUDF()
      throws Exception {

    String pqlQuery = "SELECT MAX(timeConvert(DaysSinceEpoch,'DAYS','SECONDS')) FROM mytable";
    JsonNode response = postQuery(pqlQuery);
    JsonNode aggregationResult = response.get("aggregationResults").get(0);
    assertEquals(aggregationResult.get("function").asText(), "max_timeconvert(DaysSinceEpoch,'DAYS','SECONDS')");
    assertEquals(aggregationResult.get("value").asDouble(), 16435.0 * 24 * 3600);

    pqlQuery = "SELECT MIN(div(DaysSinceEpoch,2)) FROM mytable";
    response = postQuery(pqlQuery);
    aggregationResult = response.get("aggregationResults").get(0);
    assertEquals(aggregationResult.get("function").asText(), "min_div(DaysSinceEpoch,'2')");
    assertEquals(aggregationResult.get("value").asDouble(), 16071.0 / 2);
  }

  @Test
  public void testSelectionUDF()
      throws Exception {
    SelectionPlanNode.enableUDFInSelection = true;
    String pqlQuery = "SELECT DaysSinceEpoch, timeConvert(DaysSinceEpoch,'DAYS','SECONDS') FROM mytable";
    JsonNode response = postQuery(pqlQuery);
    ArrayNode selectionResults = (ArrayNode) response.get("selectionResults").get("results");
    for (int i = 0; i < selectionResults.size(); i++) {
      long daysSinceEpoch = selectionResults.get(i).get(0).asLong();
      long secondsSinceEpoch = selectionResults.get(i).get(1).asLong();
      Assert.assertEquals(daysSinceEpoch * 24 * 60 * 60, secondsSinceEpoch);
    }

    pqlQuery =
        "SELECT DaysSinceEpoch, timeConvert(DaysSinceEpoch,'DAYS','SECONDS') FROM mytable order by DaysSinceEpoch limit 10000";
    response = postQuery(pqlQuery);
    selectionResults = (ArrayNode) response.get("selectionResults").get("results");
    long prevValue = -1;
    for (int i = 0; i < selectionResults.size(); i++) {
      long daysSinceEpoch = selectionResults.get(i).get(0).asLong();
      long secondsSinceEpoch = selectionResults.get(i).get(1).asLong();
      Assert.assertEquals(daysSinceEpoch * 24 * 60 * 60, secondsSinceEpoch);
      Assert.assertTrue(daysSinceEpoch >= prevValue);
      prevValue = daysSinceEpoch;
    }

    pqlQuery =
        "SELECT DaysSinceEpoch, timeConvert(DaysSinceEpoch,'DAYS','SECONDS') FROM mytable order by timeConvert(DaysSinceEpoch,'DAYS','SECONDS') DESC limit 10000";
    response = postQuery(pqlQuery);
    selectionResults = (ArrayNode) response.get("selectionResults").get("results");
    prevValue = Long.MAX_VALUE;
    for (int i = 0; i < selectionResults.size(); i++) {
      long daysSinceEpoch = selectionResults.get(i).get(0).asLong();
      long secondsSinceEpoch = selectionResults.get(i).get(1).asLong();
      Assert.assertEquals(daysSinceEpoch * 24 * 60 * 60, secondsSinceEpoch);
      Assert.assertTrue(secondsSinceEpoch <= prevValue);
      prevValue = secondsSinceEpoch;
    }
    SelectionPlanNode.enableUDFInSelection = false;
  }

  @AfterClass
  public void tearDown()
      throws Exception {
    // Test instance decommission before tearing down
    testInstanceDecommission();

    // Brokers and servers has been stopped
    stopController();
    stopZk();
    FileUtils.deleteDirectory(_tempDir);
  }

  private void testInstanceDecommission()
      throws Exception {
    // Fetch all instances
    JsonNode response = JsonUtils.stringToJsonNode(sendGetRequest(_controllerRequestURLBuilder.forInstanceList()));
    JsonNode instanceList = response.get("instances");
    int numInstances = instanceList.size();
    assertEquals(numInstances, getNumBrokers() + getNumServers());

    // Try to delete a server that does not exist
    String deleteInstanceRequest = _controllerRequestURLBuilder.forInstanceDelete("potato");
    try {
      sendDeleteRequest(deleteInstanceRequest);
      fail("Delete should have returned a failure status (404)");
    } catch (IOException e) {
      // Expected exception on 404 status code
    }

    // Get the server name
    String serverName = null;
    String brokerName = null;
    for (int i = 0; i < numInstances; i++) {
      String instanceName = instanceList.get(i).asText();
      if (instanceName.startsWith(CommonConstants.Helix.PREFIX_OF_SERVER_INSTANCE)) {
        serverName = instanceName;
      } else if (instanceName.startsWith(CommonConstants.Helix.PREFIX_OF_BROKER_INSTANCE)) {
        brokerName = instanceName;
      }
    }

    // Try to delete a live server
    deleteInstanceRequest = _controllerRequestURLBuilder.forInstanceDelete(serverName);
    try {
      sendDeleteRequest(deleteInstanceRequest);
      fail("Delete should have returned a failure status (409)");
    } catch (IOException e) {
      // Expected exception on 409 status code
    }

    // Stop servers
    stopServer();

    // Try to delete a server whose information is still on the ideal state
    try {
      sendDeleteRequest(deleteInstanceRequest);
      fail("Delete should have returned a failure status (409)");
    } catch (IOException e) {
      // Expected exception on 409 status code
    }

    // Delete the table
    dropOfflineTable(getTableName());

    // Now, delete server should work
    response = JsonUtils.stringToJsonNode(sendDeleteRequest(deleteInstanceRequest));
    assertTrue(response.has("status"));

    // Try to delete a broker whose information is still live
    try {
      deleteInstanceRequest = _controllerRequestURLBuilder.forInstanceDelete(brokerName);
      sendDeleteRequest(deleteInstanceRequest);
      fail("Delete should have returned a failure status (409)");
    } catch (IOException e) {
      // Expected exception on 409 status code
    }

    // Stop brokers
    stopBroker();

    // TODO: Add test to delete broker instance. Currently, stopBroker() does not work correctly.

    // Check if '/INSTANCES/<serverName>' has been erased correctly
    String instancePath = "/" + _clusterName + "/INSTANCES/" + serverName;
    assertFalse(_propertyStore.exists(instancePath, 0));

    // Check if '/CONFIGS/PARTICIPANT/<serverName>' has been erased correctly
    String configPath = "/" + _clusterName + "/CONFIGS/PARTICIPANT/" + serverName;
    assertFalse(_propertyStore.exists(configPath, 0));
  }

  @Override
  protected boolean isUsingNewConfigFormat() {
    return true;
  }
}
