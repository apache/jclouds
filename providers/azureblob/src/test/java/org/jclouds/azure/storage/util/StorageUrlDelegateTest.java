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
package org.jclouds.azure.storage.util;

import static org.testng.Assert.assertEquals;

import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.azure.storage.reference.AzureConstants;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.rest.internal.BaseRestApiTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

@Test(groups = "unit")
public class StorageUrlDelegateTest {

   private static final String ACCOUNT = "foo";
   private static final ImmutableSet<Module> MODULES = ImmutableSet.<Module> of(new BaseRestApiTest.MockModule(),
         new NullLoggingModule());

   @Test
   void testDefaultEndpoint() {

      StorageUrlDelegate target = ContextBuilder
            .newBuilder("azureblob")
            .credentials(ACCOUNT, "?token")
            .modules(MODULES)
            .buildInjector().getInstance(StorageUrlDelegate.class);

      assertEquals(target.configureStorageUrl(), "https://foo.blob.core.windows.net/");
   }

   @Test
   void testCustomEndpointWithoutStorageAccountPath() {

      StorageUrlDelegate target = ContextBuilder
            .newBuilder("azureblob").endpoint("http://localhost:10000")
            .credentials(ACCOUNT, "?token")
            .modules(MODULES)
            .buildInjector()
            .getInstance(StorageUrlDelegate.class);

      assertEquals(target.configureStorageUrl(), "http://localhost:10000/");
   }

   @Test
   void testCustomEndpointWithStorageAccountPath() {

      Properties properties = new Properties();
      properties.put(AzureConstants.PROPERTY_AZURE_VIRTUAL_HOST_STORAGE_ACCOUNT, false);

      StorageUrlDelegate target = ContextBuilder
            .newBuilder("azureblob")
            .endpoint("http://localhost:10000")
            .overrides(properties)
            .credentials(ACCOUNT, "?token")
            .modules(MODULES)
            .buildInjector()
            .getInstance(StorageUrlDelegate.class);

      assertEquals(target.configureStorageUrl(), "http://localhost:10000/foo/");

   }

}
