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
package org.apache.pinot.core.query.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.pinot.common.metrics.ServerMetrics;
import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.common.request.Function;
import org.apache.pinot.common.request.Identifier;
import org.apache.pinot.common.request.InstanceRequest;
import org.apache.pinot.common.request.transform.TransformExpressionTree;
import org.apache.pinot.common.utils.request.FilterQueryTree;
import org.apache.pinot.common.utils.request.RequestUtils;
import org.apache.pinot.core.query.aggregation.function.AggregationFunctionType;
import org.apache.pinot.core.query.aggregation.function.AggregationFunctionUtils;
import org.apache.pinot.core.query.request.context.TimerContext;


/**
 * The <code>ServerQueryRequest</code> class encapsulates the query related information as well as the query processing
 * context.
 * <p>All segment independent information should be pre-computed and stored in this class to avoid repetitive work on a
 * per segment basis.
 */
public class ServerQueryRequest {
  private final long _requestId;
  private final BrokerRequest _brokerRequest;
  private final String _tableNameWithType;
  private final List<String> _segmentsToQuery;
  private final boolean _enableTrace;
  private final String _brokerId;

  // Timing information for different phases of query execution
  private final TimerContext _timerContext;

  // Pre-computed segment independent information
  private final Set<String> _allColumns;
  private final FilterQueryTree _filterQueryTree;
  private final Set<String> _filterColumns;
  private final Set<TransformExpressionTree> _aggregationExpressions;
  private final Set<String> _aggregationColumns;
  private final Set<TransformExpressionTree> _groupByExpressions;
  private final Set<String> _groupByColumns;
  private final Set<String> _selectionColumns;

  // Query processing context
  private volatile int _segmentCountAfterPruning = -1;

  public ServerQueryRequest(InstanceRequest instanceRequest, ServerMetrics serverMetrics, long queryArrivalTimeMs) {
    _requestId = instanceRequest.getRequestId();
    _brokerRequest = instanceRequest.getQuery();
    _tableNameWithType = _brokerRequest.getQuerySource().getTableName();
    _segmentsToQuery = instanceRequest.getSearchSegments();
    _enableTrace = instanceRequest.isEnableTrace();
    _brokerId = instanceRequest.getBrokerId() != null ? instanceRequest.getBrokerId() : "unknown";
    _timerContext = new TimerContext(_tableNameWithType, serverMetrics, queryArrivalTimeMs);

    // Pre-compute segment independent information
    _allColumns = new HashSet<>();

    // Filter
    _filterQueryTree = RequestUtils.generateFilterQueryTree(_brokerRequest);
    if (_filterQueryTree != null) {
      _filterColumns = RequestUtils.extractFilterColumns(_filterQueryTree);
      _allColumns.addAll(_filterColumns);
    } else {
      _filterColumns = null;
    }

    final List<Expression> selectList = _brokerRequest.getSelectList();
    List<Function> aggregationsInfos = new ArrayList<>();
    List<Identifier> selections = new ArrayList<>();
    if (selectList != null) {
      for (Expression selectExpression : selectList) {
        if (selectExpression.getFunctionCall() != null) {
          aggregationsInfos.add(selectExpression.getFunctionCall());
        }
        if (selectExpression.getIdentifier() != null) {
          selections.add(selectExpression.getIdentifier());
        }
      }
    }
    // Aggregation
    if (!aggregationsInfos.isEmpty()) {
      _aggregationExpressions = new HashSet<>();
      for (Function aggregationInfo : aggregationsInfos) {
        if (!aggregationInfo.getOperator().equalsIgnoreCase(AggregationFunctionType.COUNT.getName())) {
          _aggregationExpressions.add(
              TransformExpressionTree.compileToExpressionTree(AggregationFunctionUtils.getColumn(aggregationInfo)));
        }
      }
      _aggregationColumns = RequestUtils.extractColumnsFromExpressions(_aggregationExpressions);
      _allColumns.addAll(_aggregationColumns);
    } else {
      _aggregationExpressions = null;
      _aggregationColumns = null;
    }

    _brokerRequest.getOrderByList();

    // Selection
    if (!selections.isEmpty()) {
      _selectionColumns =
          RequestUtils.extractSelectionColumns(_brokerRequest.getSelectList(), _brokerRequest.getOrderByList());
      _allColumns.addAll(_selectionColumns);
    } else {
      _selectionColumns = null;
    }

    // Group-by
    final List<Expression> groupByList = _brokerRequest.getGroupByList();
    if (groupByList != null && !groupByList.isEmpty()) {
      _groupByExpressions = new HashSet<>();
      for (Expression expression : groupByList) {
        _groupByExpressions.add(TransformExpressionTree.compileToExpressionTree(expression.getIdentifier().getName()));
      }
      _groupByColumns = RequestUtils.extractColumnsFromExpressions(_groupByExpressions);
      _allColumns.addAll(_groupByColumns);
    } else {
      _groupByExpressions = null;
      _groupByColumns = null;
    }
  }

  public long getRequestId() {
    return _requestId;
  }

  public BrokerRequest getBrokerRequest() {
    return _brokerRequest;
  }

  public String getTableNameWithType() {
    return _tableNameWithType;
  }

  public List<String> getSegmentsToQuery() {
    return _segmentsToQuery;
  }

  public boolean isEnableTrace() {
    return _enableTrace;
  }

  public String getBrokerId() {
    return _brokerId;
  }

  public TimerContext getTimerContext() {
    return _timerContext;
  }

  public Set<String> getAllColumns() {
    return _allColumns;
  }

  @Nullable
  public FilterQueryTree getFilterQueryTree() {
    return _filterQueryTree;
  }

  @Nullable
  public Set<String> getFilterColumns() {
    return _filterColumns;
  }

  @Nullable
  public Set<TransformExpressionTree> getAggregationExpressions() {
    return _aggregationExpressions;
  }

  @Nullable
  public Set<String> getAggregationColumns() {
    return _aggregationColumns;
  }

  @Nullable
  public Set<TransformExpressionTree> getGroupByExpressions() {
    return _groupByExpressions;
  }

  @Nullable
  public Set<String> getGroupByColumns() {
    return _groupByColumns;
  }

  @Nullable
  public Set<String> getSelectionColumns() {
    return _selectionColumns;
  }
}
