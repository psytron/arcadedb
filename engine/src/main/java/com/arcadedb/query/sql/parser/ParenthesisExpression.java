/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
/* Generated By:JJTree: Do not edit this line. OParenthesisExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InsertExecutionPlan;
import com.arcadedb.query.sql.executor.InternalExecutionPlan;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;

import java.util.*;

public class ParenthesisExpression extends MathExpression {

  protected Expression expression;
  protected Statement  statement;

  public ParenthesisExpression(final int id) {
    super(id);
  }

  public ParenthesisExpression(final SqlParser p, final int id) {
    super(p, id);
  }

  public ParenthesisExpression(final Expression exp) {
    super(-1);
    this.expression = exp;
  }

  @Override
  public Object execute(final Identifiable iCurrentRecord, final CommandContext ctx) {
    if (expression != null) {
      return expression.execute(iCurrentRecord, ctx);
    }
    if (statement != null) {
      throw new UnsupportedOperationException("Execution of select in parentheses is not supported");
    }
    return super.execute(iCurrentRecord, ctx);
  }

  @Override
  public Object execute(final Result iCurrentRecord, final CommandContext ctx) {
    if (expression != null) {
      return expression.execute(iCurrentRecord, ctx);
    }
    if (statement != null) {
      final InternalExecutionPlan execPlan = statement.createExecutionPlan(ctx, false);

      if (execPlan instanceof InsertExecutionPlan) {
        ((InsertExecutionPlan) execPlan).executeInternal();
      }
      final LocalResultSet rs = new LocalResultSet(execPlan);
      final List<Result> result = new ArrayList<>();
      while (rs.hasNext()) {
        result.add(rs.next());
      }
//      List<OResult> result = rs.stream().collect(Collectors.toList());//TODO streamed...
      rs.close();
      return result;
    }
    return super.execute(iCurrentRecord, ctx);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("(");
    if (expression != null) {
      expression.toString(params, builder);
    } else if (statement != null) {
      statement.toString(params, builder);
    }
    builder.append(")");
  }

  @Override
  protected boolean supportsBasicCalculation() {
    if (expression != null) {
      return expression.supportsBasicCalculation();
    }
    return true;
  }

  @Override
  public boolean isEarlyCalculated(final CommandContext ctx) {
    // TODO implement query execution and early calculation;
    return expression != null && expression.isEarlyCalculated(ctx);
  }

  public boolean needsAliases(final Set<String> aliases) {
    return expression.needsAliases(aliases);
  }

  public boolean isExpand() {
    if (expression != null) {
      return expression.isExpand();
    }
    return false;
  }

  public boolean isAggregate() {
    if (expression != null) {
      return expression.isAggregate();
    }
    return false;
  }

  public boolean isCount() {
    if (expression != null)
      return expression.isCount();

    return false;
  }

  public SimpleNode splitForAggregation(final AggregateProjectionSplit aggregateProj, final CommandContext ctx) {
    if (isAggregate()) {
      final ParenthesisExpression result = new ParenthesisExpression(-1);
      result.expression = expression.splitForAggregation(aggregateProj, ctx);
      return result;
    } else {
      return this;
    }
  }

  @Override
  public ParenthesisExpression copy() {
    final ParenthesisExpression result = new ParenthesisExpression(-1);
    result.expression = expression == null ? null : expression.copy();
    result.statement = statement == null ? null : statement.copy();
    result.cachedStringForm = cachedStringForm;
    return result;
  }

  public void setStatement(final Statement statement) {
    this.statement = statement;
  }

  public void extractSubQueries(final SubQueryCollector collector) {
    if (expression != null) {
      expression.extractSubQueries(collector);
    } else if (statement != null) {
      final Identifier alias = collector.addStatement(statement);
      statement = null;
      expression = new Expression(alias);
    }
  }

  public void extractSubQueries(final Identifier letAlias, final SubQueryCollector collector) {
    if (expression != null) {
      expression.extractSubQueries(collector);
    } else if (statement != null) {
      final Identifier alias = collector.addStatement(letAlias, statement);
      statement = null;
      expression = new Expression(alias);
    }
  }

  public boolean refersToParent() {
    if (expression != null && expression.refersToParent()) {
      return true;
    }
    return statement != null && statement.refersToParent();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    final ParenthesisExpression that = (ParenthesisExpression) o;

    if (!Objects.equals(expression, that.expression))
      return false;
    return Objects.equals(statement, that.statement);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    result = 31 * result + (statement != null ? statement.hashCode() : 0);
    return result;
  }

  public List<String> getMatchPatternInvolvedAliases() {
    return expression.getMatchPatternInvolvedAliases();//TODO also check the statement...?
  }

  @Override
  public void applyRemove(final ResultInternal result, final CommandContext ctx) {
    if (expression != null) {
      expression.applyRemove(result, ctx);
    } else {
      throw new CommandExecutionException("Cannot apply REMOVE " + this);
    }
  }

  public Result serialize() {
    final ResultInternal result = (ResultInternal) super.serialize();
    if (expression != null) {
      result.setProperty("expression", expression.serialize());
    }
    if (statement != null) {
      result.setProperty("statement", statement.serialize());
    }
    return result;
  }

  public void deserialize(final Result fromResult) {
    super.deserialize(fromResult);
    if (fromResult.getProperty("expression") != null) {
      expression = new Expression(-1);
      expression.deserialize(fromResult.getProperty("expression"));
    }
    if (fromResult.getProperty("statement") != null) {
      statement = Statement.deserializeFromOResult(fromResult.getProperty("statement"));
    }
  }

  @Override
  protected SimpleNode[] getCacheableElements() {
    return new SimpleNode[] { expression, statement };
  }

}
/* JavaCC - OriginalChecksum=4656e5faf4f54dc3fc45a06d8e375c35 (do not edit this line) */
