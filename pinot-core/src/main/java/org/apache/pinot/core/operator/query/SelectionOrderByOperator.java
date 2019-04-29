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
package org.apache.pinot.core.operator.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.common.utils.DataSchema;
import org.apache.pinot.core.common.Block;
import org.apache.pinot.core.indexsegment.IndexSegment;
import org.apache.pinot.core.operator.BaseOperator;
import org.apache.pinot.core.operator.ExecutionStatistics;
import org.apache.pinot.core.operator.ProjectionOperator;
import org.apache.pinot.core.operator.blocks.DocIdSetBlock;
import org.apache.pinot.core.operator.blocks.IntermediateResultsBlock;
import org.apache.pinot.core.operator.blocks.ProjectionBlock;
import org.apache.pinot.core.query.selection.SelectionOperatorService;
import org.apache.pinot.core.query.selection.SelectionOperatorUtils;


/**
 * This MSelectionOperator will take care of applying a selection query to one IndexSegment.
 * nextBlock() will return an IntermediateResultBlock for the given IndexSegment.
 *
 *
 */
public class SelectionOrderByOperator extends BaseOperator<IntermediateResultsBlock> {
  private static final String OPERATOR_NAME = "SelectionOrderByOperator";

  private final IndexSegment _indexSegment;
  private final ProjectionOperator _projectionOperator;
  private final List<Expression> _selection;
  private final List<Expression> _orderBys;
  private final int _limit;
  private final int _offset;
  private final SelectionOperatorService _selectionOperatorService;
  private final DataSchema _dataSchema;
  private final Block[] _blocks;
  private final Set<String> _selectionColumns = new HashSet<>();
  private ExecutionStatistics _executionStatistics;

  public SelectionOrderByOperator(IndexSegment indexSegment, List<Expression> selection, List<Expression> orderBys,
      int limit, int offset, ProjectionOperator projectionOperator) {
    _indexSegment = indexSegment;
    _selection = selection;
    _projectionOperator = projectionOperator;
    _orderBys = orderBys;
    _limit = limit;
    _offset = offset;
    initColumnarDataSourcePlanNodeMap(indexSegment);
    _selectionOperatorService = new SelectionOperatorService(_selection, _orderBys, limit, offset, indexSegment);
    _dataSchema = _selectionOperatorService.getDataSchema();
    _blocks = new Block[_selectionColumns.size()];
  }

  private void initColumnarDataSourcePlanNodeMap(IndexSegment indexSegment) {
    _selectionColumns.addAll(SelectionOperatorUtils.getSelectionColumns(_selection));
    if ((_selectionColumns.size() == 1) && ((_selectionColumns.toArray(new String[0]))[0].equals("*"))) {
      _selectionColumns.clear();
      for (String columnName : indexSegment.getPhysicalColumnNames()) {
        _selectionColumns.add(columnName);
      }
    }
    for (Expression orderBy : _orderBys) {
      _selectionColumns.add(orderBy.getIdentifier().getName());
    }
  }

  @Override
  protected IntermediateResultsBlock getNextBlock() {
    int numDocsScanned = 0;

    ProjectionBlock projectionBlock;
    while ((projectionBlock = _projectionOperator.nextBlock()) != null) {
      for (int i = 0; i < _dataSchema.size(); i++) {
        _blocks[i] = projectionBlock.getBlock(_dataSchema.getColumnName(i));
      }
      DocIdSetBlock docIdSetBlock = projectionBlock.getDocIdSetBlock();
      _selectionOperatorService.iterateOnBlocksWithOrdering(docIdSetBlock.getBlockDocIdSet().iterator(), _blocks);
    }

    // Create execution statistics.
    numDocsScanned += _selectionOperatorService.getNumDocsScanned();
    long numEntriesScannedInFilter = _projectionOperator.getExecutionStatistics().getNumEntriesScannedInFilter();
    long numEntriesScannedPostFilter = numDocsScanned * _projectionOperator.getNumColumnsProjected();
    long numTotalRawDocs = _indexSegment.getSegmentMetadata().getTotalRawDocs();
    _executionStatistics =
        new ExecutionStatistics(numDocsScanned, numEntriesScannedInFilter, numEntriesScannedPostFilter,
            numTotalRawDocs);

    return new IntermediateResultsBlock(_selectionOperatorService.getDataSchema(), _selectionOperatorService.getRows());
  }

  @Override
  public String getOperatorName() {
    return OPERATOR_NAME;
  }

  @Override
  public ExecutionStatistics getExecutionStatistics() {
    return _executionStatistics;
  }
}
