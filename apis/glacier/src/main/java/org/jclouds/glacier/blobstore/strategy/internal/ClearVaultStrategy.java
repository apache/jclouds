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
package org.jclouds.glacier.blobstore.strategy.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.strategy.ClearListStrategy;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.glacier.blobstore.strategy.PollingStrategy;
import org.jclouds.glacier.domain.ArchiveMetadata;
import org.jclouds.glacier.domain.ArchiveMetadataCollection;
import org.jclouds.glacier.domain.InventoryRetrievalJobRequest;
import org.jclouds.rest.ResourceNotFoundException;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An inventory will be retrieved to obtain the blob list and the method will iterate through it deleting the blobs.
 * This operation will take several hours and the result may be inaccurate (Inventories are updated every 24 hours).
 */
@Singleton
public class ClearVaultStrategy implements ClearListStrategy {
   private final GlacierClient client;
   private final PollingStrategy pollingStrategy;

   @Inject
   public ClearVaultStrategy(GlacierClient client, PollingStrategy pollingStrategy) {
      this.client = checkNotNull(client, "client");
      this.pollingStrategy = checkNotNull(pollingStrategy, "pollingStrategy");
   }

   @Override
   public void execute(String container, ListContainerOptions listContainerOptions) {
      String jobId = client.initiateJob(container, InventoryRetrievalJobRequest.builder().build());
      try {
         if (pollingStrategy.waitForSuccess(container, jobId)) {
            ArchiveMetadataCollection archives = client.getInventoryRetrievalOutput(container, jobId);
            for (ArchiveMetadata archive : archives) {
               try {
                  client.deleteArchive(container, archive.getArchiveId());
               } catch (ResourceNotFoundException ignored) {
               }
            }
         }
      } catch (InterruptedException e) {
         Throwables.propagate(e);
      }
   }
}
