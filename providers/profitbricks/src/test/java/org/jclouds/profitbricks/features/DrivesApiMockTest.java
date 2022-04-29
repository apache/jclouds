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
package org.jclouds.profitbricks.features;

import static org.testng.Assert.assertNotNull;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.jclouds.profitbricks.ProfitBricksApi;
import org.jclouds.profitbricks.domain.Drive;
import org.jclouds.profitbricks.internal.BaseProfitBricksMockTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "DrivesApiMockTest")
public class DrivesApiMockTest extends BaseProfitBricksMockTest {

   @Test
   public void addRomDriveToServerTest() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setBody(payloadFromResource("/drives/drives-add.xml")));

      ProfitBricksApi pbApi = api(server.url(rootUrl).url());
      DrivesApi api = pbApi.drivesApi();

      String content = "<ws:addRomDriveToServer>"
              + "<request>"
              + "<imageId>image-id</imageId>"
              + "<serverId>server-id</serverId>"
              + "<deviceNumber>device-number</deviceNumber>"
              + "</request>"
              + "</ws:addRomDriveToServer>";
      try {
         String requestId = api.addRomDriveToServer(Drive.Request.AddRomDriveToServerPayload.builder()
                 .serverId("server-id")
                 .imageId("image-id")
                 .deviceNumber("device-number")
                 .build());
         assertRequestHasCommonProperties(server.takeRequest(), content);
         assertNotNull(requestId);
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }

   @Test
   public void removeRomDriveFromServerTest() throws Exception {
      MockWebServer server = mockWebServer();
      server.enqueue(new MockResponse().setBody(payloadFromResource("/drives/drives-remove.xml")));

      ProfitBricksApi pbApi = api(server.url(rootUrl).url());
      DrivesApi api = pbApi.drivesApi();

      String content = "<ws:removeRomDriveFromServer>"
              + "<imageId>image-id</imageId>"
              + "<serverId>server-id</serverId>"
              + "</ws:removeRomDriveFromServer>";
      try {
         String requestId = api.removeRomDriveFromServer("image-id", "server-id");
         assertRequestHasCommonProperties(server.takeRequest(), content);
         assertNotNull(requestId);
      } finally {
         pbApi.close();
         server.shutdown();
      }
   }
}
