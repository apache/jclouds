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

import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.glacier.domain.ArchiveMetadataCollection;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Converts ArchiveMetadataCollection into PageSet<StorageMetadata>
 */
public class ArchiveMetadataCollectionToStorageMetadata implements Function<ArchiveMetadataCollection,
      PageSet<? extends StorageMetadata>>  {
   @Override
   public PageSet<? extends StorageMetadata> apply(ArchiveMetadataCollection archives) {
      return new PageSetImpl<StorageMetadata>(Iterables.transform(archives, new ArchiveMetadataToBlobMetadata()), null);
   }
}
