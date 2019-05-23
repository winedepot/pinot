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
package org.apache.pinot.core.query.aggregation;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.pinot.common.request.transform.TransformExpressionTree;
import org.apache.pinot.core.operator.blocks.TransformBlock;
import org.apache.pinot.core.query.aggregation.function.AggregationFunction;
import org.apache.pinot.common.function.AggregationFunctionType;


/**
 * Implementation of AggregationExecutor interface, to perform
 * aggregations.
 */
public class DefaultAggregationExecutor implements AggregationExecutor {
  protected final int _numFunctions;
  protected final AggregationFunction[] _functions;
  protected final AggregationResultHolder[] _resultHolders;
  protected final TransformExpressionTree[] _expressions;

  public DefaultAggregationExecutor(@Nonnull AggregationFunctionContext[] functionContexts) {
    _numFunctions = functionContexts.length;
    _functions = new AggregationFunction[_numFunctions];
    _resultHolders = new AggregationResultHolder[_numFunctions];
    _expressions = new TransformExpressionTree[_numFunctions];
    for (int i = 0; i < _numFunctions; i++) {
      AggregationFunction function = functionContexts[i].getAggregationFunction();
      _functions[i] = function;
      _resultHolders[i] = _functions[i].createAggregationResultHolder();
      if (function.getType() != AggregationFunctionType.COUNT) {
        _expressions[i] = TransformExpressionTree.compileToExpressionTree(functionContexts[i].getColumn());
      }
    }
  }

  @Override
  public void aggregate(@Nonnull TransformBlock transformBlock) {
    int length = transformBlock.getNumDocs();
    for (int i = 0; i < _numFunctions; i++) {
      AggregationFunction function = _functions[i];
      AggregationResultHolder resultHolder = _resultHolders[i];

      if (function.getType() == AggregationFunctionType.COUNT) {
        function.aggregate(length, resultHolder);
      } else {
        function.aggregate(length, resultHolder, transformBlock.getBlockValueSet(_expressions[i]));
      }
    }
  }

  @Override
  public List<Object> getResult() {
    List<Object> aggregationResults = new ArrayList<>(_numFunctions);
    for (int i = 0; i < _numFunctions; i++) {
      aggregationResults.add(_functions[i].extractAggregationResult(_resultHolders[i]));
    }
    return aggregationResults;
  }
}
