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
package org.apache.pinot.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.common.request.ExpressionType;
import org.apache.pinot.common.request.FilterOperator;
import org.apache.pinot.common.request.Function;
import org.apache.pinot.common.request.Identifier;
import org.apache.pinot.common.request.QuerySource;
import org.apache.pinot.common.request.QueryType;
import org.apache.pinot.common.utils.request.RequestUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.testng.annotations.Test;


public class BrokerRequestSerializationTest {

  @Test
  public static void testSerialization()
      throws TException {
    BrokerRequest req = new BrokerRequest();

    // Populate Query Type
    QueryType type = new QueryType();
    type.setHasAggregation(true);
    type.setHasFilter(true);
    type.setHasSelection(true);
    type.setHasGroup_by(true);
    req.setQueryType(type);

    // Populate Query source
    QuerySource s = new QuerySource();
    s.setTableName("dummy");
    req.setQuerySource(s);

    req.setDuration("dummy");
    req.setTimeInterval("dummy");

    //Populate Group-By
    List<Expression> groupByList = new ArrayList<>();
    Expression groupBy = new Expression(ExpressionType.IDENTIFIER);
    groupBy.setIdentifier(new Identifier("dummy1"));
    groupByList.add(groupBy);
    groupBy = new Expression(ExpressionType.IDENTIFIER);
    groupBy.setIdentifier(new Identifier("dummy2"));
    groupByList.add(groupBy);
    req.setGroupByList(groupByList);

    //Populate Selections
    List<Expression> selectList = new ArrayList<>();
    Expression select = new Expression(ExpressionType.IDENTIFIER);
    select.setIdentifier(new Identifier("dummy1"));
    selectList.add(select);

    //Populate Aggregations
    Function agg = new Function();
    agg.setOperator("dummy1");
    Expression fieldExpr = new Expression(ExpressionType.IDENTIFIER);
    fieldExpr.setIdentifier(new Identifier("dummy1"));
    agg.setOperands(Arrays.asList(fieldExpr));
    Expression funcExpr = new Expression(ExpressionType.FUNCTION);
    funcExpr.setFunctionCall(agg);
    req.addToSelectList(funcExpr);

    Expression filterExpr = new Expression(ExpressionType.FUNCTION);
    Function func1 = new Function(FilterOperator.AND.toString());
    func1.addToOperands(RequestUtils.generateFilterQuery(FilterOperator.EQUALITY.toString(), "dummy1", "dummy1"));
    func1.addToOperands(RequestUtils.generateFilterQuery(FilterOperator.EQUALITY.toString(), "dummy2", "dummy2"));
    fieldExpr.setFunctionCall(func1);
    req.setFilterExpression(filterExpr);
    /*
    //Populate Group-By
    GroupBy groupBy = new GroupBy();
    List<String> columns = new ArrayList<String>();
    columns.add("dummy1");
    columns.add("dummy2");
    groupBy.setColumns(columns);
    groupBy.setTopN(100);
    req.setGroupBy(groupBy);

    //Populate Selections
    Selection sel = new Selection();
    sel.setSize(1);
    SelectionSort s2 = new SelectionSort();
    s2.setColumn("dummy1");
    s2.setIsAsc(true);
    sel.addToSelectionSortSequence(s2);
    sel.addToSelectionColumns("dummy1");
    req.setSelections(sel);

    //Populate FilterQuery
    FilterQuery q1 = new FilterQuery();
    q1.setId(1);
    q1.setColumn("dummy1");
    q1.addToValue("dummy1");
    q1.addToNestedFilterQueryIds(2);
    q1.setOperator(FilterOperator.AND);
    FilterQuery q2 = new FilterQuery();
    q2.setId(2);
    q2.setColumn("dummy2");
    q2.addToValue("dummy2");
    q2.setOperator(FilterOperator.AND);

    FilterQueryMap map = new FilterQueryMap();
    map.putToFilterQueryMap(1, q1);
    map.putToFilterQueryMap(2, q2);
    req.setFilterQuery(q1);
    req.setFilterSubQueryMap(map);

    //Populate Aggregations
    AggregationInfo agg = new AggregationInfo();
    agg.setAggregationType("dummy1");
    agg.putToAggregationParams("key1", "dummy1");
    req.addToAggregationsInfo(agg);
*/
    TSerializer normalSerializer = new TSerializer();
    TSerializer compactSerializer = new TSerializer(new TCompactProtocol.Factory());
    normalSerializer.serialize(req);
    compactSerializer.serialize(req);

//    int numRequests = 100000;
//    TimerContext t = MetricsHelper.startTimer();
//    TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
//    //TSerializer serializer = new TSerializer();
//    //Compact : Size 183 , Serialization Latency : 0.03361ms
//    // Normal : Size 385 , Serialization Latency : 0.01144ms
//
//    for (int i = 0; i < numRequests; i++) {
//      try {
//        serializer.serialize(req);
//        //System.out.println(s3.length);
//        //break;
//      } catch (TException e) {
//        e.printStackTrace();
//      }
//    }
//    t.stop();
//    System.out.println("Latency is :" + (t.getLatencyMs() / (float) numRequests));
  }
}
