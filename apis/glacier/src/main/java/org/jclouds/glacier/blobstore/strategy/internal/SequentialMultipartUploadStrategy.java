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

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.glacier.blobstore.strategy.MultipartUploadStrategy;
import org.jclouds.glacier.blobstore.strategy.PayloadSlice;
import org.jclouds.glacier.blobstore.strategy.SlicingStrategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Base implementation of MultipartUploadStrategy.
 * This implementation uploads the parts sequentially.
 */
@Singleton
public class SequentialMultipartUploadStrategy implements MultipartUploadStrategy {
   private final GlacierClient client;
   private final SlicingStrategy slicer;

   @Inject
   public SequentialMultipartUploadStrategy(GlacierClient client, SlicingStrategy slicer) {
      this.client = checkNotNull(client, "client");
      this.slicer = checkNotNull(slicer, "slicer");
   }

   @Override
   public String execute(String container, Blob blob) {
      slicer.startSlicing(blob.getPayload());
      String uploadId = client.initiateMultipartUpload(container, slicer.getPartSizeInMB(),
            blob.getMetadata().getName());
      try {
         ImmutableMap.Builder<Integer, HashCode> hashes = ImmutableMap.builder();
         while (slicer.hasNext()) {
            PayloadSlice slice = slicer.nextSlice();
            hashes.put(slice.getPart(),
                  client.uploadPart(container, uploadId, slice.getRange(), slice.getPayload()));
         }
         return client.completeMultipartUpload(container, uploadId, hashes.build(),
               blob.getPayload().getContentMetadata().getContentLength());
      } catch (RuntimeException exception) {
         client.abortMultipartUpload(container, uploadId);
         throw exception;
      }
   }
}
