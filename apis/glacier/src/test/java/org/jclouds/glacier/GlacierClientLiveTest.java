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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.glacier.util.TestUtils.MiB;
import static org.jclouds.glacier.util.TestUtils.buildPayload;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.UUID;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.glacier.domain.MultipartUploadMetadata;
import org.jclouds.glacier.domain.PaginatedMultipartUploadCollection;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.util.ContentRange;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Live test for Glacier.
 */
@Test(groups = { "integration", "live" })
public class GlacierClientLiveTest extends BaseApiLiveTest<GlacierClient>{

   public GlacierClientLiveTest() {
      this.provider = "glacier";
   }

   private final String VAULT_NAME1 = UUID.randomUUID().toString();
   private final String VAULT_NAME2 = UUID.randomUUID().toString();
   private final String VAULT_NAME3 = UUID.randomUUID().toString();

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
      assertThat(path)
            .contains("https://glacier.us-east-1.amazonaws.com/")
            .contains("/vaults/" + VAULT_NAME1);
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testCreateVault" })
   public void testListAndDescribeVaults() throws Exception {
      PaginatedVaultCollection vaults = api.listVaults();
      assertThat(vaults).containsAll(ImmutableList.of(
            api.describeVault(VAULT_NAME1),
            api.describeVault(VAULT_NAME2),
            api.describeVault(VAULT_NAME3)));
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testCreateVault" })
   public void testListMultipartUploadsWithEmptyList() throws Exception {
      assertThat(api.listMultipartUploads(VAULT_NAME1)).isEmpty();
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testListMultipartUploadsWithEmptyList" })
   public void testInitiateAndAbortMultipartUpload() throws Exception {
      String uploadId = api.initiateMultipartUpload(VAULT_NAME1, 8);
      try {
         assertNotNull(uploadId);
      } finally {
         api.abortMultipartUpload(VAULT_NAME1, uploadId);
      }
   }

   @Test(groups = { "integration", "live" }, dependsOnMethods = { "testInitiateAndAbortMultipartUpload" })
   public void testListMultipartUploads() throws Exception {
      long partSizeInMb = 1;
      String uploadId = api.initiateMultipartUpload(VAULT_NAME1, partSizeInMb);
      try {
         assertNotNull(api.uploadPart(VAULT_NAME1, uploadId,
               ContentRange.fromPartNumber(0, partSizeInMb), buildPayload(partSizeInMb * MiB)));
         PaginatedMultipartUploadCollection uploads = api.listMultipartUploads(VAULT_NAME1);
         ImmutableList.Builder<String> list = ImmutableList.builder();
         for (MultipartUploadMetadata upload : uploads) {
            list.add(upload.getMultipartUploadId());
         }
         assertThat(list.build()).contains(uploadId);
         assertTrue(api.abortMultipartUpload(VAULT_NAME1, uploadId));
      } finally {
         api.abortMultipartUpload(VAULT_NAME1, uploadId);
      }
   }

   @Test(groups = { "integration", "live" },
         dependsOnMethods = { "testListAndDescribeVaults", "testListMultipartUploadsWithEmptyList",
         "testInitiateAndAbortMultipartUpload", "testListMultipartUploads" })
   public void testDeleteVault() throws Exception {
      assertTrue(api.deleteVault(VAULT_NAME1));
      assertTrue(api.deleteVault(VAULT_NAME2));
      assertTrue(api.deleteVault(VAULT_NAME3));
   }
}
