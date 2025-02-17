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
package com.arcadedb.query.sql.executor;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.TimeoutException;
import com.arcadedb.query.sql.parser.WhereClause;

import java.util.*;

/**
 * Created by luigidellaquila on 12/07/16.
 */
public class FilterStep extends AbstractExecutionStep {
  private WhereClause whereClause;

  ResultSet prevResult = null;

  public FilterStep(final WhereClause whereClause, final CommandContext ctx, final boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.whereClause = whereClause;
  }

  @Override
  public ResultSet syncPull(final CommandContext ctx, final int nRecords) throws TimeoutException {
    if (prev.isEmpty())
      throw new IllegalStateException("filter step requires a previous step");

    final ExecutionStepInternal prevStep = prev.get();

    return new ResultSet() {
      public boolean finished = false;

      Result nextItem = null;
      int fetched = 0;

      private void fetchNextItem() {
        nextItem = null;
        if (finished) {
          return;
        }
        if (prevResult == null) {
          prevResult = prevStep.syncPull(ctx, nRecords);
          if (!prevResult.hasNext()) {
            finished = true;
            return;
          }
        }
        while (!finished) {
          while (!prevResult.hasNext()) {
            prevResult = prevStep.syncPull(ctx, nRecords);
            if (!prevResult.hasNext()) {
              finished = true;
              return;
            }
          }
          nextItem = prevResult.next();
          final long begin = profilingEnabled ? System.nanoTime() : 0;
          try {
            if (whereClause.matchesFilters(nextItem, ctx)) {
              break;
            }

            nextItem = null;
          } finally {
            if (profilingEnabled) {
              cost += (System.nanoTime() - begin);
            }
          }
        }
      }

      @Override
      public boolean hasNext() {
        if (fetched >= nRecords || finished)
          return false;

        if (nextItem == null)
          fetchNextItem();

        return nextItem != null;
      }

      @Override
      public Result next() {
        if (fetched >= nRecords || finished)
          throw new NoSuchElementException();

        if (nextItem == null)
          fetchNextItem();

        if (nextItem == null)
          throw new NoSuchElementException();

        final Result result = nextItem;
        nextItem = null;
        fetched++;
        return result;
      }

      @Override
      public void close() {
        FilterStep.this.close();
      }
    };
  }

  @Override
  public String prettyPrint(final int depth, final int indent) {
    final StringBuilder result = new StringBuilder();
    result.append(ExecutionStepInternal.getIndent(depth, indent)).append("+ FILTER ITEMS WHERE ");
    if (profilingEnabled)
      result.append(" (").append(getCostFormatted()).append(")");

    result.append("\n");
    result.append(ExecutionStepInternal.getIndent(depth, indent));
    result.append("  ");
    result.append(whereClause.toString());
    return result.toString();
  }

  @Override
  public Result serialize() {
    final ResultInternal result = ExecutionStepInternal.basicSerialize(this);
    if (whereClause != null)
      result.setProperty("whereClause", whereClause.serialize());

    return result;
  }

  @Override
  public void deserialize(final Result fromResult) {
    try {
      ExecutionStepInternal.basicDeserialize(fromResult, this);
      whereClause = new WhereClause(-1);
      whereClause.deserialize(fromResult.getProperty("whereClause"));
    } catch (final Exception e) {
      throw new CommandExecutionException(e);
    }
  }

  @Override
  public boolean canBeCached() {
    return true;
  }

  @Override
  public ExecutionStep copy(final CommandContext ctx) {
    return new FilterStep(this.whereClause.copy(), ctx, profilingEnabled);
  }
}
