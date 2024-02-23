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
package org.jclouds.filesystem.integration;

import static org.jclouds.blobstore.options.ListContainerOptions.Builder.maxResults;
import static org.jclouds.filesystem.util.Utils.isMacOSX;
import static org.testng.Assert.assertEquals;
import static org.jclouds.utils.TestUtils.NO_INVOCATIONS;
import static org.jclouds.utils.TestUtils.SINGLE_NO_ARG_INVOCATION;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableSet;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.integration.internal.BaseBlobStoreIntegrationTest;
import org.jclouds.blobstore.integration.internal.BaseContainerIntegrationTest;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.filesystem.utils.TestUtils;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@Test(groups = { "integration", "live" }, testName = "blobstore.FilesystemContainerIntegrationTest")
public class FilesystemContainerIntegrationTest extends BaseContainerIntegrationTest {
   public FilesystemContainerIntegrationTest() {
      provider = "filesystem";
      BaseBlobStoreIntegrationTest.SANITY_CHECK_RETURNED_BUCKET_NAME = true;
   }

   @Override
   protected Properties setupProperties() {
      Properties props = super.setupProperties();
      props.setProperty(FilesystemConstants.PROPERTY_BASEDIR, TestUtils.TARGET_BASE_DIR);
      return props;
   }

   @Test(dataProvider = "ignoreOnWindows", groups = { "integration", "live" })
   public void testNotWithDetails() throws InterruptedException {
      String key = "hello";

      // NOTE all metadata in jclouds comes out as lowercase, in an effort to
      // normalize the
      // providers.
      Blob object = view.getBlobStore().blobBuilder(key).userMetadata(ImmutableMap.of("Adrian", "powderpuff"))
            .payload(TEST_STRING).contentType(MediaType.TEXT_PLAIN).build();
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName, object);
         validateContent(containerName, key);

         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName, maxResults(1));

         BlobMetadata metadata = (BlobMetadata) Iterables.getOnlyElement(container);
         // transient container should be lenient and not return metadata on
         // undetailed listing.

         assertEquals(metadata.getUserMetadata().size(), 0);

      } finally {
         returnContainer(containerName);
      }
   }

   // Mac OS X HFS+ does not support UserDefinedFileAttributeView:
   // https://bugs.openjdk.java.net/browse/JDK-8030048
   @Test(dataProvider = "ignoreOnMacOSX")
   @Override
   public void testWithDetails() throws InterruptedException, IOException {
      super.testWithDetails();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void containerExists() throws InterruptedException {
      super.containerExists();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void deleteContainerWithContents() throws InterruptedException {
      super.deleteContainerWithContents();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void deleteContainerWithoutContents() throws InterruptedException {
      super.deleteContainerWithoutContents();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void deleteContainerIfEmptyWithContents() throws InterruptedException {
      super.deleteContainerIfEmptyWithContents();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void deleteContainerIfEmptyWithoutContents() throws InterruptedException {
      super.deleteContainerIfEmptyWithoutContents();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testListContainer() throws InterruptedException, ExecutionException, TimeoutException {
      super.testListContainer();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testListContainerMarker() throws InterruptedException {
      super.testListContainerMarker();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testListContainerPrefix() throws InterruptedException {
      super.testListContainerPrefix();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testListRootUsesDelimiter() throws InterruptedException {
      super.testListRootUsesDelimiter();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testPutTwiceIsOkAndDoesntOverwrite() throws InterruptedException {
      super.testPutTwiceIsOkAndDoesntOverwrite();
   }

   @Override
   @Test(dataProvider = "ignoreOnWindows")
   public void testListContainerMaxResults() throws InterruptedException {
      super.testListContainerMaxResults();
   }

   @Override
   public void testDirectory() {
      throw new SkipException("There is no notion of marker blobs in the file system blob store");
   }

   @DataProvider
   public Object[][] ignoreOnMacOSX() {
      return isMacOSX() ? NO_INVOCATIONS
            : SINGLE_NO_ARG_INVOCATION;
   }

   @DataProvider
   public Object[][] ignoreOnWindows() {
      return TestUtils.isWindowsOs() ? NO_INVOCATIONS
            : SINGLE_NO_ARG_INVOCATION;
   }

   @Override
   @DataProvider
   public Object[][] getBlobsToEscape() {
      if (TestUtils.isWindowsOs()) {
         Object[][] result = new Object[1][1];
         result[0][0] = ImmutableSet.of("%20", " %20");
         return result;
      }
      return super.getBlobsToEscape();
   }

   @Override
   @Test(groups = { "integration", "live" })
   public void testSetContainerAccess() throws Exception {
      throw new SkipException("filesystem does not support anonymous access");
   }

}
