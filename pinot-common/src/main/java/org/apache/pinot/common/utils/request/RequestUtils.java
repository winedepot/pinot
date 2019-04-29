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
package org.apache.pinot.common.utils.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang.StringUtils;
import org.apache.pinot.common.request.BrokerRequest;
import org.apache.pinot.common.request.Expression;
import org.apache.pinot.common.request.ExpressionType;
import org.apache.pinot.common.request.FilterOperator;
import org.apache.pinot.common.request.Function;
import org.apache.pinot.common.request.Identifier;
import org.apache.pinot.common.request.Literal;
import org.apache.pinot.common.request.transform.TransformExpressionTree;


public class RequestUtils {
  private static String DELIMTER = "\t\t";

  private RequestUtils() {
  }

  /**
   * Generates thrift compliant filterQuery and populate it in the broker request
   * @param filterQueryTree
   * @param request
   */
  /*
  public static void generateFilterFromTree(FilterQueryTree filterQueryTree, BrokerRequest request) {
    Map<Integer, Expression> filterQueryMap = new HashMap<>();
    MutableInt currentId = new MutableInt(0);
    Expression root = traverseFilterQueryAndPopulateMap(filterQueryTree, filterQueryMap, currentId);
    filterQueryMap.put(root.getId(), root);
    request.setFilterQuery(root);
    FilterQueryMap mp = new FilterQueryMap();
    mp.setFilterQueryMap(filterQueryMap);
    request.setFilterSubQueryMap(mp);
  }

  public static void generateFilterFromTree(HavingQueryTree filterQueryTree, BrokerRequest request) {
    Map<Integer, HavingFilterQuery> filterQueryMap = new HashMap<>();
    MutableInt currentId = new MutableInt(0);
    HavingFilterQuery root = traverseHavingFilterQueryAndPopulateMap(filterQueryTree, filterQueryMap, currentId);
    filterQueryMap.put(root.getId(), root);
    request.setHavingFilterQuery(root);
    HavingFilterQueryMap mp = new HavingFilterQueryMap();
    mp.setFilterQueryMap(filterQueryMap);
    request.setHavingFilterSubQueryMap(mp);
  }

  private static Expression traverseFilterQueryAndPopulateMap(FilterQueryTree tree,
      Map<Integer, Expression> filterQueryMap, MutableInt currentId) {
    int currentNodeId = currentId.intValue();
    currentId.increment();

    final List<Integer> f = new ArrayList<>();
    if (null != tree.getChildren()) {
      for (final FilterQueryTree c : tree.getChildren()) {
        int childNodeId = currentId.intValue();
        currentId.increment();

        f.add(childNodeId);
        final Expression q = traverseFilterQueryAndPopulateMap(c, filterQueryMap, currentId);
        filterQueryMap.put(childNodeId, q);
      }
    }

    Expression query = new Expression();
    Literal column = new Literal();
    column.setValue(tree.getColumn());
    query.setLiteral(column);
    query.set query.setColumn(tree.getColumn());
    query.setSubField(tree.getSubField());
    query.setId(currentNodeId);
    query.setNestedFilterQueryIds(f);
    query.setOperator(tree.getOperator());
    query.setValue(tree.getValue());
    return query;
  }

  private static HavingFilterQuery traverseHavingFilterQueryAndPopulateMap(HavingQueryTree tree,
      Map<Integer, HavingFilterQuery> filterQueryMap, MutableInt currentId) {
    int currentNodeId = currentId.intValue();
    currentId.increment();

    final List<Integer> filterIds = new ArrayList<>();
    if (null != tree.getChildren()) {
      for (final HavingQueryTree child : tree.getChildren()) {
        int childNodeId = currentId.intValue();
        currentId.increment();
        filterIds.add(childNodeId);
        final HavingFilterQuery filterQuery = traverseHavingFilterQueryAndPopulateMap(child, filterQueryMap, currentId);
        filterQueryMap.put(childNodeId, filterQuery);
      }
    }

    HavingFilterQuery havingFilterQuery = new HavingFilterQuery();
    havingFilterQuery.setAggregationInfo(tree.getAggregationInfo());
    havingFilterQuery.setId(currentNodeId);
    havingFilterQuery.setNestedFilterQueryIds(filterIds);
    havingFilterQuery.setOperator(tree.getOperator());
    havingFilterQuery.setValue(tree.getValue());
    return havingFilterQuery;
  }
*/
  /**
   * Generate FilterQueryTree from Broker Request
   * @param request Broker Request
   * @return
   */

