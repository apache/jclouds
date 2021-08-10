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
package org.jclouds.azure.storage.util.storageurl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.jclouds.ContextBuilder;
import org.jclouds.azure.storage.config.AuthType;
import org.jclouds.azureblob.config.AppendAccountToEndpointModule;
import org.jclouds.domain.Credentials;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.rest.internal.BaseRestApiTest;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class StorageAccountInVhostTest {

   private static final String ACCOUNT = "foo";

   @Test
   void testDefaultEndpointWhenNoneSupplied() {

      StorageAccountInVhost target = new StorageAccountInVhost(
            () -> null,
            () -> new Credentials(ACCOUNT, "creds"),
            AuthType.AZURE_KEY,
            null
      );
      
      assertEquals(target.get().toString(), "https://foo.blob.core.windows.net/");
   }

   @Test
   void testCustomEndpointWithoutTrailingSlash() {

      StorageAccountInVhost target = new StorageAccountInVhost(
            () -> URI.create("https://foo2.blob.core.windows.net/"),
            () -> new Credentials(ACCOUNT, "creds"),
            AuthType.AZURE_KEY,
            ""
      );

      assertEquals(target.get().toString(), "https://foo2.blob.core.windows.net/");
   }

   @Test
   void testCustomEndpointWithTrailingSlash() {

      StorageAccountInVhost target = new StorageAccountInVhost(
            () -> URI.create("https://foo2.blob.core.windows.net/"),
            () -> new Credentials(ACCOUNT, "creds"),
            AuthType.AZURE_KEY,
            ""
      );

      assertEquals(target.get().toString(), "https://foo2.blob.core.windows.net/");

   }

   @Test
   void testInsideContext() {
      String adjustedUri = ContextBuilder
            .newBuilder("azureblob")
            .endpoint("https://foo2.blob.core.windows.net")
            .credentials(ACCOUNT, "?creds")
            .modules(ImmutableSet.<Module> of(new BaseRestApiTest.MockModule(), new NullLoggingModule()))
            .buildInjector()
            .getInstance(StorageAccountInVhost.class)
            .get()
            .toString();
      Modules.override(new AppendAccountToEndpointModule());
      assertEquals(adjustedUri, "https://foo2.blob.core.windows.net/");

   }
}
