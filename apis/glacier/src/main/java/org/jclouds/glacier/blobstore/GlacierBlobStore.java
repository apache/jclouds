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
package org.jclouds.glacier.blobstore;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.crypto.Crypto;
import org.jclouds.domain.Location;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.inject.Inject;

public class GlacierBlobStore extends BaseBlobStore {
   private final GlacierClient sync;
   private final Crypto crypto;

   @Inject
   GlacierBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
                    @Memoized Supplier<Set<? extends Location>> locations, GlacierClient sync, Crypto crypto) {
      super(context, blobUtils, defaultLocation, locations);
      this.sync = checkNotNull(sync, "sync");
      this.crypto = checkNotNull(crypto, "crypto");
   }

   @Override
   protected boolean deleteAndVerifyContainerGone(String container) {
      return sync.deleteVault(container);
   }

   @Override
   public PageSet<? extends StorageMetadata> list() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containerExists(String container) {
      return sync.describeVault(container) != null;
   }

   @Override
   public boolean createContainerInLocation(@Nullable Location location, String container) {
      return sync.createVault(container) != null;
   }

   @Override
   public boolean createContainerInLocation(@Nullable Location location, String container,
                                            CreateContainerOptions options) {
      return createContainerInLocation(location, container);
   }

   @Override
   public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions listContainerOptions) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean blobExists(String container, String key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String putBlob(String container, Blob blob) {
      return sync.uploadArchive(container, blob.getPayload(), blob.getMetadata().getName());
   }

   @Override
   public String putBlob(String container, Blob blob, PutOptions options) {
      throw new UnsupportedOperationException();
   }

   @Override
   public BlobMetadata blobMetadata(String container, String key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Blob getBlob(String container, String key, GetOptions getOptions) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeBlob(String container, String key) {
      throw new UnsupportedOperationException();
   }
}