  /*
  public static FilterQueryTree generateFilterQueryTree(BrokerRequest request) {
    FilterQueryTree root = null;

    FilterQuery q = request.getFilterQuery();

    if (null != q && null != request.getFilterSubQueryMap()) {
      root = buildFilterQuery(q.getId(), request.getFilterSubQueryMap().getFilterQueryMap());
    }

    return root;
  }

  public static FilterQueryTree buildFilterQuery(Integer id, Map<Integer, FilterQuery> queryMap) {
    FilterQuery q = queryMap.get(id);

    List<Integer> children = q.getNestedFilterQueryIds();

    List<FilterQueryTree> c = null;
    if (null != children && !children.isEmpty()) {
      c = new ArrayList<>();
      for (final Integer i : children) {
        final FilterQueryTree t = buildFilterQuery(i, queryMap);
        c.add(t);
      }
    }

    return new FilterQueryTree(q.getColumn(), q.getValue(), q.getOperator(), c);
  }
*/

  /**
   * Extracts all columns from the given filter query tree.
   */
  public static Set<String> extractFilterColumns(FilterQueryTree root) {
    Set<String> filterColumns = new HashSet<>();
    if (root.getChildren() == null) {
      filterColumns.add(root.getColumn());
    } else {
      Stack<FilterQueryTree> stack = new Stack<>();
      stack.add(root);
      while (!stack.empty()) {
        FilterQueryTree node = stack.pop();
        for (FilterQueryTree child : node.getChildren()) {
          if (child.getChildren() == null) {
            filterColumns.add(child.getColumn());
          } else {
            stack.push(child);
          }
        }
      }
    }
    return filterColumns;
  }

  /**
   * Extracts all columns from the given expressions.
   */
  public static Set<String> extractColumnsFromExpressions(Set<TransformExpressionTree> expressions) {
    Set<String> expressionColumns = new HashSet<>();
    for (TransformExpressionTree expression : expressions) {
      expression.getColumns(expressionColumns);
    }
    return expressionColumns;
  }

  /**
   * Extracts all columns from the given selection, '*' will be ignored.
   * @param selections, orderBys
   */
  public static Set<String> extractSelectionColumns(List<Expression> selections, List<Expression> orderBys) {
    Set<String> selectionColumns = new HashSet<>();
    for (Expression selection : selections) {
      if (!selection.getIdentifier().getName().equals("*")) {
        selectionColumns.add(selection.getIdentifier().getName());
      }
    }
    if (orderBys != null && !orderBys.isEmpty()) {
      for (Expression orderBy : orderBys) {
        selectionColumns.add(orderBy.getIdentifier().getName());
      }
    }
    return selectionColumns;
  }

  /**
   * Generates thrift compliant filterQuery and populate it in the broker request
   * @param filterQueryTree
   * @param request
   */
  public static void generateFilterFromTree(FilterQueryTree filterQueryTree, BrokerRequest request) {
    Expression root = traverseFilterQueryTree(filterQueryTree);
    request.setFilterExpression(root);
  }

  private static Expression traverseFilterQueryTree(FilterQueryTree filterQueryTree) {
    Expression node = new Expression(ExpressionType.FUNCTION);
    Function op = new Function();
    op.setOperator(filterQueryTree.getOperator().toString());
    List<Expression> operands = new ArrayList<>();
    if (filterQueryTree.getChildren() == null || filterQueryTree.getChildren().isEmpty()) {
      // Leaf node
      node.setIdentifier(new Identifier(filterQueryTree.getColumn()));
      node.setLiteral(new Literal(StringUtils.join(filterQueryTree.getValue(), DELIMTER)));
    } else {
      // Non-leaf node
      for (FilterQueryTree treeNode : filterQueryTree.getChildren()) {
        operands.add(traverseFilterQueryTree(treeNode));
      }
    }
    op.setOperands(operands);
    node.setFunctionCall(op);
    return node;
  }

  /**
   * Generates thrift compliant filterQuery and populate it in the broker request
   * @param havingQueryTree
   * @param request
   */
  public static void generateFilterFromTree(HavingQueryTree havingQueryTree, BrokerRequest request) {
    Expression root = traverseHavingQueryTree(havingQueryTree);
    request.setHavingExpression(root);
  }

