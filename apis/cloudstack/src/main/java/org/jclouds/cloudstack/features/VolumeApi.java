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
package org.jclouds.cloudstack.features;

import java.util.Set;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.cloudstack.domain.AsyncCreateResponse;
import org.jclouds.cloudstack.domain.Volume;
import org.jclouds.cloudstack.filters.AuthenticationFilter;
import org.jclouds.cloudstack.options.ListVolumesOptions;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.OnlyElement;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.Unwrap;

/**
 * Provides synchronous access to cloudstack via their REST API.
 * <p/>
 *
 * @see <a href="http://download.cloud.com/releases/2.2.0/api_2.2.12/TOC_User.html" />
 */
@RequestFilters(AuthenticationFilter.class)
@QueryParams(keys = "response", values = "json")
public interface VolumeApi {
   /**
    * List volumes
    *
    * @return volume list, empty if not found
    */
   @Named("listVolumes")
   @GET
   @QueryParams(keys = { "command", "listAll" }, values = { "listVolumes", "true" })
   @SelectJson("volume")
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(EmptySetOnNotFoundOr404.class)
   Set<Volume> listVolumes(ListVolumesOptions... options);

   /**
    * Get volume by id
    *
    * @param id the volume id to retrieve
    * @return volume or null if not found
    */
   @Named("listVolumes")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @QueryParams(keys = { "command", "listAll" }, values = { "listVolumes", "true" })
   @SelectJson("volume")
   @OnlyElement
   @Fallback(NullOnNotFoundOr404.class)
   Volume getVolume(@QueryParam("id") String id);


   /**
    * Create a volume with given name and diskOfferingId
    *
    * @param name           name of the volume
    * @param diskOfferingId the ID of the disk offering.
    * @param zoneId         the ID of the availability zone
    * @return AsyncCreateResponse job response used to track creation
    */
   @Named("createVolume")
   @GET
   @QueryParams(keys = "command", values = "createVolume")
   @Unwrap
   @Consumes(MediaType.APPLICATION_JSON)
   AsyncCreateResponse createVolumeFromDiskOfferingInZone(@QueryParam("name") String name,
                                                                            @QueryParam("diskofferingid") String diskOfferingId,
                                                                            @QueryParam("zoneid") String zoneId);

   /**
    * Create a volume with given name, size and diskOfferingId
    *
    * @param name           name of the volume
    * @param diskOfferingId the ID of the disk offering (the offering should have the custom disk size flag set)
    * @param zoneId         the ID of the availability zone
    * @param size           the size of volume required (in GB)
    * @return AsyncCreateResponse job response used to track creation
    */
   @GET
   @QueryParams(keys = "command", values = "createVolume")
   @Unwrap
   @Consumes(MediaType.APPLICATION_JSON)
   AsyncCreateResponse createVolumeFromCustomDiskOfferingInZone(@QueryParam("name") String name,
                                                                            @QueryParam("diskofferingid") String diskOfferingId,
                                                                            @QueryParam("zoneid") String zoneId,
                                                                            @QueryParam("size") int size);

   /**
    * Create a volume with given name and snapshotId
    *
    * @param name       name of the volume
    * @param snapshotId Snapshot id to be used while creating the volume
    * @param zoneId     the ID of the availability zone
    * @return AsyncCreateResponse job response used to track creation
    */
   @Named("createVolume")
   @GET
   @QueryParams(keys = "command", values = "createVolume")
   @Unwrap
   @Consumes(MediaType.APPLICATION_JSON)
   AsyncCreateResponse createVolumeFromSnapshotInZone(@QueryParam("name") String name,
                                                                        @QueryParam("snapshotid") String snapshotId,
                                                                        @QueryParam("zoneid") String zoneId);

   /**
    * Attaches a disk volume to a virtual machine.
    *
    * @param volumeId         the ID of the disk volume
    * @param virtualMachineId the ID of the virtual machine
    * @return AsyncCreateResponse job response used to track creation
    */
   @Named("attachVolume")
   @GET
   @QueryParams(keys = "command", values = "attachVolume")
   @Unwrap
   @Consumes(MediaType.APPLICATION_JSON)
   AsyncCreateResponse attachVolume(@QueryParam("id") String volumeId,
                                                      @QueryParam("virtualmachineid") String virtualMachineId);

   /**
    * Detaches a disk volume from a virtual machine.
    *
    * @param volumeId         the ID of the disk volume
    * @return AsyncCreateResponse job response used to track creation
    */
   @Named("detachVolume")
   @GET
   @QueryParams(keys = "command", values = "detachVolume")
   @Unwrap
   @Consumes(MediaType.APPLICATION_JSON)
   AsyncCreateResponse detachVolume(@QueryParam("id") String volumeId);

   /**
    * Deletes a detached disk volume
    *
    * @param id id of the volume
    */
   @Named("deleteVolume")
   @GET
   @QueryParams(keys = "command", values = "deleteVolume")
   @Fallback(VoidOnNotFoundOr404.class)
   void deleteVolume(@QueryParam("id") String id);

}
