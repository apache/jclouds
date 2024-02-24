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
package org.jclouds.filesystem.strategy.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.filesystem.util.Utils.isMacOSX;
import static org.jclouds.utils.TestUtils.NO_INVOCATIONS;
import static org.jclouds.utils.TestUtils.SINGLE_NO_ARG_INVOCATION;
import static org.jclouds.utils.TestUtils.randomByteSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Provider;

import org.assertj.core.api.Fail;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.internal.BlobBuilderImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Location;
import org.jclouds.filesystem.predicates.validators.internal.FilesystemBlobKeyValidatorImpl;
import org.jclouds.filesystem.predicates.validators.internal.FilesystemContainerNameValidatorImpl;
import org.jclouds.filesystem.utils.TestUtils;
import org.jclouds.io.payloads.FilePayload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.util.Throwables2;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Test class for {@link FilesystemStorageStrategyImpl } class
 */
@Test(groups = "unit", testName = "filesystem.FilesystemBlobUtilsTest", singleThreaded = true)
public class FilesystemStorageStrategyImplTest {
   private static final String CONTAINER_NAME = "funambol-test";
   private static final String TARGET_CONTAINER_NAME = TestUtils.TARGET_BASE_DIR + CONTAINER_NAME;

   private static final String LOGGING_CONFIG_KEY = "java.util.logging.config.file";
   private static final String LOGGING_CONFIG_VALUE = "src/main/resources/logging.properties";

   private static final String FS = File.separator;
   private static final Supplier<Location> defaultLocation = new Supplier<Location>() {
      @Override
      public Location get() {
         return null;
      }
   };

   static {
      System.setProperty(LOGGING_CONFIG_KEY, LOGGING_CONFIG_VALUE);
   }

   private FilesystemStorageStrategyImpl storageStrategy;

   @BeforeMethod
   protected void setUp() throws Exception {
      storageStrategy = new FilesystemStorageStrategyImpl(new Provider<BlobBuilder>() {
         @Override
         public BlobBuilder get() {
            return new BlobBuilderImpl();
         }

      }, TestUtils.TARGET_BASE_DIR, false, new FilesystemContainerNameValidatorImpl(), new FilesystemBlobKeyValidatorImpl(), defaultLocation);
      TestUtils.cleanDirectoryContent(TestUtils.TARGET_BASE_DIR);
      TestUtils.createResources();
   }

   @AfterMethod
   protected void tearDown() throws IOException {
      TestUtils.cleanDirectoryContent(TestUtils.TARGET_BASE_DIR);
   }

   public void testCreateDirectory() {
      storageStrategy.createDirectory(CONTAINER_NAME, null);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);

