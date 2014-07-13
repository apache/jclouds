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

import org.jclouds.Constants;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.internal.BaseAsyncBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.crypto.Crypto;
import org.jclouds.domain.Location;
import org.jclouds.glacier.GlacierAsyncClient;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class GlacierAsyncBlobStore extends BaseAsyncBlobStore {
   private final GlacierAsyncClient async;
   private final GlacierClient sync;
   private final Crypto crypto;

   @Inject
   GlacierAsyncBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
                         @Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor,
                         @Memoized Supplier<Set<? extends Location>> locations, GlacierAsyncClient async, Crypto crypto,
                         GlacierClient sync) {
      super(context, blobUtils, userExecutor, defaultLocation, locations);
      this.sync = checkNotNull(sync, "sync");
      this.async = checkNotNull(async, "async");
      this.crypto = checkNotNull(crypto, "crypto");
   }

   @Override
   protected boolean deleteAndVerifyContainerGone(String container) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<PageSet<? extends StorageMetadata>> list() {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Boolean> containerExists(String container) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Boolean> createContainerInLocation(@Nullable Location location, String container) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Boolean> createContainerInLocation(@Nullable Location location, String container,
                                                              CreateContainerOptions options) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<PageSet<? extends StorageMetadata>> list(String container,
                                                                    ListContainerOptions listContainerOptions) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Boolean> blobExists(String container, String key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<String> putBlob(String container, Blob blob) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<String> putBlob(String container, Blob blob, PutOptions options) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<BlobMetadata> blobMetadata(String container, String key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Blob> getBlob(String container, String key, GetOptions getOptions) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ListenableFuture<Void> removeBlob(String container, String key) {
      throw new UnsupportedOperationException();
   }
}
