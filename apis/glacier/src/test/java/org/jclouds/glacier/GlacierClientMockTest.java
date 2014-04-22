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
package org.jclouds.glacier;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static org.jclouds.Constants.PROPERTY_MAX_RETRIES;
import static org.jclouds.Constants.PROPERTY_SO_TIMEOUT;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.concurrent.config.ExecutorServiceModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;

public class GlacierClientMockTest {

   private static final String VAULT_NAME = "ConcreteVaultName";

   private static final Set<Module> modules = ImmutableSet.<Module> of(new ExecutorServiceModule(sameThreadExecutor(),
         sameThreadExecutor()));

   static GlacierClient getGlacierClient(URL server) {
      Properties overrides = new Properties();
      // prevent expect-100 bug http://code.google.com/p/mockwebserver/issues/detail?id=6
      overrides.setProperty(PROPERTY_SO_TIMEOUT, "0");
      overrides.setProperty(PROPERTY_MAX_RETRIES, "1");
      return ContextBuilder.newBuilder("glacier").credentials("accessKey", "secretKey").endpoint(server.toString())
            .modules(modules).overrides(overrides).buildApi(GlacierClient.class);
   }

   public void testCreateVault() throws IOException, InterruptedException {
      // Prepare the response
      MockResponse mr = new MockResponse();
      mr.setResponseCode(201);
      mr.addHeader("x-amzn-RequestId", "AAABZpJrTyioDC_HsOmHae8EZp_uBSJr6cnGOLKp_XJCl-Q");
      mr.addHeader("Date", "Sun, 25 Mar 2012 12:02:00 GMT");
      mr.addHeader("Location", "/111122223333/vaults/" + VAULT_NAME);
      MockWebServer server = new MockWebServer();
      server.enqueue(mr);
      server.play();

      // Send the request and check the response
      try {
         GlacierClient client = getGlacierClient(server.getUrl("/"));
         URI responseUri = client.createVault(VAULT_NAME);
         assertEquals(responseUri.toString(), server.getUrl("/") + "111122223333/vaults/" + VAULT_NAME);
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getRequestLine(), "PUT /-/vaults/" + VAULT_NAME + " HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }
}
