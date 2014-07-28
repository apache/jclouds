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

import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.glacier.domain.InventoryRetrievalJobRequest;

import com.google.common.base.Function;

public class ListContainerOptionsToInventoryRetrievalJobRequest implements Function<ListContainerOptions,
      InventoryRetrievalJobRequest>  {
   @Override
   public InventoryRetrievalJobRequest apply(ListContainerOptions listContainerOptions) {
      InventoryRetrievalJobRequest.Builder builder = InventoryRetrievalJobRequest.builder();
      if (listContainerOptions != null) {
         if (listContainerOptions.getMarker() != null) {
            builder.marker(listContainerOptions.getMarker());
         }
         if (listContainerOptions.getMaxResults() != null) {
            builder.limit(listContainerOptions.getMaxResults());
         }
      }
      return builder.build();
   }
}
