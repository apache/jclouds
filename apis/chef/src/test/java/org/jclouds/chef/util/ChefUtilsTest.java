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
package org.jclouds.chef.util;

import static org.testng.Assert.assertEquals;

import java.util.Date;
import java.util.NoSuchElementException;

import org.jclouds.domain.JsonBall;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests behavior of {@code ChefUtils}
 */
@Test(groups = { "unit" }, singleThreaded = true)
public class ChefUtilsTest {
   public static long millis = 1280251180727L;
   public static String millisString = "1280251180727";
   public static Date now = new Date(1280251180727L);

   public void testToOhaiTime() {
      assertEquals(ChefUtils.toOhaiTime(millis).toString(), millisString);
   }

   public void testFromOhaiTime() {
      assertEquals(ChefUtils.fromOhaiTime(new JsonBall(millisString)), now);

   }

   @Test(expectedExceptions = NoSuchElementException.class)
   public void testFindRoleInRunListThrowsNoSuchElementOnRecipe() {
      ChefUtils.findRoleInRunList(ImmutableList.of("recipe[java]"));
   }

   public void testFindRoleInRunList() {
      assertEquals(ChefUtils.findRoleInRunList(ImmutableList.of("role[prod]")), "prod");

   }

}
