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

import java.util.Map;

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

import org.jclouds.Fallbacks.EmptyFluentIterableOnNotFoundOr404;
import org.jclouds.Fallbacks.EmptyMapOnNotFoundOr404;
import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.nova.v2_0.domain.VolumeType;
import org.jclouds.openstack.nova.v2_0.options.CreateVolumeTypeOptions;
import org.jclouds.openstack.v2_0.ServiceType;
import org.jclouds.openstack.v2_0.services.Extension;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.Payload;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.Unwrap;
import org.jclouds.rest.annotations.WrapWith;
import org.jclouds.rest.binders.BindToJsonPayload;

import com.google.common.annotations.Beta;
import com.google.common.collect.FluentIterable;

/**
 * Provides access to the OpenStack Compute (Nova) Volume Type extension API.
 * This extension is no longer supported in OpenStack Liberty.
 * You can use the Block Storage API and endpoint to list volume types,
 * see <a href="http://developer.openstack.org/api-ref-blockstorage-v2.html#volumes-v2-types">volume types v2</a>.
 * @see VolumeApi
 */
@Beta
@Extension(of = ServiceType.COMPUTE, namespace = ExtensionNamespaces.VOLUME_TYPES)
@RequestFilters(AuthenticateRequest.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/os-volume-types")
public interface VolumeTypeApi {
   /**
    * @return set of all volume types
    */
   @Named("volumeType:list")
   @GET
   @SelectJson("volume_types")
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<VolumeType> list();

   /**
    * Gets a volume type
    *
    * @param id the id of the volume type to retrieve
    * @return the requested volume type
    */
   @Named("volumeType:get")
   @GET
   @Path("/{id}")
   @SelectJson("volume_type")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   VolumeType get(@PathParam("id") String id);

   /**
    * Creates a new volume type
    *
    * @param name    the name of the new volume type
    * @param options optional settings for the new volume type
    * @return the new volume type
    */
   @Named("volumeType:create")
   @POST
   @SelectJson("volume_type")
   @Produces(MediaType.APPLICATION_JSON)
   @WrapWith("volume_type")
   VolumeType create(@PayloadParam("name") String name, CreateVolumeTypeOptions... options);

   /**
    * Deletes a volume type
    *
    * @param id the id of the volume type to delete
    */
   @Named("volumeType:delete")
   @DELETE
   @Path("/{id}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean delete(@PathParam("id") String id);

   /**
    * Gets the extra specs for a volume type
    *
    * @param id the id of the volume type
    * @return the set of extra metadata for the flavor
    */
   @Named("volumeType:getExtraSpecs")
   @GET
   @Path("/{id}/extra_specs")
   @SelectJson("extra_specs")
   @Fallback(EmptyMapOnNotFoundOr404.class)
   Map<String, String> getExtraSpecs(@PathParam("id") String id);

   /**
    * Creates or updates the extra metadata for a given flavor
    */
   @Named("volumeType:updateExtraSpecs")
   @POST
   @Path("/{id}/extra_specs")
   @Produces(MediaType.APPLICATION_JSON)
   @MapBinder(BindToJsonPayload.class)
   void updateExtraSpecs(@PathParam("id") String id, @PayloadParam("extra_specs") Map<String, String> specs);

   /**
    * Retrieve a single extra spec value
    *
    * @param id  the id of the volume type
    * @param key the key of the extra spec item to retrieve
    */
   @Named("volumeType:getExtraSpec")
   @GET
   @Path("/{id}/extra_specs/{key}")
   @Unwrap
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   String getExtraSpec(@PathParam("id") String id, @PathParam("key") String key);

   /**
    * Creates or updates a single extra spec value
    *
    * @param id    the id of the volume type
    * @param key   the extra spec key (when creating ensure this does not include whitespace or other difficult characters)
    * @param value the new value to store associate with the key
    */
   @Named("volumeType:updateExtraSpec")
   @PUT
   @Path("/{id}/extra_specs/{key}")
   @Produces(MediaType.APPLICATION_JSON)
   @Payload("%7B\"{key}\":\"{value}\"%7D")
   void updateExtraSpec(@PathParam("id") String id,
         @PathParam("key") @PayloadParam("key") String key,
         @PayloadParam("value") String value);

   /**
    * Deletes an existing extra spec
    *
    * @param id  the id of the volume type
    * @param key the key of the extra spec to delete
    */
   @Named("volumeType:deleteExtraSpec")
   @DELETE
   @Path("/{id}/extra_specs/{key}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteExtraSpec(@PathParam("id") String id, @PathParam("key") String key);
}
