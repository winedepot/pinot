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
package org.apache.pinot.core.plan;

import java.util.List;
import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.core.common.Operator;
import org.apache.pinot.core.indexsegment.IndexSegment;
import org.apache.pinot.core.operator.query.EmptySelectionOperator;
import org.apache.pinot.core.operator.query.SelectionOnlyOperator;
import org.apache.pinot.core.operator.query.SelectionOrderByOperator;
import org.apache.pinot.core.query.selection.SelectionOperatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The <code>SelectionPlanNode</code> class provides the execution plan for selection query on a single segment.
 */
public class SelectionPlanNode implements PlanNode {
  private static final Logger LOGGER = LoggerFactory.getLogger(SelectionPlanNode.class);

  private final IndexSegment _indexSegment;
  // private final Selection _selection;
  private final List<Expression> _selection;
  private final List<Expression> _orderBy;
  private final ProjectionPlanNode _projectionPlanNode;
  private final int _limit;
  private final int _offset;

  public SelectionPlanNode(IndexSegment indexSegment, BrokerRequest brokerRequest) {
    _indexSegment = indexSegment;
    _selection = brokerRequest.getSelectList();
    _orderBy = brokerRequest.getOrderByList();
    _limit = brokerRequest.getLimit();
    _offset = brokerRequest.getOffset();
    if (_selection.size() > 0) {
      int maxDocPerNextCall = DocIdSetPlanNode.MAX_DOC_PER_CALL;

      // No ordering required, select minimum number of documents
      if (!brokerRequest.isSetOrderByList()) {
        maxDocPerNextCall = Math.min(_offset + _limit, maxDocPerNextCall);
      }

      DocIdSetPlanNode docIdSetPlanNode = new DocIdSetPlanNode(_indexSegment, brokerRequest, maxDocPerNextCall);
      _projectionPlanNode = new ProjectionPlanNode(_indexSegment,
          SelectionOperatorUtils.extractSelectionRelatedColumns(_selection, _orderBy, indexSegment), docIdSetPlanNode);
    } else {
      _projectionPlanNode = null;
    }
  }

  @Override
  public Operator run() {
    if (_selection.size() > 0) {
      if (_orderBy != null) {
        return new SelectionOrderByOperator(_indexSegment, _selection, _orderBy, _limit, _offset,
            _projectionPlanNode.run());
      } else {
        return new SelectionOnlyOperator(_indexSegment, _selection, _limit, _projectionPlanNode.run());
      }
    } else {
      return new EmptySelectionOperator(_indexSegment, _selection);
    }
  }

  @Override
  public void showTree(String prefix) {
    LOGGER.debug(prefix + "Segment Level Inner-Segment Plan Node:");
    if (_selection.size() > 0) {
      if (_orderBy != null) {
        LOGGER.debug(prefix + "Operator: SelectionOrderByOperator");
      } else {
        LOGGER.debug(prefix + "Operator: SelectionOnlyOperator");
      }
    } else {
      LOGGER.debug(prefix + "Operator: LimitZeroSelectionOperator");
    }
    LOGGER.debug(prefix + "Argument 0: IndexSegment - " + _indexSegment.getSegmentName());
    LOGGER.debug(prefix + "Argument 1: Selections - " + _selection);
    if (_selection.size() > 0) {
      LOGGER.debug(prefix + "Argument 2: Projection -");
      _projectionPlanNode.showTree(prefix + "    ");
    }
  }
}
