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
/* Generated By:JJTree: Do not edit this line. OMethodCall.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.*;

import java.util.*;
import java.util.stream.Collectors;

public class MethodCall extends SimpleNode {

  static Map<String, String> bidirectionalMethods = Map.of(//
      "out", "in",//
      "in", "out", //
      "both", "both", //
      "oute", "outv", //
      "ine", "inv", //
      "bothe", "bothe", //
      "bothv", "bothv", //
      "outv", "oute", //
      "inv", "ine");

  protected Identifier       methodName;
  protected List<Expression> params = new ArrayList<Expression>();

  private Boolean calculatedIsGraph = null;

  public MethodCall(int id) {
    super(id);
  }

  public MethodCall(SqlParser p, int id) {
    super(p, id);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append(".");
    methodName.toString(params, builder);
    builder.append("(");
    boolean first = true;
    for (Expression param : this.params) {
      if (!first) {
        builder.append(", ");
      }
      param.toString(params, builder);
      first = false;
    }
    builder.append(")");
  }

  public boolean isBidirectional() {
    return bidirectionalMethods.containsKey(methodName.getStringValue().toLowerCase(Locale.ENGLISH));
  }

  public Object execute(Object targetObjects, CommandContext ctx) {
    return execute(targetObjects, ctx, methodName.getStringValue(), params, null);
  }

  public Object execute(Object targetObjects, Iterable<Identifiable> iPossibleResults, CommandContext ctx) {
    return execute(targetObjects, ctx, methodName.getStringValue(), params, iPossibleResults);
  }

  private Object execute(final Object targetObjects, final CommandContext ctx, final String name, final List<Expression> iParams,
      final Iterable<Identifiable> iPossibleResults) {
    final List<Object> paramValues = new ArrayList<Object>();
    Object val = ctx.getVariable("$current");
    if (val == null && targetObjects == null) {
      return null;
    }
    for (Expression expr : iParams) {
      if (val instanceof Identifiable) {
        paramValues.add(expr.execute((Identifiable) val, ctx));
      } else if (val instanceof Result) {
        paramValues.add(expr.execute((Result) val, ctx));
      } else if (targetObjects instanceof Identifiable) {
        paramValues.add(expr.execute((Identifiable) targetObjects, ctx));
      } else if (targetObjects instanceof Result) {
        paramValues.add(expr.execute((Result) targetObjects, ctx));
      } else {
        throw new CommandExecutionException("Invalid value for $current: " + val);
      }
    }
    if (isGraphFunction()) {
      SQLFunction function = SQLEngine.getInstance().getFunction(name);
      if (function instanceof SQLFunctionFiltered) {
        Object current = ctx.getVariable("$current");
        if (current instanceof Result) {
          current = ((Result) current).getElement().orElse(null);
        }
        return ((SQLFunctionFiltered) function).execute(targetObjects, (Identifiable) current, null, paramValues.toArray(), iPossibleResults, ctx);
      } else {
        final Object current = ctx.getVariable("$current");
        if (current instanceof Identifiable) {
          return function.execute(targetObjects, (Identifiable) current, null, paramValues.toArray(), ctx);
        } else if (current instanceof Result) {
          return function.execute(targetObjects, ((Result) current).getElement().orElse(null), null, paramValues.toArray(), ctx);
        } else {
          return function.execute(targetObjects, null, null, paramValues.toArray(), ctx);
        }
      }

    }

    final SQLMethod method = SQLEngine.getInstance().getMethod(name);
    if (method != null) {
      if (val instanceof Result)
        val = ((Result) val).getElement().orElse(null);

      return method.execute(targetObjects, (Identifiable) val, ctx, targetObjects, paramValues.toArray());
    }
    throw new UnsupportedOperationException("OMethod call, something missing in the implementation...?");

  }

  public Object executeReverse(final Object targetObjects, final CommandContext ctx) {
    final String straightName = methodName.getStringValue().toLowerCase();
    final String inverseMethodName = bidirectionalMethods.get(straightName);

    if (inverseMethodName != null)
      return execute(targetObjects, ctx, inverseMethodName, params, null);

    throw new UnsupportedOperationException("Invalid reverse traversal: " + methodName);
  }

  public boolean needsAliases(Set<String> aliases) {
    for (Expression param : params) {
      if (param.needsAliases(aliases)) {
        return true;
      }
    }
    return false;
  }

  public MethodCall copy() {
    final MethodCall result = new MethodCall(-1);
    result.methodName = methodName.copy();
    result.params = params.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final MethodCall that = (MethodCall) o;

    if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
      return false;
    return params != null ? params.equals(that.params) : that.params == null;
  }

  @Override
  public int hashCode() {
    int result = methodName != null ? methodName.hashCode() : 0;
    result = 31 * result + (params != null ? params.hashCode() : 0);
    return result;
  }

  public void extractSubQueries(final SubQueryCollector collector) {
    if (params != null) {
      for (Expression param : params) {
        param.extractSubQueries(collector);
      }
    }
  }

  public boolean refersToParent() {
    if (params != null) {
      for (Expression exp : params) {
        if (exp.refersToParent()) {
          return true;
        }
      }
    }
    return false;
  }

  public Result serialize() {
    final ResultInternal result = new ResultInternal();
    if (methodName != null)
      result.setProperty("methodName", methodName.serialize());

    if (params != null)
      result.setProperty("items", params.stream().map(x -> x.serialize()).collect(Collectors.toList()));

    return result;
  }

  public void deserialize(final Result fromResult) {
    if (fromResult.getProperty("methodName") != null) {
      methodName = new Identifier(-1);
      Identifier.deserialize(fromResult.getProperty("methodName"));
    }
    if (fromResult.getProperty("params") != null) {
      final List<Result> ser = fromResult.getProperty("params");
      params = new ArrayList<>();
      for (Result r : ser) {
        Expression exp = new Expression(-1);
        exp.deserialize(r);
        params.add(exp);
      }
    }
  }

  public boolean isCacheable() {
    return isGraphFunction();
  }

  private boolean isGraphFunction() {
    if (calculatedIsGraph != null)
      return calculatedIsGraph;

    final String methodNameLC = methodName.getStringValue().toLowerCase();

    for (String graphMethod : bidirectionalMethods.keySet()) {
      if (graphMethod.equals(methodNameLC)) {
        calculatedIsGraph = true;
        break;
      }
    }

    if (calculatedIsGraph == null)
      calculatedIsGraph = false;

    return calculatedIsGraph;
  }
}
/* JavaCC - OriginalChecksum=da95662da21ceb8dee3ad88c0d980413 (do not edit this line) */
