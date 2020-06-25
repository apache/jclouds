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
package org.jclouds.glacier.blobstore.strategy.internal;

import static com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.Constants.PROPERTY_MAX_RETRIES;
import static org.jclouds.Constants.PROPERTY_SO_TIMEOUT;
import static org.jclouds.glacier.reference.GlacierHeaders.ARCHIVE_DESCRIPTION;
import static org.jclouds.glacier.reference.GlacierHeaders.ARCHIVE_ID;
import static org.jclouds.glacier.reference.GlacierHeaders.ARCHIVE_SIZE;
import static org.jclouds.glacier.reference.GlacierHeaders.MULTIPART_UPLOAD_ID;
import static org.jclouds.glacier.reference.GlacierHeaders.PART_SIZE;
import static org.jclouds.glacier.reference.GlacierHeaders.TREE_HASH;
import static org.jclouds.glacier.util.TestUtils.MiB;
import static org.jclouds.glacier.util.TestUtils.buildPayload;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.domain.internal.BlobBuilderImpl;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.internal.BasePayloadSlicer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.google.common.net.HttpHeaders;
import com.google.inject.Module;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

@Test(groups = {"mock"}, singleThreaded = true)
public class MultipartUploadStrategyMockTest {
   private static final Set<Module> modules = ImmutableSet.<Module> of(new ExecutorServiceModule(newDirectExecutorService(),
         newDirectExecutorService()));
   private static HashCode hash8 = HashCode.fromString("c87a460c93d4a8ffcf09a9a236cc17a486d7ed8a1a2f48e9c361c5f7ac0f1280");
   private static HashCode hash4 = HashCode.fromString("9491cb2ed1d4e7cd53215f4017c23ec4ad21d7050a1e6bb636c4f67e8cddb844");
   private static HashCode hcomp = HashCode.fromString("e196b8ae66b4e55a10c84647957c1291c84ffafa44bfdb88d87f0456e5399e46");

   MockWebServer server;
   GlacierClient client;

   private static GlacierClient getGlacierClient(URL server) {
      Properties overrides = new Properties();
      // prevent expect-100 bug http://code.google.com/p/mockwebserver/issues/detail?id=6
      overrides.setProperty(PROPERTY_SO_TIMEOUT, "0");
      overrides.setProperty(PROPERTY_MAX_RETRIES, "1");
      return ContextBuilder.newBuilder("glacier").credentials("accessKey", "secretKey").endpoint(server.toString())
            .modules(modules).overrides(overrides).buildApi(GlacierClient.class);
   }

   @BeforeMethod
   private void initServer() throws IOException {
      server = new MockWebServer();
      server.play();
      client = getGlacierClient(server.getUrl("/"));
   }

   @AfterMethod
   private void shutdownServer() throws IOException {
      server.shutdown();
   }

   @Test
   public void testSequentialMPU() throws IOException, InterruptedException {
      server.enqueue(new MockResponse().setResponseCode(201).addHeader(MULTIPART_UPLOAD_ID, "upload-id"));
      for (int i = 0; i < 12; i++) {
         server.enqueue(new MockResponse().setResponseCode(204).addHeader(TREE_HASH, hash8));
      }
      server.enqueue(new MockResponse().setResponseCode(204).addHeader(TREE_HASH, hash4));
      server.enqueue(new MockResponse().setResponseCode(201).addHeader(ARCHIVE_ID, "archive-id"));

      SequentialMultipartUploadStrategy strat = new SequentialMultipartUploadStrategy(client,
            new BaseSlicingStrategy(new BasePayloadSlicer()));

      assertThat(strat.execute("vault", new BlobBuilderImpl().name("test").payload(buildPayload(100 * MiB)).build()))
            .isEqualTo("archive-id");

      RecordedRequest initiate = server.takeRequest();
      assertThat(initiate.getRequestLine()).isEqualTo("POST /-/vaults/vault/multipart-uploads HTTP/1.1");
      assertThat(initiate.getHeader(ARCHIVE_DESCRIPTION)).isEqualTo("test");
      assertThat(Long.parseLong(initiate.getHeader(PART_SIZE))).isEqualTo(8 * MiB);

      RecordedRequest p1 = server.takeRequest();
      assertThat(p1.getRequestLine())
            .isEqualTo("PUT /-/vaults/vault/multipart-uploads/upload-id HTTP/1.1");
      assertThat(Long.parseLong(p1.getHeader(HttpHeaders.CONTENT_LENGTH))).isEqualTo(8388608);
      assertThat(HashCode.fromString(p1.getHeader(TREE_HASH))).isEqualTo(hash8);

      for (int i = 0; i < 11; i++) {
         server.takeRequest();
      }

      RecordedRequest p13 = server.takeRequest();
      assertThat(p13.getRequestLine())
            .isEqualTo("PUT /-/vaults/vault/multipart-uploads/upload-id HTTP/1.1");
      assertThat(HashCode.fromString(p13.getHeader(TREE_HASH))).isEqualTo(hash4);
      assertThat(Long.parseLong(p13.getHeader(HttpHeaders.CONTENT_LENGTH))).isEqualTo(4194304);

      RecordedRequest complete = server.takeRequest();
      assertThat(complete.getRequestLine()).isEqualTo("POST /-/vaults/vault/multipart-uploads/upload-id HTTP/1.1");
      assertThat(HashCode.fromString(complete.getHeader(TREE_HASH))).isEqualTo(hcomp);
      assertThat(Long.parseLong(complete.getHeader(ARCHIVE_SIZE))).isEqualTo(100 * MiB);
   }

   @Test(expectedExceptions = HttpResponseException.class)
   public void testSequentialMPUAbort() throws InterruptedException {
      server.enqueue(new MockResponse().setResponseCode(201).addHeader(MULTIPART_UPLOAD_ID, "upload-id"));
      server.enqueue(new MockResponse().setResponseCode(204).addHeader(TREE_HASH, hash8));
      server.enqueue(new MockResponse().setResponseCode(404));
      server.enqueue(new MockResponse().setResponseCode(204));

      SequentialMultipartUploadStrategy strat = new SequentialMultipartUploadStrategy(client,
            new BaseSlicingStrategy(new BasePayloadSlicer()));

      try {
         strat.execute("vault", new BlobBuilderImpl().name("test").payload(buildPayload(100 * MiB)).build());
      } finally {
         server.takeRequest();
         server.takeRequest();
         server.takeRequest();
         RecordedRequest abort = server.takeRequest();
         assertThat(abort.getRequestLine()).isEqualTo("DELETE /-/vaults/vault/multipart-uploads/upload-id HTTP/1.1");
      }
   }

}
