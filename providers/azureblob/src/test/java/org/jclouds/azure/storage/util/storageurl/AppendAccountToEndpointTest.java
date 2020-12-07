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
import org.jclouds.ContextBuilder;
import org.jclouds.azureblob.config.AppendAccountToEndpointModule;
import org.jclouds.domain.Credentials;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.rest.internal.BaseRestApiTest;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class AppendAccountToEndpointTest {
   
   private static final String ACCOUNT = "foo";
   
   @Test(expectedExceptions = NullPointerException.class)
   void testThrowsErrorWhenNoEndpointSupplied() {
      
      AppendAccountToEndpoint target = new AppendAccountToEndpoint(
            () -> null, 
            () -> new Credentials(ACCOUNT, "creds")
      );
      target.get();
   }
   
   @Test
   void testCustomEndpointWithoutTrailingSlash() {

      AppendAccountToEndpoint target = new AppendAccountToEndpoint(
            () -> URI.create("http://localhost:10000"),
            () -> new Credentials(ACCOUNT, "creds")
      );
      
      assertEquals(target.get().toString(), "http://localhost:10000/foo/");
   }

   @Test
   void testCustomEndpointWithTrailingSlash() {

      AppendAccountToEndpoint target = new AppendAccountToEndpoint(
            () -> URI.create("http://localhost:10000/"),
            () -> new Credentials(ACCOUNT, "creds")
      );

      assertEquals(target.get().toString(), "http://localhost:10000/foo/");

   }
   
   @Test
   void testInsideContext() {
      String adjustedUri = ContextBuilder
            .newBuilder("azureblob")
            .endpoint("http://localhost:10000")
            .credentials(ACCOUNT, "?creds")
            .modules(ImmutableSet.<Module> of(new BaseRestApiTest.MockModule(), new NullLoggingModule(), new AppendAccountToEndpointModule()))
            .buildInjector()
            .getInstance(AppendAccountToEndpoint.class)
            .get()
            .toString();

      assertEquals(adjustedUri, "http://localhost:10000/foo/");

   }
   

}
