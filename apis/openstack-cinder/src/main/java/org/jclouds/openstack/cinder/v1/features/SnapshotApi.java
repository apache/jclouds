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

import javax.inject.Named;
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
import org.jclouds.openstack.cinder.v1.domain.Snapshot;
import org.jclouds.openstack.cinder.v1.options.CreateSnapshotOptions;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.SkipEncoding;

import com.google.common.collect.FluentIterable;

/**
 * Provides access to Volume Snapshots API.
 *
 */
@SkipEncoding({'/', '='})
@RequestFilters(AuthenticateRequest.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/snapshots")
public interface SnapshotApi {
   /**
    * Returns a summary list of Snapshots.
    *
    * @return The list of Snapshots
    */
   @Named("snapshot:list")
   @GET
   @SelectJson("snapshots")
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<? extends Snapshot> list();

   /**
    * Returns a detailed list of Snapshots.
    *
    * @return The list of Snapshots
    */
   @Named("snapshot:list")
   @GET
   @Path("/detail")
   @SelectJson("snapshots")
   @Fallback(EmptyFluentIterableOnNotFoundOr404.class)
   FluentIterable<? extends Snapshot> listInDetail();

   /**
    * Return data about the given Snapshot.
    *
    * @param snapshotId Id of the Snapshot
    * @return Details of a specific Snapshot
    */
   @Named("snapshot:get")
   @GET
   @Path("/{id}")
   @SelectJson("snapshot")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Snapshot get(@PathParam("id") String snapshotId);

   /**
    * Creates a new Snapshot. The Volume status must be Available.
    *
    * @param volumeId The Volume Id from which to create the Snapshot
    * @param options See CreateSnapshotOptions
    * @return The new Snapshot
    */
   @Named("snapshot:create")
   @POST
   @SelectJson("snapshot")
   @Produces(MediaType.APPLICATION_JSON)
   @MapBinder(CreateSnapshotOptions.class)
   Snapshot create(@PayloadParam("volume_id") String volumeId, CreateSnapshotOptions... options);

   /**
    * Delete a Snapshot.
    *
    * @param snapshotId Id of the Snapshot
    * @return true if successful, false otherwise
    */
   @Named("snapshot:delete")
   @DELETE
   @Path("/{id}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean delete(@PathParam("id") String snapshotId);
}
