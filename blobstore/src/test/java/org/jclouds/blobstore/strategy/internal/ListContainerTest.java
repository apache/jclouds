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
package org.jclouds.blobstore.strategy.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.util.Closeables2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import com.google.inject.Injector;

@Test(testName = "PrefixTest", singleThreaded = true)
public class ListContainerTest {
   private BlobStore blobStore;
   private ConcatenateContainerLists concatter;

   @BeforeClass
   public void setupBlobStore() {
      Injector injector = ContextBuilder.newBuilder("transient").buildInjector();
      blobStore = injector.getInstance(BlobStore.class);
      concatter = injector.getInstance(ConcatenateContainerLists.class);
   }

   @AfterClass
   public void closeBlobSore() {
      if (blobStore != null) {
         Closeables2.closeQuietly(blobStore.getContext());
      }
   }

   public void testListBlobWithPrefixAndDelimiter() {
      String containerName = "test";
      String name = "asdf/";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder(name).payload("").build());
      Iterable<? extends StorageMetadata> results = concatter.execute(containerName,
            ListContainerOptions.Builder.prefix(name).delimiter(name));
      assertThat(results).hasSize(1);
   }

   public void testListWithPrefix() {
      String containerName = "prefix";
      String prefix = "foo";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix).payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "bar").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "baz").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("bar").payload("").build());

      Iterable<? extends StorageMetadata> results = concatter.execute(containerName,
            ListContainerOptions.Builder.prefix(prefix)./* irrelevant */recursive());
      assertThat(results).hasSize(3);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo(prefix);
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo(prefix + "bar");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 2).getName()).isEqualTo(prefix + "baz");
      assertThat(Iterables.get(results, 2).getType()).isEqualTo(StorageType.BLOB);
   }

   public void testListWithPrefixAndDelimiter() {
      String containerName = "prefixWithSeparator";
      String prefix = "foo";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "-object").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "bar-object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "baz-object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("bar-object").payload("").build());

      Iterable<? extends StorageMetadata> results = concatter.execute(containerName,
            ListContainerOptions.Builder.prefix(prefix).delimiter("-"));
      assertThat(Iterables.size(results)).isEqualTo(3);
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.RELATIVE_PATH);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo(prefix + "-");
      assertThat(Iterables.get(results, 1).getName()).isEqualTo(prefix + "bar-");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.RELATIVE_PATH);
      assertThat(Iterables.get(results, 2).getName()).isEqualTo(prefix + "baz-");
      assertThat(Iterables.get(results, 2).getType()).isEqualTo(StorageType.RELATIVE_PATH);
   }

   public void testListRecursivePrefix() {
      String containerName = "testListRecursive";
      String prefix = "foo";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "/object").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "bar/object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "baz/object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("bar/object").payload("").build());

      Iterable<? extends StorageMetadata> results = concatter.execute(containerName,
            ListContainerOptions.Builder.prefix(prefix).recursive());
      assertThat(results).hasSize(3);
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo(prefix + "/object");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo(prefix + "bar/object");
      assertThat(Iterables.get(results, 2).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 2).getName()).isEqualTo(prefix + "baz/object");
   }

   public void testListRecursivePrefixDelimiter() {
      String containerName = "testListRecursivePrefixDelimiter";
      String prefix = "foo/";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "object").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "bar/object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(prefix + "baz/object").payload("")
            .build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("bar/object").payload("").build());

      Iterable<? extends StorageMetadata> results = concatter.execute(containerName,
            ListContainerOptions.Builder.prefix(prefix));
      assertThat(results).hasSize(3);
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.RELATIVE_PATH);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo(prefix + "bar/");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.RELATIVE_PATH);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo(prefix + "baz/");
      assertThat(Iterables.get(results, 2).getType()).isEqualTo(StorageType.BLOB);
      assertThat(Iterables.get(results, 2).getName()).isEqualTo(prefix + "object");
   }

   public void testListDirectory() {
      String containerName = "testListDir";
      String directory = "dir";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.createDirectory(containerName, directory);
      blobStore.putBlob(containerName, blobStore.blobBuilder(directory + "/foo").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder(directory + "/bar").payload("").build());
      Iterable<? extends StorageMetadata> results = concatter.execute(containerName, ListContainerOptions.NONE);
      assertThat(results).hasSize(2);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo(directory);
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.FOLDER);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo(directory + '/');
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.RELATIVE_PATH);
   }

   public void testListMarkers() {
      String containerName = "testListMarkers";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder("abc").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("foo/bar").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("foo/baz").payload("").build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("qux").payload("").build());

      PageSet<? extends StorageMetadata> results = blobStore.list(
              containerName, ListContainerOptions.Builder.maxResults(1));
      assertThat(results.getNextMarker()).isEqualTo("abc");
      results = blobStore.list(containerName,
              ListContainerOptions.Builder.maxResults(1).afterMarker(results.getNextMarker()));
      assertThat(results.getNextMarker()).isEqualTo("foo/");
      results = blobStore.list(containerName,
              ListContainerOptions.Builder.maxResults(1).afterMarker(results.getNextMarker()));
      assertThat(results.getNextMarker()).isEqualTo(null);
   }

   public void testListBlobEndsWithDelimiter() {
      String containerName = "testListBlobEndsWithDelimiter";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder("foo/").payload(ByteSource.empty()).build());
      PageSet<? extends StorageMetadata> results = blobStore.list(containerName,
            ListContainerOptions.Builder.prefix("foo/"));
      assertThat(results.size()).isEqualTo(1);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo("foo/");
      assertThat(Iterables.get(results, 0).getType()).isNotEqualTo(StorageType.RELATIVE_PATH);
   }

   public void testListBlobEndsWithDelimiterAndDelimiterFilter() {
      String containerName = "testListBlobEndsWithDelimiterAndDelimiterFilter";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.putBlob(containerName, blobStore.blobBuilder("foo/").type(StorageType.FOLDER)
         .payload(ByteSource.empty()).build());
      blobStore.putBlob(containerName, blobStore.blobBuilder("bar/text").type(StorageType.BLOB)
         .payload(ByteSource.empty()).build());
      PageSet<? extends StorageMetadata> results = blobStore.list(containerName,
         ListContainerOptions.Builder.delimiter("/"));
      assertThat(results.size()).isEqualTo(2);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo("bar/");
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.RELATIVE_PATH);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo("foo/");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.FOLDER);
   }

   public void testDirectoryListing() {
      String containerName = "testDirectoryListing";
      blobStore.createContainerInLocation(null, containerName);
      blobStore.createDirectory(containerName, "dir");
      blobStore.createDirectory(containerName, "dir/dir");

      PageSet<? extends StorageMetadata> results = blobStore.list(containerName);
      assertThat(results.size()).isEqualTo(2);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo("dir");
      assertThat(Iterables.get(results, 1).getName()).isEqualTo("dir/");

      results = blobStore.list(containerName, ListContainerOptions.Builder.inDirectory("dir"));
      assertThat(results.size()).isEqualTo(1);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo("dir/dir");
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.FOLDER);

      blobStore.putBlob(containerName, blobStore.blobBuilder("dir/dir/blob").payload("").build());
      results = blobStore.list(containerName, ListContainerOptions.Builder.inDirectory("dir"));
      assertThat(results.size()).isEqualTo(2);
      assertThat(Iterables.get(results, 0).getName()).isEqualTo("dir/dir");
      assertThat(Iterables.get(results, 0).getType()).isEqualTo(StorageType.FOLDER);
      assertThat(Iterables.get(results, 1).getName()).isEqualTo("dir/dir/");
      assertThat(Iterables.get(results, 1).getType()).isEqualTo(StorageType.RELATIVE_PATH);
   }
}
