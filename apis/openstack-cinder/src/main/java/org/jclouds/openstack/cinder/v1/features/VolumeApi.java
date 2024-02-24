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
package org.jclouds.openstack.cinder.v1.features;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyFluentIterableOnNotFoundOr404;
import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.cinder.v1.domain.Volume;
import org.jclouds.openstack.cinder.v1.options.CreateVolumeOptions;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.SkipEncoding;

import com.google.common.collect.FluentIterable;

/**
 * Provides access to the Volume API.
 *
 * This API strictly handles creating and managing Volumes. To attach a Volume to a Server you need to use the
 * @see VolumeAttachmentApi
 *
 */
@SkipEncoding({'/', '='})
@RequestFilters(AuthenticateRequest.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/volumes")
public interface VolumeApi {
   /**
    * Returns a summary list of Volumes.
    *
    * @return The list of Volumes
    */
   @Named("volume:list")
   @GET
   @SelectJson("volumes")
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<? extends Volume> list();

   /**
    * Returns a detailed list of Volumes.
    *
    * @return The list of Volumes
    */
   @Named("volume:list")
   @GET
   @Path("/detail")
   @SelectJson("volumes")
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<? extends Volume> listInDetail();

   /**
    * Return data about the given Volume.
    *
    * @param volumeId Id of the Volume
    * @return Details of a specific Volume
    */
   @Named("volume:get")
   @GET
   @Path("/{id}")
   @SelectJson("volume")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Volume get(@PathParam("id") String volumeId);

   /**
    * Creates a new Volume
    *
    * @param volumeId Id of the Volume
    * @param options See CreateVolumeOptions
    * @return The new Volume
    */
   @Named("volume:create")
   @POST
   @SelectJson("volume")
   @Produces(MediaType.APPLICATION_JSON)
   @MapBinder(CreateVolumeOptions.class)
   Volume create(@PayloadParam("size") int sizeGB, CreateVolumeOptions... options);

   /**
    * Delete a Volume. The Volume status must be Available or Error.
    *
    * @param volumeId Id of the Volume
    * @return true if successful, false otherwise
    */
   @Named("volume:delete")
   @DELETE
   @Path("/{id}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean delete(@PathParam("id") String volumeId);
}
