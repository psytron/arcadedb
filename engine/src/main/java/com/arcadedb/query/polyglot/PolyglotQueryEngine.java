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
 */
package com.arcadedb.query.polyglot;

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.QueryEngine;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;

import java.util.*;
import java.util.concurrent.*;

public class PolyglotQueryEngine implements QueryEngine {
  private       GraalPolyglotEngine          polyglotEngine;
  private final String                       language;
  private final long                         timeout;
  private final DatabaseInternal             database;
  private       List<String>                 allowedPackages = null;
  private       ExecutorService              userCodeExecutor;
  private       ArrayBlockingQueue<Runnable> userCodeExecutorQueue;

  private static final AnalyzedQuery ANALYZED_QUERY = new AnalyzedQuery() {
    @Override
    public boolean isIdempotent() {
      return false;
    }

    @Override
    public boolean isDDL() {
      return false;
    }
  };

  public static class PolyglotQueryEngineFactory implements QueryEngineFactory {
    private final String       language;
    private       List<String> allowedPackages = null;

    public PolyglotQueryEngineFactory(final String language) {
      this.language = language;
    }

    public PolyglotQueryEngineFactory setAllowedPackages(final List<String> allowedPackages) {
      this.allowedPackages = allowedPackages;
      return this;
    }

    @Override
    public String getLanguage() {
      return language;
    }

    @Override
    public QueryEngine getInstance(final DatabaseInternal database) {
      return new PolyglotQueryEngine(database, language, allowedPackages);
    }

    public static Iterable<String> getSupportedLanguages() {
      return GraalPolyglotEngine.newBuilder(null, Engine.create()).build().getSupportedLanguages();
    }
  }

  protected PolyglotQueryEngine(final DatabaseInternal database, final String language, final List<String> allowedPackages) {
    this.language = language;
    this.database = database;
    this.allowedPackages = allowedPackages;
    this.polyglotEngine = GraalPolyglotEngine.newBuilder(database, Engine.create()).setLanguage(language).setAllowedPackages(allowedPackages).build();
    this.userCodeExecutorQueue = new ArrayBlockingQueue<>(10000);
    this.userCodeExecutor = new ThreadPoolExecutor(8, 8, 30, TimeUnit.SECONDS, userCodeExecutorQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    this.timeout = database.getConfiguration().getValueAsLong(GlobalConfiguration.POLYGLOT_COMMAND_TIMEOUT);
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public ResultSet command(final String query, final Object... parameters) {
    if (parameters == null || parameters.length == 0)
      return command(query, (Map) null);
    throw new UnsupportedOperationException("Execution of a command with positional parameter is not supported for polyglot engine");
  }

  @Override
  public ResultSet command(final String query, final Map<String, Object> parameters) {
    try {
      return executeUserCode(() -> {

        synchronized (polyglotEngine) {
          if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet())
              polyglotEngine.setAttribute(entry.getKey(), entry.getValue());
          }

          final Value result = polyglotEngine.eval(query);

          if (result.isHostObject()) {
            final Object host = result.asHostObject();
            if (host instanceof ResultSet)
              return host;

            final InternalResultSet resultSet = new InternalResultSet();
            if (host instanceof Iterable) {
              for (Object o : (Iterable) host)
                resultSet.add(extractResult(o));
            } else
              resultSet.add(extractResult(host));

            return resultSet;

          }

          final InternalResultSet resultSet = new InternalResultSet();

          Object value;
          if (result.isString())
            value = result.asString();
          else if (result.isBoolean())
            value = result.asBoolean();
          else if (result.isNumber()) {
            if (result.fitsInInt())
              value = result.asInt();
            else if (result.fitsInLong())
              value = result.asLong();
            else if (result.fitsInFloat())
              value = result.asFloat();
            else
              value = result.asFloat();
          } else if (result.isNull())
            value = null;
          else
            // UNKNOWN OR NOT SUPPORTED
            value = null;

          resultSet.add(new ResultInternal().setProperty("value", value));
          return resultSet;
        }

      }, timeout);

    } catch (CommandExecutionException e) {
      throw e;
    } catch (ExecutionException e) {
      // USE THE UNDERLYING CAUSE BYPASSING THE NOT RELEVANT EXECUTION EXCEPTION
      throw new CommandExecutionException("Error on executing user code", e.getCause());
    } catch (Exception e) {
      throw new CommandExecutionException("Error on executing user code", e);
    }
  }

  @Override
  public QueryEngine registerFunctions(final String function) {
    synchronized (polyglotEngine) {
      try {
        polyglotEngine.eval(function);
      } catch (CommandExecutionException e) {
        throw e;
      } catch (Exception e) {
        throw new CommandExecutionException("Error on executing user code", e);
      }
    }
    return this;
  }

  @Override
  public QueryEngine unregisterFunctions() {
    this.polyglotEngine = GraalPolyglotEngine.newBuilder(database, Engine.create()).setLanguage(language).setAllowedPackages(allowedPackages).build();
    return this;
  }

  @Override
  public AnalyzedQuery analyze(final String query) {
    try {
      executeUserCode(() -> {
        synchronized (polyglotEngine) {
          polyglotEngine.eval(query);
        }
        return null;
      }, timeout);
    } catch (CommandExecutionException e) {
      throw e;
    } catch (ExecutionException e) {
      // USE THE UNDERLYING CAUSE BYPASSING THE NOT RELEVANT EXECUTION EXCEPTION
      throw new CommandExecutionException("Error on executing user code", e.getCause());
    } catch (Exception e) {
      throw new CommandExecutionException("Error on analyzing user code", e);
    }

    return ANALYZED_QUERY;
  }

  @Override
  public ResultSet query(final String query, final Map<String, Object> parameters) {
    throw new UnsupportedOperationException("Execution of a query (idempotent) is not supported for polyglot engine. Use command instead");
  }

  @Override
  public ResultSet query(final String query, final Object... parameters) {
    throw new UnsupportedOperationException("Execution of a query (idempotent) is not supported for polyglot engine. Use command instead");
  }

  @Override
  public void close() {
    userCodeExecutor.shutdown();
    userCodeExecutorQueue.clear();
    polyglotEngine.close();
  }

  private ResultSet executeUserCode(final Callable task, final long executionTimeoutMs) throws Exception {
    // IF NOT INITIALIZED, EXECUTE AS SOON AS THE SERVICE STARTS
    final Future future = userCodeExecutor.submit(task);
    if (future == null)
      return null;

    try {
      Object result = executionTimeoutMs > 0 ? future.get(executionTimeoutMs, TimeUnit.MILLISECONDS) : future.get();
      if (result instanceof Exception)
        throw (Exception) result;

      return (ResultSet) result;

    } catch (TimeoutException e) {
      future.cancel(true); //this method will stop the running underlying task
      throw e;
    }
  }

  private ResultInternal extractResult(final Object o) {
    if (o instanceof Document)
      return new ResultInternal((Document) o);
    else if (o instanceof Identifiable)
      return new ResultInternal((Identifiable) o);
    else if (o instanceof Map)
      return new ResultInternal((Map) o);

    return new ResultInternal().setProperty("value", o);
  }

}
