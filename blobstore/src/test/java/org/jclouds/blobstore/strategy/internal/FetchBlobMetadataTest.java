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

import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.util.Closeables2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Comparator;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(testName = "FetchBlobMetadataTest", singleThreaded = true)
public class FetchBlobMetadataTest {

   private static final String CONTAINER_NAME = "container";

   private BlobStore blobStore;
   private FetchBlobMetadata fetchBlobMetadata;

   @BeforeClass
   public void setupBlobStore() {
      Injector injector = ContextBuilder.newBuilder("transient").buildInjector();
      blobStore = injector.getInstance(BlobStore.class);
      fetchBlobMetadata = injector.getInstance(FetchBlobMetadata.class);
      fetchBlobMetadata.setContainerName(CONTAINER_NAME);
   }

   @AfterClass
   public void closeBlobSore() {
      if (blobStore != null) {
         Closeables2.closeQuietly(blobStore.getContext());
      }
   }

   @Test
   public void testRetainsOriginalOrder() {
      blobStore.createContainerInLocation(null, CONTAINER_NAME);
      for (int blobIndex = 0; blobIndex < 20; blobIndex++) {
         final Blob blob = blobStore.blobBuilder("prefix-" + blobIndex).payload("").build();
         blobStore.putBlob(CONTAINER_NAME, blob);
      }

      final PageSet<? extends StorageMetadata> pageSet =
              blobStore.list(CONTAINER_NAME, ListContainerOptions.Builder.withDetails());
      final PageSet<? extends StorageMetadata> resultPageSet = fetchBlobMetadata.apply(pageSet);
      assertNotNull(resultPageSet);

      assertTrue(Ordering.from(new Comparator<StorageMetadata>() {
         @Override
         public int compare(StorageMetadata o1, StorageMetadata o2) {
            return o1.getName().compareTo(o2.getName());
         }
      }).isOrdered(resultPageSet));
   }
}
