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
package org.jclouds.profitbricks.http;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.jclouds.http.HttpResponseException;
import org.jclouds.profitbricks.ProfitBricksApi;
import org.jclouds.profitbricks.domain.DataCenter;
import org.jclouds.profitbricks.domain.Location;
import org.jclouds.profitbricks.domain.Server;
import org.jclouds.profitbricks.features.DataCenterApi;
import org.jclouds.profitbricks.features.ServerApi;
import org.jclouds.profitbricks.internal.BaseProfitBricksMockTest;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.InsufficientResourcesException;
import org.jclouds.rest.ResourceNotFoundException;
import org.testng.annotations.Test;


/**
 * Mock tests for the {@link ResponseStatusFromPayloadHttpCommandExecutorService} class.
 */
@Test(groups = "unit", testName = "ResponseStatusFromPayloadHttpCommandExecutorServiceTest")
public class ResponseStatusFromPayloadHttpCommandExecutorServiceTest extends BaseProfitBricksMockTest {

   private static final int MAX_RETRIES = 5;

   @Test
   public void testNotFound() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setResponseCode(500).setBody(payloadFromResource("/fault-404.xml")));

      ProfitBricksApi pbApi = api(server.url("/").url());
      DataCenterApi api = pbApi.dataCenterApi();

      String id = "random-non-existing-id";
      try {
         api.clearDataCenter(id);
         fail("Request should have failed");
      } catch (Exception ex) {
         assertTrue(ex instanceof ResourceNotFoundException, "Exception should be an ResourceNotFoundException");
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   @Test
   public void testBadRequest() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setResponseCode(500).setBody(payloadFromResource("/fault-400.xml")));

      ProfitBricksApi pbApi = api(server.url("/").url());
      DataCenterApi api = pbApi.dataCenterApi();

      try {
         api.createDataCenter(DataCenter.Request.creatingPayload("D@tacenter", Location.DE_FKB));
         fail("Request should have failed");
      } catch (Exception ex) {
         assertTrue(ex instanceof IllegalArgumentException, "Exception should be an IllegalArgumentException");
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   @Test
   public void testUnauthorized() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setResponseCode(401).setBody(payloadFromResource("/html/fault-401.html")));

      ProfitBricksApi pbApi = api(server.url("/").url());
      DataCenterApi api = pbApi.dataCenterApi();

      try {
         api.clearDataCenter("some-datacenter-id");
         fail("Request should have failed");
      } catch (Exception ex) {
         assertTrue(ex instanceof AuthorizationException, "Exception should be an AuthorizationException");
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   @Test
   public void testOverLimitSettings() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setResponseCode(503).setBody(payloadFromResource("/fault-413.xml")));

      ProfitBricksApi pbApi = api(server.url("/").url());
      ServerApi api = pbApi.serverApi();

      try {
         api.createServer(
                 Server.Request.creatingBuilder()
                 .dataCenterId("some-datacenter-id")
                 .name("node1")
                 .cores(99)
                 .ram(12800)
                 .build());
         fail("Request should have failed.");
      } catch (Exception ex) {
         assertTrue(ex instanceof InsufficientResourcesException, "Exception should be InsufficientResourcesException");
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   @Test
   public void testServiceUnderMaintenance() throws Exception {
      MockWebServer server = mockWebServer();
      for (int i = 0; i <= MAX_RETRIES; i++)  // jclouds retries 5 times
         server.enqueue(new MockResponse().setStatus(statusLine503ok()).setBody(payloadFromResource("/html/maintenance-503.html")));

      ProfitBricksApi pbApi = api(server.url("/").url());
      DataCenterApi api = pbApi.dataCenterApi();

      try {
         api.clearDataCenter("some-datacenter-id");
         fail("Request should have failed.");
      } catch (Exception ex) {
         assertTrue(ex instanceof HttpResponseException, "Exception should be HttpResponseException");
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   public String statusLine503ok() {
      return "HTTP/1.1 503 OK";
   }
}
