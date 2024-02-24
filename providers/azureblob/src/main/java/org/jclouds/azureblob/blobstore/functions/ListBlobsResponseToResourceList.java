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
package org.jclouds.azureblob.blobstore.functions;

import java.util.SortedSet;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.azureblob.domain.ListBlobsResponse;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.functions.PrefixToResourceMetadata;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Singleton
public class ListBlobsResponseToResourceList implements
         Function<ListBlobsResponse, PageSet<? extends StorageMetadata>> {
   private final BlobPropertiesToBlobMetadata object2blobMd;
   private final PrefixToResourceMetadata prefix2ResourceMd;

   protected final Function<StorageMetadata, String> indexer = new Function<StorageMetadata, String>() {
      @Override
      public String apply(StorageMetadata from) {
         return from.getName();
      }
   };

   @Inject
   public ListBlobsResponseToResourceList(BlobPropertiesToBlobMetadata object2blobMd,
            PrefixToResourceMetadata prefix2ResourceMd) {
      this.object2blobMd = object2blobMd;
      this.prefix2ResourceMd = prefix2ResourceMd;
   }

   public PageSet<? extends StorageMetadata> apply(ListBlobsResponse from) {
      // use sorted set to order relative paths correctly
      SortedSet<StorageMetadata> contents = Sets.<StorageMetadata> newTreeSet(Iterables.transform(from,
               object2blobMd));

      for (String prefix : from.getBlobPrefixes()) {
         contents.add(prefix2ResourceMd.apply(prefix));
      }
      return new PageSetImpl<StorageMetadata>(contents, from.getNextMarker());
   }
}
