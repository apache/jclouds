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

import java.util.List;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.azurecompute.arm.domain.Offer;
import org.jclouds.azurecompute.arm.domain.Publisher;
import org.jclouds.azurecompute.arm.domain.SKU;
import org.jclouds.azurecompute.arm.domain.Version;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Azure Resource Management API includes operations for managing the OS images in your subscription.
 */
@Path("/providers/Microsoft.Compute/locations/{location}")
@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(APPLICATION_JSON)
public interface OSImageApi {

   /**
    * List Publishers in location
    */
   @Named("publisher:list")
   @GET
   @Path("/publishers")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<Publisher> listPublishers();

   /**
    * List Offers in publisher
    */
   @Named("offer:list")
   @GET
   @Path("/publishers/{publisher}/artifacttypes/vmimage/offers")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<Offer> listOffers(@PathParam("publisher") String publisher);

   /**
    * List SKUs in offer
    */
   @Named("sku:list")
   @GET
   @Path("/publishers/{publisher}/artifacttypes/vmimage/offers/{offer}/skus")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<SKU> listSKUs(@PathParam("publisher") String publisher, @PathParam("offer") String offer);

   /**
    * List Versions in SKU
    */
   @Named("version:list")
   @GET
   @Path("/publishers/{publisher}/artifacttypes/vmimage/offers/{offer}/skus/{sku}/versions")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<Version> listVersions(@PathParam("publisher") String publisher, @PathParam("offer") String offer,
                          @PathParam("sku") String sku);
   
   /**
    * Get the details of a Version
    */
   @Named("version:get")
   @GET
   @Path("/publishers/{publisher}/artifacttypes/vmimage/offers/{offer}/skus/{sku}/versions/{version}")
   @Fallback(EmptyListOnNotFoundOr404.class)
   Version getVersion(@PathParam("publisher") String publisher, @PathParam("offer") String offer,
                          @PathParam("sku") String sku, @PathParam("version") String version);

}
