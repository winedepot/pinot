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

import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Selection;
import org.apache.pinot.core.common.Operator;
import org.apache.pinot.core.indexsegment.IndexSegment;
import org.apache.pinot.core.operator.query.EmptySelectionOperator;
import org.apache.pinot.core.operator.query.SelectionOnlyOperator;
import org.apache.pinot.core.operator.query.SelectionOperator;
import org.apache.pinot.core.operator.query.SelectionOrderByOperator;
import org.apache.pinot.core.query.selection.SelectionOperatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The <code>SelectionPlanNode</code> class provides the execution plan for selection query on a
 * single segment.
 */
public class SelectionPlanNode implements PlanNode {

  private static final Logger LOGGER = LoggerFactory.getLogger(SelectionPlanNode.class);

  private final IndexSegment _indexSegment;
  private final Selection _selection;
  private TransformPlanNode _transformPlanNode;
  private ProjectionPlanNode _projectionPlanNode;
  boolean supportUDF = false;

  public SelectionPlanNode(IndexSegment indexSegment, BrokerRequest brokerRequest) {
    _indexSegment = indexSegment;
    _selection = brokerRequest.getSelections();
    if (supportUDF) {
      if (_selection.getSize() > 0) {
        _transformPlanNode = new TransformPlanNode(_indexSegment, brokerRequest);
      } else {
        _transformPlanNode = null;
      }
    } else {
      if (_selection.getSize() > 0) {
        int maxDocPerNextCall = DocIdSetPlanNode.MAX_DOC_PER_CALL;

        // No ordering required, select minimum number of documents
        if (!_selection.isSetSelectionSortSequence()) {
          maxDocPerNextCall = Math
              .min(_selection.getOffset() + _selection.getSize(), maxDocPerNextCall);
        }
        DocIdSetPlanNode docIdSetPlanNode = new DocIdSetPlanNode(_indexSegment, brokerRequest,
            maxDocPerNextCall);
        _projectionPlanNode = new ProjectionPlanNode(_indexSegment,
            SelectionOperatorUtils.extractSelectionRelatedColumns(_selection, indexSegment),
            docIdSetPlanNode);
      } else {
        _projectionPlanNode = null;
      }
    }
  }

  @Override
  public Operator run() {
    if (!supportUDF) {
      if (_selection.getSize() > 0) {
        if (_selection.isSetSelectionSortSequence()) {
          return new SelectionOrderByOperator(_indexSegment, _selection, _projectionPlanNode.run());
        } else {
          return new SelectionOnlyOperator(_indexSegment, _selection, _projectionPlanNode.run());
        }
      } else {
        return new EmptySelectionOperator(_indexSegment, _selection);
      }
    } else {
      if (_selection.getSize() > 0) {
        if (_selection.isSetSelectionSortSequence()) {
          return new SelectionOperator(_indexSegment, _selection, _transformPlanNode.run());
        } else {
          return new SelectionOperator(_indexSegment, _selection, _transformPlanNode.run());
        }
      } else {
        return new EmptySelectionOperator(_indexSegment, _selection);
      }
    }
  }

  @Override
  public void showTree(String prefix) {
    LOGGER.debug(prefix + "Segment Level Inner-Segment Plan Node:");
    if (_selection.getSize() > 0) {
      if (_selection.isSetSelectionSortSequence()) {
        LOGGER.debug(prefix + "Operator: SelectionOrderByOperator");
      } else {
        LOGGER.debug(prefix + "Operator: SelectionOnlyOperator");
      }
    } else {
      LOGGER.debug(prefix + "Operator: LimitZeroSelectionOperator");
    }
    LOGGER.debug(prefix + "Argument 0: IndexSegment - " + _indexSegment.getSegmentName());
    LOGGER.debug(prefix + "Argument 1: Selections - " + _selection);
    if (_selection.getSize() > 0) {
      LOGGER.debug(prefix + "Argument 2: Transform -");
      _transformPlanNode.showTree(prefix + "    ");
    }
  }
}
