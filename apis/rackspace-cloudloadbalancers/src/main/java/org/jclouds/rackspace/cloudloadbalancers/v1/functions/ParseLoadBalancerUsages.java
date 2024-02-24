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
package org.jclouds.rackspace.cloudloadbalancers.v1.functions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.collect.IterableWithMarker;
import org.jclouds.collect.internal.Arg0ToPagedIterable;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.Json;
import org.jclouds.openstack.v2_0.domain.PaginatedCollection;
import org.jclouds.openstack.v2_0.domain.Link;
import org.jclouds.openstack.v2_0.options.PaginationOptions;
import org.jclouds.rackspace.cloudloadbalancers.v1.CloudLoadBalancersApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancerUsage;
import org.jclouds.rackspace.cloudloadbalancers.v1.features.ReportApi;
import org.jclouds.rackspace.cloudloadbalancers.v1.functions.ParseLoadBalancerUsages.LoadBalancerUsages;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.inject.TypeLiteral;

/**
 * boiler plate until we determine a better way
 */
@Singleton
public class ParseLoadBalancerUsages extends ParseJson<LoadBalancerUsages> {

   @Inject
   public ParseLoadBalancerUsages(Json json) {
      super(json, TypeLiteral.get(LoadBalancerUsages.class));
   }

   static class LoadBalancerUsages extends PaginatedCollection<LoadBalancerUsage> {

      @ConstructorProperties({ "loadBalancerUsageRecords", "links" })
      protected LoadBalancerUsages(Iterable<LoadBalancerUsage> loadBalancerUsageRecords, Iterable<Link> links) {
         super(loadBalancerUsageRecords, links);
      }
   }

   public static class ToPagedIterable extends Arg0ToPagedIterable.FromCaller<LoadBalancerUsage, ToPagedIterable> {

      private final CloudLoadBalancersApi api;

      @Inject
      protected ToPagedIterable(CloudLoadBalancersApi api) {
         this.api = checkNotNull(api, "api");
      }

      @Override
      protected Function<Object, IterableWithMarker<LoadBalancerUsage>> markerToNextForArg0(Optional<Object> arg0) {
         String region = arg0.get().toString();
         final ReportApi reportApi = api.getReportApi(region);

         return new Function<Object, IterableWithMarker<LoadBalancerUsage>>() {

            @Override
            public IterableWithMarker<LoadBalancerUsage> apply(Object input) {
               PaginationOptions paginationOptions = PaginationOptions.class.cast(input);
               IterableWithMarker<LoadBalancerUsage> list = reportApi.listLoadBalancerUsage(paginationOptions);
               return list;
            }

            @Override
            public String toString() {
               return "listLoadBalancerUsage()";
            }
         };
      }

   }

}
