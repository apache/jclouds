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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.Constants.PROPERTY_MAX_RETRIES;
import static org.jclouds.Constants.PROPERTY_SO_TIMEOUT;
import static org.jclouds.glacier.util.TestUtils.MiB;
import static org.jclouds.glacier.util.TestUtils.buildPayload;
import static org.jclouds.util.Strings2.urlEncode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.glacier.domain.ArchiveMetadataCollection;
import org.jclouds.glacier.domain.ArchiveRetrievalJobRequest;
import org.jclouds.glacier.domain.InventoryRetrievalJobRequest;
import org.jclouds.glacier.domain.JobMetadata;
import org.jclouds.glacier.domain.MultipartUploadMetadata;
import org.jclouds.glacier.domain.PaginatedMultipartUploadCollection;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.Payload;
import org.jclouds.json.Json;
import org.jclouds.json.internal.GsonWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.inject.Module;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

/**
 * Mock test for Glacier.
 */
@Test(singleThreaded = true, groups = {"mock"}, testName = "GlacierClientMockTest")
public class GlacierClientMockTest {

   private static final String REQUEST_ID = "AAABZpJrTyioDC_HsOmHae8EZp_uBSJr6cnGOLKp_XJCl-Q";
   private static final String DATE = "Sun, 25 Mar 2012 12:00:00 GMT";
   private static final String HTTP = "HTTP/1.1";
   private static final String VAULT_NAME = "examplevault";
   private static final String VAULT_NAME1 = "examplevault1";
   private static final String VAULT_NAME2 = "examplevault2";
   private static final String VAULT_NAME3 = "examplevault3";
   private static final String LOCATION = "/111122223333/";
   private static final String VAULT_LOCATION = LOCATION + "vaults/" + VAULT_NAME;
   private static final String VAULT_ARN_PREFIX = "arn:aws:glacier:us-east-1:012345678901:vaults/";
   private static final String VAULT_ARN = VAULT_ARN_PREFIX + VAULT_NAME;
   private static final String VAULT_ARN1 = VAULT_ARN_PREFIX + VAULT_NAME1;
   private static final String VAULT_ARN3 = VAULT_ARN_PREFIX + VAULT_NAME3;
   private static final String ARCHIVE_ID = "NkbByEejwEggmBz2fTHgJrg0XBoDfjP4q6iu87-TjhqG6eGoOY9Z8i1_AUyUsuhPAdTqLHy8pTl5nfCFJmDl2yEZONi5L26Omw12vcs01MNGntHEQL8MBfGlqrEXAMPLEArchiveId";
   private static final String ARCHIVE_LOCATION = VAULT_LOCATION + "/archives/" + ARCHIVE_ID;
   private static final HashCode TREEHASH = HashCode.fromString("beb0fe31a1c7ca8c6c04d574ea906e3f97b31fdca7571defb5b44dca89b5af60");
   private static final String DESCRIPTION = "test description";
   private static final String MULTIPART_UPLOAD_LOCATION = VAULT_LOCATION + "/multipart-uploads/" + ARCHIVE_ID;
   private static final String MULTIPART_UPLOAD_ID = "OW2fM5iVylEpFEMM9_HpKowRapC3vn5sSL39_396UW9zLFUWVrnRHaPjUJddQ5OxSHVXjYtrN47NBZ-khxOjyEXAMPLE";
   private static final String MARKER = "xsQdFIRsfJr20CW2AbZBKpRZAFTZSJIMtL2hYf8mvp8dM0m4RUzlaqoEye6g3h3ecqB_zqwB7zLDMeSWhwo65re4C4Ev";
   private static final String JOB_ID = "HkF9p6o7yjhFx-K3CGl6fuSm6VzW9T7esGQfco8nUXVYwS0jlb5gq1JZ55yHgt5vP54ZShjoQzQVVh7vEXAMPLEjobID";
   private static final Set<Module> modules = ImmutableSet.<Module> of(new ExecutorServiceModule(sameThreadExecutor(),
         sameThreadExecutor()));

   private MockWebServer server;
   private GlacierClient client;

