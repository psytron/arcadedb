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
package com.arcadedb.utility;

import com.arcadedb.database.Document;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.serializer.BinaryComparator;

import java.util.*;

public class CollectionUtils {

  public static int compare(final List l1, final List l2) {
    final int length = Math.min(l1.size(), l2.size());

    for (int j = 0; j < length; j++) {
      final int cmp = BinaryComparator.compareTo(l1.get(j), l2.get(j));
      if (cmp != 0)
        return cmp;
    }

    if (l1.size() > l2.size())
      return 1;
    else if (l1.size() < l2.size())
      return -1;
    return 0;
  }

  /**
   * Returns the count of the remaining items that have not been iterated yet.<br>
   * <b>NOTE: the default implementation consumes the iterator</b>.
   */
  public static long countEntries(final Iterator iterator) {
    long tot = 0;

    while (iterator.hasNext()) {
      iterator.next();
      tot++;
    }

    return tot;
  }

  public static List<Document> resultsetToListOfDocuments(final ResultSet resultset) {
    final List<Document> list = new ArrayList<>();
    while (resultset.hasNext())
      list.add(resultset.next().toElement());
    return list;
  }

  public static Document getFirstResultAsDocument(final ResultSet resultset) {
    while (resultset.hasNext())
      return resultset.next().toElement();
    return null;
  }

  public static Object getFirstResultValue(final ResultSet resultset, final String propertyName) {
    while (resultset.hasNext())
      return resultset.next().getProperty(propertyName);
    return null;
  }
}
