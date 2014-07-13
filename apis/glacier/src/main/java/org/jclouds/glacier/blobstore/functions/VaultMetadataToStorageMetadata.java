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
package org.jclouds.glacier.blobstore.functions;

import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.StorageMetadataImpl;
import org.jclouds.glacier.domain.VaultMetadata;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class VaultMetadataToStorageMetadata implements Function<VaultMetadata, StorageMetadata> {
   @Override
   public StorageMetadata apply(VaultMetadata vault) {
      return new StorageMetadataImpl(StorageType.CONTAINER, vault.getVaultARN(), vault.getVaultName(), null, null, null,
            vault.getCreationDate(), vault.getLastInventoryDate(), ImmutableMap.<String, String>of());
   }
}
