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
package org.jclouds.openstack.v2_0.features;

import java.util.Set;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.v2_0.domain.Extension;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;

/**
 * Provides access to OpenStack Extension APIs.
 */
@Consumes(MediaType.APPLICATION_JSON)
@RequestFilters(AuthenticateRequest.class)
@Path("/extensions")
public interface ExtensionApi {

   /**
    * Lists all available extensions
    *
    * @return all extensions
    */
   @Named("extension:list")
   @GET
   @SelectJson("extensions")
   @Fallback(EmptySetOnNotFoundOr404.class)
   Set<Extension> list();

   /**
    * Extensions may also be queried individually by their unique alias.
    *
    * @param id
    *           id of the extension
    * @return extension or null if not found
    */
   @Named("extension:get")
   @GET
   @SelectJson("extension")
   @Path("/{alias}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Extension get(@PathParam("alias") String id);
}
