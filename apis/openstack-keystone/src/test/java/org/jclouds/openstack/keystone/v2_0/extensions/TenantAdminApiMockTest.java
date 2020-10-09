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
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;
import org.jclouds.openstack.keystone.v2_0.options.CreateTenantOptions;
import org.jclouds.openstack.keystone.v2_0.options.UpdateTenantOptions;
import org.jclouds.openstack.v2_0.internal.BaseOpenStackMockTest;
import org.testng.annotations.Test;


/**
 * Tests TenantApi Guice wiring and parsing
 */
@Test(groups = "unit", testName = "TenantAdminApiMockTest")
public class TenantAdminApiMockTest extends BaseOpenStackMockTest<KeystoneApi> {

   public void createTenant() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(201).setBody(
            stringFromResource("/tenant_create_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         TenantAdminApi tenantAdminApi = keystoneApi.getTenantAdminApi().get();
         CreateTenantOptions createTenantOptions = CreateTenantOptions.Builder.description("jclouds-description")
               .enabled(true);
         Tenant testTenant = tenantAdminApi.create("jclouds-tenant", createTenantOptions);

         assertNotNull(testTenant);
         assertEquals(testTenant.getId(), "t1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest createTenantRequest = server.takeRequest();
         assertEquals(createTenantRequest.getRequestLine(), "POST /tenants HTTP/1.1");
         assertEquals(createTenantRequest.getBody().readUtf8(),
               "{\"tenant\":{\"name\":\"jclouds-tenant\",\"description\":\"jclouds-description\",\"enabled\":true}}");
      } finally {
         server.shutdown();
      }
   }

   public void updateTenant() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200).setBody(
            stringFromResource("/tenant_update_response.json"))));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         TenantAdminApi tenantAdminApi = keystoneApi.getTenantAdminApi().get();
         UpdateTenantOptions updateTenantOptions = UpdateTenantOptions.Builder
               .description("jclouds-description-modified").enabled(false).name("jclouds-tenant-modified");
         Tenant updatedTenant = tenantAdminApi.update("t1000", updateTenantOptions);

         assertNotNull(updatedTenant);
         assertEquals(updatedTenant.getId(), "t1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateTenantRequest = server.takeRequest();
         assertEquals(updateTenantRequest.getRequestLine(), "PUT /tenants/t1000 HTTP/1.1");
         assertEquals(
               updateTenantRequest.getBody().readUtf8(),
               "{\"tenant\":{\"name\":\"jclouds-tenant-modified\",\"description\":\"jclouds-description-modified\",\"enabled\":false}}");
      } finally {
         server.shutdown();
      }
   }

   public void deleteTenant() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(204)));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         TenantAdminApi tenantAdminApi = keystoneApi.getTenantAdminApi().get();
         tenantAdminApi.delete("t1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateTenantRequest = server.takeRequest();
         assertEquals(updateTenantRequest.getRequestLine(), "DELETE /tenants/t1000 HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }

   public void addRoleOnTenant() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(201)));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         TenantAdminApi tenantAdminApi = keystoneApi.getTenantAdminApi().get();
         tenantAdminApi.addRoleOnTenant("u1000", "t1000", "r1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateTenantRequest = server.takeRequest();
         assertEquals(updateTenantRequest.getRequestLine(),
               "PUT /tenants/u1000/users/t1000/roles/OS-KSADM/r1000 HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }

   public void deleteRoleOnTenant() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access_version_uids.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/admin_extensions.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(204)));

      try {
         KeystoneApi keystoneApi = api(server.url("/").toString(), "openstack-keystone");
         TenantAdminApi tenantAdminApi = keystoneApi.getTenantAdminApi().get();
         tenantAdminApi.deleteRoleOnTenant("t1000", "u1000", "r1000");

         assertEquals(server.getRequestCount(), 3);
         assertAuthentication(server);
         assertExtensions(server);
         RecordedRequest updateTenantRequest = server.takeRequest();
         assertEquals(updateTenantRequest.getRequestLine(),
               "DELETE /tenants/t1000/users/u1000/roles/OS-KSADM/r1000 HTTP/1.1");
      } finally {
         server.shutdown();
      }
   }

}
