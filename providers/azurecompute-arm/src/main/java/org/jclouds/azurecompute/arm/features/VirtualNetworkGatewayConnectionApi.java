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
package org.jclouds.azurecompute.arm.features;

import java.io.Closeable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.azurecompute.arm.domain.vpn.VirtualNetworkGatewayConnection;
import org.jclouds.azurecompute.arm.domain.vpn.VirtualNetworkGatewayConnectionProperties;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.azurecompute.arm.functions.URIParser;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.binders.BindToJsonPayload;

@Path("/resourcegroups/{resourcegroup}/providers/Microsoft.Network/connections")
@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(MediaType.APPLICATION_JSON)
public interface VirtualNetworkGatewayConnectionApi extends Closeable {

   @Named("virtualnetworkgatewayconnection:list")
   @GET
   @SelectJson("value")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<VirtualNetworkGatewayConnection> list();

   @Named("virtualnetworkgatewayconnection:get")
   @Path("/{name}")
   @GET
   @Fallback(NullOnNotFoundOr404.class)
   VirtualNetworkGatewayConnection get(@PathParam("name") String name);
   
   @Named("virtualnetworkgatewayconnection:getSharedKey")
   @Path("/{name}/sharedkey")
   @GET
   @Fallback(NullOnNotFoundOr404.class)
   String getSharedKey(@PathParam("name") String name);

   @Named("virtualnetworkgatewayconnection:createOrUpdate")
   @MapBinder(BindToJsonPayload.class)
   @Path("/{name}")
   @PUT
   VirtualNetworkGatewayConnection createOrUpdate(@PathParam("name") String name,
         @PayloadParam("location") String location, @Nullable @PayloadParam("tags") Map<String, String> tags,
         @PayloadParam("properties") VirtualNetworkGatewayConnectionProperties properties);

   @Named("virtualnetworkgatewayconnection:delete")
   @Path("/{name}")
   @DELETE
   @ResponseParser(URIParser.class)
   @Fallback(NullOnNotFoundOr404.class)
   URI delete(@PathParam("name") String name);
}
