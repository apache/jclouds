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
package org.jclouds.blobstore.integration.internal;

import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.md5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.blobstore.options.GetOptions.Builder.ifETagDoesntMatch;
import static org.jclouds.blobstore.options.GetOptions.Builder.ifETagMatches;
import static org.jclouds.blobstore.options.GetOptions.Builder.ifModifiedSince;
import static org.jclouds.blobstore.options.GetOptions.Builder.ifUnmodifiedSince;
import static org.jclouds.blobstore.options.GetOptions.Builder.range;
import static org.jclouds.concurrent.FutureIterables.awaitCompletion;
import static org.jclouds.io.ByteStreams2.hashAndClose;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Fail;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.Tier;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.strategy.internal.MultipartUploadChunkSizeCalculator;
import org.jclouds.crypto.Crypto;
import org.jclouds.encryption.internal.JCECrypto;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.ByteStreams2;
import org.jclouds.io.ContentMetadataBuilder;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.io.payloads.ByteSourcePayload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.logging.Logger;
import org.jclouds.util.Closeables2;
import org.jclouds.utils.TestUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.ListenableFuture;

public class BaseBlobIntegrationTest extends BaseBlobStoreIntegrationTest {
   private static final ByteSource oneHundredOneConstitutions = TestUtils.randomByteSource().slice(0, 101 * 45118);

   @BeforeClass(groups = { "integration", "live" }, dependsOnMethods = "setupContext")
   @Override
   public void setUpResourcesOnThisThread(ITestContext testContext) throws Exception {
      super.setUpResourcesOnThisThread(testContext);
   }

   public static ByteSource getTestDataSupplier() throws IOException {
      return oneHundredOneConstitutions;
   }

