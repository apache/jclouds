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
package org.jclouds.azure.storage.util.storageurl;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.jclouds.domain.Credentials;
import org.jclouds.location.Provider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static org.jclouds.azure.storage.util.storageurl.TrailingSlashUtil.ensureTrailingSlash;
import java.net.URI;

@Singleton
public class AppendAccountToEndpoint implements StorageUrlSupplier {

   private final Supplier<URI> endpointSupplier;
   private final Supplier<Credentials> credentialsSupplier;

   @Inject
   public AppendAccountToEndpoint(@Provider Supplier<URI> endpointSupplier, @Provider Supplier<Credentials> credentialsSupplier) {
      this.endpointSupplier = endpointSupplier;
      this.credentialsSupplier = credentialsSupplier;
   }

   @Override
   public URI get() {

      URI endpoint = endpointSupplier.get();

      Preconditions.checkNotNull(endpoint, "An endpoint must be configured in order to use AppendAccountToEndpoint module");

      String endpointTrailingSlash = new StringBuilder(ensureTrailingSlash(endpoint))
            .append(credentialsSupplier.get().identity)
            .append("/")
            .toString();

      return URI.create(endpointTrailingSlash);
      
   }

}
