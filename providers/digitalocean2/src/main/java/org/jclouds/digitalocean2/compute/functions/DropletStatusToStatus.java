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
package org.jclouds.digitalocean2.compute.functions;

import jakarta.inject.Singleton;

import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.digitalocean2.domain.Droplet;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

/**
 * Transforms an {@link org.jclouds.compute.domain.NodeMetadata.Status} to the jclouds portable model.
 */
@Singleton
public class DropletStatusToStatus implements Function<Droplet.Status, Status> {

   private static final Function<Droplet.Status, Status> toPortableStatus = Functions.forMap(
         ImmutableMap.<Droplet.Status, Status> builder()
               .put(Droplet.Status.NEW, Status.PENDING)
               .put(Droplet.Status.ACTIVE, Status.RUNNING)
               .put(Droplet.Status.ARCHIVE, Status.TERMINATED)
               .put(Droplet.Status.OFF, Status.SUSPENDED)
               .build(), 
         Status.UNRECOGNIZED);

   @Override
   public Status apply(final Droplet.Status input) {
      return toPortableStatus.apply(input);
   }
}