   private static GlacierClient getGlacierClient(URL server) {
      Properties overrides = new Properties();
      // prevent expect-100 bug http://code.google.com/p/mockwebserver/issues/detail?id=6
      overrides.setProperty(PROPERTY_SO_TIMEOUT, "0");
      overrides.setProperty(PROPERTY_MAX_RETRIES, "1");
      return ContextBuilder.newBuilder("glacier").credentials("accessKey", "secretKey").endpoint(server.toString())
            .modules(modules).overrides(overrides).buildApi(GlacierClient.class);
   }

   private static MockResponse buildBaseResponse(int responseCode) {
      MockResponse mr = new MockResponse();
      mr.setResponseCode(responseCode);
      mr.addHeader(GlacierHeaders.REQUEST_ID, REQUEST_ID);
      mr.addHeader(HttpHeaders.DATE, DATE);
      return mr;
   }

   private static String getResponseBody(String path) throws IOException {
      return Resources.toString(Resources.getResource(GlacierClientMockTest.class, path), UTF_8);
   }

   @BeforeMethod
   private void initServer() throws IOException {
      server = new MockWebServer();
      server.play();
      client = getGlacierClient(server.getUrl("/"));
   }

   @AfterMethod
   private void shutdownServer() {
      try {
         server.shutdown();
      } catch (IOException e) {
         // MockWebServer 2.1.0 introduces an active wait for its executor
         // termination. That active wait is a hardcoded value and throws an IOE
         // if the executor has not terminated in that timeout. It is safe to
         // ignore this exception as the functionality has been properly
         // verified.
      }
   }

   @Test
   public void testCreateVault() throws InterruptedException {
      MockResponse mr = buildBaseResponse(201);
      mr.addHeader(HttpHeaders.LOCATION, VAULT_LOCATION);
      server.enqueue(mr);

      assertThat(client.createVault(VAULT_NAME)).isEqualTo(URI.create(server.getUrl("/") + VAULT_LOCATION.substring(1)));

      assertEquals(server.takeRequest().getRequestLine(), "PUT /-/vaults/" + VAULT_NAME + " " + HTTP);
   }

   @Test
   public void testDeleteVault() throws InterruptedException {
      server.enqueue(buildBaseResponse(204));

      assertThat(client.deleteVault(VAULT_NAME)).isTrue();

      assertEquals(server.takeRequest().getRequestLine(), "DELETE /-/vaults/" + VAULT_NAME + " " + HTTP);
   }

   @Test
   public void testDescribeVault() throws InterruptedException, IOException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/describeVaultResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      VaultMetadata vault = client.describeVault(VAULT_NAME);
      assertThat(vault.getVaultName()).isEqualTo(VAULT_NAME);
      assertThat(vault.getVaultARN()).isEqualTo(VAULT_ARN);
      assertThat(vault.getSizeInBytes()).isEqualTo(78088912);
      assertThat(vault.getNumberOfArchives()).isEqualTo(192);

