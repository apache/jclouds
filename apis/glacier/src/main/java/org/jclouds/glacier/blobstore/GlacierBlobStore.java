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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
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
import org.jclouds.glacier.blobstore.functions.ArchiveMetadataCollectionToStorageMetadata;
import org.jclouds.glacier.blobstore.functions.ListContainerOptionsToInventoryRetrievalJobRequest;
import org.jclouds.glacier.blobstore.functions.PaginatedVaultCollectionToStorageMetadata;
import org.jclouds.glacier.blobstore.strategy.MultipartUploadStrategy;
import org.jclouds.glacier.blobstore.strategy.PollingStrategy;
import org.jclouds.glacier.domain.ArchiveRetrievalJobRequest;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class GlacierBlobStore extends BaseBlobStore {
   private final GlacierClient sync;
   private final Crypto crypto;
   private final Provider<MultipartUploadStrategy> multipartUploadStrategy;
   private final Provider<PollingStrategy> pollingStrategy;
   private final PaginatedVaultCollectionToStorageMetadata vaultsToContainers;
   private final ArchiveMetadataCollectionToStorageMetadata archivesToBlobs;
   private final ListContainerOptionsToInventoryRetrievalJobRequest containerOptionsToInventoryRetrieval;

   @Inject
   GlacierBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
                    @Memoized Supplier<Set<? extends Location>> locations, GlacierClient sync, Crypto crypto,
                    Provider<MultipartUploadStrategy> multipartUploadStrategy,
                    Provider<PollingStrategy> pollingStrategy,
                    PaginatedVaultCollectionToStorageMetadata vaultsToContainers,
                    ArchiveMetadataCollectionToStorageMetadata archivesToBlobs, ListContainerOptionsToInventoryRetrievalJobRequest containerOptionsToInventoryRetrieval) {
      super(context, blobUtils, defaultLocation, locations);
      this.containerOptionsToInventoryRetrieval = checkNotNull(containerOptionsToInventoryRetrieval,
            "containerOptionsToInventoryRetrieval");
      this.archivesToBlobs = checkNotNull(archivesToBlobs, "archivesToBlobs");
      this.pollingStrategy = checkNotNull(pollingStrategy, "pollingStrategy");
      this.vaultsToContainers = checkNotNull(vaultsToContainers, "vaultsToContainers");
      this.multipartUploadStrategy = checkNotNull(multipartUploadStrategy, "multipartUploadStrategy");
      this.sync = checkNotNull(sync, "sync");
      this.crypto = checkNotNull(crypto, "crypto");
   }

   @Override
   protected boolean deleteAndVerifyContainerGone(String container) {
      return sync.deleteVault(container);
   }

   @Override
   public PageSet<? extends StorageMetadata> list() {
      return vaultsToContainers.apply(sync.listVaults());
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
      String jobId = sync.initiateJob(container, containerOptionsToInventoryRetrieval.apply(listContainerOptions));
      try {
         if (pollingStrategy.get().waitForSuccess(container, jobId)) {
            return archivesToBlobs.apply(sync.getInventoryRetrievalOutput(container, jobId));
         }
         return null;
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean blobExists(String container, String key) {
      return this.blobMetadata(container, key) != null;
   }

   @Override
   public String putBlob(String container, Blob blob) {
      return sync.uploadArchive(container, blob.getPayload());
   }

   @Override
   public String putBlob(String container, Blob blob, PutOptions options) {
      if (options.isMultipart()) {
         return multipartUploadStrategy.get().execute(container, blob);
      }
      return putBlob(container, blob);
   }

   @Override
   public BlobMetadata blobMetadata(String container, String key) {
      PageSet<? extends StorageMetadata> blobs = this.list(container, null);
      for (StorageMetadata blob : blobs) {
         if (blob.getName().equals(key)) {
            return (BlobMetadata) blob;
         }
      }
      return null;
   }

   private ArchiveRetrievalJobRequest buildArchiveRetrievalRequest(String key, GetOptions getOptions) {
      ArchiveRetrievalJobRequest.Builder requestBuilder = ArchiveRetrievalJobRequest.builder().archiveId(key);
      if (getOptions != null) {
         checkArgument(getOptions.getRanges().size() <= 1);
         if (getOptions.getRanges().size() == 1) {
            requestBuilder.range(ContentRange.fromString(getOptions.getRanges().get(0)));
         }
      }
      return requestBuilder.build();
   }

   @Override
   public Blob getBlob(String container, String key, GetOptions getOptions) {
      String jobId = sync.initiateJob(container, buildArchiveRetrievalRequest(key, getOptions));
      try {
         if (pollingStrategy.get().waitForSuccess(container, jobId)) {
            MutableBlobMetadata blobMetadata = new MutableBlobMetadataImpl();
            blobMetadata.setContainer(container);
            blobMetadata.setName(key);

            Blob blob = new BlobImpl(blobMetadata);
            blob.setPayload(sync.getJobOutput(container, jobId));
            return blob;
         }
         return null;
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void removeBlob(String container, String key) {
      sync.deleteArchive(container, key);
   }
}
