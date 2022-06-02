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
/* Generated By:JJTree: Do not edit this line. OBaseIdentifier.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.AggregationContext;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;

import java.util.Map;
import java.util.Set;

public class BaseIdentifier extends SimpleNode {

  protected LevelZeroIdentifier levelZero;

  protected SuffixIdentifier suffix;

  public BaseIdentifier(int id) {
    super(id);
  }

  public BaseIdentifier(SqlParser p, int id) {
    super(p, id);
  }

  public BaseIdentifier(Identifier identifier) {
    this.suffix = new SuffixIdentifier(identifier);
  }

  public BaseIdentifier(RecordAttribute attr) {
    this.suffix = new SuffixIdentifier(attr);
  }

  public void toString(Map<String, Object> params, StringBuilder builder) {
    if (levelZero != null) {
      levelZero.toString(params, builder);
    } else if (suffix != null) {
      suffix.toString(params, builder);
    }
  }

  public Object execute(Record iCurrentRecord, CommandContext ctx) {
    if (levelZero != null) {
      return levelZero.execute(iCurrentRecord, ctx);
    }
    if (suffix != null) {
      return suffix.execute(iCurrentRecord, ctx);
    }
    return null;
  }

  public Object execute(Result iCurrentRecord, CommandContext ctx) {
    if (levelZero != null) {
      return levelZero.execute(iCurrentRecord, ctx);
    }
    if (suffix != null) {
      return suffix.execute(iCurrentRecord, ctx);
    }
    return null;
  }

  public boolean isIndexedFunctionCall() {
    if (levelZero != null) {
      return levelZero.isIndexedFunctionCall();
    }
    return false;
  }

  public long estimateIndexedFunction(FromClause target, CommandContext context, BinaryCompareOperator operator, Object right) {
    if (levelZero != null) {
      return levelZero.estimateIndexedFunction(target, context, operator, right);
    }

    return -1;
  }

  public Iterable<Record> executeIndexedFunction(FromClause target, CommandContext context, BinaryCompareOperator operator, Object right) {
    if (levelZero != null) {
      return levelZero.executeIndexedFunction(target, context, operator, right);
    }

    return null;
  }

  /**
   * tests if current expression is an indexed function AND that function can also be executed without using the index
   *
   * @param target   the query target
   * @param context  the execution context
   * @param operator
   * @param right
   *
   * @return true if current expression is an indexed function AND that function can also be executed without using the index, false
   * otherwise
   */
  public boolean canExecuteIndexedFunctionWithoutIndex(FromClause target, CommandContext context, BinaryCompareOperator operator, Object right) {
    if (this.levelZero == null) {
      return false;
    }
    return levelZero.canExecuteIndexedFunctionWithoutIndex(target, context, operator, right);
  }

  /**
   * tests if current expression is an indexed function AND that function can be used on this target
   *
   * @param target   the query target
   * @param context  the execution context
   * @param operator
   * @param right
   *
   * @return true if current expression involves an indexed function AND that function can be used on this target, false otherwise
   */
  public boolean allowsIndexedFunctionExecutionOnTarget(FromClause target, CommandContext context, BinaryCompareOperator operator, Object right) {
    if (this.levelZero == null) {
      return false;
    }
    return levelZero.allowsIndexedFunctionExecutionOnTarget(target, context, operator, right);
  }

  /**
   * tests if current expression is an indexed function AND the function has also to be executed after the index search. In some
   * cases, the index search is accurate, so this condition can be excluded from further evaluation. In other cases the result from
   * the index is a superset of the expected result, so the function has to be executed anyway for further filtering
   *
   * @param target  the query target
   * @param context the execution context
   *
   * @return true if current expression is an indexed function AND the function has also to be executed after the index search.
   */
  public boolean executeIndexedFunctionAfterIndexSearch(FromClause target, CommandContext context, BinaryCompareOperator operator, Object right) {
    if (this.levelZero == null) {
      return false;
    }
    return levelZero.executeIndexedFunctionAfterIndexSearch(target, context, operator, right);
  }

  public boolean isBaseIdentifier() {
    return suffix != null && suffix.isBaseIdentifier();
  }

  public boolean isExpand() {
    if (levelZero != null) {
      return levelZero.isExpand();
    }
    return false;
  }

  public Expression getExpandContent() {
    return levelZero.getExpandContent();
  }

  public boolean needsAliases(Set<String> aliases) {
    if (levelZero != null && levelZero.needsAliases(aliases)) {
      return true;
    }
    return suffix != null && suffix.needsAliases(aliases);
  }

  public boolean isAggregate() {
    if (levelZero != null && levelZero.isAggregate()) {
      return true;
    }
    return suffix != null && suffix.isAggregate();
  }

  public boolean isCount() {
    if (levelZero != null && levelZero.isCount()) {
      return true;
    }
    return suffix != null && suffix.isCount();
  }

  public boolean isEarlyCalculated() {
    if (levelZero != null && levelZero.isEarlyCalculated()) {
      return true;
    }
    return suffix != null && suffix.isEarlyCalculated();
  }

  public SimpleNode splitForAggregation(AggregateProjectionSplit aggregateProj) {
    if (isAggregate()) {
      BaseIdentifier result = new BaseIdentifier(-1);
      if (levelZero != null) {
        SimpleNode splitResult = levelZero.splitForAggregation(aggregateProj);
        if (splitResult instanceof LevelZeroIdentifier) {
          result.levelZero = (LevelZeroIdentifier) splitResult;
        } else {
          return splitResult;
        }
      } else if (suffix != null) {
        result.suffix = suffix.splitForAggregation(aggregateProj);
      } else {
        throw new IllegalStateException();
      }
      return result;
    } else {
      return this;
    }
  }

  public AggregationContext getAggregationContext(CommandContext ctx) {
    if (isAggregate()) {

      if (levelZero != null) {
        return levelZero.getAggregationContext(ctx);
      } else if (suffix != null) {
        return suffix.getAggregationContext(ctx);
      } else {
        throw new CommandExecutionException("cannot aggregate on " + this);
      }
    } else {
      throw new CommandExecutionException("cannot aggregate on " + this);
    }
  }

  public void setLevelZero(LevelZeroIdentifier levelZero) {
    this.levelZero = levelZero;
  }

  public BaseIdentifier copy() {
    BaseIdentifier result = new BaseIdentifier(-1);
    result.levelZero = levelZero == null ? null : levelZero.copy();
    result.suffix = suffix == null ? null : suffix.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    BaseIdentifier that = (BaseIdentifier) o;

    if (levelZero != null ? !levelZero.equals(that.levelZero) : that.levelZero != null)
      return false;
    return suffix != null ? suffix.equals(that.suffix) : that.suffix == null;
  }

  @Override
  public int hashCode() {
    int result = levelZero != null ? levelZero.hashCode() : 0;
    result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
    return result;
  }

  public boolean refersToParent() {
    if (levelZero != null && levelZero.refersToParent()) {
      return true;
    }
    return suffix != null && suffix.refersToParent();
  }

  public SuffixIdentifier getSuffix() {
    return suffix;
  }

  public LevelZeroIdentifier getLevelZero() {

    return levelZero;
  }

  public void applyRemove(ResultInternal result, CommandContext ctx) {
    if (suffix != null) {
      suffix.applyRemove(result, ctx);
    } else {
      throw new CommandExecutionException("cannot apply REMOVE " + this);
    }
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (levelZero != null) {
      result.setProperty("levelZero", levelZero.serialize());
    }
    if (suffix != null) {
      result.setProperty("suffix", suffix.serialize());
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("levelZero") != null) {
      levelZero = new LevelZeroIdentifier(-1);
      levelZero.deserialize(fromResult.getProperty("levelZero"));
    }
    if (fromResult.getProperty("suffix") != null) {
      suffix = new SuffixIdentifier(-1);
      suffix.deserialize(fromResult.getProperty("suffix"));
    }
  }

  public boolean isDefinedFor(Result currentRecord) {
    if (suffix != null) {
      return suffix.isDefinedFor(currentRecord);
    }
    return true;
  }

  public boolean isDefinedFor(Record currentRecord) {
    if (suffix != null) {
      return suffix.isDefinedFor(currentRecord);
    }
    return true;
  }

  public void extractSubQueries(Identifier letAlias, SubQueryCollector collector) {
    if (this.levelZero != null) {
      this.levelZero.extractSubQueries(letAlias, collector);
    }
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (this.levelZero != null) {
      this.levelZero.extractSubQueries(collector);
    }
  }

  public boolean isCacheable() {
    if (levelZero != null) {
      return levelZero.isCacheable();
    }

    if (suffix != null) {
      return suffix.isCacheable();
    }

    return true;
  }
}
/* JavaCC - OriginalChecksum=ed89af10d8be41a83428c5608a4834f6 (do not edit this line) */
