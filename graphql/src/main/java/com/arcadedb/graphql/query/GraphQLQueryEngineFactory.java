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
package com.arcadedb.graphql.query;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.exception.QueryParsingException;
import com.arcadedb.graphql.schema.GraphQLSchema;
import com.arcadedb.log.LogManager;
import com.arcadedb.query.QueryEngine;

import java.util.logging.*;

import static com.arcadedb.graphql.query.GraphQLQueryEngine.ENGINE_NAME;

public class GraphQLQueryEngineFactory implements QueryEngine.QueryEngineFactory {
  @Override
  public String getLanguage() {
    return "graphql";
  }

  @Override
  public QueryEngine getInstance(final DatabaseInternal database) {
    if (database.getWrappers().containsKey(ENGINE_NAME))
      return (QueryEngine) database.getWrappers().get(ENGINE_NAME);

    try {
      final GraphQLSchema schema = new GraphQLSchema(database);
      final QueryEngine engine = new GraphQLQueryEngine(schema);
      database.setWrapper(ENGINE_NAME, engine);
      return engine;

    } catch (final Throwable e) {
      LogManager.instance().log(this, Level.SEVERE, "Error on initializing GraphQL query engine", e);
      throw new QueryParsingException("Error on initializing GraphQL query engine", e);
    }
  }
}
