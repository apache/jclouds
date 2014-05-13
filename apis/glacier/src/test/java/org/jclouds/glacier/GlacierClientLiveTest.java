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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.UUID;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.testng.annotations.Test;

/**
 * Live test for Glacier.
 *
 * @author Roman Coedo
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
      assertTrue(api.deleteVaultIfEmpty(UUID.randomUUID().toString()));
   }

   @Test(groups = { "integration", "live" })
   public void testDescribeNonExistentVault() throws Exception {
      VaultMetadata vault = api.describeVault(UUID.randomUUID().toString());
      assertEquals(vault, null);
   }

   private void testDescribeVault() throws Exception {
      VaultMetadata vault = api.describeVault(VAULT_NAME1);
      assertEquals(vault.getVaultName(), VAULT_NAME1);
      assertEquals(vault.getNumberOfArchives(), 0);
      assertEquals(vault.getSizeInBytes(), 0);
      assertEquals(vault.getLastInventoryDate(), null);
   }

   private void testListVaults() throws Exception {
      PaginatedVaultCollection vaults = api.listVaults();
      Iterator<VaultMetadata> i = vaults.iterator();
      assertEquals(i.next().getVaultName(), VAULT_NAME1);
      assertEquals(i.next().getVaultName(), VAULT_NAME2);
      assertEquals(i.next().getVaultName(), VAULT_NAME3);
   }

   @Test(groups = { "integration", "live" })
   public void testCreateDescribeAndListVault() throws Exception {
      try {
         String path = api.createVault(VAULT_NAME1).toString();
         api.createVault(VAULT_NAME2);
         api.createVault(VAULT_NAME3);
         assertTrue(path.contains("https://glacier.us-east-1.amazonaws.com/"));
         assertTrue(path.contains("/vaults/" + VAULT_NAME1));
         this.testDescribeVault();
         this.testListVaults();
      } finally {
         api.deleteVaultIfEmpty(VAULT_NAME1);
         api.deleteVaultIfEmpty(VAULT_NAME2);
         api.deleteVaultIfEmpty(VAULT_NAME3);
      }
   }
}