   /**
    * Attempt to capture the issue detailed in
    * http://groups.google.com/group/jclouds/browse_thread/thread/4a7c8d58530b287f
    */
   @Test(groups = { "integration", "live" })
   public void testPutBlobParallel() throws Exception {
      final ByteSource expected = createTestInput(32 * 1024);

      final String container = getContainerName();
      try {
         Map<Integer, ListenableFuture<?>> responses = Maps.newHashMap();
         for (int i = 0; i < 3; i++) {
            final String name = String.valueOf(i);
            responses.put(i, this.exec.submit(new Callable<Void>() {

               @Override
               public Void call() throws Exception {
                  Payload payload = Payloads.newByteSourcePayload(expected);
                  payload.getContentMetadata().setContentType("image/png");
                  Blob blob = view.getBlobStore().blobBuilder(name).payload(payload).contentLength(expected.size()).build();
                  view.getBlobStore().putBlob(container, blob);

                  assertConsistencyAwareBlobExists(container, name);

                  blob = view.getBlobStore().getBlob(container, name);
                  byte[] actual = ByteStreams2.toByteArrayAndClose(blob.getPayload().openStream());
                  assertThat(actual).isEqualTo(expected.read());

                  view.getBlobStore().removeBlob(container, name);
                  assertConsistencyAwareBlobDoesntExist(container, name);
                  return null;
               }

            }));
         }
         Map<Integer, Exception> exceptions = awaitCompletion(responses, exec, 30000L, Logger.CONSOLE,
                  "putFileParallel");
         assert exceptions.size() == 0 : exceptions;

      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testFileGetParallel() throws Exception {
      final ByteSource supplier = createTestInput(32 * 1024);
      final String container = getContainerName();
      try {
         final String name = "constitution.txt";

         uploadByteSource(container, name, supplier);
         Map<Integer, ListenableFuture<?>> responses = Maps.newHashMap();
         for (int i = 0; i < 10; i++) {
            responses.put(i, this.exec.submit(new Callable<Void>() {
               @Override public Void call() throws Exception {
                  try {
                     Blob blob = view.getBlobStore().getBlob(container, name);
                     validateMetadata(blob.getMetadata(), container, name);
                     assertEquals(hashAndClose(blob.getPayload().openStream(), md5()), supplier.hash(md5()));
                  } catch (IOException e) {
                     Throwables.propagate(e);
                  }
                  return null;
               }
            }));
         }
         Map<Integer, Exception> exceptions = awaitCompletion(responses, exec, 30000L, Logger.CONSOLE,
                  "get constitution");
         if (!exceptions.isEmpty()) {
            throw exceptions.values().iterator().next();
         }

      } finally {
         returnContainer(container);
      }

   }

   private void uploadByteSource(String container, String name, ByteSource byteSource) throws IOException {
      BlobStore blobStore = view.getBlobStore();
      blobStore.putBlob(container, blobStore.blobBuilder(name)
            .payload(new ByteSourcePayload(byteSource))
            .contentType("text/plain")
            .contentMD5(byteSource.hash(md5()))
            .contentLength(byteSource.size())
            .build());
   }

   @Test(groups = { "integration", "live" })
   public void testGetIfModifiedSince() throws InterruptedException {
      String container = getContainerName();
      try {
         String name = "apples";

         Date before = new Date(System.currentTimeMillis() - 1000);
         // first create the blob
         addObjectAndValidateContent(container, name);
         // now, modify it
         addObjectAndValidateContent(container, name);
         Date after = new Date(System.currentTimeMillis() + 1000);

         view.getBlobStore().getBlob(container, name, ifModifiedSince(before));
         validateContent(container, name);

         try {
            view.getBlobStore().getBlob(container, name, ifModifiedSince(after));
            validateContent(container, name);
         } catch (HttpResponseException ex) {
            assertEquals(ex.getResponse().getStatusCode(), 304);
         }
      } finally {
         returnContainer(container);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testOverwriteBlob() throws InterruptedException {
      String container = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      try {
         String blobName = "hello";
         Blob blob = blobStore.blobBuilder(blobName)
               .payload(TEST_STRING)
               .build();
         blobStore.putBlob(container, blob);
         Blob overwriteBlob = blobStore.blobBuilder(blobName)
               .payload("overwrite content")
               .build();
         blobStore.putBlob(container, overwriteBlob);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCreateBlobWithExpiry() throws InterruptedException {
      String container = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      try {
         final String blobName = "hello";
         final Date expires = new Date((System.currentTimeMillis() / 1000) * 1000 + 60 * 1000);

         blobStore.putBlob(container, blobStore.blobBuilder(blobName).payload(TEST_STRING).expires(expires).build());

         assertConsistencyAwareBlobExpiryMetadata(container, blobName, expires);

      } finally {
         returnContainer(container);
      }
   }

   private void putBlobWithMd5(Payload payload, long contentLength, HashCode contentMD5) throws InterruptedException, IOException {
      String container = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      try {
         String blobName = "putBlobWithMd5-" + new Random().nextLong();
         Blob blob = blobStore
            .blobBuilder(blobName)
            .payload(payload)
            .contentLength(contentLength)
            .contentMD5(contentMD5)
            .build();
         blobStore.putBlob(container, blob);
      } finally {
         returnContainer(container);
      }
   }

   protected int getIncorrectContentMD5StatusCode() {
      return 400;
   }

   @Test(groups = { "integration", "live" })
   public void testPutCorrectContentMD5ByteSource() throws InterruptedException, IOException {
      ByteSource payload = createTestInput(1024);
      HashCode contentMD5 = md5().hashBytes(payload.read());
      putBlobWithMd5(new ByteSourcePayload(payload), payload.size(), contentMD5);
   }

   @Test(groups = { "integration", "live" })
   public void testPutIncorrectContentMD5ByteSource() throws InterruptedException, IOException {
      ByteSource payload = createTestInput(1024);
      HashCode contentMD5 = md5().hashBytes(new byte[0]);
      try {
         putBlobWithMd5(new ByteSourcePayload(payload), payload.size(), contentMD5);
         fail();
      } catch (HttpResponseException hre) {
         if (hre.getResponse().getStatusCode() != getIncorrectContentMD5StatusCode()) {
            throw hre;
         }
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutCorrectContentMD5InputStream() throws InterruptedException, IOException {
      ByteSource payload = createTestInput(1024);
      HashCode contentMD5 = md5().hashBytes(payload.read());
      putBlobWithMd5(new InputStreamPayload(payload.openStream()), payload.size(), contentMD5);
   }

   @Test(groups = { "integration", "live" })
   public void testPutIncorrectContentMD5InputStream() throws InterruptedException, IOException {
      ByteSource payload = createTestInput(1024);
      HashCode contentMD5 = md5().hashBytes(new byte[0]);
      try {
         putBlobWithMd5(new InputStreamPayload(payload.openStream()), payload.size(), contentMD5);
         fail();
      } catch (HttpResponseException hre) {
         if (hre.getResponse().getStatusCode() != getIncorrectContentMD5StatusCode()) {
            throw hre;
         }
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetIfUnmodifiedSince() throws InterruptedException {
      String container = getContainerName();
      try {

         String name = "apples";

         Date before = new Date(System.currentTimeMillis() - 10000);
         addObjectAndValidateContent(container, name);
         Date after = new Date(System.currentTimeMillis() + 10000);

         awaitConsistency();
         view.getBlobStore().getBlob(container, name, ifUnmodifiedSince(after));
         validateContent(container, name);

         try {
            view.getBlobStore().getBlob(container, name, ifUnmodifiedSince(before));
            validateContent(container, name);
         } catch (HttpResponseException ex) {
            assertEquals(ex.getResponse().getStatusCode(), 412);
         }
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetIfMatch() throws InterruptedException {
      String container = getContainerName();
      try {

         String name = "apples";

         String goodETag = addObjectAndValidateContent(container, name);

         view.getBlobStore().getBlob(container, name, ifETagMatches(goodETag));
         validateContent(container, name);

         try {
            view.getBlobStore().getBlob(container, name, ifETagMatches("powerfrisbee"));
            validateContent(container, name);
         } catch (HttpResponseException ex) {
            assertEquals(ex.getResponse().getStatusCode(), 412);
         }
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetIfNoneMatch() throws InterruptedException {
      String container = getContainerName();
      try {

         String name = "apples";

         String goodETag = addObjectAndValidateContent(container, name);

         view.getBlobStore().getBlob(container, name, ifETagDoesntMatch("powerfrisbee"));
         validateContent(container, name);

         try {
            view.getBlobStore().getBlob(container, name, ifETagDoesntMatch(goodETag));
         } catch (HttpResponseException ex) {
            assertEquals(ex.getResponse().getStatusCode(), 304);
         }
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetRangeOutOfRange() throws InterruptedException, IOException {
      String container = getContainerName();
      try {
         String name = "apples";

         addObjectAndValidateContent(container, name);
         try {
            view.getBlobStore().getBlob(container, name, range(TEST_STRING.length(), TEST_STRING.length() + 1));
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
         } catch (HttpResponseException e) {
            assertThat(e.getResponse().getStatusCode()).isEqualTo(416);
         }
      } finally {
         returnContainer(container);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testGetRange() throws InterruptedException, IOException {
      String container = getContainerName();
      try {

         String name = "apples";

         addObjectAndValidateContent(container, name);
         Blob blob1 = view.getBlobStore().getBlob(container, name, range(0, 5));
         validateMetadata(blob1.getMetadata(), container, name);
         assertEquals(getContentAsStringOrNullAndClose(blob1), TEST_STRING.substring(0, 6));
         assertThat(blob1.getAllHeaders().get(HttpHeaders.CONTENT_RANGE)).containsExactly("bytes 0-5/46");

         Blob blob2 = view.getBlobStore().getBlob(container, name, range(6, TEST_STRING.length()));
         validateMetadata(blob2.getMetadata(), container, name);
         assertEquals(getContentAsStringOrNullAndClose(blob2), TEST_STRING.substring(6, TEST_STRING.length()));
         assertThat(blob2.getAllHeaders().get(HttpHeaders.CONTENT_RANGE)).containsExactly("bytes 6-45/46");

         /* RFC 2616 14.35.1
            "If the entity is shorter than the specified suffix-length, the
            entire entity-body is used." */
         Blob blob3 = view.getBlobStore().getBlob(container, name, new GetOptions().tail(TEST_STRING.length() + 10));
         validateMetadata(blob3.getMetadata(), container, name);
         assertEquals(getContentAsStringOrNullAndClose(blob3), TEST_STRING);
         // not all providers return Content-Range for non-partial responses
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetTwoRanges() throws InterruptedException, IOException {
      String container = getContainerName();
      try {

         String name = "apples";

         addObjectAndValidateContent(container, name);
         Blob blob = view.getBlobStore().getBlob(container, name, range(0, 5).range(6, TEST_STRING.length()));
         validateMetadata(blob.getMetadata(), container, name);
         assertEquals(getContentAsStringOrNullAndClose(blob), TEST_STRING);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testGetRangeMultipart() throws InterruptedException, IOException {
      String container = getContainerName();
      InputStream expect = null;
      InputStream actual = null;
      try {
         String name = "apples";
         long length = getMinimumMultipartBlobSize();
         ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
         Blob blob = view.getBlobStore().blobBuilder(name)
                 .payload(byteSource)
                 .contentLength(length)
                 .build();
         view.getBlobStore().putBlob(container, blob, new PutOptions().multipart(true));
         blob = view.getBlobStore().getBlob(container, name, range(0, 5));
         validateMetadata(blob.getMetadata(), container, name);
         expect = byteSource.slice(0, 6).openStream();
         actual = blob.getPayload().openStream();
         assertThat(actual).hasContentEqualTo(expect);
      } finally {
         Closeables2.closeQuietly(expect);
         Closeables2.closeQuietly(actual);
         returnContainer(container);
      }
   }

   private String addObjectAndValidateContent(String sourcecontainer, String sourceKey) throws InterruptedException {
      String eTag = addBlobToContainer(sourcecontainer, sourceKey);
      validateContent(sourcecontainer, sourceKey);
      awaitConsistency();
      return eTag;
   }

   @Test(groups = { "integration", "live" })
   public void deleteObjectNotFound() throws InterruptedException {
      String container = getContainerName();
      String name = "test";
      try {
         view.getBlobStore().removeBlob(container, name);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void blobNotFound() throws InterruptedException {
      String container = getContainerName();
      String name = "test";
      try {
         assert !view.getBlobStore().blobExists(container, name);
      } finally {
         returnContainer(container);
      }
   }

   @DataProvider(name = "delete")
   public Object[][] createData() {
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
         return new Object[][] { { "normal" }, { "sp ace" } };
      } else {
         return new Object[][] { { "normal" }, { "sp ace" }, { "qu?stion" }, { "unic₪de" }, { "path/foo" }, { "colon:" },
               { "asteri*k" }, { "quote\"" }, { "{great<r}" }, { "lesst>en" }, { "p|pe" } };
      }
   }

   @Test(groups = { "integration", "live" }, dataProvider = "delete")
   public void deleteObject(String name) throws InterruptedException {
      String container = getContainerName();
      try {
         addBlobToContainer(container, name, name, MediaType.TEXT_PLAIN);
         awaitConsistency();
         view.getBlobStore().removeBlob(container, name);
         awaitConsistency();
         assertContainerEmptyDeleting(container, name);
      } finally {
         returnContainer(container);
      }
   }

   private void assertContainerEmptyDeleting(String container, String name) {
      Iterable<? extends StorageMetadata> listing = Iterables.filter(view.getBlobStore().list(container),
               new Predicate<StorageMetadata>() {

                  @Override
                  public boolean apply(StorageMetadata input) {
                     return input.getType() == StorageType.BLOB;
                  }

               });
      assertEquals(Iterables.size(listing), 0, String.format(
               "deleting %s, we still have %s blobs left in container %s, using encoding %s", name, Iterables
                        .size(listing), container, LOCAL_ENCODING));
   }

   @Test(groups = { "integration", "live" })
   public void deleteObjectNoContainer() {
      try {
         view.getBlobStore().removeBlob("donb", "test");
      } catch (HttpResponseException e) {
         assertEquals(e.getResponse().getStatusCode(), 404);
      } catch (ContainerNotFoundException e) {
      }

   }

   @Test(groups = { "integration", "live" }, dataProvider = "delete")
   public void deleteMultipleObjects(String name) throws InterruptedException {
      String name2 = name + "2";
      String container = getContainerName();
      try {
         addBlobToContainer(container, name, name, MediaType.TEXT_PLAIN);
         addBlobToContainer(container, name2, name2, MediaType.TEXT_PLAIN);
         awaitConsistency();
         view.getBlobStore().removeBlobs(container, ImmutableSet.of(name, name2));
         awaitConsistency();
         assertContainerEmptyDeleting(container, name);
      } finally {
         returnContainer(container);
      }
   }

   @DataProvider(name = "putTests")
   public Object[][] createData1() throws IOException {
      File file = new File("pom.xml");
      String realObject = Files.toString(file, Charsets.UTF_8);

      return new Object[][] { { "file", "text/xml", file, realObject },
               { "string", "text/xml", realObject, realObject },
               { "bytes", "application/octet-stream", realObject.getBytes(), realObject } };
   }

   @Test(groups = { "integration", "live" }, dataProvider = "putTests")
   public void testPutObject(String name, String type, Object content, Object realObject) throws InterruptedException,
            IOException {
      PayloadBlobBuilder blobBuilder = view.getBlobStore().blobBuilder(name).payload(Payloads.newPayload(content))
               .contentType(type);
      addContentMetadata(blobBuilder);
      Blob blob = blobBuilder.build();
      String container = getContainerName();
      try {
         assertNotNull(view.getBlobStore().putBlob(container, blob));
         awaitConsistency();

         blob = view.getBlobStore().getBlob(container, blob.getMetadata().getName());
         validateMetadata(blob.getMetadata(), container, name);
         checkContentMetadata(blob);

         String returnedString = getContentAsStringOrNullAndClose(blob);
         assertEquals(returnedString, realObject);
         PageSet<? extends StorageMetadata> set = view.getBlobStore().list(container);
         assert set.size() == 1 : set;
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutObjectStream() throws InterruptedException, IOException, ExecutionException {
      PayloadBlobBuilder blobBuilder = view.getBlobStore().blobBuilder("streaming").payload(
               new ByteSourcePayload(ByteSource.wrap("foo".getBytes())));
      addContentMetadata(blobBuilder);

      Blob blob = blobBuilder.build();

      String container = getContainerName();
      try {

         assertNotNull(view.getBlobStore().putBlob(container, blob));
         awaitConsistency();

         blob = view.getBlobStore().getBlob(container, blob.getMetadata().getName());
         String returnedString = getContentAsStringOrNullAndClose(blob);
         assertEquals(returnedString, "foo");
         validateMetadata(blob.getMetadata(), container, blob.getMetadata().getName());
         checkContentMetadata(blob);
         PageSet<? extends StorageMetadata> set = view.getBlobStore().list(container);
         assert set.size() == 1 : set;
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutByteSource() throws Exception {
      long length = 42;
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new ByteSourcePayload(byteSource);
      testPut(payload, null, payload, length, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutInputStream() throws Exception {
      long length = 42;
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new InputStreamPayload(byteSource.openStream());
      testPut(payload, null, new ByteSourcePayload(byteSource), length, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutZeroLengthByteSource() throws Exception {
      long length = 0;
      // do not use ByteSource.empty() since it is backed by a
      // ByteArrayInputStream which supports reset
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new ByteSourcePayload(byteSource);
      testPut(payload, null, payload, length, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutZeroLengthInputStream() throws Exception {
      long length = 0;
      // do not use ByteSource.empty() since it is backed by a
      // ByteArrayInputStream which supports reset
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new InputStreamPayload(byteSource.openStream());
      testPut(payload, null, payload, length, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutMultipartByteSource() throws Exception {
      long length = Math.max(getMinimumMultipartBlobSize(), MultipartUploadChunkSizeCalculator.DEFAULT_PART_SIZE + 1);
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new ByteSourcePayload(byteSource);
      HashCode hashCode = byteSource.hash(Hashing.md5());
      testPut(payload, hashCode, payload, length, new PutOptions().multipart(true));
   }

   @Test(groups = { "integration", "live" })
   public void testPutMultipartInputStream() throws Exception {
      long length = Math.max(getMinimumMultipartBlobSize(), MultipartUploadChunkSizeCalculator.DEFAULT_PART_SIZE + 1);
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, length);
      Payload payload = new InputStreamPayload(byteSource.openStream());
      testPut(payload, null, new ByteSourcePayload(byteSource), length, new PutOptions().multipart(true));
   }

   @Test(groups = { "integration", "live" })
   public void testSetBlobAccess() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String containerName = getContainerName();
      String blobName = "set-access-blob-name";
      try {
         addBlobToContainer(containerName, blobName, blobName, MediaType.TEXT_PLAIN);

         assertThat(blobStore.getBlobAccess(containerName, blobName)).isEqualTo(BlobAccess.PRIVATE);

         blobStore.setBlobAccess(containerName, blobName, BlobAccess.PUBLIC_READ);
         assertThat(blobStore.getBlobAccess(containerName, blobName)).isEqualTo(BlobAccess.PUBLIC_READ);

         // test that blob is anonymously readable
         HttpRequest request = view.getSigner().signGetBlob(containerName, blobName).toBuilder()
                .replaceQueryParams(ImmutableMap.<String, String>of()).build();
         HttpResponse response = view.utils().http().invoke(request);
         assertThat(response.getStatusCode()).isEqualTo(200);

         blobStore.setBlobAccess(containerName, blobName, BlobAccess.PRIVATE);
         assertThat(blobStore.getBlobAccess(containerName, blobName)).isEqualTo(BlobAccess.PRIVATE);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobAccess() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String containerName = getContainerName();
      try {
         String blobNamePrivate = "put-access-blob-name-private";
         Blob blobPrivate = blobStore.blobBuilder(blobNamePrivate).payload(new byte[1]).build();
         blobStore.putBlob(containerName, blobPrivate, new PutOptions().setBlobAccess(BlobAccess.PRIVATE));
         assertThat(blobStore.getBlobAccess(containerName, blobNamePrivate)).isEqualTo(BlobAccess.PRIVATE);

         String blobNamePublic = "put-access-blob-name-public";
         Blob blobPublic = blobStore.blobBuilder(blobNamePublic).payload(new byte[1]).build();
         blobStore.putBlob(containerName, blobPublic, new PutOptions().setBlobAccess(BlobAccess.PUBLIC_READ));
         assertThat(blobStore.getBlobAccess(containerName, blobNamePublic)).isEqualTo(BlobAccess.PUBLIC_READ);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobAccessMultipart() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String containerName = getContainerName();
      ByteSource byteSource = TestUtils.randomByteSource().slice(0, getMinimumMultipartBlobSize());
      Payload payload = Payloads.newByteSourcePayload(byteSource);
      payload.getContentMetadata().setContentLength(byteSource.size());
      try {
         String blobNamePrivate = "put-access-blob-name-private";
         Blob blobPrivate = blobStore.blobBuilder(blobNamePrivate).payload(payload).build();
         blobStore.putBlob(containerName, blobPrivate, new PutOptions().setBlobAccess(BlobAccess.PRIVATE).multipart(true));
         assertThat(blobStore.getBlobAccess(containerName, blobNamePrivate)).isEqualTo(BlobAccess.PRIVATE);

         String blobNamePublic = "put-access-blob-name-public";
         Blob blobPublic = blobStore.blobBuilder(blobNamePublic).payload(payload).build();
         blobStore.putBlob(containerName, blobPublic, new PutOptions().setBlobAccess(BlobAccess.PUBLIC_READ).multipart(true));
         assertThat(blobStore.getBlobAccess(containerName, blobNamePublic)).isEqualTo(BlobAccess.PUBLIC_READ);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierStandard() throws Exception {
      testPutBlobTierHelper(Tier.STANDARD, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierInfrequent() throws Exception {
      testPutBlobTierHelper(Tier.INFREQUENT, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierArchive() throws Exception {
      testPutBlobTierHelper(Tier.ARCHIVE, new PutOptions());
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierStandardMultipart() throws Exception {
      testPutBlobTierHelper(Tier.STANDARD, new PutOptions().multipart(true));
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierInfrequentMultipart() throws Exception {
      testPutBlobTierHelper(Tier.INFREQUENT, new PutOptions().multipart(true));
   }

   @Test(groups = { "integration", "live" })
   public void testPutBlobTierArchiveMultipart() throws Exception {
      testPutBlobTierHelper(Tier.ARCHIVE, new PutOptions().multipart(true));
   }

   protected void testPutBlobTierHelper(Tier tier, PutOptions options) throws Exception {
      String blobName = "put-blob-tier-" + tier;
      ByteSource payload = createTestInput(1024);
      BlobStore blobStore = view.getBlobStore();
      String containerName = getContainerName();
      try {
         Blob blob = blobStore.blobBuilder(blobName)
            .payload(payload)
            .contentLength(payload.size())
            .tier(tier)
            .build();
         blobStore.putBlob(containerName, blob, options);
         checkTier(blobStore.blobMetadata(containerName, blobName), tier);
      } finally {
         returnContainer(containerName);
      }
   }

   protected void checkTier(BlobMetadata metadata, Tier expected) {
      assertThat(metadata.getTier()).isEqualTo(expected);
   }

   protected void checkUserMetadata(Map<String, String> userMetadata1, Map<String, String> userMetadata2) {
      assertThat(userMetadata1).isEqualTo(userMetadata2);
   }

   private void testPut(Payload payload, HashCode hashCode, Payload expectedPayload, long length, PutOptions options)
         throws IOException, InterruptedException {
      BlobStore blobStore = view.getBlobStore();
      String blobName = "multipart-upload";
      Map<String, String> userMetadata = ImmutableMap.of("key1", "value1", "key2", "value2");
      PayloadBlobBuilder blobBuilder = blobStore.blobBuilder(blobName)
            .userMetadata(userMetadata)
            .payload(payload)
            .contentLength(length);
      addContentMetadata(blobBuilder);
      if (hashCode != null) {
         blobBuilder.contentMD5(payload.getContentMetadata().getContentMD5AsHashCode());
      }

      String container = getContainerName();
      try {
         String etag = blobStore.putBlob(container, blobBuilder.build(), options);
         assertThat(etag).isNotNull();

         Blob blob = blobStore.getBlob(container, blobName);
         Long contentLength = blob.getMetadata().getContentMetadata().getContentLength();
         if (contentLength != null) {
            assertThat(contentLength).isEqualTo(length);
         }

         InputStream is = null;
         try {
            is = blob.getPayload().openStream();
            assertThat(is).hasContentEqualTo(expectedPayload.openStream());
         } finally {
            Closeables2.closeQuietly(is);
         }
         validateMetadata(blob.getMetadata(), container, blob.getMetadata().getName());
         checkContentMetadata(blob);
         checkUserMetadata(blob.getMetadata().getUserMetadata(), userMetadata);
      } finally {
         returnContainer(container);
      }
   }

   protected long getMinimumMultipartBlobSize() {
      throw new SkipException("multipart upload not supported");
   }

   protected void checkContentMetadata(Blob blob) {
      checkCacheControl(blob, "max-age=3600");
      checkContentType(blob, "text/csv");
      checkContentDisposition(blob, "attachment; filename=photo.jpg");
      checkContentEncoding(blob, "gzip");
      checkContentLanguage(blob, "en");
   }

   protected void addContentMetadata(PayloadBlobBuilder blobBuilder) {
      blobBuilder.cacheControl("max-age=3600");
      blobBuilder.contentType("text/csv");
      blobBuilder.contentDisposition("attachment; filename=photo.jpg");
      blobBuilder.contentEncoding("gzip");
      blobBuilder.contentLanguage("en");
   }

   protected void checkCacheControl(Blob blob, String cacheControl) {
      assertThat(blob.getPayload().getContentMetadata().getCacheControl()).isEqualTo(cacheControl);
      assertThat(blob.getMetadata().getContentMetadata().getCacheControl()).isEqualTo(cacheControl);
   }

   protected void checkContentType(Blob blob, String contentType) {
      assert blob.getPayload().getContentMetadata().getContentType().startsWith(contentType) : blob.getPayload()
               .getContentMetadata().getContentType();
      assert blob.getMetadata().getContentMetadata().getContentType().startsWith(contentType) : blob.getMetadata()
               .getContentMetadata().getContentType();
   }

   protected void checkContentDisposition(Blob blob, String contentDisposition) {
      assert blob.getPayload().getContentMetadata().getContentDisposition().startsWith(contentDisposition) : blob
               .getPayload().getContentMetadata().getContentDisposition();
      assert blob.getMetadata().getContentMetadata().getContentDisposition().startsWith(contentDisposition) : blob
               .getMetadata().getContentMetadata().getContentDisposition();

   }

   protected void checkContentEncoding(Blob blob, String contentEncoding) {
      assert blob.getPayload().getContentMetadata().getContentEncoding().indexOf(contentEncoding) != -1 : blob
               .getPayload().getContentMetadata().getContentEncoding();
      assert blob.getMetadata().getContentMetadata().getContentEncoding().indexOf(contentEncoding) != -1 : blob
               .getMetadata().getContentMetadata().getContentEncoding();
   }

   protected void checkContentLanguage(Blob blob, String contentLanguage) {
      assert blob.getPayload().getContentMetadata().getContentLanguage().startsWith(contentLanguage) : blob
               .getPayload().getContentMetadata().getContentLanguage();
      assert blob.getMetadata().getContentMetadata().getContentLanguage().startsWith(contentLanguage) : blob
               .getMetadata().getContentMetadata().getContentLanguage();
   }

   protected void checkMPUParts(Blob newBlob, List<MultipartPart> parts) {
   }

   protected static volatile Crypto crypto;
   static {
      try {
         crypto = new JCECrypto();
      } catch (NoSuchAlgorithmException e) {
         Throwables.propagate(e);
      } catch (CertificateException e) {
         Throwables.propagate(e);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testMetadata() throws InterruptedException, IOException {
      String name = "hello";
      // NOTE all metadata in jclouds comes out as lowercase, in an effort to
      // normalize the
      // providers.
      Blob blob = view.getBlobStore().blobBuilder(name).userMetadata(ImmutableMap.of("Adrian", "powderpuff"))
               .payload(TEST_STRING).contentType(MediaType.TEXT_PLAIN)
               .contentMD5(md5().hashString(TEST_STRING, Charsets.UTF_8).asBytes())
               .build();
      String container = getContainerName();
      try {
         assertNull(view.getBlobStore().blobMetadata(container, "powderpuff"));

         addBlobToContainer(container, blob);
         Blob newObject = validateContent(container, name);

         BlobMetadata metadata = newObject.getMetadata();

         validateMetadata(metadata);
         validateMetadata(metadata, container, name);
         validateMetadata(view.getBlobStore().blobMetadata(container, name));

         // write 2 items with the same name to ensure that provider doesn't
         // accept dupes
         blob.getMetadata().getUserMetadata().put("Adrian", "wonderpuff");
         blob.getMetadata().getUserMetadata().put("Adrian", "powderpuff");

         awaitConsistency();
         addBlobToContainer(container, blob);
         awaitConsistency();
         validateMetadata(view.getBlobStore().blobMetadata(container, name));

      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyBlobCopyMetadata() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Map<String, String> userMetadata = ImmutableMap.of("key1", "value1", "key2", "value2");
      PayloadBlobBuilder blobBuilder = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size());
      addContentMetadata(blobBuilder);
      blobBuilder.userMetadata(userMetadata);
      Blob blob = blobBuilder.build();

      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.NONE);
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
         checkContentMetadata(toBlob);
         checkUserMetadata(toBlob.getMetadata().getUserMetadata(), userMetadata);
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyBlobReplaceMetadata() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      PayloadBlobBuilder blobBuilder = blobStore
            .blobBuilder(fromName)
            .userMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
            .payload(payload)
            .cacheControl("max-age=1800")
            .contentLength(payload.size())
            .contentDisposition("attachment; filename=original.jpg")
            .contentEncoding("compress")
            .contentLanguage("fr")
            .contentType("audio/ogg");
      Blob blob = blobBuilder.build();

      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         Map<String, String> userMetadata = ImmutableMap.of("key3", "value3", "key4", "value4");
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder()
               .contentMetadata(ContentMetadataBuilder.create()
                     .cacheControl("max-age=3600")
                     .contentType("text/csv")
                     .contentDisposition("attachment; filename=photo.jpg")
                     .contentEncoding("gzip")
                     .contentLanguage("en")
                     .build())
               .userMetadata(userMetadata)
               .build());
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
         checkContentMetadata(toBlob);
         checkUserMetadata(toBlob.getMetadata().getUserMetadata(), userMetadata);
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfMatch() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         String eTag = blobStore.putBlob(fromContainer, blob);
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifMatch(eTag).build());
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfMatchNegative() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         try {
            blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifMatch("fake-etag").build());
            Fail.failBecauseExceptionWasNotThrown(HttpResponseException.class);
         } catch (HttpResponseException hre) {
            assertThat(hre.getResponse().getStatusCode()).isEqualTo(412);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfNoneMatch() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifNoneMatch("fake-etag").build());
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfNoneMatchNegative() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         String eTag = blobStore.putBlob(fromContainer, blob);
         try {
            blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifNoneMatch(eTag).build());
            Fail.failBecauseExceptionWasNotThrown(HttpResponseException.class);
         } catch (HttpResponseException hre) {
            assertThat(hre.getResponse().getStatusCode()).isEqualTo(412);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfModifiedSince() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         Date before = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifModifiedSince(before).build());
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfModifiedSinceNegative() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         // TODO: some problem with S3 and times in the future?
         Date after = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
         try {
            blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifModifiedSince(after).build());
            Fail.failBecauseExceptionWasNotThrown(HttpResponseException.class);
         } catch (HttpResponseException hre) {
            // most object stores return 412 but swift returns 304
            assertThat(hre.getResponse().getStatusCode()).isIn(304, 412);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfUnmodifiedSince() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         Date after = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
         blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifUnmodifiedSince(after).build());
         Blob toBlob = blobStore.getBlob(toContainer, toName);
         InputStream is = null;
         try {
            is = toBlob.getPayload().openStream();
            assertEquals(ByteStreams.toByteArray(is), payload.read());
         } finally {
            Closeables2.closeQuietly(is);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testCopyIfUnmodifiedSinceNegative() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String fromName = "source";
      String toName = "to";
      ByteSource payload = TestUtils.randomByteSource().slice(0, 1024);
      Blob blob = blobStore
            .blobBuilder(fromName)
            .payload(payload)
            .contentLength(payload.size())
            .build();
      String fromContainer = getContainerName();
      String toContainer = getContainerName();
      try {
         blobStore.putBlob(fromContainer, blob);
         Date before = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
         try {
            blobStore.copyBlob(fromContainer, fromName, toContainer, toName, CopyOptions.builder().ifUnmodifiedSince(before).build());
            Fail.failBecauseExceptionWasNotThrown(HttpResponseException.class);
         } catch (HttpResponseException hre) {
            assertThat(hre.getResponse().getStatusCode()).isEqualTo(412);
         }
      } finally {
         returnContainer(toContainer);
         returnContainer(fromContainer);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testMultipartUploadNoPartsAbort() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         String name = "blob-name";
         Blob blob = blobStore.blobBuilder(name).build();
         MultipartUpload mpu = blobStore.initiateMultipartUpload(container, blob.getMetadata(), new PutOptions());

         List<MultipartPart> parts = blobStore.listMultipartUpload(mpu);
         assertThat(parts).isEqualTo(ImmutableList.of());

         blobStore.abortMultipartUpload(mpu);

         assertThat(blobStore.list(container)).isEmpty();

         blob = blobStore.getBlob(container, name);
         assertThat(blob).isNull();
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testMultipartUploadOnePartAbort() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         String name = "blob-name";
         Blob blob = blobStore.blobBuilder(name).build();
         MultipartUpload mpu = blobStore.initiateMultipartUpload(container, blob.getMetadata(), new PutOptions());

         ByteSource byteSource = TestUtils.randomByteSource().slice(0, 1);
         Payload payload = Payloads.newByteSourcePayload(byteSource);
         payload.getContentMetadata().setContentLength(byteSource.size());
         MultipartPart part = blobStore.uploadMultipartPart(mpu, 1, payload);

         blobStore.abortMultipartUpload(mpu);

         assertThat(blobStore.list(container)).isEmpty();

         blob = blobStore.getBlob(container, name);
         assertThat(blob).isNull();
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testMultipartUploadSinglePart() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         String name = "blob-name";
         PayloadBlobBuilder blobBuilder = blobStore.blobBuilder(name)
               .userMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
               // TODO: fake payload to add content metadata
               .payload(new byte[0]);
         addContentMetadata(blobBuilder);
         Blob blob = blobBuilder.build();
         MultipartUpload mpu = blobStore.initiateMultipartUpload(container, blob.getMetadata(), new PutOptions());

         ByteSource byteSource = TestUtils.randomByteSource().slice(0, 1);
         Payload payload = Payloads.newByteSourcePayload(byteSource);
         payload.getContentMetadata().setContentLength(byteSource.size());
         MultipartPart part = blobStore.uploadMultipartPart(mpu, 1, payload);

         List<MultipartPart> parts = blobStore.listMultipartUpload(mpu);
         assertThat(parts).hasSize(1);
         assertThat(parts.get(0).partNumber()).isEqualTo(part.partNumber());
         assertThat(parts.get(0).partSize()).isEqualTo(part.partSize());
         assertThat(parts.get(0).partETag()).isEqualTo(part.partETag());
         // not checking lastModified since B2, S3, and Swift do not return it from uploadMultipartPart

         blobStore.completeMultipartUpload(mpu, ImmutableList.of(part));

         Blob newBlob = blobStore.getBlob(container, name);
         assertThat(newBlob).isNotNull();
         assertThat(ByteStreams2.toByteArrayAndClose(newBlob.getPayload().openStream())).isEqualTo(byteSource.read());
         checkContentMetadata(newBlob);
         checkUserMetadata(newBlob.getMetadata().getUserMetadata(), blob.getMetadata().getUserMetadata());
         checkMPUParts(newBlob, parts);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testMultipartUploadMultipleParts() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         String name = "blob-name";
         PayloadBlobBuilder blobBuilder = blobStore.blobBuilder(name)
               .userMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
               // TODO: fake payload to add content metadata
               .payload(new byte[0]);
         addContentMetadata(blobBuilder);
         Blob blob = blobBuilder.build();
         MultipartUpload mpu = blobStore.initiateMultipartUpload(container, blob.getMetadata(), new PutOptions());

         ByteSource byteSource = TestUtils.randomByteSource().slice(0, blobStore.getMinimumMultipartPartSize() + 1);
         ByteSource byteSource1 = byteSource.slice(0, blobStore.getMinimumMultipartPartSize());
         ByteSource byteSource2 = byteSource.slice(blobStore.getMinimumMultipartPartSize(), 1);
         Payload payload1 = Payloads.newByteSourcePayload(byteSource1);
         Payload payload2 = Payloads.newByteSourcePayload(byteSource2);
         payload1.getContentMetadata().setContentLength(byteSource1.size());
         payload2.getContentMetadata().setContentLength(byteSource2.size());
         MultipartPart part1 = blobStore.uploadMultipartPart(mpu, 1, payload1);
         MultipartPart part2 = blobStore.uploadMultipartPart(mpu, 2, payload2);

         List<MultipartPart> parts = blobStore.listMultipartUpload(mpu);
         assertThat(parts).hasSize(2);
         assertThat(parts.get(0).partNumber()).isEqualTo(part1.partNumber());
         assertThat(parts.get(0).partSize()).isEqualTo(part1.partSize());
         assertThat(parts.get(0).partETag()).isEqualTo(part1.partETag());
         assertThat(parts.get(1).partNumber()).isEqualTo(part2.partNumber());
         assertThat(parts.get(1).partSize()).isEqualTo(part2.partSize());
         assertThat(parts.get(1).partETag()).isEqualTo(part2.partETag());
         // not checking lastModified since B2, S3, and Swift do not return it from uploadMultipartPart

         blobStore.completeMultipartUpload(mpu, ImmutableList.of(part1, part2));

         Blob newBlob = blobStore.getBlob(container, name);
         assertThat(ByteStreams2.toByteArrayAndClose(newBlob.getPayload().openStream())).isEqualTo(byteSource.read());
         checkContentMetadata(newBlob);
         checkUserMetadata(newBlob.getMetadata().getUserMetadata(), blob.getMetadata().getUserMetadata());
         checkMPUParts(newBlob, parts);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testListMultipartUploads() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String name = "blob-name";
      PayloadBlobBuilder blobBuilder = blobStore.blobBuilder(name)
            .userMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
            // TODO: fake payload to add content metadata
            .payload(new byte[0]);
      addContentMetadata(blobBuilder);
      Blob blob = blobBuilder.build();
      MultipartUpload mpu = null;

      String container = getContainerName();
      try {
         List<MultipartUpload> uploads = blobStore.listMultipartUploads(container);
         assertThat(uploads).isEmpty();

         mpu = blobStore.initiateMultipartUpload(container, blob.getMetadata(), new PutOptions());

         // some providers like Azure cannot list an MPU until the first blob is uploaded
         assertThat(uploads.size()).isBetween(0, 1);

         // B2 requires at least two parts to call complete
         ByteSource byteSource = TestUtils.randomByteSource().slice(0, blobStore.getMinimumMultipartPartSize() + 1);
         ByteSource byteSource1 = byteSource.slice(0, blobStore.getMinimumMultipartPartSize());
         ByteSource byteSource2 = byteSource.slice(blobStore.getMinimumMultipartPartSize(), 1);
         Payload payload1 = Payloads.newByteSourcePayload(byteSource1);
         Payload payload2 = Payloads.newByteSourcePayload(byteSource2);
         payload1.getContentMetadata().setContentLength(byteSource1.size());
         payload2.getContentMetadata().setContentLength(byteSource2.size());
         MultipartPart part1 = blobStore.uploadMultipartPart(mpu, 1, payload1);
         MultipartPart part2 = blobStore.uploadMultipartPart(mpu, 2, payload2);

         uploads = blobStore.listMultipartUploads(container);
         assertThat(uploads).hasSize(1);

         blobStore.completeMultipartUpload(mpu, ImmutableList.of(part1, part2));
         mpu = null;

         uploads = blobStore.listMultipartUploads(container);
         assertThat(uploads).isEmpty();

         // cannot test abort since Azure does not have explicit support
      } finally {
         if (mpu != null) {
            blobStore.abortMultipartUpload(mpu);
         }
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" }, expectedExceptions = {KeyNotFoundException.class})
   public void testCopy404BlobFail() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         blobStore.copyBlob(container, "blob", container, "blob2", CopyOptions.NONE);
      } finally {
         returnContainer(container);
      }
   }

   @Test(groups = { "integration", "live" }, expectedExceptions = {KeyNotFoundException.class})
   public void testCopy404BlobMetaFail() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String container = getContainerName();
      try {
         blobStore.copyBlob(container, "blob", container, "blob2",
               CopyOptions.builder().userMetadata(ImmutableMap.of("x", "1")).build());
      } finally {
         returnContainer(container);
      }
   }

   protected void validateMetadata(BlobMetadata metadata) throws IOException {
      assert metadata.getContentMetadata().getContentType().startsWith("text/plain") : metadata.getContentMetadata()
               .getContentType();
      assertEquals(metadata.getContentMetadata().getContentLength(), Long.valueOf(TEST_STRING.length()));
      assertEquals(metadata.getUserMetadata().get("adrian"), "powderpuff");
      checkMD5(metadata);
   }

   protected void checkMD5(BlobMetadata metadata) throws IOException {
      assertEquals(metadata.getContentMetadata().getContentMD5(), md5().hashString(TEST_STRING, UTF_8).asBytes());
   }

   /** @return ByteSource containing a random length 0..length of random bytes. */
   private static ByteSource createTestInput(int length) {
      return TestUtils.randomByteSource().slice(0, new Random().nextInt(length));
   }
}
