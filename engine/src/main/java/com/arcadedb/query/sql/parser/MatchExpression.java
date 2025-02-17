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
/* Generated By:JJTree: Do not edit this line. OMatchExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import java.util.*;
import java.util.stream.*;

public class MatchExpression extends SimpleNode {
  protected MatchFilter         origin;
  protected List<MatchPathItem> items = new ArrayList<MatchPathItem>();

  public MatchExpression(final int id) {
    super(id);
  }

  public MatchExpression(final SqlParser p, final int id) {
    super(p, id);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    origin.toString(params, builder);
    for (final MatchPathItem item : items) {
      item.toString(params, builder);
    }
  }

  @Override
  public MatchExpression copy() {
    final MatchExpression result = new MatchExpression(-1);
    result.origin = origin == null ? null : origin.copy();
    result.items = items == null ? null : items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals( final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final  MatchExpression that = (MatchExpression) o;

    if (!Objects.equals(origin, that.origin))
      return false;
    return Objects.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    int result = origin != null ? origin.hashCode() : 0;
    result = 31 * result + (items != null ? items.hashCode() : 0);
    return result;
  }

  public MatchFilter getOrigin() {
    return origin;
  }

  public void setOrigin(final MatchFilter origin) {
    this.origin = origin;
  }

  public List<MatchPathItem> getItems() {
    return items;
  }

  public void setItems(final List<MatchPathItem> items) {
    this.items = items;
  }
}
/* JavaCC - OriginalChecksum=73491fb653c32baf66997290db29f370 (do not edit this line) */
