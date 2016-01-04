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

import java.util.UUID;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.util.ContentRange;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;

/**
 * Live test for Glacier.
 */
@Test(groups = {"live", "liveshort"})
public class GlacierClientLiveTest extends BaseApiLiveTest<GlacierClient> {

   private final String VAULT_NAME1 = UUID.randomUUID().toString();
   private final String VAULT_NAME2 = UUID.randomUUID().toString();
   private final String VAULT_NAME3 = UUID.randomUUID().toString();

   public GlacierClientLiveTest() {
      this.provider = "glacier";
   }

   @Test
   public void testDeleteVaultIfEmptyOrNotFound() throws Exception {
      assertThat(api.deleteVault(UUID.randomUUID().toString())).isTrue();
   }

   @Test
   public void testDescribeNonExistentVault() throws Exception {
      assertThat(api.describeVault(UUID.randomUUID().toString())).isNull();
   }

   @Test
   public void testCreateVault() throws Exception {
      assertThat(api.createVault(VAULT_NAME1).toString()).contains("/vaults/" + VAULT_NAME1);
      assertThat(api.createVault(VAULT_NAME2).toString()).contains("/vaults/" + VAULT_NAME2);
      assertThat(api.createVault(VAULT_NAME3).toString()).contains("/vaults/" + VAULT_NAME3);
   }

   @Test(dependsOnMethods = {"testCreateVault"})
   public void testListAndDescribeVaults() throws Exception {
      PaginatedVaultCollection vaults = api.listVaults();
      assertThat(vaults).containsAll(ImmutableList.of(
            api.describeVault(VAULT_NAME1),
            api.describeVault(VAULT_NAME2),
            api.describeVault(VAULT_NAME3)));
   }

   @Test(dependsOnMethods = {"testCreateVault"})
   public void testListMultipartUploadWithEmptyList() throws Exception {
      assertThat(api.listMultipartUploads(VAULT_NAME1)).isEmpty();
   }

   @Test(dependsOnMethods = {"testListMultipartUploadWithEmptyList"})
   public void testInitiateListAndAbortMultipartUpload() throws Exception {
      long partSizeInMb = 1;
      String uploadId = api.initiateMultipartUpload(VAULT_NAME1, partSizeInMb);
      try {
         assertThat(api.listMultipartUploads(VAULT_NAME1)).extracting("multipartUploadId").contains(uploadId);

         HashCode part1 = api.uploadPart(VAULT_NAME1, uploadId,
               ContentRange.fromPartNumber(0, partSizeInMb), buildPayload(partSizeInMb * MiB));
         HashCode part2 = api.uploadPart(VAULT_NAME1, uploadId,
               ContentRange.fromPartNumber(1, partSizeInMb), buildPayload(partSizeInMb * MiB));
         assertThat(part1).isNotNull();
         assertThat(part2).isNotNull();
         assertThat(api.listParts(VAULT_NAME1, uploadId).iterator()).extracting("treeHash").containsExactly(part1, part2);
      } finally {
         assertThat(api.abortMultipartUpload(VAULT_NAME1, uploadId)).isTrue();
      }
   }

   @Test(dependsOnMethods = {"testListAndDescribeVaults", "testListMultipartUploadWithEmptyList",
         "testInitiateListAndAbortMultipartUpload"})
   public void testDeleteVaultAndArchive() throws Exception {
      assertThat(api.deleteVault(VAULT_NAME1)).isTrue();
      assertThat(api.deleteVault(VAULT_NAME2)).isTrue();
      assertThat(api.deleteVault(VAULT_NAME3)).isTrue();
   }
}
