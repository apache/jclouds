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
package org.jclouds.openstack.trove.v1.features;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyFluentIterableOnNotFoundOr404;
import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.trove.v1.binders.BindCreateInstanceToJson;
import org.jclouds.openstack.trove.v1.domain.Instance;
import org.jclouds.openstack.trove.v1.functions.ParsePasswordFromRootedInstance;
import org.jclouds.rest.ResourceNotFoundException;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.SkipEncoding;

import com.google.common.collect.FluentIterable;

/**
 * This API is for creating, listing, and deleting an Instance, and allows enabling a root user.

 * @see Instance
 */
@SkipEncoding({'/', '='})
@RequestFilters(AuthenticateRequest.class)
public interface InstanceApi {

   /**
    * Same as {@link #create(String, int, String)} but accept an integer Flavor ID.
    *
    * @param flavor The flavor ID.
    * @param volumeSize The size in GB of the instance volume.
    * @param name The name of the instance.
    * @return The instance created.
    *
    * @see InstanceApi#create(String, int, String)
    */
   @Named("instance:create")
   @POST
   @Path("/instances")
   @SelectJson("instance")
   @Consumes(MediaType.APPLICATION_JSON)
   @MapBinder(BindCreateInstanceToJson.class)
   Instance create(@PayloadParam("flavorRef") int flavor, @PayloadParam("size") int volumeSize, @PayloadParam("name") String name);

   /**
    * Create a database instance by flavor type and volume size.
    *
    * @param flavor The flavor URL or flavor id as string.
    * @param volumeSize The size in GB of the instance volume.
    * @param name The name of the instance.
    * @return The instance created.
    */
   @Named("instance:create")
   @POST
   @Path("/instances")
   @SelectJson("instance")
   @Consumes(MediaType.APPLICATION_JSON)
   @MapBinder(BindCreateInstanceToJson.class)
   Instance create(@PayloadParam("flavorRef") String flavor, @PayloadParam("size") int volumeSize, @PayloadParam("name") String name);

   /**
    * Deletes an Instance by id.
    *
    * @param instanceId The instance id.
    * @return true if successful.
    */
   @Named("instances:delete/{id}")
   @DELETE
   @Path("/instances/{id}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(FalseOnNotFoundOr404.class)
   boolean delete(@PathParam("id") String instanceId);

   /**
    * Enables root for an instance.
    *
    * @param instanceId The instance id.
    * @return String The password for the root user.
    */
   @Named("instances/{id}/root")
   @POST
   @Path("/instances/{id}/root")
   @Consumes(MediaType.APPLICATION_JSON)
   @ResponseParser(ParsePasswordFromRootedInstance.class)
   String enableRoot(@PathParam("id") String instanceId);

   /**
    * Checks to see if root is enabled for an instance.
    *
    * @param instanceId The instance id.
    * @throws ResourceNotFoundException
    * @return boolean True if root is enabled.
    */
   @Named("instances/{id}/root")
   @GET
   @Path("/instances/{id}/root")
   @Consumes(MediaType.APPLICATION_JSON)
   @SelectJson("rootEnabled")
   boolean isRooted(@PathParam("id") String instanceId);

   /**
    * Returns a summary list of Instances.
    *
    * @return The list of Instances.
    */
   @Named("instance:list")
   @GET
   @Path("/instances")
   @SelectJson("instances")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<Instance> list();

   /**
    * Returns an Instance by id.
    *
    * @param instanceId The instance id.
    * @return Instance or Null on not found.
    */
   @Named("instances:get/{id}")
   @GET
   @Path("/instances/{id}")
   @SelectJson("instance")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Instance get(@PathParam("id") String instanceId);
}
