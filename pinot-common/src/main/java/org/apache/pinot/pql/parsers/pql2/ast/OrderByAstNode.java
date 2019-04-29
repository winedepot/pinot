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
import org.apache.pinot.common.request.Literal;
import org.apache.pinot.pql.parsers.Pql2CompilationException;


/**
 * AST node for ORDER BY clauses.
 */
public class OrderByAstNode extends BaseAstNode {
  @Override
  public void updateBrokerRequest(BrokerRequest brokerRequest) {
    final List<Expression> orderByList = new ArrayList<>();
    for (AstNode astNode : getChildren()) {
      if (astNode instanceof OrderByExpressionAstNode) {
        OrderByExpressionAstNode node = (OrderByExpressionAstNode) astNode;
        Expression expression = new Expression(ExpressionType.IDENTIFIER);
        expression.setIdentifier(new Identifier(node.getColumn()));
        // A hack to set ordering: ASC/DESC
        expression.setLiteral(new Literal(node.getOrdering()));
        orderByList.add(expression);
      } else {
        throw new Pql2CompilationException("Child node of ORDER BY node is not an expression node");
      }
    }
    brokerRequest.setOrderByList(orderByList);
  }
}