      storageStrategy.createDirectory(CONTAINER_NAME, "subdir");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "subdir", true);

      storageStrategy.createDirectory(CONTAINER_NAME, "subdir1" + FS);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "subdir1", true);

      storageStrategy.createDirectory(CONTAINER_NAME, FS + "subdir2");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "subdir2", true);

      storageStrategy.createDirectory(CONTAINER_NAME, "subdir3" + FS + "subdir4");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "subdir2", true);
   }

   public void testCreateDirectory_DirectoryAlreadyExists() {
      storageStrategy.createDirectory(CONTAINER_NAME, null);
      storageStrategy.createDirectory(CONTAINER_NAME, null);
   }

   public void testCreateContainer() {
      boolean result;

      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      result = storageStrategy.createContainer(CONTAINER_NAME);
      assertTrue(result, "Container not created");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);
   }

   public void testCreateContainerAccess() {
      boolean result;

      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      result = storageStrategy.createContainer(CONTAINER_NAME);
      assertTrue(result, "Container not created");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);

      storageStrategy.setContainerAccess(CONTAINER_NAME, ContainerAccess.PRIVATE);
      assertEquals(storageStrategy.getContainerAccess(CONTAINER_NAME), ContainerAccess.PRIVATE);
      storageStrategy.setContainerAccess(CONTAINER_NAME, ContainerAccess.PUBLIC_READ);
      assertEquals(storageStrategy.getContainerAccess(CONTAINER_NAME), ContainerAccess.PUBLIC_READ);
   }

   public void testCreateContainer_ContainerAlreadyExists() {
      boolean result;

      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      result = storageStrategy.createContainer(CONTAINER_NAME);
      assertTrue(result, "Container not created");
      result = storageStrategy.createContainer(CONTAINER_NAME);
      assertFalse(result, "Container not created");
   }

   public void testDeleteDirectory() throws IOException {
      TestUtils.createContainerAsDirectory(CONTAINER_NAME);
      TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("lev1" + FS + "lev2" + FS + "lev3" + FS, ".txt"),
               TestUtils.createRandomBlobKey("lev1" + FS + "lev2" + FS + "lev4" + FS, ".jpg") });

      // delete directory in different ways
      storageStrategy.deleteDirectory(CONTAINER_NAME, "lev1" + FS + "lev2" + FS + "lev4");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "lev1" + FS + "lev2" + FS + "lev4", false);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "lev1" + FS + "lev2", true);

      storageStrategy.deleteDirectory(CONTAINER_NAME, "lev1" + FS + "lev2" + FS + "lev3" + FS);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "lev1" + FS + "lev2" + FS + "lev3", false);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "lev1" + FS + "lev2", true);

      storageStrategy.deleteDirectory(CONTAINER_NAME, FS + "lev1");
      TestUtils.directoryExists(TARGET_CONTAINER_NAME + FS + "lev1", false);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);

      // delete the directory and all the files inside
      TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("lev1" + FS + "lev2" + FS + "lev3" + FS, ".txt"),
               TestUtils.createRandomBlobKey("lev1" + FS + "lev2" + FS + "lev4" + FS, ".jpg") });
      storageStrategy.deleteDirectory(CONTAINER_NAME, null);
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
   }

   public void testDirectoryExists() throws IOException {
      final String SUBDIRECTORY_NAME = "ad" + FS + "sda" + FS + "asd";
      boolean result;

      result = storageStrategy.directoryExists(CONTAINER_NAME, null);
      assertFalse(result, "Directory exist");

      // create the container
      TestUtils.createContainerAsDirectory(CONTAINER_NAME);
      // check if exists
      result = storageStrategy.directoryExists(CONTAINER_NAME, null);
      assertTrue(result, "Directory doesn't exist");
      result = storageStrategy.directoryExists(CONTAINER_NAME + FS, null);
      assertTrue(result, "Directory doesn't exist");

      result = storageStrategy.directoryExists(CONTAINER_NAME, SUBDIRECTORY_NAME);
      assertFalse(result, "Directory exist");

      // create subdirs inside the container
      TestUtils.createContainerAsDirectory(CONTAINER_NAME + FS + SUBDIRECTORY_NAME);
      // check if exists
      result = storageStrategy.directoryExists(CONTAINER_NAME, SUBDIRECTORY_NAME);
      assertTrue(result, "Directory doesn't exist");
      result = storageStrategy.directoryExists(CONTAINER_NAME, FS + SUBDIRECTORY_NAME);
      assertTrue(result, "Directory doesn't exist");
      result = storageStrategy.directoryExists(CONTAINER_NAME, SUBDIRECTORY_NAME + FS);
      assertTrue(result, "Directory doesn't exist");
      result = storageStrategy.directoryExists(CONTAINER_NAME + FS, FS + SUBDIRECTORY_NAME);
      assertTrue(result, "Directory doesn't exist");

   }

   public void testClearContainer() throws IOException {
      storageStrategy.createContainer(CONTAINER_NAME);
      Set<String> blobs = TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("clean_container-", ".jpg"),
               TestUtils.createRandomBlobKey("bf" + FS + "sd" + FS + "as" + FS + "clean_container-", ".jpg") });
      // test if file exits
      for (String blob : blobs) {
         TestUtils.fileExists(TARGET_CONTAINER_NAME + FS + blob, true);
      }

      // clear the container
      storageStrategy.clearContainer(CONTAINER_NAME);
      // test if container still exits
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);
      // test if file was cleared
      for (String blob : blobs) {
         TestUtils.fileExists(TARGET_CONTAINER_NAME + FS + blob, false);
      }
   }

   public void testClearContainer_NotExistingContainer() throws IOException {
      // test if container still exits
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      // clear the container
      storageStrategy.clearContainer(CONTAINER_NAME);
      // test if container still exits
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
   }

   public void testClearContainerAndThenDeleteContainer() throws IOException {
      storageStrategy.createContainer(CONTAINER_NAME);
      Set<String> blobs = TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("clean_container-", ".jpg"),
               TestUtils.createRandomBlobKey("bf" + FS + "sd" + FS + "as" + FS + "clean_container-", ".jpg") });
      // test if file exits
      for (String blob : blobs) {
         TestUtils.fileExists(TARGET_CONTAINER_NAME + FS + blob, true);
      }

      // clear the container
      storageStrategy.clearContainer(CONTAINER_NAME);
      // test if container still exits
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, true);
      // test if file was cleared
      for (String blob : blobs) {
         TestUtils.fileExists(TARGET_CONTAINER_NAME + FS + blob, false);
      }

      // delete the container
      storageStrategy.deleteContainer(CONTAINER_NAME);
      // test if container still exits
      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      assertFalse(storageStrategy.containerExists(CONTAINER_NAME), "Container still exists");
   }

   public void testDeleteContainer() throws IOException {
      final String BLOB_KEY1 = "blobName.jpg";
      final String BLOB_KEY2 = "aa" + FS + "bb" + FS + "cc" + FS + "dd" + FS + "ee" + FS + "ff" + FS + "23" + FS
               + "blobName.jpg";
      boolean result;

      result = storageStrategy.createContainer(CONTAINER_NAME);

      // put data inside the container
      TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] { BLOB_KEY1, BLOB_KEY2 });

      storageStrategy.deleteContainer(CONTAINER_NAME);
      assertTrue(result, "Cannot delete container");
      TestUtils.directoryExists(CONTAINER_NAME, false);
   }

   public void testDeleteContainer_EmptyContainer() {
      boolean result;

      result = storageStrategy.createContainer(CONTAINER_NAME);
      assertTrue(result, "Cannot create container");

      storageStrategy.deleteContainer(CONTAINER_NAME);
      TestUtils.directoryExists(CONTAINER_NAME, false);
   }

   public void testDeleteContainerNoErrorWhenNotExists() {
      storageStrategy.deleteContainer(CONTAINER_NAME);
   }

   public void testGetAllContainerNames() {
      Iterable<String> resultList;

      // no container
      resultList = storageStrategy.getAllContainerNames();
      assertNotNull(resultList, "Result is null");
      assertFalse(resultList.iterator().hasNext(), "Containers detected");

      // create containers
      storageStrategy.createContainer(CONTAINER_NAME + "1");
      storageStrategy.createContainer(CONTAINER_NAME + "2");
      storageStrategy.createContainer(CONTAINER_NAME + "3");

      List<String> containers = Lists.newArrayList();
      resultList = storageStrategy.getAllContainerNames();
      Iterator<String> containersIterator = resultList.iterator();
      while (containersIterator.hasNext()) {
         containers.add(containersIterator.next());
      }
      assertEquals(containers.size(), 3, "Different containers number");
      assertTrue(containers.contains(CONTAINER_NAME + "1"), "Containers doesn't exist");
      assertTrue(containers.contains(CONTAINER_NAME + "2"), "Containers doesn't exist");
      assertTrue(containers.contains(CONTAINER_NAME + "3"), "Containers doesn't exist");
   }

   public void testContainerExists() {
      boolean result;

      TestUtils.directoryExists(TARGET_CONTAINER_NAME, false);
      result = storageStrategy.containerExists(CONTAINER_NAME);
      assertFalse(result, "Container exists");
      storageStrategy.createContainer(CONTAINER_NAME);
      result = storageStrategy.containerExists(CONTAINER_NAME);
      assertTrue(result, "Container exists");
   }

   public void testNewBlob() {
      String blobKey;
      Blob newBlob;

      blobKey = TestUtils.createRandomBlobKey("blobtest-", ".txt");
      newBlob = storageStrategy.newBlob(blobKey);
      assertNotNull(newBlob, "Created blob was null");
      assertNotNull(newBlob.getMetadata(), "Created blob metadata were null");
      assertEquals(newBlob.getMetadata().getName(), blobKey, "Created blob name is different");

      blobKey = TestUtils.createRandomBlobKey("blobtest-", "");
      newBlob = storageStrategy.newBlob(blobKey);
      assertEquals(newBlob.getMetadata().getName(), blobKey, "Created blob name is different");

      blobKey = TestUtils.createRandomBlobKey("asd" + FS + "asd" + FS + "asdasd" + FS + "afadsf-", "");
      newBlob = storageStrategy.newBlob(blobKey);
      assertEquals(newBlob.getMetadata().getName(), blobKey, "Created blob name is different");
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testWriteDirectoryBlob() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("a/b/c/directory-", "/");
      Blob blob = storageStrategy.newBlob(blobKey);
      storageStrategy.putBlob(CONTAINER_NAME, blob);
      // verify that the files is equal
      File blobFullPath = new File(TARGET_CONTAINER_NAME, blobKey);
      assertTrue(blobFullPath.isDirectory());

      assertTrue(storageStrategy.blobExists(CONTAINER_NAME, blobKey));
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testGetDirectoryBlob() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("a/b/c/directory-", "/");
      Blob blob = storageStrategy.newBlob(blobKey);
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      assertTrue(storageStrategy.blobExists(CONTAINER_NAME, blobKey));

      blob = storageStrategy.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getName(), blobKey, "Created blob name is different");

      assertTrue(!storageStrategy.blobExists(CONTAINER_NAME,
              blobKey.substring(0, blobKey.length() - 1)));
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testGetBlobContentType_AutoDetect_True() throws IOException {
      FilesystemStorageStrategyImpl storageStrategyAutoDetectContentType = new FilesystemStorageStrategyImpl(
          new Provider<BlobBuilder>() {
             @Override
             public BlobBuilder get() {
                return new BlobBuilderImpl();
             }
          }, TestUtils.TARGET_BASE_DIR, true, new FilesystemContainerNameValidatorImpl(), new FilesystemBlobKeyValidatorImpl(), defaultLocation);

      String blobKey = TestUtils.createRandomBlobKey("file-", ".jpg");
      TestUtils.createBlobsInContainer(CONTAINER_NAME, blobKey);
      Blob blob = storageStrategyAutoDetectContentType.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getContentMetadata().getContentType(), "image/jpeg");

      blobKey = TestUtils.createRandomBlobKey("file-", ".pdf");
      TestUtils.createBlobsInContainer(CONTAINER_NAME, blobKey);
      blob = storageStrategyAutoDetectContentType.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getContentMetadata().getContentType(), "application/pdf");

      blobKey = TestUtils.createRandomBlobKey("file-", ".mp4");
      TestUtils.createBlobsInContainer(CONTAINER_NAME, blobKey);
      blob = storageStrategyAutoDetectContentType.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getContentMetadata().getContentType(), "video/mp4");
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testGetBlobContentType_AutoDetect_False() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("file-", ".jpg");
      TestUtils.createBlobsInContainer(CONTAINER_NAME, blobKey);
      Blob blob = storageStrategy.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getContentMetadata().getContentType(), null);
   }

   public void testListDirectoryBlob() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("directory-", File.separator);
      Blob blob = storageStrategy.newBlob(blobKey);
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      Iterable<String> keys = storageStrategy.getBlobKeysInsideContainer(CONTAINER_NAME, null, null);
      Iterator<String> iter = keys.iterator();
      assertTrue(iter.hasNext());
      assertEquals(iter.next(), blobKey);
      assertFalse(iter.hasNext());
   }

   public void testDeleteDirectoryBlob() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("a/b/c/directory-", "/");
      Blob blob = storageStrategy.newBlob(blobKey);
      storageStrategy.putBlob(CONTAINER_NAME, blob);
      File blobFullPath = new File(TARGET_CONTAINER_NAME, blobKey);
      assertTrue(blobFullPath.isDirectory());

      storageStrategy.removeBlob(CONTAINER_NAME, blobKey);
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testDeleteIntermediateDirectoryBlob() throws IOException {
      String parentKey = TestUtils.createRandomBlobKey("a/b/c/directory-", "/");
      String childKey = TestUtils.createRandomBlobKey(parentKey + "directory-", "/");
      storageStrategy.putBlob(CONTAINER_NAME, storageStrategy.newBlob(parentKey));
      storageStrategy.putBlob(CONTAINER_NAME, storageStrategy.newBlob(childKey));

      storageStrategy.removeBlob(CONTAINER_NAME, parentKey);
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
      assertFalse(storageStrategy.blobExists(CONTAINER_NAME, parentKey));
      assertTrue(storageStrategy.blobExists(CONTAINER_NAME, childKey));
   }

   public void testWritePayloadOnFile() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("writePayload-", ".img");
      File sourceFile = TestUtils.getImageForBlobPayload();
      FilePayload filePayload = new FilePayload(sourceFile);
      Blob blob = storageStrategy.newBlob(blobKey);
      blob.setPayload(filePayload);

      // write files
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      // verify that the files is equal
      File blobFullPath = new File(TARGET_CONTAINER_NAME, blobKey);
      ByteSource expectedInput = Files.asByteSource(sourceFile);
      ByteSource actualInput = Files.asByteSource(blobFullPath);
      assertTrue(expectedInput.contentEquals(actualInput),
            "Files are not equal");
   }

   public void testWritePayloadOnFileInputStream() throws IOException {
      String blobKey = TestUtils.createRandomBlobKey("writePayload-", ".img");
      File sourceFile = TestUtils.getImageForBlobPayload();
      InputStreamPayload fileInputStreamPayload = new InputStreamPayload(
            new FileInputStream(sourceFile));
      Blob blob = storageStrategy.newBlob(blobKey);
      blob.setPayload(fileInputStreamPayload);

      // write files
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      // verify that the files is equal
      File blobFullPath = new File(TARGET_CONTAINER_NAME, blobKey);
      ByteSource expectedInput = Files.asByteSource(sourceFile);
      ByteSource actualInput = Files.asByteSource(blobFullPath);
      assertTrue(expectedInput.contentEquals(actualInput),
            "Files are not equal");
   }

   public void testWritePayloadOnFile_SourceFileDoesntExist() {
      File sourceFile = new File("asdfkjsadkfjasdlfasdflk.asdfasdfas");
      FilePayload payload = new FilePayload(sourceFile);
      try {
         payload.getInput();
         fail("Exception not thrown");
      } catch (Exception ex) {
         assertNotNull(Throwables2.getFirstThrowableOfType(ex, IOException.class));
      }
   }

   public void testGetFileForBlobKey() {
      String blobKey;
      File fileForPayload;
      String fullPath = (new File(TARGET_CONTAINER_NAME).getAbsolutePath()) + FS;

      blobKey = TestUtils.createRandomBlobKey("getFileForBlobKey-", ".img");
      fileForPayload = storageStrategy.getFileForBlobKey(CONTAINER_NAME, blobKey);
      assertNotNull(fileForPayload, "Result File object is null");
      assertEquals(fileForPayload.getAbsolutePath(), fullPath + blobKey, "Wrong file path");

      blobKey = TestUtils.createRandomBlobKey("asd" + FS + "vmad" + FS + "andsnf" + FS + "getFileForBlobKey-", ".img");
      fileForPayload = storageStrategy.getFileForBlobKey(CONTAINER_NAME, blobKey);
      assertEquals(fileForPayload.getAbsolutePath(), fullPath + blobKey, "Wrong file path");
   }

   public void testGetFileForBlobKey_AbsolutePath() throws Exception {
      String absoluteBasePath = (new File(getAbsoluteDirectory(), "basedir")).getAbsolutePath() + FS;
      String absoluteContainerPath = absoluteBasePath + CONTAINER_NAME + FS;

      // create storageStrategy with an absolute path
      FilesystemStorageStrategyImpl storageStrategyAbsolute = new FilesystemStorageStrategyImpl(
               new Provider<BlobBuilder>() {
                  @Override
                  public BlobBuilder get() {
                     return new BlobBuilderImpl();
                  }
               }, absoluteBasePath, false, new FilesystemContainerNameValidatorImpl(), new FilesystemBlobKeyValidatorImpl(), defaultLocation);
      TestUtils.cleanDirectoryContent(absoluteContainerPath);

      String blobKey;
      File fileForPayload;

      blobKey = TestUtils.createRandomBlobKey("getFileForBlobKey-", ".img");
      fileForPayload = storageStrategyAbsolute.getFileForBlobKey(CONTAINER_NAME, blobKey);
      assertNotNull(fileForPayload, "Result File object is null");
      assertEquals(fileForPayload.getAbsolutePath(), absoluteContainerPath + blobKey, "Wrong file path");

      blobKey = TestUtils.createRandomBlobKey("asd" + FS + "vmad" + FS + "andsnf" + FS + "getFileForBlobKey-", ".img");
      fileForPayload = storageStrategyAbsolute.getFileForBlobKey(CONTAINER_NAME, blobKey);
      assertEquals(fileForPayload.getAbsolutePath(), absoluteContainerPath + blobKey, "Wrong file path");
   }

   public void testBlobExists() throws IOException {
      String[] sourceBlobKeys = { TestUtils.createRandomBlobKey("blobExists-", ".jpg"),
               TestUtils.createRandomBlobKey("blobExists-", ".jpg"),
               TestUtils.createRandomBlobKey("afasd" + FS + "asdma" + FS + "blobExists-", ".jpg") };

      for (String blobKey : sourceBlobKeys) {
         assertFalse(storageStrategy.blobExists(CONTAINER_NAME, blobKey), "Blob " + blobKey + " exists");
      }
      TestUtils.createBlobsInContainer(CONTAINER_NAME, sourceBlobKeys);
      for (String blobKey : sourceBlobKeys) {
         assertTrue(storageStrategy.blobExists(CONTAINER_NAME, blobKey), "Blob " + blobKey + " doesn't exist");
      }
   }

   public void testRemoveBlob() throws IOException {
      storageStrategy.createContainer(CONTAINER_NAME);
      Set<String> blobKeys = TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("removeBlob-", ".jpg"),
               TestUtils.createRandomBlobKey("removeBlob-", ".jpg"),
               TestUtils.createRandomBlobKey("346" + FS + "g3sx2" + FS + "removeBlob-", ".jpg"),
               TestUtils.createRandomBlobKey("346" + FS + "g3sx2" + FS + "removeBlob-", ".jpg") });

      Set<String> remainingBlobKeys = Sets.newHashSet();
      for (String key : blobKeys) {
         remainingBlobKeys.add(key);
      }
      for (String blobKeyToRemove : blobKeys) {
         storageStrategy.removeBlob(CONTAINER_NAME, blobKeyToRemove);
         // checks if the blob was removed
         TestUtils.fileExists(blobKeyToRemove, false);
         remainingBlobKeys.remove(blobKeyToRemove);
         // checks if all other blobs still exists
         for (String remainingBlobKey : remainingBlobKeys) {
            TestUtils.fileExists(TARGET_CONTAINER_NAME + FS + remainingBlobKey, true);
         }
      }
   }

   public void testRemoveBlob_ContainerNotExists() {
      storageStrategy.removeBlob("asdasdasd", "sdfsdfsdfasd");
   }

   public void testRemoveBlob_BlobNotExists() {
      storageStrategy.createContainer(CONTAINER_NAME);
      storageStrategy.removeBlob(CONTAINER_NAME, "sdfsdfsdfasd");
   }

   public void testGetBlobKeysInsideContainer() throws IOException {
      Iterable<String> resultList;

      // no container
      resultList = storageStrategy.getBlobKeysInsideContainer(CONTAINER_NAME, null, null);
      assertNotNull(resultList, "Result is null");
      assertFalse(resultList.iterator().hasNext(), "Blobs detected");

      // create blobs
      storageStrategy.createContainer(CONTAINER_NAME);
      Set<String> createBlobKeys = TestUtils.createBlobsInContainer(CONTAINER_NAME, new String[] {
               TestUtils.createRandomBlobKey("GetBlobKeys-", ".jpg"),
               TestUtils.createRandomBlobKey("GetBlobKeys-", ".jpg"),
               TestUtils.createRandomBlobKey("563" + "/" + "g3sx2" + "/" + "removeBlob-", ".jpg"),
               TestUtils.createRandomBlobKey("563" + "/" + "g3sx2" + "/" + "removeBlob-", ".jpg") });
      storageStrategy.getBlobKeysInsideContainer(CONTAINER_NAME, null, null);

      List<String> retrievedBlobKeys = Lists.newArrayList();
      resultList = storageStrategy.getBlobKeysInsideContainer(CONTAINER_NAME, null, null);
      Iterator<String> containersIterator = resultList.iterator();
      while (containersIterator.hasNext()) {
         retrievedBlobKeys.add(containersIterator.next());
      }
      int expectedBlobs = retrievedBlobKeys.size() - 2;  // ignore two directories
      assertEquals(expectedBlobs, createBlobKeys.size(), "Different blobs number");
      for (String createdBlobKey : createBlobKeys) {
         assertTrue(retrievedBlobKeys.contains(createdBlobKey), "Blob " + createdBlobKey + " not found");
      }
   }

   public void testCountsBlob() {
      storageStrategy.countBlobs(CONTAINER_NAME, ListContainerOptions.NONE);
   }

   public void testInvalidBlobKey() {
      try {
         storageStrategy.newBlob(FS + "test.jpg");
         fail("Wrong blob key not recognized");
      } catch (IllegalArgumentException e) {
      }
   }

   public void testInvalidContainerName() {
      try {
         storageStrategy.createContainer("file" + FS + "system");
         fail("Wrong container name not recognized");
      } catch (IllegalArgumentException e) {
      }
   }

   @Test(dataProvider = "ignoreOnMacOSX")
   public void testOverwriteBlobMetadata() throws Exception {
      String blobKey = TestUtils.createRandomBlobKey("writePayload-", ".img");

      // write blob
      Blob blob = new BlobBuilderImpl()
            .name(blobKey)
            .payload(randomByteSource().slice(0, 1024))
            .userMetadata(ImmutableMap.of("key1", "value1"))
            .build();
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      blob = storageStrategy.getBlob(CONTAINER_NAME, blobKey);
      assertEquals(blob.getMetadata().getUserMetadata().get("key1"), "value1");

      // overwrite blob
      blob = new BlobBuilderImpl()
            .name(blobKey)
            .payload(randomByteSource().slice(0, 1024))
            // no metadata
            .build();
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      blob = storageStrategy.getBlob(CONTAINER_NAME, blobKey);
      assertFalse(blob.getMetadata().getUserMetadata().containsKey("key1"));
   }

   @Test
   public void testPutIncorrectContentLength() throws Exception {
      Blob blob = new BlobBuilderImpl()
            .name("key")
            .payload(randomByteSource().slice(0, 1024))
            .contentLength(512)
            .build();
      try {
         storageStrategy.putBlob(CONTAINER_NAME, blob);
         Fail.failBecauseExceptionWasNotThrown(IOException.class);
      } catch (IOException ioe) {
         // expected
      }
   }

   @Test
   public void testDeletingInvalidPathFileEndsNormally() {
      String invalidPathBlobKey = "A<!:!@#$%^&*?]8 /\0";
      try {
         storageStrategy.removeBlob(CONTAINER_NAME, invalidPathBlobKey);
      } catch (InvalidPathException ipe) {
         fail("Deleting an invalid path ended with an InvalidPathException.", ipe);
      }
   }

   @Test
   public void testGetBlobTrailingSlash() throws Exception {
      String key = "key";
      ByteSource byteSource = randomByteSource().slice(0, 1024);
      Blob blob = new BlobBuilderImpl()
            .name(key)
            .payload(byteSource)
            .contentLength(byteSource.size())
            .build();
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      blob = storageStrategy.getBlob(CONTAINER_NAME, key);
      assertThat(blob).isNotNull();

      blob = storageStrategy.getBlob(CONTAINER_NAME, key + "/");
      assertThat(blob).isNull();
   }

   @Test
   public void testPutBlobTrailingSlash() throws Exception {
      String key = "key";
      ByteSource byteSource = ByteSource.empty();
      Blob blob = new BlobBuilderImpl()
            .name(key + "/")
            .payload(byteSource)
            .contentLength(byteSource.size())
            .build();
      storageStrategy.putBlob(CONTAINER_NAME, blob);

      blob = storageStrategy.getBlob(CONTAINER_NAME, key);
      assertThat(blob).isNull();

      blob = storageStrategy.getBlob(CONTAINER_NAME, key + "/");
      assertThat(blob).isNotNull();
   }

   // ---------------------------------------------------------- Private methods

   /**
    * Calculates an absolute directory path that depends on operative system
    *
    * @return
    */
   private String getAbsoluteDirectory() throws IOException {
      File tempFile = File.createTempFile("prefix", "suffix");
      String tempAbsolutePath = tempFile.getParent();

      return tempAbsolutePath;
   }

   @DataProvider
   public Object[][] ignoreOnMacOSX() {
        return isMacOSX() ? NO_INVOCATIONS
                : SINGLE_NO_ARG_INVOCATION;
   }
}
