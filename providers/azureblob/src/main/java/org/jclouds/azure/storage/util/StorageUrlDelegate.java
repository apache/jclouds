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
package org.jclouds.azure.storage.util;

import static org.jclouds.azure.storage.reference.AzureConstants.PROPERTY_AZURE_VIRTUAL_HOST_STORAGE_ACCOUNT;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.domain.Credentials;
import org.jclouds.providers.ProviderMetadata;

import com.google.common.base.Supplier;

@Singleton
public class StorageUrlDelegate {

   private ProviderMetadata providerMetadata;
   private Supplier<Credentials> credentialsSupplier;
   private boolean storageAccountInsideVirtualHost;

   @Inject
   public StorageUrlDelegate(ProviderMetadata providerMetadata,
         @org.jclouds.location.Provider Supplier<Credentials> creds,
         @Named(PROPERTY_AZURE_VIRTUAL_HOST_STORAGE_ACCOUNT) boolean storageAccountInsideVirtualHost) {
      this.providerMetadata = providerMetadata;
      this.credentialsSupplier = creds;
      this.storageAccountInsideVirtualHost = storageAccountInsideVirtualHost;
   }

   public String configureStorageUrl() {

      StringBuilder builder = new StringBuilder();
      if (providerMetadata != null && providerMetadata.getEndpoint() != null
            && !providerMetadata.getEndpoint().isEmpty()) {
         builder.append(providerMetadata.getEndpoint());
         if (!builder.substring(builder.length()).equals("/")) {
            builder.append("/");
         }
      } else {
         builder.append("https://" + credentialsSupplier.get().identity + ".blob.core.windows.net/");
      }

      if (!storageAccountInsideVirtualHost) {
         builder.append(credentialsSupplier.get().identity).append("/");
      }

      return builder.toString();
   }

}
