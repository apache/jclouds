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
package org.jclouds.openstack.swift.v1.features;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.swift.v1.binders.BindMetadataToHeaders.BindObjectMetadataToHeaders;
import org.jclouds.openstack.swift.v1.binders.BindToHeaders;
import org.jclouds.openstack.swift.v1.domain.DeleteStaticLargeObjectResponse;
import org.jclouds.openstack.swift.v1.domain.Segment;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.functions.ETagHeader;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.binders.BindToJsonPayload;

import com.google.common.annotations.Beta;

/**
 * Provides access to the OpenStack Object Storage (Swift) Static Large Object API features.
 * <p/>
 * This API is new to jclouds and hence is in Beta. That means we need people to use it and give us feedback. Based
 * on that feedback, minor changes to the interfaces may happen. This code will replace
 * org.jclouds.openstack.swift.SwiftClient in jclouds 2.0 and it is recommended you adopt it sooner than later.
 */
@Beta
@RequestFilters(AuthenticateRequest.class)
@Consumes(APPLICATION_JSON)
@Path("/{objectName}")
public interface StaticLargeObjectApi {

   /**
    * Creates or updates a static large object's manifest.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    * @param segments
    *           ordered parts which will be concatenated upon download.
    * @param metadata
    *           corresponds to {@link SwiftObject#getMetadata()}.
    *
    * @return {@link SwiftObject#getEtag()} of the object, which is the MD5
    *         checksum of the concatenated ETag values of the {@code segments}.
    */
   @Named("staticLargeObject:replaceManifest")
   @PUT
   @ResponseParser(ETagHeader.class)
   @QueryParams(keys = "multipart-manifest", values = "put")
   String replaceManifest(@PathParam("objectName") String objectName,
         @BinderParam(BindToJsonPayload.class) List<Segment> segments,
         @BinderParam(BindObjectMetadataToHeaders.class) Map<String, String> metadata);

   /**
    * Creates or updates a static large object's manifest.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    * @param segments
    *           ordered parts which will be concatenated upon download.
    * @param metadata
    *           corresponds to {@link SwiftObject#getMetadata()}.
    * @param headers
    *           Binds the map to headers, without prefixing/escaping the header name/key.
    *
    * @return {@link SwiftObject#getEtag()} of the object, which is the MD5
    *         checksum of the concatenated ETag values of the {@code segments}.
    */
   @Named("staticLargeObject:replaceManifest")
   @PUT
   @ResponseParser(ETagHeader.class)
   @QueryParams(keys = "multipart-manifest", values = "put")
   String replaceManifest(@PathParam("objectName") String objectName,
         @BinderParam(BindToJsonPayload.class) List<Segment> segments,
         @BinderParam(BindObjectMetadataToHeaders.class) Map<String, String> metadata,
         @BinderParam(BindToHeaders.class) Map<String, String> headers);

   /**
    * Deletes a static large object, if present, including all of its segments.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    */
   @Named("staticLargeObject:delete")
   @DELETE
   @Fallback(VoidOnNotFoundOr404.class)
   @QueryParams(keys = "multipart-manifest", values = "delete")
   DeleteStaticLargeObjectResponse delete(@PathParam("objectName") String objectName);

   /**
    * Get a static large object's manifest.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    *
    * @return A list of the multipart segments
    */
   @Named("staticLargeObject:getManifest")
   @GET
   @Fallback(EmptyListOnNotFoundOr404.class)
   @QueryParams(keys = {"format", "multipart-manifest"}, values = {"json", "get"})
   List<Segment> getManifest(@PathParam("objectName") String objectName);
}
