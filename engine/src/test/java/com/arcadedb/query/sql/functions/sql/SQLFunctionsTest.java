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
package com.arcadedb.query.sql.functions.sql;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.SQLQueryEngine;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.query.sql.function.SQLFunctionAbstract;
import com.arcadedb.query.sql.function.text.SQLMethodHash;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import com.arcadedb.utility.CollectionUtils;
import com.arcadedb.utility.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;

import static com.arcadedb.TestHelper.checkActiveDatabases;

public class SQLFunctionsTest {
  private final DatabaseFactory factory = new DatabaseFactory("./target/databases/SQLFunctionsTest");
  private       Database        database;

  @Test
  public void queryMax() {
    final ResultSet result = database.command("sql", "select max(id) as max from Account");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("max"));
    }
  }

  @Test
  public void queryMaxInline() {
    final ResultSet result = database.command("sql", "select max(1,2,7,0,-2,3) as max");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("max"));

      Assertions.assertEquals(((Number) d.getProperty("max")).intValue(), 7);
    }
  }

  @Test
  public void queryMin() {
    final ResultSet result = database.command("sql", "select min(id) as min from Account");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("min"));
      Assertions.assertEquals(((Number) d.getProperty("min")).longValue(), 0l);
    }
  }

  @Test
  public void queryMinInline() {
    final ResultSet result = database.command("sql", "select min(1,2,7,0,-2,3) as min");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("min"));
      Assertions.assertEquals(((Number) d.getProperty("min")).intValue(), -2);
    }
  }

  @Test
  public void querySum() {
    final ResultSet result = database.command("sql", "select sum(id) as sum from Account");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("sum"));
    }
  }

  @Test
  public void queryCount() {
    final ResultSet result = database.command("sql", "select count(*) as total from Account");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("total"));
      Assertions.assertTrue(((Number) d.getProperty("total")).longValue() > 0);
    }
  }

  @Test
  public void queryCountWithConditions() {
    final DocumentType indexed = database.getSchema().getOrCreateDocumentType("Indexed");
    indexed.createProperty("key", Type.STRING);

    database.transaction(() -> {
      indexed.createTypeIndex(Schema.INDEX_TYPE.LSM_TREE, false, "key");

      database.newDocument("Indexed").set("key", "one").save();
      database.newDocument("Indexed").set("key", "two").save();

      final ResultSet result = database.command("sql", "select count(*) as total from Indexed where key > 'one'");

      Assertions.assertTrue(result.hasNext());
      for (final ResultSet it = result; it.hasNext(); ) {
        final Result d = it.next();
        Assertions.assertNotNull(d.getProperty("total"));
        Assertions.assertTrue(((Number) d.getProperty("total")).longValue() > 0);
      }
    });
  }

  @Test
  public void queryDistinct() {
    final ResultSet result = database.command("sql", "select distinct(name) as name from City");

    Assertions.assertTrue(result.hasNext());

    final Set<String> cities = new HashSet<>();
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result city = it.next();
      final String cityName = city.getProperty("name");
      Assertions.assertFalse(cities.contains(cityName));
      cities.add(cityName);
    }
  }

  @Test
  public void queryFunctionRenamed() {
    final ResultSet result = database.command("sql", "select distinct(name) from City");

    Assertions.assertTrue(result.hasNext());

    for (final ResultSet it = result; it.hasNext(); ) {
      final Result city = it.next();
      Assertions.assertTrue(city.hasProperty("name"));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void queryUnionAllAsAggregationNotRemoveDuplicates() {
    ResultSet result = database.command("sql", "select from City");
    final int count = (int) CollectionUtils.countEntries(result);

    result = database.command("sql", "select unionAll(name) as name from City");
    Assertions.assertTrue(result.hasNext());
    final Collection<Object> citiesFound = result.next().getProperty("name");
    Assertions.assertEquals(citiesFound.size(), count);
  }

  @Test
  public void querySetNotDuplicates() {
    final ResultSet result = database.command("sql", "select set(name) as name from City");

    Assertions.assertTrue(result.hasNext());

    final Collection<Object> citiesFound = result.next().getProperty("name");
    Assertions.assertTrue(citiesFound.size() > 1);

    final Set<String> cities = new HashSet<String>();
    for (final Object city : citiesFound) {
      Assertions.assertFalse(cities.contains(city.toString()));
      cities.add(city.toString());
    }
  }

  @Test
  public void queryList() {
    final ResultSet result = database.command("sql", "select list(name) as names from City");

    Assertions.assertTrue(result.hasNext());

    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      final List<Object> citiesFound = d.getProperty("names");
      Assertions.assertTrue(citiesFound.size() > 1);
    }
  }

  @Test
  public void testSelectMap() {
    final ResultSet result = database.query("sql", "select list( 1, 4, 5.00, 'john', map( 'kAA', 'vAA' ) ) as myresult");

    Assertions.assertTrue(result.hasNext());

    final Result document = result.next();
    final List myresult = document.getProperty("myresult");
    Assertions.assertNotNull(myresult);

    Assertions.assertTrue(myresult.remove(Integer.valueOf(1)));
    Assertions.assertTrue(myresult.remove(Integer.valueOf(4)));
    Assertions.assertTrue(myresult.remove(Float.valueOf(5)));
    Assertions.assertTrue(myresult.remove("john"));

    Assertions.assertEquals(myresult.size(), 1);

    Assertions.assertTrue(myresult.get(0) instanceof Map, "The object is: " + myresult.getClass());
    final Map map = (Map) myresult.get(0);

    final String value = (String) map.get("kAA");
    Assertions.assertEquals(value, "vAA");

    Assertions.assertEquals(map.size(), 1);
  }

  @Test
  public void querySet() {
    final ResultSet result = database.command("sql", "select set(name) as names from City");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      final Set<Object> citiesFound = d.getProperty("names");
      Assertions.assertTrue(citiesFound.size() > 1);
    }
  }

  @Test
  public void queryMap() {
    final ResultSet result = database.command("sql", "select map(name, country.name) as names from City");

    Assertions.assertTrue(result.hasNext());

    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      final Map<Object, Object> citiesFound = d.getProperty("names");
      Assertions.assertEquals(1, citiesFound.size());
    }
  }

  @Test
  public void queryUnionAllAsInline() {
    final ResultSet result = database.command("sql", "select unionAll(name, country) as edges from City");

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertEquals(1, d.getPropertyNames().size());
      Assertions.assertTrue(d.hasProperty("edges"));
    }
  }

  @Test
  public void queryComposedAggregates() {
    final ResultSet result = database.command("sql", "select MIN(id) as min, max(id) as max, AVG(id) as average, sum(id) as total from Account");

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("min"));
      Assertions.assertNotNull(d.getProperty("max"));
      Assertions.assertNotNull(d.getProperty("average"));
      Assertions.assertNotNull(d.getProperty("total"));

      Assertions.assertTrue(((Number) d.getProperty("max")).longValue() > ((Number) d.getProperty("average")).longValue());
      Assertions.assertTrue(((Number) d.getProperty("average")).longValue() >= ((Number) d.getProperty("min")).longValue());
      Assertions.assertTrue(((Number) d.getProperty("total")).longValue() >= ((Number) d.getProperty("max")).longValue(),
          "Total " + d.getProperty("total") + " max " + d.getProperty("max"));
    }
  }

  @Test
  public void queryFormat() {
    final ResultSet result = database.command("sql", "select format('%d - %s (%s)', nr, street, type, dummy ) as output from Account");
    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("output"));
    }
  }

  @Test
  public void querySysdateNoFormat() {
    final ResultSet result = database.command("sql", "select sysdate() as date from Account");

    Assertions.assertTrue(result.hasNext());
    Object lastDate = null;
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("date"));

      if (lastDate != null)
        d.getProperty("date").equals(lastDate);

      lastDate = d.getProperty("date");
    }
  }

  @Test
  public void querySysdateWithFormat() {
    ResultSet result = database.command("sql", "select sysdate('dd-MM-yyyy') as date from Account");

    Assertions.assertTrue(result.hasNext());
    Object lastDate = null;
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();

      final String date = d.getProperty("date");

      Assertions.assertNotNull(date);
      Assertions.assertEquals(10, date.length());

      if (lastDate != null)
        d.getProperty("date").equals(lastDate);

      lastDate = d.getProperty("date");
    }

    result = database.command("sql", "select sysdate('yyyy-MM-dd HH:mm:ss') as date");

    Assertions.assertTrue(result.hasNext());
    lastDate = null;
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();

      final String date = d.getProperty("date");

      Assertions.assertNotNull(date);
      Assertions.assertEquals(19, date.length());

      if (lastDate != null)
        d.getProperty("date").equals(lastDate);

      lastDate = d.getProperty("date");
    }

    result = database.command("sql", "select sysdate('yyyy-MM-dd HH:mm:ss', 'GMT-5') as date");

    Assertions.assertTrue(result.hasNext());
    lastDate = null;
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();

      final String date = d.getProperty("date");

      Assertions.assertNotNull(date);
      Assertions.assertEquals(19, date.length());

      if (lastDate != null)
        d.getProperty("date").equals(lastDate);

      lastDate = d.getProperty("date");
    }
  }

  @Test
  public void queryDate() {
    ResultSet result = database.command("sql", "select count(*) as tot from Account");
    Assertions.assertTrue(result.hasNext());
    final int tot = ((Number) result.next().getProperty("tot")).intValue();

    database.transaction(() -> {
      final ResultSet result2 = database.command("sql", "update Account set created = date()");
      Assertions.assertEquals(tot, (Long) result2.next().getProperty("count"));
    });

    final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

    result = database.command("sql", "select from Account where created <= date('" + dateFormat.format(new Date()) + "', \"" + pattern + "\")");

    int count = 0;
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("created"));
      ++count;
    }
    Assertions.assertEquals(tot, count);
  }

  @Test
  public void queryUndefinedFunction() {
    try {
      database.command("sql", "select blaaaa(salary) as max from Account");
      Assertions.fail();
    } catch (final CommandExecutionException e) {
      // EXPECTED
    }
  }

  @Test
  public void queryCustomFunction() {
    ((SQLQueryEngine) database.getQueryEngine("sql")).getFunctionFactory().register(new SQLFunctionAbstract("bigger") {
      @Override
      public String getSyntax() {
        return "bigger(<first>, <second>)";
      }

      @Override
      public Object execute(final Object iThis, final Identifiable iCurrentRecord, final Object iCurrentResult, final Object[] iParams, final CommandContext iContext) {
        if (iParams[0] == null || iParams[1] == null)
          // CHECK BOTH EXPECTED PARAMETERS
          return null;

        if (!(iParams[0] instanceof Number) || !(iParams[1] instanceof Number))
          // EXCLUDE IT FROM THE RESULT SET
          return null;

        // USE DOUBLE TO AVOID LOSS OF PRECISION
        final double v1 = ((Number) iParams[0]).doubleValue();
        final double v2 = ((Number) iParams[1]).doubleValue();

        return Math.max(v1, v2);
      }
    });

    final ResultSet result = database.command("sql", "select from Account where bigger(id,1000) = 1000");

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertTrue((Integer) d.getProperty("id") <= 1000);
    }

    ((SQLQueryEngine) database.getQueryEngine("sql")).getFunctionFactory().unregister("bigger");
  }

  @Test
  public void queryAsLong() {
    final long moreThanInteger = 1 + (long) Integer.MAX_VALUE;
    final String sql = "select numberString.asLong() as value from ( select '" + moreThanInteger + "' as numberString from Account ) limit 1";
    final ResultSet result = database.command("sql", sql);

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("value"));
      Assertions.assertTrue(d.getProperty("value") instanceof Long);
      Assertions.assertEquals(moreThanInteger, (Long) d.getProperty("value"));
    }
  }

  @Test
  public void testHashMethod() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    final ResultSet result = database.command("sql", "select name, name.hash() as n256, name.hash('sha-512') as n512 from City");

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      final String name = d.getProperty("name");

      Assertions.assertEquals(SQLMethodHash.createHash(name, "SHA-256"), d.getProperty("n256"));
      Assertions.assertEquals(SQLMethodHash.createHash(name, "SHA-512"), d.getProperty("n512"));
    }
  }

  @Test
  public void testFirstFunction() {
    final List<Long> sequence = new ArrayList<>(100);
    for (long i = 0; i < 100; ++i) {
      sequence.add(i);
    }
    database.transaction(() -> {
      database.newDocument("V").set("sequence", sequence).save();
      sequence.remove(0);
      database.newDocument("V").set("sequence", sequence).save();
    });

    final ResultSet result = database.command("sql", "select first(sequence) as first from V where sequence is not null");

    Assertions.assertTrue(result.hasNext());
    Assertions.assertEquals(0, (Long) result.next().getProperty("first"));
    Assertions.assertTrue(result.hasNext());
    Assertions.assertEquals(1, (Long) result.next().getProperty("first"));
  }

  @Test
  public void testFirstAndLastFunctionsWithMultipleValues() {
    database.transaction(() -> {
      database.execute("sql",//
          "CREATE DOCUMENT TYPE mytype;\n" +//
              "INSERT INTO mytype SET value = 1;\n" +//
              "INSERT INTO mytype SET value = [1,2,3];\n" +//
              "INSERT INTO mytype SET value = [1];\n" +//
              "INSERT INTO mytype SET value = map(\"a\",1,\"b\",2);");
    });

    final ResultSet result = database.query("sql", "SELECT first(value) as first, last(value) as last FROM mytype");

    final Object[] array = result.stream().toArray();

    Assertions.assertEquals(4, array.length);
    for (final Object r : array) {
      ((Result) r).hasProperty("first");
      Assertions.assertNotNull(((Result) r).getProperty("first"));

      ((Result) r).hasProperty("last");
      Assertions.assertNotNull(((Result) r).getProperty("last"));
    }
  }

  @Test
  public void testLastFunction() {
    final List<Long> sequence = new ArrayList<Long>(100);
    for (long i = 0; i < 100; ++i) {
      sequence.add(i);
    }

    database.transaction(() -> {
      database.newDocument("V").set("sequence2", sequence).save();
      sequence.remove(sequence.size() - 1);
      database.newDocument("V").set("sequence2", sequence).save();
    });

    final ResultSet result = database.command("sql", "select last(sequence2) as last from V where sequence2 is not null");

    Assertions.assertTrue(result.hasNext());
    Assertions.assertEquals(99, (Long) result.next().getProperty("last"));
    Assertions.assertTrue(result.hasNext());
    Assertions.assertEquals(98, (Long) result.next().getProperty("last"));
  }

  @Test
  public void querySplit() {
    final String sql = "select v.split('-') as value from ( select '1-2-3' as v ) limit 1";

    final ResultSet result = database.command("sql", sql);

    Assertions.assertTrue(result.hasNext());
    for (final ResultSet it = result; it.hasNext(); ) {
      final Result d = it.next();
      Assertions.assertNotNull(d.getProperty("value"));
      Assertions.assertTrue(d.getProperty("value").getClass().isArray());

      final Object[] array = d.getProperty("value");

      Assertions.assertEquals(array.length, 3);
      Assertions.assertEquals(array[0], "1");
      Assertions.assertEquals(array[1], "2");
      Assertions.assertEquals(array[2], "3");
    }
  }

  @BeforeEach
  public void beforeEach() {
    checkActiveDatabases();
    FileUtils.deleteRecursively(new File("./target/databases/SQLFunctionsTest"));
    database = factory.create();
    database.getSchema().createDocumentType("V");
    database.getSchema().createDocumentType("Account");
    database.transaction(() -> {
      for (int i = 0; i < 100; i++) {
        database.newDocument("Account").set("id", i).save();
      }
    });

    database.getSchema().createDocumentType("City");
    database.getSchema().createDocumentType("Country");
    database.transaction(() -> {
      final MutableDocument italy = database.newDocument("Country").set("name", "Italy").save();
      final MutableDocument usa = database.newDocument("Country").set("name", "USA").save();

      database.newDocument("City").set("name", "Rome").set("country", italy).save();
      database.newDocument("City").set("name", "Grosseto").set("country", italy).save();
      database.newDocument("City").set("name", "Miami").set("country", usa).save();
      database.newDocument("City").set("name", "Austin").set("country", usa).save();
    });
  }

  @AfterEach
  public void afterEach() {
    if (database != null)
      database.drop();
    checkActiveDatabases();
    FileUtils.deleteRecursively(new File("./target/databases/SQLFunctionsTest"));
  }
}
