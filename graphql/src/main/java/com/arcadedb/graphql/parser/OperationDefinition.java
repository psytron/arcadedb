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
/* Generated by: JJTree: Do not edit this line. OperationDefinition.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class OperationDefinition extends Definition {
  protected SelectionSet        selectionSet;
  protected OperationType       operationType;
  protected Name                name;
  protected VariableDefinitions variableDefinitions;
  protected Directives          directives;

  public OperationDefinition(final int id) {
    super(id);
  }

  public OperationDefinition(final GraphQLParser p, final int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(final GraphQLParserVisitor visitor, final Object data) {
    return visitor.visit(this, data);
  }

  public SelectionSet getSelectionSet() {
    return selectionSet;
  }

  public Directives getDirectives() {
    return directives;
  }

  public boolean isQuery() {
    return operationType == null || operationType.isQuery();
  }

  public String getName() {
    return name != null ? name.value : null;
  }
}
/* ParserGeneratorCC - OriginalChecksum=e02cbdbebbb7227a93ebc862dcd13871 (do not edit this line) */
