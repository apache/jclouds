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
import static com.google.common.base.Preconditions.checkState;
import static org.jclouds.util.Predicates2.retry;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GlacierBlobStore extends BaseBlobStore {
   public static final long DEFAULT_INVENTORY_UPDATE_TIME = TimeUnit.HOURS.toMillis(24);

   @Inject(optional = true)
   @Named("jclouds.glacier.inventory.update.time")
   private final long inventoryUpdateTime = DEFAULT_INVENTORY_UPDATE_TIME;

   private final GlacierClient sync;
   private final Provider<MultipartUploadStrategy> multipartUploadStrategy;
   private final Provider<PollingStrategy> pollingStrategy;
   private final PaginatedVaultCollectionToStorageMetadata vaultsToContainers;
   private final ArchiveMetadataCollectionToStorageMetadata archivesToBlobs;
   private final ListContainerOptionsToInventoryRetrievalJobRequest containerOptionsToInventoryRetrieval;

   @Inject
   GlacierBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
                    @Memoized Supplier<Set<? extends Location>> locations, GlacierClient sync,
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
   }

   /**
    * Deletes the container and all its blobs.
    * Inventories will be retrieved until the container is gone. Since inventories need 24 hours to be updated this
    * operation may take days.
    *
    * @param container
    *          container name
    * @see <a href="http://aws.amazon.com/glacier/faqs/#data-inventories" />
    */
   @Override
   public void deleteContainer(String container) {
      // attempt to delete possibly-empty vault to avoid inventory retrieval
      if (!sync.deleteVault(container)) {
         deletePathAndEnsureGone(container);
      }
   }

   @Override
   protected void deletePathAndEnsureGone(String container) {
      checkState(retry(new Predicate<String>() {
          public boolean apply(String container) {
             clearContainer(container);
             return sync.deleteVault(container);
          }
       }, inventoryUpdateTime).apply(container), "%s still exists after deleting!", container);
   }

   @Override
   protected boolean deleteAndVerifyContainerGone(String container) {
      return sync.deleteVault(container);
   }

   /**
    * Lists the containers.
    *
    * @return a PageSet of StorageMetadata
    */
   @Override
   public PageSet<? extends StorageMetadata> list() {
      return vaultsToContainers.apply(sync.listVaults());
   }

   /**
    * Checks if the container exists.
    * This implementation invokes {@link GlacierClient#describeVault(String)}.
    *
    * @param container
    *          container name
    * @return true if the vault exists, false otherwise
    */
   @Override
   public boolean containerExists(String container) {
      return sync.describeVault(container) != null;
   }

   /**
    * Creates a container.
    * Location is currently ignored.
    *
    * @param location
    *          currently ignored
    * @param container
    *          container name
    * @return true if the container was created, false otherwise
    */
   @Override
   public boolean createContainerInLocation(@Nullable Location location, String container) {
      return sync.createVault(container) != null;
   }

   /**
    * Creates a container.
    * Location and options are currently ignored.
    *
    * @param location
    *          currently ignored
    * @param container
    *          container name
    * @param options
    *          currently ignored
    * @return true if the container was created, false otherwise
    */
   @Override
   public boolean createContainerInLocation(@Nullable Location location, String container,
                                            CreateContainerOptions options) {
      return createContainerInLocation(location, container);
   }

   /**
    * Lists the blobs in the container.
    * An inventory will be retrieved to obtain the list. Note that this will take hours and the result may be
    * inaccurate (Inventories are updated every 24 hours).
    *
    * @param container
    *          container name
    * @param listContainerOptions
    *          list options
    * @return the blob list
    * @see <a href="http://aws.amazon.com/glacier/faqs/#data-inventories" />
    */
   @Override
   public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions listContainerOptions) {
      String jobId = sync.initiateJob(container, containerOptionsToInventoryRetrieval.apply(listContainerOptions));
      try {
         if (pollingStrategy.get().waitForSuccess(container, jobId)) {
            return archivesToBlobs.apply(sync.getInventoryRetrievalOutput(container, jobId));
         }
      } catch (InterruptedException e) {
         Throwables.propagate(e);
      }
      return null;
   }

   /**
    * Checks if the blob exists in the container.
    * An inventory will be retrieved to obtain the blob list and the method will iterate through it. This operation
    * will take several hours and the result may be inaccurate (Inventories are updated every 24 hours).
    *
    * @param container
    *          container name
    * @param key
    *          blob key
    * @return true if the blob exists, false otherwise
    * @see <a href="http://aws.amazon.com/glacier/faqs/#data-inventories" />
    */
   @Override
   public boolean blobExists(String container, String key) {
      return blobMetadata(container, key) != null;
   }

   /**
    * Stores a blob in a container. The blob name will be ignored, since it's not supported by Glacier.
    *
    * @param container
    *          container name
    * @param blob
    *          blob to upload
    * @return the blob name
    */
   @Override
   public String putBlob(String container, Blob blob) {
      return sync.uploadArchive(container, blob.getPayload());
   }

   /**
    * Stores the blob in a container.
    *
    * @param container
    *          container name
    * @param blob
    *          blob to upload
    * @param options
    *          upload options.
    * @return the blob name
    */
   @Override
   public String putBlob(String container, Blob blob, PutOptions options) {
      if (options.isMultipart()) {
         return multipartUploadStrategy.get().execute(container, blob);
      }
      return putBlob(container, blob);
   }

   /**
    * Retrieves the blob metadata.
    * An inventory will be retrieved to obtain the blob list and the method will iterate through it. This operation
    * will take several hours and the result may be inaccurate (Inventories are updated every 24 hours).
    *
    * @param container
    *          container name
    * @param key
    *          blob name
    * @return null if the blob doesn't exist, the blob metadata otherwise
    * @see <a href="http://aws.amazon.com/glacier/faqs/#data-inventories" />
    */
   @Override
   public BlobMetadata blobMetadata(String container, String key) {
      PageSet<? extends StorageMetadata> blobMetadataSet = list(container, null);
      for (StorageMetadata blob : blobMetadataSet) {
         if (blob.getName().equals(key)) {
            return (BlobMetadata) blob;
         }
      }
      return null;
   }

   private static ArchiveRetrievalJobRequest buildArchiveRetrievalRequest(String key, GetOptions getOptions) {
      ArchiveRetrievalJobRequest.Builder requestBuilder = ArchiveRetrievalJobRequest.builder().archiveId(key);
      if (getOptions != null) {
         int size = getOptions.getRanges().size();
         checkArgument(size <= 1, "The number of ranges should be zero or one");
         if (size == 1) {
            requestBuilder.range(ContentRange.fromString(getOptions.getRanges().get(0)));
         }
      }
      return requestBuilder.build();
   }

   /**
    * Retrieves the blob
    * This operation will take several hours.
    *
    * @param container
    *          container name
    * @param key
    *          blob name
    * @return null if the blob doesn't exist or the archive retrieval fails, the blob otherwise
    */
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
      } catch (InterruptedException e) {
         Throwables.propagate(e);
      }
      return null;
   }

   /**
    * Deletes the blob.
    *
    * @param container
    *          container name
    * @param key
    *          blob name
    */
   @Override
   public void removeBlob(String container, String key) {
      sync.deleteArchive(container, key);
   }
}
