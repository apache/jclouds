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
package org.jclouds.openstack.nova.v2_0.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import jakarta.inject.Inject;

import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.domain.Location;
import org.jclouds.openstack.nova.v2_0.domain.Image.Status;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.ImageInRegion;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;

/**
 * A function for transforming a nova-specific Image into a generic Image object.
 */
public class ImageInRegionToImage implements Function<ImageInRegion, Image> {
   private final Map<Status, org.jclouds.compute.domain.Image.Status> toPortableImageStatus;
   private final Function<org.jclouds.openstack.nova.v2_0.domain.Image, OperatingSystem> imageToOs;
   private final Supplier<Map<String, Location>> locationIndex;

   @Inject
   public ImageInRegionToImage(Map<org.jclouds.openstack.nova.v2_0.domain.Image.Status, Image.Status> toPortableImageStatus,
            Function<org.jclouds.openstack.nova.v2_0.domain.Image, OperatingSystem> imageToOs,
            Supplier<Map<String, Location>> locationIndex) {
      this.toPortableImageStatus = checkNotNull(toPortableImageStatus, "toPortableImageStatus");
      this.imageToOs = checkNotNull(imageToOs, "imageToOs");
      this.locationIndex = checkNotNull(locationIndex, "locationIndex");
   }

   @Override
   public Image apply(ImageInRegion imageInRegion) {
      Location location = locationIndex.get().get(imageInRegion.getRegion());
      checkState(location != null, "location %s not in locationIndex: %s", imageInRegion.getRegion(), locationIndex.get());
      org.jclouds.openstack.nova.v2_0.domain.Image image = imageInRegion.getImage();
      return new ImageBuilder().id(imageInRegion.slashEncode()).providerId(image.getId()).name(image.getName())
               .userMetadata(image.getMetadata()).operatingSystem(imageToOs.apply(image)).description(image.getName())
               .location(location).status(toPortableImageStatus.get(image.getStatus())).build();
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).toString();
   }
}
