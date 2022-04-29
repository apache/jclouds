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
package org.jclouds.azurecompute.arm.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.tryFind;
import static java.util.Arrays.asList;
import static org.jclouds.location.predicates.LocationPredicates.idEquals;

import java.util.Map;
import java.util.Set;

import org.jclouds.azurecompute.arm.compute.extensions.AzureComputeImageExtension;
import org.jclouds.azurecompute.arm.compute.options.AzureTemplateOptions;
import org.jclouds.azurecompute.arm.domain.Plan;
import org.jclouds.azurecompute.arm.domain.VMImage;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.domain.Location;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class VMImageToImage implements Function<VMImage, Image> {

   private static final Map<String, OsFamily> OTHER_OS_MAP = ImmutableMap.<String, OsFamily> builder()
         .put("openlogic", OsFamily.CENTOS)
         .put("win", OsFamily.WINDOWS)
         .put("sles", OsFamily.SUSE)
         .put("oracle-linux", OsFamily.OEL)
         .build();

   private final Supplier<Set<? extends org.jclouds.domain.Location>> locations;

   @Inject
   VMImageToImage(@Memoized Supplier<Set<? extends Location>> locations) {
      this.locations = locations;
   }

   @Override
   public Image apply(final VMImage image) {
      final ImageBuilder builder = new ImageBuilder();
      addMarketplacePlanToMetadataIfPresent(builder, image);
      
      Location location = FluentIterable.from(locations.get()).firstMatch(idEquals(image.location())).get();
      
      if (image.custom()) {
         builder
            .id(image.encodeFieldsToUniqueIdCustom())
            .providerId(image.customImageId())
            .name(image.name())
            .location(location)
            .description(image.group())
            .status(Image.Status.AVAILABLE)
            .version("latest");
      } else {
         builder
            .id(image.encodeFieldsToUniqueId())
            .providerId(image.publisher())
            .name(image.offer())
            .location(location)
            .description(image.sku())
            .status(Image.Status.AVAILABLE)
            .version(image.sku());
      }
      
      final OperatingSystem.Builder osBuilder = osFamily().apply(image);
      builder.operatingSystem(osBuilder.build());
      
      return builder.build();
   }
   
   private static void addMarketplacePlanToMetadataIfPresent(ImageBuilder builder, VMImage image) {
      if (image.versionProperties() != null && image.versionProperties().plan() != null) {
         // Store the plan information in the metadata so the adapter can
         // properly configure it when deploying images from the marketplace
         Plan plan = image.versionProperties().plan();
         builder.userMetadata(ImmutableMap.of("publisher", plan.publisher(), "name", plan.name(), "product",
               plan.product()));
      }
   }

   /**
    * In case that image is offered from Azure Marketplace then it requires to provide "Plan Information" (publisher/name/product)
    * when creating VM from such an image. This method first tries to get this information from image but also allows to
    * override those values by information passed in Azure template options (@see AzureTemplateOptions).
    *
    * In case there is used custom image which was created from Marketplace image then plan information is missing as this image
    * cannot be referenced in format "location/publisher/offer/sku" from which Plan Information is normally parsed.
    * In this case user can provide plan information (planPublisher/planName/planProduct) in template otherwise VM creation fails with error.
    * It's allowed to override any of the original "image" plan information but only if those were present.
    *
    * @param image image
    * @param templateOptions Azure template options
    * @return Plan Information (publisher, plan name, product) or null if any of publisher/name/product not defined
    */
   @Nullable
   public static Plan createMarketplacePlanIfPresent(Image image, AzureTemplateOptions templateOptions) {
      Map<String, String> imageMetadata = image.getUserMetadata();

      String planPublisher = getFirstNonEmptyOrReturnNull(templateOptions.getPlanPublisher(), imageMetadata.get("publisher"));
      String planName = getFirstNonEmptyOrReturnNull(templateOptions.getPlanName(), imageMetadata.get("name"));
      String planProduct = getFirstNonEmptyOrReturnNull(templateOptions.getPlanProduct(), imageMetadata.get("product"));

      if (Strings.isNullOrEmpty(planPublisher) || Strings.isNullOrEmpty(planName) || Strings.isNullOrEmpty(planProduct)) {
         return null;
      } else {
         return Plan.create(planPublisher, planName, planProduct);
      }
   }

   @Nullable
   private static String getFirstNonEmptyOrReturnNull(String first, String second)   {
      if (!Strings.isNullOrEmpty(first))   {
         return first;
      } else if (!Strings.isNullOrEmpty(second))   {
         return second;
      } else {
         return null;
      }
   }

   public static Function<VMImage, OperatingSystem.Builder> osFamily() {
      return new Function<VMImage, OperatingSystem.Builder>() {
         @Override
         public OperatingSystem.Builder apply(final VMImage image) {
            final String label = checkNotNull(image.offer(), "offer").toLowerCase();
            OsFamily family = findInStandardFamilies(label).or(findInOtherOSMap(label)).or(OsFamily.UNRECOGNIZED);
            
            // Fallback to generic operating system type
            if (OsFamily.UNRECOGNIZED == family && image.versionProperties() != null
                  && image.versionProperties().osDiskImage() != null
                  && image.versionProperties().osDiskImage().operatingSystem() != null) {
               family = OsFamily.fromValue(image.versionProperties().osDiskImage().operatingSystem().toUpperCase());
            }

            // only 64bit OS images are supported by Azure ARM
            return OperatingSystem.builder().family(family).is64Bit(true)
                  .description(image.custom() ? AzureComputeImageExtension.CUSTOM_IMAGE_OFFER : image.sku())
                  .version(image.custom() ? "latest" : image.sku());
         }
      };
   }

   private static Optional<OsFamily> findInStandardFamilies(final String label) {
      return tryFind(asList(OsFamily.values()), new Predicate<OsFamily>() {
         @Override
         public boolean apply(OsFamily input) {
            return label.contains(input.value());
         }
      });
   }

   private static Optional<OsFamily> findInOtherOSMap(final String label) {
      return tryFind(OTHER_OS_MAP.keySet(), new Predicate<String>() {
         @Override
         public boolean apply(String input) {
            return label.contains(input);
         }
      }).transform(new Function<String, OsFamily>() {
         @Override
         public OsFamily apply(String input) {
            return OTHER_OS_MAP.get(input);
         }
      });
   }

}
