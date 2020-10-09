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
import static org.testng.Assert.assertTrue;

import java.util.Set;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.Role;
import org.jclouds.openstack.v2_0.internal.BaseOpenStackMockTest;
import org.testng.annotations.Test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;


/**
 * Tests RoleApi Guice wiring and parsing
 */
@Test(groups = "unit", testName = "RoleAdminApiMockTest")
public class RoleAdminApiMockTest extends BaseOpenStackMockTest<KeystoneApi> {

   Set<Role> expectedRoles = ImmutableSet.of(
         Role.builder().id("22529316b2384072b2e8946af5e8cfb6").name("admin").build(),
         Role.builder().id("9fe2ff9ee4384b1894a90878d3e92bab").name("_member_")
               .description("Default role for project membership").build());

   public void listRoles() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200).setBody(
            stringFromResource("/role_list_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         RoleAdminApi roleAdminApi = keystoneApi.getRoleAdminApi().get();
         FluentIterable<? extends Role> roles = roleAdminApi.list();

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateRoleRequest = server.takeRequest();
         assertEquals(updateRoleRequest.getRequestLine(), "GET /OS-KSADM/roles HTTP/1.1");

         assertEquals(roles.size(), 2);
         assertEquals(roles.toSet(), expectedRoles);

      } finally {
         server.shutdown();
      }
   }

   public void createRole() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(201).setBody(
            stringFromResource("/role_create_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         RoleAdminApi roleAdminApi = keystoneApi.getRoleAdminApi().get();
         Role testRole = roleAdminApi.create("jclouds-role");

         assertNotNull(testRole);
         assertEquals(testRole.getId(), "r1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest createRoleRequest = server.takeRequest();
         assertEquals(createRoleRequest.getRequestLine(), "POST /OS-KSADM/roles HTTP/1.1");
         assertEquals(createRoleRequest.getBody().readUtf8(), "{\"role\":{\"name\":\"jclouds-role\"}}");
      } finally {
         server.shutdown();
      }
   }

   public void getRole() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200).setBody(
            stringFromResource("/role_create_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         RoleAdminApi roleAdminApi = keystoneApi.getRoleAdminApi().get();
         Role role = roleAdminApi.get("r1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateRoleRequest = server.takeRequest();
         assertEquals(updateRoleRequest.getRequestLine(), "GET /OS-KSADM/roles/r1000 HTTP/1.1");

         /*
          * Check response
          */
         assertEquals(role.getId(), "r1000");
         assertEquals(role.getName(), "jclouds-role");
      } finally {
         server.shutdown();
      }
   }

   public void deleteRole() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(204)));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         RoleAdminApi roleAdminApi = keystoneApi.getRoleAdminApi().get();
         boolean success = roleAdminApi.delete("r1000");

         assertTrue(success);
         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateRoleRequest = server.takeRequest();
         assertEquals(updateRoleRequest.getRequestLine(), "DELETE /OS-KSADM/roles/r1000 HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }

}
