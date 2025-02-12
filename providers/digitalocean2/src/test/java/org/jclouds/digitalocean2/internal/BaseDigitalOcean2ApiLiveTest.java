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
package org.jclouds.digitalocean2.internal;

import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_NODE_RUNNING;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_NODE_SUSPENDED;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_NODE_TERMINATED;
import static org.testng.Assert.assertTrue;
import static org.testng.util.Strings.isNullOrEmpty;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jclouds.apis.BaseApiLiveTest;
import org.jclouds.compute.config.ComputeServiceProperties;
import org.jclouds.digitalocean2.DigitalOcean2Api;
import org.jclouds.digitalocean2.config.DigitalOcean2RateLimitModule;
import org.jclouds.digitalocean2.domain.Image;
import org.jclouds.digitalocean2.domain.Region;
import org.jclouds.digitalocean2.domain.Size;

import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class BaseDigitalOcean2ApiLiveTest extends BaseApiLiveTest<DigitalOcean2Api> {

   private Predicate<Long> actionCompleted;
   private Predicate<Integer> nodeTerminated;
   private Predicate<Integer> nodeStopped;
   private Predicate<Integer> nodeRunning;

   public BaseDigitalOcean2ApiLiveTest() {
      provider = "digitalocean2";
   }

   @Override protected Properties setupProperties() {
      Properties props = super.setupProperties();
      props.put(ComputeServiceProperties.POLL_INITIAL_PERIOD, 1000);
      props.put(ComputeServiceProperties.POLL_MAX_PERIOD, 10000);
      props.put(ComputeServiceProperties.TIMEOUT_IMAGE_AVAILABLE, TimeUnit.MINUTES.toMillis(45));
      return props;
   }

   @Override protected DigitalOcean2Api create(Properties props, Iterable<Module> modules) {
      Injector injector = newBuilder().modules(modules).overrides(props).buildInjector();
      actionCompleted = injector.getInstance(Key.get(new TypeLiteral<Predicate<Long>>(){}));
      nodeTerminated = injector.getInstance(Key.get(new TypeLiteral<Predicate<Integer>>(){},
            Names.named(TIMEOUT_NODE_TERMINATED)));
      nodeStopped = injector.getInstance(Key.get(new TypeLiteral<Predicate<Integer>>(){},
            Names.named(TIMEOUT_NODE_SUSPENDED)));
      nodeRunning = injector.getInstance(Key.get(new TypeLiteral<Predicate<Integer>>(){},
            Names.named(TIMEOUT_NODE_RUNNING)));
      return injector.getInstance(DigitalOcean2Api.class);
   }

   @Override protected Iterable<Module> setupModules() {
      return ImmutableSet.<Module> builder().addAll(super.setupModules()).add(new DigitalOcean2RateLimitModule())
            .build();
   }

   protected void assertActionCompleted(long actionId) {
      assertTrue(actionCompleted.apply(actionId), String.format("Action %s did not complete in the configured timeout", actionId));
   }

   protected void assertNodeStopped(int dropletId) {
      assertTrue(nodeStopped.apply(dropletId), String.format("Droplet %s did not stop in the configured timeout", dropletId));
   }

   protected void assertNodeRunning(int dropletId) {
      assertTrue(nodeRunning.apply(dropletId), String.format("Droplet %s did not start in the configured timeout", dropletId));
   }

   protected void assertNodeTerminated(int dropletId) {
      assertTrue(nodeTerminated.apply(dropletId), String.format("Droplet %s was not terminated in the configured timeout", dropletId));
   }
   
   protected Region firstAvailableRegion() {
      return api.regionApi().list().concat().firstMatch(new Predicate<Region>() {
         @Override
         public boolean apply(Region input) {
            return input.available();
         }
      }).get();
   }
   
   protected Size cheapestSizeInRegionForImage(final Region region, final Image image) {
      return sizesByPrice().min(api.sizeApi().list().concat().filter(new Predicate<Size>() {
         @Override
         public boolean apply(Size input) {
            return input.available() && input.regions().contains(region.slug()) && input.disk() >= image.minDiskSize();
         }
      }));
   }
   
   protected Image ubuntuImageInRegion(final Region region) {
      return api.imageApi().list().concat().firstMatch(new Predicate<Image>() {
         @Override
         public boolean apply(Image input) {
            return "Ubuntu".equalsIgnoreCase(input.distribution()) && !isNullOrEmpty(input.slug())
                  && input.regions().contains(region.slug());
         }
      }).get();
   }
   
   protected static Ordering<Size> sizesByPrice() {
      return new Ordering<Size>() {
         @Override
         public int compare(Size left, Size right) {
            return ComparisonChain.start()
                  .compare(left.priceHourly(), right.priceHourly())
                  .compare(left.priceMonthly(), right.priceMonthly())
                  .result();
         }
      };
   }
}
