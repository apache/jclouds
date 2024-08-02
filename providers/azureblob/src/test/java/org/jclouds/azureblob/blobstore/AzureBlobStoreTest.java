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
package org.jclouds.azureblob.blobstore;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import java.util.regex.Pattern;
/**
 * Tests behavior of {@code AzureBlobStore}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "AzureBlobStore")
public class AzureBlobStoreTest {
    
    private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-zA-Z0-9+/=]*$");

    public void testMakeBlockId() {
       // how can i achieve something like a junit5 parametrized test in testng?
       checkBlockIdForPartNumber(0);
       checkBlockIdForPartNumber(1);
       checkBlockIdForPartNumber(248);
       checkBlockIdForPartNumber(504);
       checkBlockIdForPartNumber(760);
       checkBlockIdForPartNumber(1016);
       checkBlockIdForPartNumber(1272);
       checkBlockIdForPartNumber(4600);
       checkBlockIdForPartNumber(6654);
       checkBlockIdForPartNumber(867840);
       checkBlockIdForPartNumber(868091);
       checkBlockIdForPartNumber(868096);
       checkBlockIdForPartNumber(-1);
       checkBlockIdForPartNumber(-1023);
   }
   
   private void checkBlockIdForPartNumber(int partNumber) {
       String blockId = AzureBlobStore.makeBlockId(partNumber);
       assertTrue(VALIDATION_PATTERN.matcher(blockId).find());
   }
}
