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

import java.net.URI;
import java.util.List;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks;
import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.azurecompute.arm.domain.Deployment;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.azurecompute.arm.functions.URIParser;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.Payload;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;

/**
 * - create deployment
 * - delete deployment
 * - get information about deployment
 */
@Path("/resourcegroups/{resourcegroup}/providers/microsoft.resources/deployments")
@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(MediaType.APPLICATION_JSON)
public interface DeploymentApi {

   /**
    * The Create Template Deployment operation starts the process of an ARM Template deployment.
    * It then returns a Deployment object.
    */
   @Named("deployment:create")
   @Path("/{deploymentname}")
   @Payload("{template}")
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   Deployment create(@PathParam("deploymentname") String deploymentname,
                               @PayloadParam("template") String template);

   /**
    * Get Deployment Information returns information about the specified deployment.
    */
   @Named("deployment:get")
   @Path("/{deploymentname}")
   @GET
   @Fallback(NullOnNotFoundOr404.class)
   Deployment get(@PathParam("deploymentname") String deploymentname);

   /**
    * Validate Deployment validates deployment template before deployment
    */
   @Named("deployment:validate")
   @Path("/{deploymentname}/validate")
   @Payload("{template}")
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   Deployment validate(@PathParam("deploymentname") String deploymentname,
                                 @PayloadParam("template") String template);

   /**
    * List all deployments in a resource group
    */
   @Named("deployment:list")
   @GET
   @SelectJson("value")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<Deployment> list();

   /**
    * The Delete Template Deployment operation starts the process of an ARM Template removal.
    */
   @Named("deployment:delete")
   @DELETE
   @ResponseParser(URIParser.class)
   @Path("/{deploymentname}")
   @Fallback(Fallbacks.NullOnNotFoundOr404.class)
   URI delete(@PathParam("deploymentname") String deploymentname);
}