      assertEquals(server.takeRequest().getRequestLine(), "GET /-/vaults/" + VAULT_NAME + " " + HTTP);
   }

   @Test
   public void testListVaults() throws InterruptedException, IOException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listVaultsResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      assertThat(client.listVaults()).extracting("vaultName").containsExactly(VAULT_NAME1, VAULT_NAME2, VAULT_NAME3);

      assertEquals(server.takeRequest().getRequestLine(), "GET /-/vaults " + HTTP);
   }

   @Test
   public void testListVaultsWithEmptyList() throws InterruptedException, IOException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listVaultsWithEmptyListResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      assertThat(client.listVaults()).isEmpty();
   }

   @Test
   public void testListVaultsWithQueryParams() throws InterruptedException, IOException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listVaultsWithQueryParamsResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      PaginatedVaultCollection vaults = client.listVaults(PaginationOptions.Builder.limit(2).marker(VAULT_ARN1));
      assertThat(vaults).extracting("vaultName").containsExactly(VAULT_NAME1, VAULT_NAME2);
      assertThat(vaults.nextMarker().get()).isEqualTo(VAULT_ARN3);

      assertEquals(server.takeRequest().getRequestLine(),
              "GET /-/vaults?limit=2&marker=" + urlEncode(VAULT_ARN1, '/') + " " + HTTP);
   }

   @Test
   public void testUploadArchive() throws InterruptedException {
      MockResponse mr = buildBaseResponse(201);
      mr.addHeader(GlacierHeaders.TREE_HASH, TREEHASH);
      mr.addHeader(HttpHeaders.LOCATION, ARCHIVE_LOCATION);
      mr.addHeader(GlacierHeaders.ARCHIVE_ID, ARCHIVE_ID);
      server.enqueue(mr);

      assertThat(client.uploadArchive(VAULT_NAME, buildPayload(10), DESCRIPTION)).isEqualTo(ARCHIVE_ID);

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getRequestLine(), "POST /-/vaults/" + VAULT_NAME + "/archives " + HTTP);
      assertEquals(request.getHeader(GlacierHeaders.ARCHIVE_DESCRIPTION), DESCRIPTION);
      assertNotNull(request.getHeaders(GlacierHeaders.TREE_HASH));
      assertNotNull(request.getHeaders(GlacierHeaders.LINEAR_HASH));
   }

   @Test
   public void testDeleteArchive() throws InterruptedException {
      MockResponse mr = buildBaseResponse(204);
      server.enqueue(mr);

      assertThat(client.deleteArchive(VAULT_NAME, ARCHIVE_ID)).isTrue();

      assertEquals(server.takeRequest().getRequestLine(), "DELETE /-/vaults/" + VAULT_NAME + "/archives/" + ARCHIVE_ID + " " + HTTP);
   }

   @Test
   public void testInitiateMultipartUpload() throws InterruptedException {
      MockResponse mr = buildBaseResponse(201);
      mr.addHeader(HttpHeaders.LOCATION, MULTIPART_UPLOAD_LOCATION);
      mr.addHeader(GlacierHeaders.MULTIPART_UPLOAD_ID, MULTIPART_UPLOAD_ID);
      server.enqueue(mr);

      assertThat(client.initiateMultipartUpload(VAULT_NAME, 4, DESCRIPTION)).isEqualTo(MULTIPART_UPLOAD_ID);

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getRequestLine(), "POST /-/vaults/" + VAULT_NAME + "/multipart-uploads " + HTTP);
      assertEquals(request.getHeader(GlacierHeaders.PART_SIZE), "4194304");
      assertEquals(request.getHeader(GlacierHeaders.ARCHIVE_DESCRIPTION), DESCRIPTION);
   }

   @Test
   public void testUploadPart() throws InterruptedException {
      MockResponse mr = buildBaseResponse(204);
      mr.addHeader(GlacierHeaders.TREE_HASH, TREEHASH);
      server.enqueue(mr);

      assertThat(client.uploadPart(VAULT_NAME, MULTIPART_UPLOAD_ID, ContentRange.fromPartNumber(0, 4),
              buildPayload(4 * MiB))).isEqualTo(TREEHASH);

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getRequestLine(),
            "PUT /-/vaults/" + VAULT_NAME + "/multipart-uploads/" + MULTIPART_UPLOAD_ID + " " + HTTP);
      assertEquals(request.getHeader(HttpHeaders.CONTENT_RANGE), "bytes 0-4194303/*");
      assertEquals(request.getHeader(HttpHeaders.CONTENT_LENGTH), "4194304");
   }

   // TODO: Change size to 4096 when moving to JDK 7
   @Test
   public void testUploadPartMaxSize() throws InterruptedException {
      // force the server to discard the request body
      server.setBodyLimit(0);
      MockResponse mr = buildBaseResponse(204);
      mr.addHeader(GlacierHeaders.TREE_HASH, TREEHASH);
      server.enqueue(mr);

      long size = 1024;
      ContentRange range = ContentRange.fromPartNumber(0, size);
      Payload payload = buildPayload(1);
      payload.getContentMetadata().setContentLength(size * MiB);
      try {
         /* The client.uploadPart call should throw an HttpResponseException since the payload is smaller than expected.
          * This trick makes the test way faster.
          */
         client.uploadPart(VAULT_NAME, MULTIPART_UPLOAD_ID, range, payload);
         Assert.fail();
      } catch (HttpResponseException e) {
      }

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getRequestLine(), "PUT /-/vaults/" + VAULT_NAME + "/multipart-uploads/" + MULTIPART_UPLOAD_ID + " " + HTTP);
      assertEquals(request.getHeader(HttpHeaders.CONTENT_RANGE), range.buildHeader());
      assertEquals(request.getHeader(HttpHeaders.CONTENT_LENGTH), payload.getContentMetadata().getContentLength().toString());
   }

   @Test
   public void testCompleteMultipartUpload() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(201);
      mr.addHeader(HttpHeaders.LOCATION, ARCHIVE_LOCATION);
      mr.addHeader(GlacierHeaders.ARCHIVE_ID, ARCHIVE_ID);
      server.enqueue(mr);

      HashCode partHashcode = HashCode.fromString("9bc1b2a288b26af7257a36277ae3816a7d4f16e89c1e7e77d0a5c48bad62b360");
      ImmutableMap<Integer, HashCode> map = ImmutableMap.of(
            1, partHashcode,
            2, partHashcode,
            3, partHashcode,
            4, partHashcode);
      assertThat(client.completeMultipartUpload(VAULT_NAME, MULTIPART_UPLOAD_ID, map, 8 * MiB)).isEqualTo(ARCHIVE_ID);

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getRequestLine(),
            "POST /-/vaults/" + VAULT_NAME + "/multipart-uploads/" + MULTIPART_UPLOAD_ID + " " + HTTP);
      assertEquals(request.getHeader(GlacierHeaders.TREE_HASH),
            "9491cb2ed1d4e7cd53215f4017c23ec4ad21d7050a1e6bb636c4f67e8cddb844");
      assertEquals(request.getHeader(GlacierHeaders.ARCHIVE_SIZE), "8388608");
   }

   @Test
   public void testAbortMultipartUpload() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(204);
      server.enqueue(mr);

      assertTrue(client.abortMultipartUpload(VAULT_NAME, MULTIPART_UPLOAD_ID));

      assertEquals(server.takeRequest().getRequestLine(),
              "DELETE /-/vaults/" + VAULT_NAME + "/multipart-uploads/" + MULTIPART_UPLOAD_ID + " " + HTTP);
   }

   @Test
   public void testListParts() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listPartsResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      MultipartUploadMetadata mpu = client.listParts(VAULT_NAME, MULTIPART_UPLOAD_ID, PaginationOptions.Builder.limit(1).marker("1001"));
      assertThat(mpu.getArchiveDescription()).isEqualTo("archive description 1");
      assertThat(mpu.getMultipartUploadId()).isEqualTo(MULTIPART_UPLOAD_ID);
      assertThat(mpu.getPartSizeInBytes()).isEqualTo(4194304);
      assertThat(mpu).extracting("treeHash").containsExactly(HashCode.fromString("01d34dabf7be316472c93b1ef80721f5d4"));
      assertThat(mpu).extracting("range").containsExactly(ContentRange.fromString("4194304-8388607"));

      assertEquals(server.takeRequest().getRequestLine(),
              "GET /-/vaults/examplevault/multipart-uploads/" + MULTIPART_UPLOAD_ID + "?limit=1&marker=1001 " + HTTP);
   }

   @Test
   public void testListMultipartUploads() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listMultipartUploadsResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      PaginatedMultipartUploadCollection mpus = client.listMultipartUploads(VAULT_NAME, PaginationOptions.Builder.limit(1).marker(MARKER));
      assertThat(mpus).extracting("archiveDescription").containsExactly("archive 2");
      assertThat(mpus).extracting("multipartUploadId").containsExactly("nPyGOnyFcx67qqX7E-0tSGiRi88hHMOwOxR-_jNyM6RjVMFfV29lFqZ3rNsSaWBugg6OP92pRtufeHdQH7ClIpSF6uJc");
      assertThat(mpus).extracting("partSizeInBytes").containsExactly(4194304L);
      assertThat(mpus).extracting("vaultARN").containsExactly(VAULT_ARN);

      assertEquals(server.takeRequest().getRequestLine(),
              "GET /-/vaults/examplevault/multipart-uploads?limit=1&marker=" + MARKER + " " + HTTP);
   }

   @Test
   public void testListMultipartUploadsWithEmptyList() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listMultipartUploadsWithEmptyListResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      assertThat(client.listMultipartUploads(VAULT_NAME, PaginationOptions.Builder.limit(1).marker(MARKER))).isEmpty();

      assertEquals(server.takeRequest().getRequestLine(),
              "GET /-/vaults/examplevault/multipart-uploads?limit=1&marker=" + MARKER + " " + HTTP);
   }

   @Test
   public void testInitiateArchiveRetrievalJob() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(202);
      mr.addHeader(HttpHeaders.LOCATION, VAULT_LOCATION + "/jobs/" + JOB_ID);
      mr.addHeader(GlacierHeaders.JOB_ID, JOB_ID);
      server.enqueue(mr);

      ContentRange range = ContentRange.fromString("2097152-4194303");
      ArchiveRetrievalJobRequest retrieval = ArchiveRetrievalJobRequest.builder()
            .archiveId(ARCHIVE_ID)
            .description(DESCRIPTION)
            .range(range)
            .build();
      assertThat(client.initiateJob(VAULT_NAME, retrieval)).isEqualTo(JOB_ID);

      RecordedRequest request = server.takeRequest();
      Json json = new GsonWrapper(new Gson());
      ArchiveRetrievalJobRequest job = json.fromJson(new String(request.getBody()), ArchiveRetrievalJobRequest.class);
      assertThat(job.getDescription()).isEqualTo(DESCRIPTION);
      assertThat(job.getRange()).isEqualTo(range);
      assertThat(job.getArchiveId()).isEqualTo(ARCHIVE_ID);
      assertThat(job.getType()).isEqualTo("archive-retrieval");

      assertEquals(request.getRequestLine(), "POST /-/vaults/" + VAULT_NAME + "/jobs " + HTTP);
   }

   @Test
   public void testInitiateInventoryRetrievalJob() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(202);
      mr.addHeader(HttpHeaders.LOCATION, VAULT_LOCATION + "/jobs/" + JOB_ID);
      mr.addHeader(GlacierHeaders.JOB_ID, JOB_ID);
      server.enqueue(mr);

      String marker = "vyS0t2jHQe5qbcDggIeD50chS1SXwYMrkVKo0KHiTUjEYxBGCqRLKaiySzdN7QXGVVV5XZpNVG67pCZ_uykQXFMLaxOSu2hO_-5C0AtWMDrfo7LgVOyfnveDRuOSecUo3Ueq7K0";
      int limit = 10000;
      String startDate = "2013-12-04T21:25:42Z";
      String endDate = "2013-12-05T21:25:42Z";
      String format = "CSV";
      InventoryRetrievalJobRequest job = InventoryRetrievalJobRequest.builder()
            .format(format)
            .endDate(endDate)
            .startDate(startDate)
            .limit(limit)
            .marker(marker)
            .build();
      assertThat(client.initiateJob(VAULT_NAME, job)).isEqualTo(JOB_ID);

      RecordedRequest request = server.takeRequest();
      Json json = new GsonWrapper(new Gson());
      job = json.fromJson(new String(request.getBody()), InventoryRetrievalJobRequest.class);
      assertThat(job.getFormat()).isEqualTo(format);
      assertThat(job.getParameters().getMarker()).isEqualTo(marker);
      assertThat(job.getParameters().getLimit()).isEqualTo(limit);
      assertThat(job.getParameters().getStartDate()).isEqualTo(startDate);
      assertThat(job.getParameters().getEndDate()).isEqualTo(endDate);
      assertThat(job.getType()).isEqualTo("inventory-retrieval");

      assertEquals(request.getRequestLine(), "POST /-/vaults/examplevault/jobs HTTP/1.1");
   }

   @Test
   public void testDescribeJob() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/describeJobResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      JobMetadata job = client.describeJob(VAULT_NAME, JOB_ID);
      assertThat(job.getAction()).isEqualTo("ArchiveRetrieval");
      assertThat(job.getArchiveId()).isEqualTo(ARCHIVE_ID);
      assertThat(ContentRange.fromString("0-16777215")).isEqualTo(job.getRetrievalByteRange());
      assertThat(job.getVaultArn()).isEqualTo("arn:aws:glacier:us-east-1:012345678901:vaults/examplevault");

      assertEquals(server.takeRequest().getRequestLine(), "GET /-/vaults/" + VAULT_NAME + "/jobs/" + JOB_ID + " " + HTTP);
   }

   @Test
   public void testListJobs() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/listJobsResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      assertThat(client.listJobs(VAULT_NAME)).extracting("jobId").containsExactly(
               "s4MvaNHIh6mOa1f8iY4ioG2921SDPihXxh3Kv0FBX-JbNPctpRvE4c2_BifuhdGLqEhGBNGeB6Ub-JMunR9JoVa8y1hQ",
               "CQ_tf6fOR4jrJCL61Mfk6VM03oY8lmnWK93KK4gLig1UPAbZiN3UV4G_5nq4AfmJHQ_dOMLOX5k8ItFv0wCPN0oaz5dG");

      assertEquals(server.takeRequest().getRequestLine(), "GET /-/vaults/examplevault/jobs HTTP/1.1");
   }

   @Test
   public void testGetJobOutput() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/getJobOutputResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      Payload payload = client.getJobOutput(VAULT_NAME, JOB_ID);
      assertThat(payload.getContentMetadata().getContentType()).isEqualTo(MediaType.JSON_UTF_8.toString());
      assertThat(payload.getContentMetadata().getContentLength()).isEqualTo(mr.getBody().size());
      assertThat(payload.openStream())
              .hasContentEqualTo(Resources.getResource(GlacierClientMockTest.class,
                      "/json/getJobOutputResponseBody.json").openStream());

      assertEquals(server.takeRequest().getRequestLine(),
              "GET /-/vaults/" + VAULT_NAME + "/jobs/" + JOB_ID + "/output " + HTTP);
   }

   @Test
   public void testGetJobOutputWithContentRange() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(206);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/getJobOutputResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      ContentRange range = ContentRange.fromString("16-32");
      client.getJobOutput(VAULT_NAME, JOB_ID, range);

      RecordedRequest request = server.takeRequest();
      assertEquals(request.getHeader("Range"), "bytes=" + range.toString());
      assertEquals(request.getRequestLine(),
              "GET /-/vaults/" + VAULT_NAME + "/jobs/" + JOB_ID + "/output " + HTTP);
   }

   @Test
   public void testGetInventoryRetrievalOutput() throws IOException, InterruptedException {
      MockResponse mr = buildBaseResponse(200);
      mr.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8);
      mr.setBody(getResponseBody("/json/getJobOutputResponseBody.json"));
      mr.addHeader(HttpHeaders.CONTENT_LENGTH, mr.getBody().size());
      server.enqueue(mr);

      ArchiveMetadataCollection archives = client.getInventoryRetrievalOutput(VAULT_NAME, JOB_ID);
      assertThat(archives.getVaultARN()).isEqualTo("arn:aws:glacier:us-east-1:012345678901:vaults/examplevault");
      assertThat(archives).extracting("archiveId").containsExactly(
              "DMTmICA2n5Tdqq5BV2z7og-A20xnpAPKt3UXwWxdWsn_D6auTUrW6kwy5Qyj9xd1MCE1mBYvMQ63LWaT8yTMzMaCxB_9VBWrW4Jw4zsvg5kehAPDVKcppUD1X7b24JukOr4mMAq-oA",
              "2lHzwhKhgF2JHyvCS-ZRuF08IQLuyB4265Hs3AXj9MoAIhz7tbXAvcFeHusgU_hViO1WeCBe0N5lsYYHRyZ7rrmRkNRuYrXUs_sjl2K8ume_7mKO_0i7C-uHE1oHqaW9d37pabXrSA");
      assertThat(archives).extracting("description").containsExactly("my archive1", "my archive2");
      assertThat(archives).extracting("size").containsExactly(2140123L, 2140123L);
      assertThat(archives).extracting("treeHash").containsExactly(
              HashCode.fromString("6b9d4cf8697bd3af6aa1b590a0b27b337da5b18988dbcc619a3e608a554a1e62"),
              HashCode.fromString("7f2fe580edb35154041fa3d4b41dd6d3adaef0c85d2ff6309f1d4b520eeecda3"));
   }
}
