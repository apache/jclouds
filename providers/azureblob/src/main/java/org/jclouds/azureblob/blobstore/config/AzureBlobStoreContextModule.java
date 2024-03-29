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
package org.jclouds.azureblob.blobstore.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import org.jclouds.azure.storage.config.AuthType;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.AzureBlobRequestSigner;
import org.jclouds.azureblob.blobstore.AzureBlobStore;
import org.jclouds.azureblob.config.InsufficientAccessRightsException;
import org.jclouds.azureblob.domain.PublicAccess;
import org.jclouds.blobstore.BlobRequestSigner;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class AzureBlobStoreContextModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(ConsistencyModel.class).toInstance(ConsistencyModel.STRICT);
      bind(BlobStore.class).to(AzureBlobStore.class).in(Scopes.SINGLETON);
      bind(BlobRequestSigner.class).to(AzureBlobRequestSigner.class);
   }

   @Provides
   @Singleton
   protected final LoadingCache<String, PublicAccess> containerAcls(final AzureBlobClient client, @Named("sasAuth") final boolean sasAuthentication, AuthType authType) {
      return CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build(
               new CacheLoader<String, PublicAccess>() {
                  @Override
                  public PublicAccess load(String container) throws CacheLoader.InvalidCacheLoadException {
                     if (!sasAuthentication && authType == AuthType.AZURE_KEY) {
                        return client.getPublicAccessForContainer(container);
                     }
                     throw new InsufficientAccessRightsException("SAS Authentication does not support getAcl and setAcl calls.");
                  }

                  @Override
                  public String toString() {
                     return "getPublicAccessForContainer()";
                  }
               });
   }
}
