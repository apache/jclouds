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
package org.jclouds.openstack.nova.v2_0.extensions;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.nova.v2_0.domain.ServerWithSecurityGroups;
import org.jclouds.openstack.v2_0.ServiceType;
import org.jclouds.openstack.v2_0.services.Extension;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;

import com.google.common.annotations.Beta;

/**
 * Provides access to the OpenStack Compute (Nova) Create Server extension API.
 *
 * This provides details including the security groups associated with a Server.
 * <p/>
 *
 * NOTE: the equivalent to listServersInDetail() isn't available at the other end, so not
 * extending ServerApi at this time.
 *
 * @see org.jclouds.openstack.nova.v2_0.features.ServerApi
 */
@Beta
@Extension(of = ServiceType.COMPUTE, namespace = ExtensionNamespaces.CREATESERVEREXT,
      name = ExtensionNames.CREATESERVEREXT, alias = ExtensionAliases.CREATESERVEREXT)
@RequestFilters(AuthenticateRequest.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/os-create-server-ext")
public interface ServerWithSecurityGroupsApi {
   /**
    * Retrieve details of the specified server, including security groups
    *
    * @param id id of the server
    * @return server or null if not found
    */
   @Named("server:get")
   @GET
   @SelectJson("server")
   @Path("/{id}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   ServerWithSecurityGroups get(@PathParam("id") String id);
}
