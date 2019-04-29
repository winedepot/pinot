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
package org.apache.pinot.pql.parsers.pql2.ast;

import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.common.request.ExpressionType;
import org.apache.pinot.common.request.Identifier;
import org.apache.pinot.pql.parsers.Pql2CompilationException;


/**
 * AST node for an output column.
 */
public class OutputColumnAstNode extends BaseAstNode {
  @Override
  public void updateBrokerRequest(BrokerRequest brokerRequest) {
    for (AstNode astNode : getChildren()) {
      // If the column is a function call, it must be an aggregation function
      if (astNode instanceof FunctionCallAstNode) {
        FunctionCallAstNode node = (FunctionCallAstNode) astNode;
        Expression funcExpression = new Expression(ExpressionType.FUNCTION);
        funcExpression.setFunctionCall(node.buildAggregationInfo());
        brokerRequest.addToSelectList(funcExpression);
      } else if (astNode instanceof IdentifierAstNode) {
        if (brokerRequest.getSelectList() == null) {
          brokerRequest.setSelectList(new ArrayList<>());
        }
        final List<Expression> selectList = brokerRequest.getSelectList();
        IdentifierAstNode node = (IdentifierAstNode) astNode;
        Expression expression = new Expression(ExpressionType.IDENTIFIER);
        expression.setIdentifier(new Identifier(node.getName()));
        selectList.add(expression);
      } else {
        throw new Pql2CompilationException("Output column is neither a function nor an identifier");
      }
    }
  }
}
