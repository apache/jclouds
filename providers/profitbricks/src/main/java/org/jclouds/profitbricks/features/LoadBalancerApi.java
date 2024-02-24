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
package org.jclouds.profitbricks.features;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

import org.jclouds.Fallbacks;
import org.jclouds.http.filters.BasicAuthentication;
import org.jclouds.profitbricks.binder.loadbalancer.CreateLoadBalancerRequestBinder;
import org.jclouds.profitbricks.binder.loadbalancer.DeregisterLoadBalancerRequestBinder;
import org.jclouds.profitbricks.binder.loadbalancer.RegisterLoadBalancerRequestBinder;
import org.jclouds.profitbricks.binder.loadbalancer.UpdateLoadBalancerRequestBinder;
import org.jclouds.profitbricks.domain.LoadBalancer;
import org.jclouds.profitbricks.http.filters.ProfitBricksSoapMessageEnvelope;
import org.jclouds.profitbricks.http.parser.RequestIdOnlyResponseHandler;
import org.jclouds.profitbricks.http.parser.loadbalancer.LoadBalancerIdOnlyResponseHandler;
import org.jclouds.profitbricks.http.parser.loadbalancer.LoadBalancerListResponseHandler;
import org.jclouds.profitbricks.http.parser.loadbalancer.LoadBalancerResponseHandler;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.Payload;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.XMLResponseParser;

@RequestFilters({BasicAuthentication.class, ProfitBricksSoapMessageEnvelope.class})
@Consumes(MediaType.TEXT_XML)
@Produces(MediaType.TEXT_XML)
public interface LoadBalancerApi {

   @POST
   @Named("loadbalancer:getall")
   @Payload("<ws:getAllLoadBalancers/>")
   @XMLResponseParser(LoadBalancerListResponseHandler.class)
   @Fallback(Fallbacks.EmptyListOnNotFoundOr404.class)
   List<LoadBalancer> getAllLoadBalancers();

   @POST
   @Named("loadbalancer:get")
   @Payload("<ws:getLoadBalancer><loadBalancerId>{id}</loadBalancerId></ws:getLoadBalancer>")
   @XMLResponseParser(LoadBalancerResponseHandler.class)
   @Fallback(Fallbacks.NullOnNotFoundOr404.class)
   LoadBalancer getLoadBalancer(@PayloadParam("id") String identifier);

   @POST
   @Named("loadbalancer:create")
   @MapBinder(CreateLoadBalancerRequestBinder.class)
   @XMLResponseParser(LoadBalancerIdOnlyResponseHandler.class)
   String createLoadBalancer(@PayloadParam("loadbalancer") LoadBalancer.Request.CreatePayload payload);

   @POST
   @Named("loadbalancer:register")
   @MapBinder(RegisterLoadBalancerRequestBinder.class)
   @XMLResponseParser(LoadBalancerResponseHandler.class)
   LoadBalancer registerLoadBalancer(@PayloadParam("loadbalancer") LoadBalancer.Request.RegisterPayload payload);

   @POST
   @Named("loadbalancer:deregister")
   @MapBinder(DeregisterLoadBalancerRequestBinder.class)
   @XMLResponseParser(RequestIdOnlyResponseHandler.class)
   String deregisterLoadBalancer(@PayloadParam("loadbalancer") LoadBalancer.Request.DeregisterPayload payload);

   @POST
   @Named("loadbalancer:delete")
   @Payload("<ws:deleteLoadBalancer><loadBalancerId>{id}</loadBalancerId></ws:deleteLoadBalancer>")
   boolean deleteLoadBalancer(@PayloadParam("id") String id);

   @POST
   @Named("loadbalancer:update")
   @MapBinder(UpdateLoadBalancerRequestBinder.class)
   @XMLResponseParser(RequestIdOnlyResponseHandler.class)
   String updateLoadBalancer(@PayloadParam("loadbalancer") LoadBalancer.Request.UpdatePayload payload);
}