  private static Expression traverseHavingQueryTree(HavingQueryTree havingQueryTree) {
    Expression node = new Expression(ExpressionType.FUNCTION);

    Function op = new Function();
    op.setOperator(havingQueryTree.getOperator().name());
    List<Expression> operands = new ArrayList<>();
    if (havingQueryTree.getChildren() == null || havingQueryTree.getChildren().isEmpty()) {
      final Expression func = new Expression(ExpressionType.FUNCTION);
      func.setFunctionCall(havingQueryTree.getFunction());
      operands.add(func);
      final Expression right = new Expression(ExpressionType.LITERAL);
      right.setLiteral(new Literal(StringUtils.join(havingQueryTree.getValue(), DELIMTER)));
      operands.add(right);
    } else {
      // Non-leaf node
      for (HavingQueryTree treeNode : havingQueryTree.getChildren()) {
        operands.add(traverseHavingQueryTree(treeNode));
      }
    }
    op.setOperands(operands);
    node.setFunctionCall(op);
    return node;
  }

  public static Expression generateFilterQuery(String op, String left, String right) {
    Expression expr = new Expression(ExpressionType.FUNCTION);
    Function func = new Function(op);
    Expression leftExpr = new Expression(ExpressionType.IDENTIFIER);
    leftExpr.setIdentifier(new Identifier(left));
    func.addToOperands(leftExpr);
    Expression rightExpr = new Expression(ExpressionType.LITERAL);
    rightExpr.setLiteral(new Literal(right));
    func.addToOperands(rightExpr);
    expr.setFunctionCall(func);
    return expr;
  }

  public static boolean isSelectionQuery(BrokerRequest brokerRequest) {
    final List<Expression> selectList = brokerRequest.getSelectList();
    if (selectList == null) {
      return true;
    }
    for (Expression select : selectList) {
      if (select.getFunctionCall() != null) {
        return false;
      }
    }
    return true;
  }

  public static List<Function> extractFunctions(BrokerRequest brokerRequest) {
    return extractFunctions(brokerRequest.getSelectList());
  }

  public static List<Function> extractFunctions(List<Expression> selectList) {
    List<Function> functions = null;
    for (Expression expr : selectList) {
      if (expr.isSetFunctionCall()) {
        if (functions == null) {
          functions = new ArrayList<>();
        }
        functions.add(expr.getFunctionCall());
      }
    }
    return functions;
  }

  public static FilterQueryTree generateFilterQueryTree(BrokerRequest request) {
    return generateFilterQueryTree(request.getFilterExpression());
  }

  public static FilterQueryTree generateFilterQueryTree(Expression filterExpression) {
    return buildFilterQuery(filterExpression);
  }

  private static FilterQueryTree buildFilterQuery(Expression filterExpression) {
    if (filterExpression == null) {
      return null;
    }
    List<FilterQueryTree> c = null;
    if (filterExpression.getFunctionCall() != null) {
      List<Expression> children = filterExpression.getFunctionCall().getOperands();
      if (null != children && !children.isEmpty()) {
        c = new ArrayList<>();
        for (final Expression i : children) {
          final FilterQueryTree t = buildFilterQuery(i);
          c.add(t);
        }
      }
    }
    String column = null;
    if (filterExpression.getIdentifier() != null) {
      column = filterExpression.getIdentifier().getName();
    }
    FilterOperator operator = null;
    if (filterExpression.getFunctionCall() != null) {
      operator = FilterOperator.valueOf(filterExpression.getFunctionCall().getOperator());
    }
    List<String> value = null;
    if (filterExpression.getLiteral() != null) {
      //value = Arrays.asList(StringUtils.split(filterExpression.getLiteral().getValue(), "\t\t"));
      value = Arrays.asList(filterExpression.getLiteral().getValue());
    }
    return new FilterQueryTree(column, value, operator, c);
  }

  public static boolean isAggregationQuery(BrokerRequest brokerRequest) {
    final List<Expression> selectList = brokerRequest.getSelectList();
    if (selectList == null) {
      return false;
    }
    for (Expression select : selectList) {
      if (select.getFunctionCall() != null) {
        return true;
      }
    }
    return false;
  }

  public static List<String> extractGroupByExpression(List<Expression> groupBys) {
    List<String> groupbyExpressions = new ArrayList<>();
    for (Expression groupby : groupBys) {
      groupbyExpressions.add(groupby.getIdentifier().getName());
    }
    return groupbyExpressions;
  }
}
