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
import static org.jclouds.glacier.blobstore.strategy.internal.BasePollingStrategy.DEFAULT_TIME_BETWEEN_POLLS;
import static org.jclouds.glacier.util.TestUtils.MiB;
import static org.jclouds.glacier.util.TestUtils.buildData;
import static org.jclouds.glacier.util.TestUtils.buildPayload;

import java.io.IOException;
import java.io.InputStream;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.glacier.blobstore.strategy.internal.BasePollingStrategy;
import org.jclouds.glacier.domain.ArchiveRetrievalJobRequest;
import org.jclouds.glacier.domain.InventoryRetrievalJobRequest;
import org.jclouds.glacier.domain.JobMetadata;
import org.jclouds.glacier.domain.JobStatus;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.util.ContentRange;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.io.Closer;

/**
 * Long live test for Glacier.
 */
public class GlacierClientLongLiveTest extends BaseApiLiveTest<GlacierClient>{

   private static final long PART_SIZE = 1;
   private static final String VAULT_NAME = "JCLOUDS_LIVE_TESTS";
   private static final String ARCHIVE_DESCRIPTION = "test archive";

   private String archiveId = null;
   private String archiveRetrievalJob = null;
   private String inventoryRetrievalJob = null;

   public GlacierClientLongLiveTest() {
      this.provider = "glacier";
   }

   @Test(groups = {"live", "livelong", "setup"})
   public void testSetVault() throws Exception {
      api.createVault(VAULT_NAME);
      api.uploadArchive(VAULT_NAME, buildPayload(1 * MiB), ARCHIVE_DESCRIPTION);
   }

   @Test(groups = {"live", "livelong", "longtest"})
   public void testUploadArchive() {
      String archiveId = api.uploadArchive(VAULT_NAME, buildPayload(1 * MiB));
      assertThat(api.deleteArchive(VAULT_NAME, archiveId)).isTrue();
   }

   @Test(groups = {"live", "livelong", "longtest"})
   public void testCompleteMultipartUpload() {
      String uploadId = api.initiateMultipartUpload(VAULT_NAME, PART_SIZE);
      ImmutableMap.Builder<Integer, HashCode> hashes = ImmutableMap.builder();
      hashes.put(0, api.uploadPart(VAULT_NAME, uploadId, ContentRange.fromPartNumber(0, PART_SIZE),
            buildPayload(PART_SIZE * MiB)));
      hashes.put(1, api.uploadPart(VAULT_NAME, uploadId, ContentRange.fromPartNumber(1, PART_SIZE),
            buildPayload(PART_SIZE * MiB)));
      archiveId = api.completeMultipartUpload(VAULT_NAME, uploadId, hashes.build(), PART_SIZE * 2 * MiB);
      assertThat(archiveId).isNotNull();
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testUploadArchive", "testCompleteMultipartUpload"})
   public void testInitiateJob() {
      ArchiveRetrievalJobRequest archiveRetrieval = ArchiveRetrievalJobRequest.builder().archiveId(archiveId).build();
      InventoryRetrievalJobRequest inventoryRetrieval = InventoryRetrievalJobRequest.builder().build();
      archiveRetrievalJob = api.initiateJob(VAULT_NAME, archiveRetrieval);
      inventoryRetrievalJob = api.initiateJob(VAULT_NAME, inventoryRetrieval);
      assertThat(archiveRetrievalJob).isNotNull();
      assertThat(inventoryRetrievalJob).isNotNull();
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testInitiateJob"})
   public void testDescribeJob() {
      VaultMetadata vaultMetadata = api.describeVault(VAULT_NAME);

      JobMetadata archiveRetrievalMetadata = api.describeJob(VAULT_NAME, archiveRetrievalJob);
      assertThat(archiveRetrievalMetadata.getArchiveId()).isEqualTo(archiveId);
      assertThat(archiveRetrievalMetadata.getJobId()).isEqualTo(archiveRetrievalJob);
      assertThat(archiveRetrievalMetadata.getVaultArn()).isEqualTo(vaultMetadata.getVaultARN());

      JobMetadata inventoryRetrievalMetadata = api.describeJob(VAULT_NAME, inventoryRetrievalJob);
      assertThat(inventoryRetrievalMetadata.getJobId()).isEqualTo(inventoryRetrievalJob);
      assertThat(inventoryRetrievalMetadata.getVaultArn()).isEqualTo(vaultMetadata.getVaultARN());
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testInitiateJob"})
   public void testListJobs() {
         assertThat(api.listJobs(VAULT_NAME)).extracting("jobId").contains(inventoryRetrievalJob, archiveRetrievalJob);
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testInitiateJob", "testDescribeJob", "testListJobs"})
   public void testWaitForSucceed() throws InterruptedException {
      new BasePollingStrategy(api).waitForSuccess(VAULT_NAME, archiveRetrievalJob);
      new BasePollingStrategy(api, 0, DEFAULT_TIME_BETWEEN_POLLS).waitForSuccess(VAULT_NAME,
            inventoryRetrievalJob);
      assertThat(api.describeJob(VAULT_NAME, archiveRetrievalJob).getStatusCode()).isEqualTo(JobStatus.SUCCEEDED);
      assertThat(api.describeJob(VAULT_NAME, inventoryRetrievalJob).getStatusCode()).isEqualTo(JobStatus.SUCCEEDED);
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testWaitForSucceed"})
   public void testGetJobOutput() throws IOException {
      Closer closer = Closer.create();
      try {
         InputStream inputStream = closer.register(api.getJobOutput(VAULT_NAME, archiveRetrievalJob).openStream());
         InputStream expectedInputStream = closer.register(buildData(PART_SIZE * 2 * MiB).openStream());
         assertThat(inputStream).hasContentEqualTo(expectedInputStream);
      } finally {
         closer.close();
      }
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testWaitForSucceed"})
   public void testGetInventoryRetrievalOutput() throws InterruptedException {
      assertThat(api.getInventoryRetrievalOutput(VAULT_NAME, inventoryRetrievalJob))
            .extracting("description").contains(ARCHIVE_DESCRIPTION);
   }

   @Test(groups = {"live", "livelong", "longtest"}, dependsOnMethods = {"testGetJobOutput"})
   public void testDeleteArchive() throws Exception {
      assertThat(api.deleteArchive(VAULT_NAME, archiveId)).isTrue();
   }
}
