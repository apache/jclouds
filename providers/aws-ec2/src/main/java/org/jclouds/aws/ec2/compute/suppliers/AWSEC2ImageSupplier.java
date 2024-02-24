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
package org.jclouds.aws.ec2.compute.suppliers;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.jclouds.Constants.PROPERTY_USER_THREADS;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_AMI_QUERY;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_CC_REGIONS;

import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jclouds.aws.ec2.compute.config.ClusterCompute;
import org.jclouds.aws.ec2.compute.config.ImageQuery;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.ec2.compute.domain.RegionAndName;
import org.jclouds.ec2.compute.functions.ImagesToRegionAndIdMap;
import org.jclouds.location.Region;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public final class AWSEC2ImageSupplier implements Supplier<Set<? extends Image>> {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   private Logger logger = Logger.NULL;
   
   private final Set<String> clusterComputeIds;
   private final CallForImages.Factory factory;
   private final ListeningExecutorService userExecutor;

   private final Supplier<Set<String>> regions;
   private final Map<String, String> queries;
   private final Iterable<String> clusterRegions;
   private final Supplier<LoadingCache<RegionAndName, ? extends Image>> cache;
   
   @Inject AWSEC2ImageSupplier(@Region Supplier<Set<String>> regions, @ImageQuery Map<String, String> queries,
         @Named(PROPERTY_EC2_CC_REGIONS) String clusterRegions,
         Supplier<LoadingCache<RegionAndName, ? extends Image>> cache, CallForImages.Factory factory,
         @ClusterCompute Set<String> clusterComputeIds,
         @Named(PROPERTY_USER_THREADS) ListeningExecutorService userExecutor) {
      this.factory = factory;
      this.regions = regions;
      this.queries = queries;
      this.clusterRegions = Splitter.on(',').split(clusterRegions);
      this.cache = cache;
      this.clusterComputeIds = clusterComputeIds;
      this.userExecutor = userExecutor;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public Set<? extends Image> get() {
      String amiQuery = queries.get(PROPERTY_EC2_AMI_QUERY);
      String ccAmiQuery = queries.get(PROPERTY_EC2_CC_AMI_QUERY);

      Set<String> regionIds = regions.get();

      ListenableFuture<Iterable<Image>> normalImages = images(regionIds, amiQuery, PROPERTY_EC2_AMI_QUERY);
      ImmutableSet<Image> clusterImages;
      try {
         clusterImages = ImmutableSet
               .copyOf(images(filter(clusterRegions, in(regionIds)), ccAmiQuery, PROPERTY_EC2_CC_AMI_QUERY).get());
      } catch (Exception e) {
         logger.warn(e, "Error parsing images in query %s", ccAmiQuery);
         throw propagate(e);
      }
      Iterables.addAll(clusterComputeIds, transform(clusterImages, new Function<Image, String>() {

         @Override
         public String apply(Image arg0) {
            return arg0.getId();
         }

      }));

      Iterable<? extends Image> parsedImages;
      try {
         parsedImages = ImmutableSet.copyOf(concat(clusterImages, normalImages.get()));
      } catch (Exception e) {
         logger.warn(e, "Error parsing images in query %s", amiQuery);
         throw propagate(e);
      }

      Map<RegionAndName, ? extends Image> imageMap = ImagesToRegionAndIdMap.imagesToMap(parsedImages);
      cache.get().invalidateAll();
      cache.get().putAll(Map.class.cast(imageMap));
      logger.debug("<< images(%d)", imageMap.size());

      // Forwarding so that later changes to the underlying cache are visible.
      return new ForwardingSet<Image>() {
         protected Set<Image> delegate() {
            return (Set<Image>) ImmutableSet.copyOf(cache.get().asMap().values());
         }
      };
   }
   
   private ListenableFuture<Iterable<Image>> images(Iterable<String> regions, String query, String tag) {
      if (query == null) {
         logger.debug(">> no %s specified, skipping image parsing", tag);
         return Futures.<Iterable<Image>> immediateFuture(ImmutableSet.<Image> of());
      } else {
         return userExecutor.submit(factory.parseImagesFromRegionsUsingFilter(regions, QueryStringToMultimap.INSTANCE
                  .apply(query)));
      }
   }

   public static enum QueryStringToMultimap implements Function<String, Multimap<String, String>> {
      INSTANCE;
      @Override
      public Multimap<String, String> apply(String arg0) {
         ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
         for (String pair : Splitter.on(';').split(arg0)) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 1)
               builder.putAll(keyValue[0], ImmutableSet.<String> of());
            else
               builder.putAll(keyValue[0], Splitter.on(',').split(keyValue[1]));
         }
         return builder.build();
      }
   }
}
