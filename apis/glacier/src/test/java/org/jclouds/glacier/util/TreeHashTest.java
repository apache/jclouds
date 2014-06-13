/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.glacier.util;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.jclouds.io.ByteSources;
import org.jclouds.io.payloads.ByteSourcePayload;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.io.ByteSource;

@Test(groups = "unit", testName = "TreeHasherTest")
public class TreeHashTest {

   private static final int MB = 1024 * 1024;

   @Test
   public void testTreeHasherWith1MBPayload() throws IOException {
      TreeHash th = TreeHash.Hasher.buildTreeHashFromPayload(new ByteSourcePayload(getData(1 * MB)));
      assertEquals(th.getLinearHash(),
            HashCode.fromString("9bc1b2a288b26af7257a36277ae3816a7d4f16e89c1e7e77d0a5c48bad62b360"));
      assertEquals(th.getTreeHash(),
            HashCode.fromString("9bc1b2a288b26af7257a36277ae3816a7d4f16e89c1e7e77d0a5c48bad62b360"));
   }

   @Test
   public void testTreeHasherWith2MBPayload() throws IOException {
      TreeHash th = TreeHash.Hasher.buildTreeHashFromPayload(new ByteSourcePayload(getData(2 * MB)));
      assertEquals(th.getLinearHash(),
            HashCode.fromString("5256ec18f11624025905d057d6befb03d77b243511ac5f77ed5e0221ce6d84b5"));
      assertEquals(th.getTreeHash(),
            HashCode.fromString("560c2c9333c719cb00cfdffee3ba293db17f58743cdd1f7e4055373ae6300afa"));
   }

   @Test
   public void testTreeHasherWith3MBPayload() throws IOException {
      TreeHash th = TreeHash.Hasher.buildTreeHashFromPayload(new ByteSourcePayload(getData(3 * MB)));
      assertEquals(th.getLinearHash(),
            HashCode.fromString("6f850bc94ae6f7de14297c01616c36d712d22864497b28a63b81d776b035e656"));
      assertEquals(th.getTreeHash(),
            HashCode.fromString("70239f4f2ead7561f69d48b956b547edef52a1280a93c262c0b582190be7db17"));
   }

   @Test
   public void testTreeHasherWithMoreThan3MBPayload() throws IOException {
      TreeHash th = TreeHash.Hasher.buildTreeHashFromPayload(new ByteSourcePayload(getData(3 * MB + 512 * 1024)));
      assertEquals(th.getLinearHash(),
            HashCode.fromString("34c8bdd269f89a091cf17d5d23503940e0abf61c4b6544e42854b9af437f31bb"));
      assertEquals(th.getTreeHash(),
            HashCode.fromString("daede4eb580f914dacd5e0bdf7015c937fd615c1e6c6552d25cb04a8b7219828"));
   }

   @Test
   public void testBuildTreeHashFromMap() throws IOException {
      Map<Integer, HashCode> map = Maps.newTreeMap();
      map.put(2, HashCode.fromString("9bc1b2a288b26af7257a36277ae3816a7d4f16e89c1e7e77d0a5c48bad62b360"));
      map.put(1, HashCode.fromString("9bc1b2a288b26af7257a36277ae3816a7d4f16e89c1e7e77d0a5c48bad62b360"));
      HashCode treehash = TreeHash.Hasher.buildTreeHashFromMap(map);
      assertEquals(treehash, HashCode.fromString("560c2c9333c719cb00cfdffee3ba293db17f58743cdd1f7e4055373ae6300afa"));
   }

   private ByteSource getData(int size) {
      byte[] array = new byte[1024];
      Arrays.fill(array, (byte) 'a');
      return ByteSources.repeatingArrayByteSource(array).slice(0, size);
   }
}
