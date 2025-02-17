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
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.query.sql.executor.BasicCommandContext;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.DDLExecutionPlan;
import com.arcadedb.query.sql.executor.InternalExecutionPlan;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;

/**
 * Created by luigidellaquila on 12/08/16.
 */
public abstract class DDLStatement extends Statement {

  public DDLStatement(final int id) {
    super(id);
  }

  public DDLStatement(final SqlParser p, final int id) {
    super(p, id);
  }

  public abstract ResultSet executeDDL(CommandContext ctx);

  public ResultSet execute(final Database db, final Object[] args, final CommandContext parentCtx, final boolean usePlanCache) {
    final BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null)
      ctx.setParentWithoutOverridingChild(parentCtx);

    ctx.setDatabase(db);
    ctx.setInputParameters(args);
    final DDLExecutionPlan executionPlan = (DDLExecutionPlan) createExecutionPlan(ctx, false);
    return executionPlan.executeInternal();
  }

  public ResultSet execute(final Database db, final Map params, final CommandContext parentCtx, final boolean usePlanCache) {
    final BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    final DDLExecutionPlan executionPlan = (DDLExecutionPlan) createExecutionPlan(ctx, false);
    return executionPlan.executeInternal();
  }

  public InternalExecutionPlan createExecutionPlan(final CommandContext ctx, final boolean enableProfiling) {
    return new DDLExecutionPlan(ctx, this);
  }
}
