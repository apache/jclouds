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

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.UUID;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.testng.annotations.Test;

/**
 * Live test for Glacier.
 */
@Test(groups = { "integration", "live" })
public class GlacierClientLiveTest extends BaseApiLiveTest<GlacierClient>{

   public GlacierClientLiveTest() {
      this.provider = "glacier";
   }

   private static final String VAULT_NAME1 = "testV1";
   private static final String VAULT_NAME2 = "testV2";
   private static final String VAULT_NAME3 = "testV3";

   @Test(groups = { "integration", "live" })
   public void testDeleteVaultIfEmptyOrNotFound() throws Exception {
      assertTrue(api.deleteVault(UUID.randomUUID().toString()));
   }

   @Test(groups = { "integration", "live" })
   public void testDescribeNonExistentVault() throws Exception {
      VaultMetadata vault = api.describeVault(UUID.randomUUID().toString());
      assertNull(vault);
   }

   @Test(groups = { "integration", "live" })
   public void testCreateVault() throws Exception {
      String path = api.createVault(VAULT_NAME1).toString();
      api.createVault(VAULT_NAME2);
      api.createVault(VAULT_NAME3);
      assertTrue(path.contains("https://glacier.us-east-1.amazonaws.com/"));
      assertTrue(path.contains("/vaults/" + VAULT_NAME1));
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testCreateVault" })
   public void testListAndDescribeVaults() throws Exception {
      PaginatedVaultCollection vaults = api.listVaults();
      assertTrue(vaults.contains(api.describeVault(VAULT_NAME1)));
      assertTrue(vaults.contains(api.describeVault(VAULT_NAME2)));
      assertTrue(vaults.contains(api.describeVault(VAULT_NAME3)));
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testListAndDescribeVaults" })
   public void testDeleteVault() throws Exception {
      api.deleteVault(VAULT_NAME1);
      api.deleteVault(VAULT_NAME2);
      api.deleteVault(VAULT_NAME3);
   }
}
