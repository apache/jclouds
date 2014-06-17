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

import static com.google.common.base.Charsets.UTF_8;
import static org.jclouds.glacier.util.TestUtils.buildData;

import java.io.IOException;

import org.testng.annotations.Test;

@Test(groups = "unit", testName = "DescriptionValidatorTest")
public class DescriptionValidatorTest {

   private static final DescriptionValidator VALIDATOR = new DescriptionValidator();

   public void testValidate() throws IOException {
      VALIDATOR.validate("This is a valid description");
      VALIDATOR.validate("This_is*A#valid@Description");
      VALIDATOR.validate("This~Is~A~Valid~Description");
      VALIDATOR.validate("&Valid$Description");
      VALIDATOR.validate("");
      VALIDATOR.validate(buildData(1024).asCharSource(UTF_8).read());
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testIllegalCharacter() {
      VALIDATOR.validate(Character.toString((char) 31));
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testIllegalCharacter2() {
      VALIDATOR.validate(Character.toString((char) 127));
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testDescriptionTooLong() throws IOException {
      VALIDATOR.validate(buildData(1025).asCharSource(UTF_8).read());
   }
}
