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

import com.arcadedb.TestHelper;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.engine.Bucket;
import com.arcadedb.graph.Vertex;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 */
public class UpdateStatementExecutionTest extends TestHelper {
  private String className;

  public UpdateStatementExecutionTest() {
    autoStartTx = true;
  }

  @Override
  public void beginTest() {
    final DocumentType clazz = database.getSchema().createDocumentType("OUpdateStatementExecutionTest");
    className = clazz.getName();

    for (int i = 0; i < 10; i++) {
      final MutableDocument doc = database.newDocument(className);
      doc.set("name", "name" + i);
      doc.set("surname", "surname" + i);
      doc.set("number", 4L);

      final List<String> tagsList = new ArrayList<>();
      tagsList.add("foo");
      tagsList.add("bar");
      tagsList.add("baz");
      doc.set("tagsList", tagsList);

      final Map<String, String> tagsMap = new HashMap<>();
      tagsMap.put("foo", "foo");
      tagsMap.put("bar", "bar");
      tagsMap.put("baz", "baz");
      doc.set("tagsMap", tagsMap);

      doc.save();
    }
  }

  @Test
  public void testSetString() {
    ResultSet result = database.command("sql", "update " + className + " set surname = 'foo'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));

    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals("foo", item.getProperty("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testCopyField() {
    ResultSet result = database.command("sql", "update " + className + " set surname = name");

    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals((Object) item.getProperty("name"), item.getProperty("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testSetExpression() {
    ResultSet result = database.command("sql", "update " + className + " set surname = 'foo'+name ");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals("foo" + item.getProperty("name"), item.getProperty("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testConditionalSet() {
    ResultSet result = database.command("sql", "update " + className + " set surname = 'foo' where name = 'name3'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    boolean found = false;
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      if ("name3".equals(item.getProperty("name"))) {
        Assertions.assertEquals("foo", item.getProperty("surname"));
        found = true;
      }
    }
    Assertions.assertTrue(found);
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testSetOnList() {
    ResultSet result = database.command("sql", "update " + className + " set tagsList[0] = 'abc' where name = 'name3'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    boolean found = false;
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      if ("name3".equals(item.getProperty("name"))) {
        final List<String> tags = new ArrayList<>();
        tags.add("abc");
        tags.add("bar");
        tags.add("baz");
        Assertions.assertEquals(tags, item.getProperty("tagsList"));
        found = true;
      }
    }
    Assertions.assertTrue(found);
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testSetOnList2() {
    ResultSet result = database.command("sql", "update " + className + " set tagsList[6] = 'abc' where name = 'name3'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    boolean found = false;
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      if ("name3".equals(item.getProperty("name"))) {
        final List<String> tags = new ArrayList<>();
        tags.add("foo");
        tags.add("bar");
        tags.add("baz");
        tags.add(null);
        tags.add(null);
        tags.add(null);
        tags.add("abc");
        Assertions.assertEquals(tags, item.getProperty("tagsList"));
        found = true;
      }
    }
    Assertions.assertTrue(found);
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testSetOnMap() {
    ResultSet result = database.command("sql", "update " + className + " set tagsMap['foo'] = 'abc' where name = 'name3'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    boolean found = false;
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      if ("name3".equals(item.getProperty("name"))) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("foo", "abc");
        tags.put("bar", "bar");
        tags.put("baz", "baz");
        Assertions.assertEquals(tags, item.getProperty("tagsMap"));
        found = true;
      } else {
        final Map<String, String> tags = new HashMap<>();
        tags.put("foo", "foo");
        tags.put("bar", "bar");
        tags.put("baz", "baz");
        Assertions.assertEquals(tags, item.getProperty("tagsMap"));
      }
    }
    Assertions.assertTrue(found);
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testPlusAssign() {
    ResultSet result = database.command("sql", "update " + className + " set name += 'foo', newField += 'bar', number += 5");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertTrue(item.getProperty("name").toString().endsWith("foo")); // test concatenate string to string
      Assertions.assertEquals(8, item.getProperty("name").toString().length());
      Assertions.assertEquals("bar", item.getProperty("newField")); // test concatenate null to string
      Assertions.assertEquals((Object) 9L, item.getProperty("number")); // test sum numbers
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testMinusAssign() {
    ResultSet result = database.command("sql", "update " + className + " set number -= 5");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals((Object) (-1L), item.getProperty("number"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testStarAssign() {
    ResultSet result = database.command("sql", "update " + className + " set number *= 5");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals((Object) 20L, item.getProperty("number"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testSlashAssign() {
    ResultSet result = database.command("sql", "update " + className + " set number /= 2");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals((Object) 2L, item.getProperty("number"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemove() {
    ResultSet result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      final Result item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertNotNull(item.getProperty("surname"));
    }

    result.close();
    result = database.command("sql", "update " + className + " remove surname");
    for (int i = 0; i < 1; i++) {
      Assertions.assertTrue(result.hasNext());
      final Result item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      final Result item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertNull(item.getProperty("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testContent() {

    ResultSet result = database.command("sql", "update " + className + " content {'name': 'foo', 'secondName': 'bar'}");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals("foo", item.getProperty("name"));
      Assertions.assertEquals("bar", item.getProperty("secondName"));
      Assertions.assertNull(item.getProperty("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testMerge() {

    ResultSet result = database.command("sql", "update " + className + " merge {'name': 'foo', 'secondName': 'bar'}");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 10L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals("foo", item.getProperty("name"));
      Assertions.assertEquals("bar", item.getProperty("secondName"));
      Assertions.assertTrue(item.getProperty("surname").toString().startsWith("surname"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testUpsertBucket() {
    final List<Bucket> buckets = database.getSchema().getType(className).getBuckets(false);

    // BY BUCKET ID
    ResultSet result = database.command("sql", "update bucket:" + buckets.get(0).getName() + " set foo = 'bar' upsert where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    // BY BUCKET ID
    result = database.command("sql", "update bucket:" + buckets.get(0).getId() + " set foo = 'bar' upsert where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from bucket:" + buckets.get(0).getName());
    Assertions.assertTrue(result.hasNext());

    while (result.hasNext()) {
      item = result.next();
      Assertions.assertNotNull(item);
      final String name = item.getProperty("name");
      Assertions.assertNotNull(name);
      if ("name1".equals(name)) {
        Assertions.assertEquals("bar", item.getProperty("foo"));
      } else {
        Assertions.assertNull(item.getProperty("foo"));
      }
    }
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.command("sql", "update bucket:" + buckets.get(0).getName() + " remove foo upsert where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from bucket:" + buckets.get(0).getName());
    Assertions.assertTrue(result.hasNext());

    while (result.hasNext()) {
      item = result.next();
      Assertions.assertNotNull(item);
      final String name = item.getProperty("name");
      Assertions.assertNotNull(name);
      Assertions.assertNull(item.getProperty("foo"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testUpsert1() {
    ResultSet result = database.command("sql", "update " + className + " set foo = 'bar' upsert where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      final String name = item.getProperty("name");
      Assertions.assertNotNull(name);
      if ("name1".equals(name)) {
        Assertions.assertEquals("bar", item.getProperty("foo"));
      } else {
        Assertions.assertNull(item.getProperty("foo"));
      }
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  /**
   * Issue https://github.com/ArcadeData/arcadedb/issues/380
   */
  @Test
  public void testUpsertVertices() {
    database.command("sql", "CREATE vertex TYPE extra_node");
    database.command("sql", "CREATE PROPERTY extra_node.extraitem STRING");
    database.command("sql", "CREATE INDEX ON extra_node (extraitem) UNIQUE");
    final ResultSet result = database.command("sql", "update extra_node set extraitem = 'Hugo2' upsert return after $current where extraitem = 'Hugo'");

    Assertions.assertTrue(result.hasNext());
    final Result item = result.next();
    Assertions.assertNotNull(item);
    final Vertex current = item.getProperty("$current");
    Assertions.assertEquals("Hugo2", current.getString("extraitem"));
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testUpsertAndReturn() {
    final ResultSet result = database.command("sql", "update " + className + " set foo = 'bar' upsert  return after  where name = 'name1' ");

    Assertions.assertTrue(result.hasNext());
    final Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals("bar", item.getProperty("foo"));
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testUpsert2() {

    ResultSet result = database.command("sql", "update " + className + " set foo = 'bar' upsert where name = 'name11'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    for (int i = 0; i < 11; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      final String name = item.getProperty("name");
      Assertions.assertNotNull(name);
      if ("name11".equals(name)) {
        Assertions.assertEquals("bar", item.getProperty("foo"));
      } else {
        Assertions.assertNull(item.getProperty("foo"));
      }
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemove1() {
    final String className = "overridden" + this.className;

    final DocumentType clazz = database.getSchema().createDocumentType(className);
    clazz.createProperty("theProperty", Type.LIST);

    final MutableDocument doc = database.newDocument(className);
    final List theList = new ArrayList();
    for (int i = 0; i < 10; i++) {
      theList.add("n" + i);
    }
    doc.set("theProperty", theList);

    doc.save();

    ResultSet result = database.command("sql", "update " + className + " remove theProperty[0]");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    Assertions.assertTrue(result.hasNext());
    item = result.next();
    Assertions.assertNotNull(item);
    final List ls = item.getProperty("theProperty");
    Assertions.assertNotNull(ls);
    Assertions.assertEquals(9, ls.size());
    Assertions.assertFalse(ls.contains("n0"));
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemove2() {
    final String className = "overridden" + this.className;
    final DocumentType clazz = database.getSchema().createDocumentType(className);
    clazz.createProperty("theProperty", Type.LIST);

    final MutableDocument doc = database.newDocument(className);
    final List theList = new ArrayList();
    for (int i = 0; i < 10; i++) {
      theList.add("n" + i);
    }
    doc.set("theProperty", theList);

    doc.save();

    ResultSet result = database.command("sql", "update " + className + " remove theProperty[0, 1, 3]");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    Assertions.assertTrue(result.hasNext());
    item = result.next();
    Assertions.assertNotNull(item);
    final List ls = item.getProperty("theProperty");

    Assertions.assertNotNull(ls);
    Assertions.assertEquals(ls.size(), 7);
    Assertions.assertFalse(ls.contains("n0"));
    Assertions.assertFalse(ls.contains("n1"));
    Assertions.assertTrue(ls.contains("n2"));
    Assertions.assertFalse(ls.contains("n3"));
    Assertions.assertTrue(ls.contains("n4"));

    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemove3() {
    final String className = "overridden" + this.className;
    final DocumentType clazz = database.getSchema().createDocumentType(className);
    clazz.createProperty("theProperty", Type.EMBEDDED);

    final MutableDocument doc = database.newDocument(className);
    final MutableDocument emb = database.newDocument(className);
    emb.set("sub", "foo");
    emb.set("aaa", "bar");
    doc.set("theProperty", emb);

    doc.save();

    ResultSet result = database.command("sql", "update " + className + " remove theProperty.sub");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from " + className);
    Assertions.assertTrue(result.hasNext());
    item = result.next();
    Assertions.assertNotNull(item);
    final ResultInternal ls = item.getProperty("theProperty");
    Assertions.assertNotNull(ls);
    Assertions.assertFalse(ls.getPropertyNames().contains("sub"));
    Assertions.assertEquals("bar", ls.getProperty("aaa"));
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemoveFromMapSquare() {

    database.command("sql", "UPDATE " + className + " REMOVE tagsMap[\"bar\"]").close();

    final ResultSet result = database.query("sql", "SELECT tagsMap FROM " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      final Result item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals(2, ((Map) item.getProperty("tagsMap")).size());
      Assertions.assertFalse(((Map) item.getProperty("tagsMap")).containsKey("bar"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testRemoveFromMapEquals() {

    database.command("sql", "UPDATE " + className + " REMOVE tagsMap = \"bar\"").close();

    final ResultSet result = database.query("sql", "SELECT tagsMap FROM " + className);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      final Result item = result.next();
      Assertions.assertNotNull(item);
      Assertions.assertEquals(2, ((Map) item.getProperty("tagsMap")).size());
      Assertions.assertFalse(((Map) item.getProperty("tagsMap")).containsKey("bar"));
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void testReturnBefore() {
    final ResultSet result = database.command("sql", "update " + className + " set name = 'foo' RETURN BEFORE where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    final Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals("name1", item.getProperty("name"));

    Assertions.assertFalse(result.hasNext());
    result.close();
  }

  @Test
  public void upsertVertices() {
    database.getSchema().createVertexType("UpsertableVertex");

    for (int i = 0; i < 10; i++) {
      final MutableDocument doc = database.newVertex("UpsertableVertex");
      doc.set("name", "name" + i);
      doc.set("surname", "surname" + i);
      doc.set("number", 4L);

      final List<String> tagsList = new ArrayList<>();
      tagsList.add("foo");
      tagsList.add("bar");
      tagsList.add("baz");
      doc.set("tagsList", tagsList);

      final Map<String, String> tagsMap = new HashMap<>();
      tagsMap.put("foo", "foo");
      tagsMap.put("bar", "bar");
      tagsMap.put("baz", "baz");
      doc.set("tagsMap", tagsMap);

      doc.save();
    }

    ResultSet result = database.command("sql", "update UpsertableVertex set foo = 'bar' upsert where name = 'name1'");
    Assertions.assertTrue(result.hasNext());
    Result item = result.next();
    Assertions.assertNotNull(item);
    Assertions.assertEquals((Object) 1L, item.getProperty("count"));
    Assertions.assertFalse(result.hasNext());
    result.close();

    result = database.query("sql", "SElect from UpsertableVertex");
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(result.hasNext());
      item = result.next();
      Assertions.assertNotNull(item);
      final String name = item.getProperty("name");
      Assertions.assertNotNull(name);
      if ("name1".equals(name)) {
        Assertions.assertEquals("bar", item.getProperty("foo"));
      } else {
        Assertions.assertNull(item.getProperty("foo"));
      }
    }
    Assertions.assertFalse(result.hasNext());
    result.close();
  }
}
