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

import static com.google.common.base.Preconditions.checkState;
import static org.jclouds.concurrent.FutureIterables.transformParallel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.inject.Named;

import com.google.common.util.concurrent.Futures;
import org.jclouds.Constants;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.reference.BlobStoreConstants;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.javax.annotation.concurrent.NotThreadSafe;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;

/**
 * Retrieves all blobmetadata in the list as efficiently as possible
 */
@NotThreadSafe
public class FetchBlobMetadata implements Function<PageSet<? extends StorageMetadata>, PageSet<? extends StorageMetadata>> {

   protected final BackoffLimitedRetryHandler retryHandler;
   protected final BlobStore blobstore;
   protected final ListeningExecutorService userExecutor;
   @Resource
   @Named(BlobStoreConstants.BLOBSTORE_LOGGER)
   protected Logger logger = Logger.NULL;

   private String container;
   /**
    * maximum duration of an blob Request
    */
   @Inject(optional = true)
   @Named(Constants.PROPERTY_REQUEST_TIMEOUT)
   protected Long maxTime;

   @Inject
   FetchBlobMetadata(@Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor, BlobStore blobstore,
            BackoffLimitedRetryHandler retryHandler) {
      this.userExecutor = userExecutor;
      this.blobstore = blobstore;
      this.retryHandler = retryHandler;
   }

   public FetchBlobMetadata setContainerName(String container) {
      this.container = container;
      return this;
   }

   public PageSet<? extends StorageMetadata> apply(PageSet<? extends StorageMetadata> in) {
      checkState(container != null, "container name should be initialized");

      if (in == null) {
         return new PageSetImpl<>(Collections.<StorageMetadata>emptyList(), null);
      }

      Map<String, StorageMetadata> orderedMap = new LinkedHashMap<>(in.size());
      for (StorageMetadata storageMetadata : in) {
         orderedMap.put(storageMetadata.getName(), null);
      }

      Iterable<StorageMetadata> returnv = Lists.newArrayList(transformParallel(in,
          new Function<StorageMetadata, ListenableFuture<? extends StorageMetadata>>() {

         @Override
         public ListenableFuture<StorageMetadata> apply(final StorageMetadata from) {
            if (from.getType() != StorageType.BLOB) {
               return Futures.immediateFuture(from);
            }
            return userExecutor.submit(new Callable<StorageMetadata>() {
               @Override public StorageMetadata call() {
                  return blobstore.blobMetadata(container, from.getName());
               }
            });
         }

      }, userExecutor, maxTime, logger, String.format("getting metadata from containerName: %s", container)));

      for (StorageMetadata storageMetadata : returnv) {
         orderedMap.put(storageMetadata.getName(), storageMetadata);
      }

      return new PageSetImpl<>(orderedMap.values(), in.getNextMarker());
   }
}
