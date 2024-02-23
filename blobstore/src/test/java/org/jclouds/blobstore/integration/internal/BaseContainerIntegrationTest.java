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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagateIfPossible;
import static com.google.common.collect.Iterables.get;
import static com.google.common.hash.Hashing.md5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.blobstore.options.ListContainerOptions.Builder.afterMarker;
import static org.jclouds.blobstore.options.ListContainerOptions.Builder.inDirectory;
import static org.jclouds.blobstore.options.ListContainerOptions.Builder.maxResults;
import static org.jclouds.utils.TestUtils.NO_INVOCATIONS;
import static org.jclouds.utils.TestUtils.SINGLE_NO_ARG_INVOCATION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.ws.rs.core.MediaType;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.util.concurrent.Uninterruptibles;

public class BaseContainerIntegrationTest extends BaseBlobStoreIntegrationTest {

   @Test(groups = { "integration", "live" })
   public void containerDoesntExist() {
      Random random = new Random();
      assert !view.getBlobStore().containerExists("forgetaboutit" + random.nextInt(Integer.MAX_VALUE));
      assert !view.getBlobStore().containerExists("cloudcachestorefunctionalintegrationtest-first" +
            random.nextInt(Integer.MAX_VALUE));
   }

   @Test(groups = { "integration", "live" })
   // TODO: the test name does not describe its behavior
   public void testPutTwiceIsOkAndDoesntOverwrite() throws InterruptedException {
      String containerName = getContainerName();
      try {
         view.getBlobStore().createContainerInLocation(null, containerName);

         Blob blob = view.getBlobStore().blobBuilder("hello").payload(TEST_STRING).build();
         view.getBlobStore().putBlob(containerName, blob);

         view.getBlobStore().createContainerInLocation(null, containerName);
         awaitConsistency();
         assertEquals(view.getBlobStore().countBlobs(containerName), 1);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test
   public void testListMarkerAfterLastKey() throws Exception {
      String key = "hello";
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName,
         // NOTE all metadata in jclouds comes out as lowercase, in an effort to
         // normalize the providers.
               view.getBlobStore().blobBuilder(key).userMetadata(ImmutableMap.of("Adrian", "powderpuff"))
                     .payload(TEST_STRING).contentType(MediaType.TEXT_PLAIN)
                     .contentMD5(md5().hashString(TEST_STRING, UTF_8).asBytes())
                     .build());
         validateContent(containerName, key);
         awaitConsistency();
         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName, afterMarker(key));
         assertThat(container).isEmpty();
      } finally {
         returnContainer(containerName);
      }
   }

   @Test
   public void testListContainerWithZeroMaxResults() throws Exception {
      String key = "hello";
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName,
         // NOTE all metadata in jclouds comes out as lowercase, in an effort to
         // normalize the providers.
               view.getBlobStore().blobBuilder(key).userMetadata(ImmutableMap.of("Adrian", "powderpuff"))
                     .payload(TEST_STRING).contentType(MediaType.TEXT_PLAIN)
                     .contentMD5(md5().hashString(TEST_STRING, UTF_8).asBytes())
                     .build());
         awaitConsistency();
         validateContent(containerName, key);

         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName, maxResults(0));
         assertThat(container).isEmpty();
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testWithDetails() throws InterruptedException, IOException {
      String key = "hello";
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName,
         // NOTE all metadata in jclouds comes out as lowercase, in an effort to
         // normalize the providers.
               view.getBlobStore().blobBuilder(key).userMetadata(ImmutableMap.of("Adrian", "powderpuff"))
                     .payload(TEST_STRING).contentType(MediaType.TEXT_PLAIN)
                     .contentMD5(md5().hashString(TEST_STRING, UTF_8).asBytes())
                     .build());
         awaitConsistency();
         validateContent(containerName, key);

         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName,
               maxResults(1).withDetails());

         BlobMetadata metadata = BlobMetadata.class.cast(get(container, 0));

         assert metadata.getContentMetadata().getContentType().startsWith("text/plain") : metadata.getContentMetadata()
               .getContentType();
         assertEquals(metadata.getContentMetadata().getContentLength(), Long.valueOf(TEST_STRING.length()));
         assertEquals(metadata.getUserMetadata().get("adrian"), "powderpuff");
         checkMD5(metadata);
      } finally {
         returnContainer(containerName);
      }
   }

   protected void checkMD5(BlobMetadata metadata) throws IOException {
      assertEquals(metadata.getContentMetadata().getContentMD5(), md5().hashString(TEST_STRING, UTF_8).asBytes());
   }

   @Test(groups = { "integration", "live" })
   public void testClearWhenContentsUnderPath() throws InterruptedException {
      String containerName = getContainerName();
      try {
         add5BlobsUnderPathAnd5UnderRootToContainer(containerName);
         view.getBlobStore().clearContainer(containerName);
         assertConsistencyAwareContainerSize(containerName, 0);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testClearWithOptions() throws InterruptedException {
      String containerName = getContainerName();
      try {
         ListContainerOptions options;

         // Should wipe out all objects, as there are empty folders
         // above
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/");
         options.recursive();
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareContainerSize(containerName, 0);

         view.getBlobStore().clearContainer(containerName);
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/2/3");
         options.recursive();
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareBlobExists(containerName, "path/1/a");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/b");
         assertConsistencyAwareBlobDoesntExist(containerName, "path/1/2/3");

         view.getBlobStore().clearContainer(containerName);
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/2/3/4/");
         options.recursive();
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareBlobExists(containerName, "path/1/a");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/b");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/3/5/e");
         assertConsistencyAwareBlobDoesntExist(containerName, "path/1/2/3/4");

         // non-recursive, should not clear anything, as prefix does not match
         view.getBlobStore().clearContainer(containerName);
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/2/3");
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareBlobExists(containerName, "path/1/a");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/b");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/3/c");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/3/5/e");


         // non-recursive, should only clear path/1/2/3/c
         view.getBlobStore().clearContainer(containerName);
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/2/3/");
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareBlobExists(containerName, "path/1/a");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/b");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/3/4/d");
         assertConsistencyAwareBlobDoesntExist(containerName, "path/1/2/3/c");

         // non-recursive, should only clear path/1/2/3/c
         view.getBlobStore().clearContainer(containerName);
         add5NestedBlobsToContainer(containerName);
         options = new ListContainerOptions();
         options.prefix("path/1/2/3/c");
         view.getBlobStore().clearContainer(containerName, options);
         assertConsistencyAwareBlobExists(containerName, "path/1/a");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/b");
         assertConsistencyAwareBlobExists(containerName, "path/1/2/3/4/d");
         assertConsistencyAwareBlobDoesntExist(containerName, "path/1/2/3/c");
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testListContainerMarker() throws InterruptedException {
      String containerName = getContainerName();
      try {
         addAlphabetUnderRoot(containerName);

         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName, maxResults(1));

         assert container.getNextMarker() != null;
         assertEquals(container.size(), 1);
         String marker = container.getNextMarker();

         container = view.getBlobStore().list(containerName, afterMarker(marker));
         assertEquals(container.getNextMarker(), null);
         assert container.size() == 25 : String.format("size should have been 25, but was %d: %s", container.size(),
               container);
         assert container.getNextMarker() == null;
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testListRootUsesDelimiter() throws InterruptedException {
      String containerName = getContainerName();
      try {
         String prefix = "rootdelimiter";
         addTenObjectsUnderPrefix(containerName, prefix);
         add15UnderRoot(containerName);
         awaitConsistency();
         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName);
         assert container.getNextMarker() == null;
         assertEquals(container.size(), 16);
      } finally {
         returnContainer(containerName);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testDirectory() throws InterruptedException {
      String containerName = getContainerName();
      try {
         String directory = "directory";

         assert !view.getBlobStore().directoryExists(containerName, directory);

         view.getBlobStore().createDirectory(containerName, directory);

         assert view.getBlobStore().directoryExists(containerName, directory);
         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName);
         // we should have only the directory under root
         assert container.getNextMarker() == null;
         assert container.size() == 1 : container;

         container = view.getBlobStore().list(containerName, inDirectory(directory));

         // we should have nothing in the directory
         assert container.getNextMarker() == null;
         assert container.size() == 0 : container;

         addTenObjectsUnderPrefix(containerName, directory);

         awaitConsistency();

         container = view.getBlobStore().list(containerName);
         // we should get back the subdir entry and the directory marker
         assert container.getNextMarker() == null;
         assertThat(container).hasSize(2);

         container = view.getBlobStore().list(containerName, inDirectory(directory));
         // we should have only the 10 items under the directory
         assert container.getNextMarker() == null;
         assert container.size() == 10 : container;

         // try 2 level deep directory
         assert !view.getBlobStore().directoryExists(containerName, directory + "/" + directory);
         view.getBlobStore().createDirectory(containerName, directory + "/" + directory);

         awaitConsistency();

         assert view.getBlobStore().directoryExists(containerName, directory + "/" + directory);

         view.getBlobStore().clearContainer(containerName, inDirectory(directory));
         awaitConsistency();

         assert view.getBlobStore().directoryExists(containerName, directory);
         assertThat(view.getBlobStore().directoryExists(containerName, directory + "/" + directory)).isFalse();

         // should have only the 2 level-deep directory above
         container = view.getBlobStore().list(containerName, inDirectory(directory));
         assert container.getNextMarker() == null;
         assertThat(container).hasSize(0);

         view.getBlobStore().createDirectory(containerName, directory + "/" + directory);

         awaitConsistency();

         container = view.getBlobStore().list(containerName, inDirectory(directory).recursive());
         assert container.getNextMarker() == null;
         assert container.size() == 1 : container;

         view.getBlobStore().clearContainer(containerName, inDirectory(directory).recursive());

         // should no longer have the 2 level-deep directory above
         container = view.getBlobStore().list(containerName, inDirectory(directory));
         assert container.getNextMarker() == null;
         assert container.size() == 0 : container;

         container = view.getBlobStore().list(containerName);
         // should only have the directory
         assert container.getNextMarker() == null;
         assert container.size() == 1 : container;
         view.getBlobStore().deleteDirectory(containerName, directory);

         container = view.getBlobStore().list(containerName);
         // now should be completely empty
         assert container.getNextMarker() == null;
         assert container.size() == 0 : container;
      } finally {
         returnContainer(containerName);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testListContainerPrefix() throws InterruptedException {
      String containerName = getContainerName();
      try {
         String prefix = "containerprefix";
         addTenObjectsUnderPrefix(containerName, prefix);
         add15UnderRoot(containerName);
         awaitConsistency();
         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(
               containerName, new ListContainerOptions().prefix(prefix + "/").delimiter("/"));
         assert container.getNextMarker() == null;
         assertEquals(container.size(), 10);
      } finally {
         returnContainer(containerName);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testListContainerMaxResults() throws InterruptedException {
      String containerName = getContainerName();
      try {
         addAlphabetUnderRoot(containerName);

         PageSet<? extends StorageMetadata> container;
         ListContainerOptions options = maxResults(10);

         container = view.getBlobStore().list(containerName, options);
         assertThat(container).hasSize(10);
         assertThat(container.getNextMarker()).isNotNull();

         container = view.getBlobStore().list(containerName, options.afterMarker(container.getNextMarker()));
         assertThat(container).hasSize(10);
         assertThat(container.getNextMarker()).isNotNull();

         container = view.getBlobStore().list(containerName, options.afterMarker(container.getNextMarker()));
         assertThat(container).hasSize(6);
         assertThat(container.getNextMarker()).isNull();
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(dataProvider = "ignoreOnWindows", groups = { "integration", "live" })
   public void testDelimiter() throws Exception {
      String containerName = getContainerName();
      try {
         for (String blobName : new String[] { "asdf", "boo" + File.separator + "bar", "boo" + File.separator
               + "baz"  + File.separator + "xyzzy", "cquux" + File.separator + "thud", "cquux" + File.separator
               + "bla" }) {
            Blob blob = view.getBlobStore().blobBuilder(blobName).payload(TEST_STRING).build();
            addBlobToContainer(containerName, blob);
         }

         // test root directory without marker
         PageSet<? extends StorageMetadata> pageSet = view.getBlobStore().list(containerName);
         assertThat(pageSet).hasSize(3);
         assertThat(pageSet.getNextMarker()).isNull();

         // list root directory with marker
         ListContainerOptions options = new ListContainerOptions().maxResults(1);
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(1);
         assertThat(pageSet.iterator().next().getName()).isEqualTo("asdf");
         assertThat(pageSet.getNextMarker()).isNotNull();

         options.afterMarker(pageSet.getNextMarker());
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(1);
         assertThat(pageSet.iterator().next().getName()).isEqualTo("boo/");
         assertThat(pageSet.getNextMarker()).isNotNull();

         options.afterMarker(pageSet.getNextMarker());
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(1);
         assertThat(pageSet.iterator().next().getName()).isEqualTo("cquux/");
         assertThat(pageSet.getNextMarker()).isNull();

         // list child directory with marker
         options = new ListContainerOptions().inDirectory("boo").maxResults(1);
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(1);
         assertThat(pageSet.iterator().next().getName()).isEqualTo("boo/bar");
         assertThat(pageSet.getNextMarker()).isNotNull();

         options.afterMarker(pageSet.getNextMarker());
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(1);
         assertThat(pageSet.iterator().next().getName()).isEqualTo("boo/baz/");
         assertThat(pageSet.getNextMarker()).isNull();

         // list child directory without marker
         options = new ListContainerOptions().inDirectory("boo").maxResults(2);
         pageSet = view.getBlobStore().list(containerName, options);
         assertThat(pageSet).hasSize(2);
         Iterator<? extends StorageMetadata> it = pageSet.iterator();
         assertThat(it.next().getName()).isEqualTo("boo/bar");
         assertThat(it.next().getName()).isEqualTo("boo/baz/");
         assertThat(pageSet.getNextMarker()).isNull();
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void containerExists() throws InterruptedException {
      String containerName = getContainerName();
      try {
         assert view.getBlobStore().containerExists(containerName);
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void deleteContainerWithContents() throws InterruptedException {
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName, "test");
         view.getBlobStore().deleteContainer(containerName);
         awaitConsistency();
         assertNotExists(containerName);
      } finally {
         recycleContainerAndAddToPool(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void deleteContainerWithoutContents() throws InterruptedException {
      final String containerName = getContainerName();
      try {
         view.getBlobStore().deleteContainer(containerName);
         awaitConsistency();
         assertNotExists(containerName);
      } finally {
         // this container is now deleted, so we can't reuse it directly
         recycleContainerAndAddToPool(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void deleteContainerIfEmptyWithContents() throws InterruptedException {
      String containerName = getContainerName();
      try {
         addBlobToContainer(containerName, "test");
         awaitConsistency();
         assertFalse(view.getBlobStore().deleteContainerIfEmpty(containerName));
         awaitConsistency();
         assertTrue(view.getBlobStore().containerExists(containerName));
      } finally {
         recycleContainerAndAddToPool(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void deleteContainerIfEmptyWithoutContents() throws InterruptedException {
      final String containerName = getContainerName();
      try {
         assertTrue(view.getBlobStore().deleteContainerIfEmpty(containerName));
         awaitConsistency();
         assertNotExists(containerName);
         // verify that true is returned even if the container does not exist
         assertTrue(view.getBlobStore().deleteContainerIfEmpty(containerName));
      } finally {
         // this container is now deleted, so we can't reuse it directly
         recycleContainerAndAddToPool(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testListContainer() throws InterruptedException, ExecutionException, TimeoutException {
      String containerName = getContainerName();
      try {
         add15UnderRoot(containerName);
         awaitConsistency();
         Set<? extends StorageMetadata> container = view.getBlobStore().list(containerName);
         assertEquals(container.size(), 15);
      } finally {
         returnContainer(containerName);
      }

   }

   @Test(groups = { "integration", "live" })
   public void testListContainerGetBlobSize() throws Exception {
      String containerName = getContainerName();
      try {
         ByteSource byteSource = ByteSource.wrap(new byte[42]);

         for (int i = 0; i < 2; i++) {
            view.getBlobStore().putBlob(containerName, view.getBlobStore()
                  .blobBuilder(i + "")
                  .payload(byteSource)
                  .contentLength(byteSource.size())
                  .build());
         }

         PageSet<? extends StorageMetadata> container = view.getBlobStore().list(containerName);

         for (StorageMetadata metadata : container) {
            assertEquals(metadata.getSize(), Long.valueOf(byteSource.size()));
         }
      } finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = { "integration", "live" })
   public void testSetContainerAccess() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      String containerName = getContainerName();
      try {
         assertThat(blobStore.getContainerAccess(containerName)).isEqualTo(ContainerAccess.PRIVATE);

         blobStore.setContainerAccess(containerName, ContainerAccess.PUBLIC_READ);
         assertThat(blobStore.getContainerAccess(containerName)).isEqualTo(ContainerAccess.PUBLIC_READ);

         String blobName = "blob";
         blobStore.putBlob(containerName, blobStore.blobBuilder(blobName).payload("").build());

         // test that blob is anonymously readable
         HttpRequest request = view.getSigner().signGetBlob(containerName, blobName).toBuilder()
                .replaceQueryParams(ImmutableMap.<String, String>of()).build();
         HttpResponse response = view.utils().http().invoke(request);
         assertThat(response.getStatusCode()).isEqualTo(200);

         blobStore.setContainerAccess(containerName, ContainerAccess.PRIVATE);
         assertThat(blobStore.getContainerAccess(containerName)).isEqualTo(ContainerAccess.PRIVATE);
      } finally {
         recycleContainerAndAddToPool(containerName);
      }
   }

   @Test(groups = {"integration", "live"})
   public void testContainerListWithPrefix() throws InterruptedException {
      final String containerName = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      String prefix = "blob";
      try {
         blobStore.putBlob(containerName, blobStore.blobBuilder(prefix).payload("").build());
         blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "foo").payload("").build());
         blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "bar").payload("").build());
         blobStore.putBlob(containerName, blobStore.blobBuilder("foo").payload("").build());
         checkEqualNames(ImmutableSet.of(prefix, prefix + "foo", prefix + "bar"),
               blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix)));
      }
      finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = {"integration", "live"})
   public void testContainerListWithDetails() throws InterruptedException {
      final String containerName = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      String prefix = "testContainerListWithDetails/";
      try {
         blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "foo/bar").payload("").build());
         blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "car").payload("").build());
         checkEqualNames(
             ImmutableSet.of(prefix + "foo/", prefix + "car"),
             blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix).delimiter("/"))
         );
         checkEqualNames(
             ImmutableSet.of(prefix + "foo/", prefix + "car"),
             blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix).delimiter("/").withDetails())
         );
      }
      finally {
         returnContainer(containerName);
      }
   }

   @Test(groups = {"integration", "live"})
   public void testDelimiterList() throws InterruptedException {
      final String containerName = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      String payload = "foo";
      try {
         blobStore.putBlob(containerName, blobStore.blobBuilder("test-foo-foo").payload(payload).build());
         blobStore.putBlob(containerName, blobStore.blobBuilder("test-bar-foo").payload(payload).build());
         blobStore.putBlob(containerName, blobStore.blobBuilder("foo").payload(payload).build());
         // NOTE: the test does not work if we use a file separator character ("/" or "\"), as the file system blob
         // store will create directories when putting such a blob. When listing results, these directories will also
         // show up in the result set.
         checkEqualNames(ImmutableSet.of("foo", "test-"), blobStore.list(containerName,
               ListContainerOptions.Builder.delimiter("-")));
         checkEqualNames(ImmutableSet.of("test-foo-foo", "test-bar-foo", "foo"),
               blobStore.list(containerName, ListContainerOptions.Builder.delimiter(".")));

         blobStore.putBlob(containerName, blobStore.blobBuilder("bar").payload(payload).build());
         blobStore.putBlob(containerName, blobStore.blobBuilder("bazar").payload(payload).build());
         checkEqualNames(ImmutableSet.of("bar", "baza"), blobStore.list(containerName,
               ListContainerOptions.Builder.delimiter("a").prefix("ba")));
      } finally {
         returnContainer(containerName);
      }
   }

   /** Test that listing with a marker prefix matches the first key with that prefix. */
   @Test
   public void testListMarkerPrefix() throws Exception {
      BlobStore blobStore = view.getBlobStore();
      final String container = getContainerName();
      try {
         blobStore.createContainerInLocation(null, container);
         blobStore.putBlob(container, blobStore.blobBuilder("a/a").payload("").build());
         blobStore.putBlob(container, blobStore.blobBuilder("b/b").payload("").build());
         ListContainerOptions options = new ListContainerOptions().afterMarker("b/").recursive();
         PageSet<? extends StorageMetadata> res = blobStore.list(container, options);
         assertThat(res).hasSize(1);
         assertThat(res.iterator().next().getName()).isEqualTo("b/b");
      } finally {
         returnContainer(container);
      }
   }

   /** Test that listing with an empty string for prefix and delimiter returns all of the keys. */
   @Test(groups = {"integration", "live"})
   public void testListEmptyPrefixDelimiter() throws Exception {
      final String container = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      blobStore.createContainerInLocation(null, container);

      try {
         ImmutableList<String> blobs = ImmutableList.of("a", "b", "c");
         for (String blob : blobs) {
            blobStore.putBlob(container, blobStore.blobBuilder(blob).payload("").build());
         }
         ListContainerOptions options = ListContainerOptions.Builder.delimiter("")
                 .prefix("").afterMarker("");
         PageSet<? extends StorageMetadata> rs = blobStore.list(container, options);
         ImmutableList.Builder<String> builder = ImmutableList.builder();
         for (StorageMetadata sm : rs) {
            builder.add(sm.getName());
         }
         assertThat(builder.build()).containsExactlyElementsOf(blobs);
      } finally {
         returnContainer(container);
      }
   }

   @DataProvider
   public Object[][] getBlobsToEscape() {
      ImmutableSet<String> testNames = ImmutableSet.of("%20", "%20 ", " %20", " ", "%", "%%");
      Object[][] result = new Object[1][1];
      result[0][0] = testNames;
      return result;
   }

   @Test(dataProvider = "getBlobsToEscape", groups = {"integration", "live"})
   public void testBlobNameEscaping(Set<String> blobNames) throws InterruptedException {
      final String containerName = getContainerName();
      BlobStore blobStore = view.getBlobStore();
      try {
         for (String name : blobNames) {
            Blob blob = blobStore.blobBuilder(name).payload(ByteSource.wrap("test".getBytes())).contentLength(4)
                  .build();
            blobStore.putBlob(containerName, blob);
         }
         checkEqualNames(blobNames, blobStore.list(containerName));
      } finally {
         returnContainer(containerName);
      }
   }

   private void checkEqualNames(Set<String> expectedSet, PageSet<? extends StorageMetadata> results) {
      Set<String> names = new HashSet<String>();
      for (StorageMetadata sm : results) {
         names.add(sm.getName());
      }

      assertThat(names).containsOnlyElementsOf(expectedSet);
   }

   protected void addAlphabetUnderRoot(String containerName) throws InterruptedException {
      for (char letter = 'a'; letter <= 'z'; letter++) {
         view.getBlobStore().putBlob(containerName,
               view.getBlobStore().blobBuilder(letter + "").payload(letter + "content").build());
      }
      assertContainerSize(containerName, 26);

   }

   protected void assertContainerSize(final String containerName, final int size) throws InterruptedException {
      assertConsistencyAware(new Runnable() {
         public void run() {
            try {
               assertEquals(view.getBlobStore().countBlobs(containerName), size);
            } catch (Exception e) {
               propagateIfPossible(e);
            }
         }
      });
   }

   protected void add15UnderRoot(String containerName) throws InterruptedException {
      for (int i = 0; i < 15; i++) {
         view.getBlobStore().putBlob(containerName,
               view.getBlobStore().blobBuilder(i + "").payload(i + "content").build());
      }
   }

   protected void addTenObjectsUnderPrefix(String containerName, String prefix) throws InterruptedException {
      for (int i = 0; i < 10; i++) {
         view.getBlobStore().putBlob(containerName,
               view.getBlobStore().blobBuilder(prefix + "/" + i).payload(i + "content").build());
      }
   }

   protected void awaitConsistency() {
      if (view.getConsistencyModel() == ConsistencyModel.EVENTUAL) {
         Uninterruptibles.sleepUninterruptibly(AWAIT_CONSISTENCY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      }
   }

   @DataProvider
   public Object[][] ignoreOnWindows() {
      return isWindowsOs() ? NO_INVOCATIONS
            : SINGLE_NO_ARG_INVOCATION;
   }

   private static boolean isWindowsOs() {
      return System.getProperty("os.name", "").toLowerCase().contains("windows");
   }
}
