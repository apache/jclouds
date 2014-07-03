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

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "ContentRangeTest")
public class ContentRangeTest {
   @Test
   public void testContentRangeFromString() {
      ContentRange range = ContentRange.fromString("0-10");
      assertThat(range.getFrom()).isEqualTo(0);
      assertThat(range.getTo()).isEqualTo(10);

      range = ContentRange.fromString("1000-2000");
      assertThat(range.getFrom()).isEqualTo(1000);
      assertThat(range.getTo()).isEqualTo(2000);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromStringWithoutTo() {
      ContentRange.fromString("-10");
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromStringWithoutFrom() {
      ContentRange.fromString("10-");
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromStringWithEmptyString() {
      ContentRange.fromString("");
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromStringWithNullString() {
      ContentRange.fromString(null);
   }

   @Test
   public void testContentRangeFromPartNumber() {
      ContentRange range = ContentRange.fromPartNumber(0, 4096);
      assertThat(range.getFrom()).isEqualTo(0);
      assertThat(range.getTo()).isEqualTo((4096L << 20) - 1);

      range = ContentRange.fromPartNumber(1, 4096);
      assertThat(range.getFrom()).isEqualTo(4096L << 20);
      assertThat(range.getTo()).isEqualTo(2 * (4096L << 20) - 1);

      range = ContentRange.fromPartNumber(2, 4096);
      assertThat(range.getFrom()).isEqualTo(2 * (4096L << 20));
      assertThat(range.getTo()).isEqualTo(3 * (4096L << 20) - 1);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromPartNumberWithNegativePartNumber() {
      ContentRange.fromPartNumber(-1, 4096);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentRangeFromPartNumberWithZeroPartSize() {
      ContentRange.fromPartNumber(0, 0);
   }

   @Test
   public void testBuildContentRange() {
      ContentRange range = ContentRange.build(0, 4096);
      assertThat(range.getFrom()).isEqualTo(0);
      assertThat(range.getTo()).isEqualTo(4096);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuildContentRangeWithTransposedValues() {
      ContentRange.build(50, 10);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuildContentRangeWithNegatives() {
      ContentRange.build(-100, -50);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testBuildContentRangeWithZeroTo() {
      ContentRange.build(0, 0);
   }
}
