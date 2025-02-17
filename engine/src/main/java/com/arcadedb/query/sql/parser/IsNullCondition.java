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
/* Generated By:JJTree: Do not edit this line. OIsNullCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;

import java.util.*;

public class IsNullCondition extends BooleanExpression {

  protected Expression expression;

  public IsNullCondition(final int id) {
    super(id);
  }

  public IsNullCondition(final SqlParser p, final int id) {
    super(p, id);
  }

  @Override
  public boolean evaluate(final Identifiable currentRecord, final CommandContext ctx) {
    return expression.execute(currentRecord, ctx) == null;
  }

  @Override
  public boolean evaluate(final Result currentRecord, final CommandContext ctx) {
    return expression.execute(currentRecord, ctx) == null;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(final Expression expression) {
    this.expression = expression;
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    expression.toString(params, builder);
    builder.append(" is null");
  }

  @Override
  public boolean supportsBasicCalculation() {
    return expression.supportsBasicCalculation();
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    return expression.supportsBasicCalculation() ? 0 : 1;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    if (expression.supportsBasicCalculation()) {
      return Collections.emptyList();
    }
    return Collections.singletonList(expression);
  }

  @Override
  public boolean needsAliases(final Set<String> aliases) {
    return expression.needsAliases(aliases);
  }

  @Override
  public IsNullCondition copy() {
    final IsNullCondition result = new IsNullCondition(-1);
    result.expression = expression.copy();
    return result;
  }

  @Override
  public void extractSubQueries(final SubQueryCollector collector) {
    this.expression.extractSubQueries(collector);
  }

  @Override
  protected Object[] getIdentityElements() {
    return getCacheableElements();
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    return expression.getMatchPatternInvolvedAliases();
  }

  @Override
  protected SimpleNode[] getCacheableElements() {
    return new SimpleNode[] { expression };
  }
}
/* JavaCC - OriginalChecksum=29ebbc506a98f90953af91a66a03aa1e (do not edit this line) */
