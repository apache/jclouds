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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.azurecompute.arm.config.GraphRBAC;
import org.jclouds.azurecompute.arm.config.OAuthResource;
import org.jclouds.azurecompute.arm.domain.ServicePrincipal;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Endpoint;
import org.jclouds.rest.annotations.OnlyElement;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;

import com.google.inject.name.Named;

@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(MediaType.APPLICATION_JSON)
@Endpoint(GraphRBAC.class)
@OAuthResource(value = GraphRBAC.STANDARD_ENDPOINT, chinaEndpoint = GraphRBAC.CHINA_ENDPOINT)
public interface GraphRBACApi {

   @Named("servicePrincipal:get")
   @GET
   @Path("/servicePrincipals")
   @QueryParams(keys = "$filter", values = "appId eq '{jclouds.identity}'")
   @SelectJson("value")
   @OnlyElement
   ServicePrincipal getCurrentServicePrincipal();
}
