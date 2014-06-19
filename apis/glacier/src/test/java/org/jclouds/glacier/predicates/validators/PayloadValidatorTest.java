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

import static org.jclouds.glacier.util.TestUtils.GiB;
import static org.jclouds.glacier.util.TestUtils.buildPayload;

import org.jclouds.io.Payload;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "PayloadValidatorTest")
public class PayloadValidatorTest {

   private static final PayloadValidator VALIDATOR = new PayloadValidator();

   public void testValidate() {
      VALIDATOR.validate(buildPayload(10));
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testNoContentLength() {
      Payload payload = buildPayload(10);
      payload.getContentMetadata().setContentLength(null);
      VALIDATOR.validate(payload);
   }

   @Test(expectedExceptions = NullPointerException.class)
   public void testNullPayload() {
      VALIDATOR.validate(null);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testContentLengthTooBig() {
      VALIDATOR.validate(buildPayload(5 * GiB));
   }

}
