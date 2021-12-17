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
package org.jclouds.glacier.predicates.validators;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "PartSizeValidatorTest")
public class PartSizeValidatorTest {
   private static final PartSizeValidator VALIDATOR = new PartSizeValidator();

   public void testValidate() {
      VALIDATOR.validate(1L);
      VALIDATOR.validate(2L);
      VALIDATOR.validate(4L);
      VALIDATOR.validate(32L);
      VALIDATOR.validate(4096L);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testZero() {
      VALIDATOR.validate(0L);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testTooBig() {
      VALIDATOR.validate(8192L);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testNotPowerOfTwo() {
      VALIDATOR.validate(25L);
   }
}
