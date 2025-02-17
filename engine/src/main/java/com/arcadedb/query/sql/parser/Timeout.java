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
/* Generated By:JJTree: Do not edit this line. OTimeout.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;

import java.util.*;

public class Timeout extends SimpleNode {
  public static final String RETURN    = "RETURN";
  public static final String EXCEPTION = "EXCEPTION";

  protected Number val;
  protected String failureStrategy;

  public Timeout(final int id) {
    super(id);
  }

  public Timeout(final SqlParser p, final int id) {
    super(p, id);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append(" TIMEOUT " + val);
    if (failureStrategy != null) {
      builder.append(" ");
      builder.append(failureStrategy);
    }
  }

  public Timeout copy() {
    final Timeout result = new Timeout(-1);
    result.val = val;
    result.failureStrategy = failureStrategy;
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final Timeout timeout = (Timeout) o;

    if (!Objects.equals(val, timeout.val))
      return false;
    return Objects.equals(failureStrategy, timeout.failureStrategy);
  }

  @Override
  public int hashCode() {
    int result = val != null ? val.hashCode() : 0;
    result = 31 * result + (failureStrategy != null ? failureStrategy.hashCode() : 0);
    return result;
  }

  public Number getVal() {
    return val;
  }

  public String getFailureStrategy() {
    return failureStrategy;
  }

  public Result serialize() {
    final ResultInternal result = new ResultInternal();
    result.setProperty("val", val);
    result.setProperty("failureStrategy", failureStrategy);
    return result;
  }

  public void deserialize(final Result fromResult) {
    val = fromResult.getProperty("val");
    failureStrategy = fromResult.getProperty("failureStrategy");
  }

  public Timeout setValue(final Number val) {
    this.val = val;
    return this;
  }

}
/* JavaCC - OriginalChecksum=fef7f5d488f7fca1b6ad0b70c6841931 (do not edit this line) */
