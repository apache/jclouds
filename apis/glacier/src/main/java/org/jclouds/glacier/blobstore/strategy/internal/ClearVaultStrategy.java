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

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of ClearListStrategy.
 * This is a long duration operation.
 */
@Singleton
public class ClearVaultStrategy implements ClearListStrategy {
   private final GlacierClient sync;
   private final PollingStrategy pollingStrategy;

   @Inject
   public ClearVaultStrategy(GlacierClient sync, PollingStrategy pollingStrategy) {
      this.pollingStrategy = checkNotNull(pollingStrategy, "pollingStrategy");
      this.sync = checkNotNull(sync, "sync");
   }

   @Override
   public void execute(String container, ListContainerOptions listContainerOptions) {
      String jobId = sync.initiateJob(container, InventoryRetrievalJobRequest.builder().build());
      try {
         if (pollingStrategy.waitForSuccess(container, jobId)) {
            ArchiveMetadataCollection archives = sync.getInventoryRetrievalOutput(container, jobId);
            for (ArchiveMetadata archive : archives) {
               try {
                  sync.deleteArchive(container, archive.getArchiveId());
               } catch (ResourceNotFoundException ignored) {
               }
            }
         }
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }
}
