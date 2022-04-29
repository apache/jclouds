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
package org.jclouds.openstack.keystone.v2_0.extensions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.openstack.keystone.v2_0.options.UpdateUserOptions;
import org.jclouds.openstack.v2_0.internal.BaseOpenStackMockTest;
import org.testng.annotations.Test;


/**
 * Tests UserApi Guice wiring and parsing
 */
@Test(groups = "unit", testName = "UserAdminApiMockTest")
public class UserAdminApiMockTest extends BaseOpenStackMockTest<KeystoneApi> {

   public void createUser() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(201).setBody(
            stringFromResource("/user_create_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         UserAdminApi userAdminApi = keystoneApi.getUserAdminApi().get();
         CreateUserOptions createUserOptions = CreateUserOptions.Builder.email("john.smith@example.org").enabled(true)
               .tenant("12345");
         User testUser = userAdminApi.create("jqsmith", "jclouds-password", createUserOptions);

         assertNotNull(testUser);
         assertEquals(testUser.getId(), "u1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest createUserRequest = server.takeRequest();
         assertEquals(createUserRequest.getRequestLine(), "POST /users HTTP/1.1");
         assertEquals(
               createUserRequest.getBody().readUtf8(),
               "{\"user\":{\"name\":\"jqsmith\",\"tenantId\":\"12345\",\"password\":\"jclouds-password\",\"email\":\"john.smith@example.org\",\"enabled\":true}}");
      } finally {
         server.shutdown();
      }
   }

   public void updateUser() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200).setBody(
            stringFromResource("/user_update_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         UserAdminApi userAdminApi = keystoneApi.getUserAdminApi().get();
         UpdateUserOptions updateUserOptions = UpdateUserOptions.Builder.email("john.smith.renamed@example.org")
               .enabled(false).name("jqsmith-renamed").password("jclouds-password");
         User updatedUser = userAdminApi.update("u1000", updateUserOptions);

         assertNotNull(updatedUser);
         assertEquals(updatedUser.getId(), "u1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateUserRequest = server.takeRequest();
         assertEquals(updateUserRequest.getRequestLine(), "PUT /users/u1000 HTTP/1.1");
         assertEquals(
               updateUserRequest.getBody().readUtf8(),
               "{\"user\":{\"name\":\"jqsmith-renamed\",\"email\":\"john.smith.renamed@example.org\",\"password\":\"jclouds-password\",\"enabled\":false}}");
      } finally {
         server.shutdown();
      }
   }

   public void deleteUser() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(204)));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         UserAdminApi userAdminApi = keystoneApi.getUserAdminApi().get();
         userAdminApi.delete("u1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateUserRequest = server.takeRequest();
         assertEquals(updateUserRequest.getRequestLine(), "DELETE /users/u1000 HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }

}
