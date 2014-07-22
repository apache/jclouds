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
package org.jclouds.glacier.blobstore.config;

import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.strategy.ClearListStrategy;
import org.jclouds.glacier.blobstore.GlacierAsyncBlobStore;
import org.jclouds.glacier.blobstore.GlacierBlobStore;
import org.jclouds.glacier.blobstore.strategy.ClearVaultStrategy;
import org.jclouds.glacier.blobstore.strategy.MultipartUploadStrategy;
import org.jclouds.glacier.blobstore.strategy.PollingStrategy;
import org.jclouds.glacier.blobstore.strategy.SlicingStrategy;
import org.jclouds.glacier.blobstore.strategy.internal.BasePollingStrategy;
import org.jclouds.glacier.blobstore.strategy.internal.BaseSlicingStrategy;
import org.jclouds.glacier.blobstore.strategy.internal.SequentialMultipartUploadStrategy;

import com.google.inject.AbstractModule;

public class GlacierBlobStoreContextModule extends AbstractModule {
   @Override
   protected void configure() {
      bind(ConsistencyModel.class).toInstance(ConsistencyModel.EVENTUAL);
      bind(BlobStore.class).to(GlacierBlobStore.class);
      bind(AsyncBlobStore.class).to(GlacierAsyncBlobStore.class);
      bind(MultipartUploadStrategy.class).to(SequentialMultipartUploadStrategy.class);
      bind(SlicingStrategy.class).to(BaseSlicingStrategy.class);
      bind(ClearListStrategy.class).to(ClearVaultStrategy.class);
      bind(PollingStrategy.class).to(BasePollingStrategy.class);
   }
}
