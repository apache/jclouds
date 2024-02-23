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

import java.util.Map;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.swift.v1.binders.BindMetadataToHeaders.BindObjectMetadataToHeaders;
import org.jclouds.openstack.swift.v1.binders.BindToHeaders;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.functions.ETagHeader;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Headers;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;

import com.google.common.annotations.Beta;

/**
 * Provides access to the OpenStack Object Storage (Swift) Dynamic Large Object
 * API features.
 * <p/>
 * This API is new to jclouds and hence is in Beta.
 */
@Beta
@RequestFilters(AuthenticateRequest.class)
@Consumes(APPLICATION_JSON)
@Path("/{objectName}")
public interface DynamicLargeObjectApi {
   /**
    * Creates or updates a dynamic large object's manifest.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    * @param metadata
    *           corresponds to {@link SwiftObject#getMetadata()}.
    * @param headers
    *           Binds the map to headers, without prefixing/escaping the header
    *           name/key.
    *
    * @return {@link SwiftObject#getEtag()} of the object, which is the MD5
    *         checksum of the concatenated ETag values of the {@code segments}.
    *         
    * @see {@code StaticLargeObjectApi}
    */
   @Deprecated
   @Named("dynamicLargeObject:putManifest")
   @PUT
   @ResponseParser(ETagHeader.class)
   @Headers(keys = "X-Object-Manifest", values = "{containerName}/{objectName}/")
   String putManifest(@PathParam("objectName") String objectName,
         @BinderParam(BindObjectMetadataToHeaders.class) Map<String, String> metadata,
         @BinderParam(BindToHeaders.class) Map<String, String> headers);

   /**
    * Creates or updates a dynamic large object's manifest.
    *
    * @param objectName
    *           corresponds to {@link SwiftObject#getName()}.
    * @param metadata
    *           corresponds to {@link SwiftObject#getMetadata()}.
    *
    * @return {@link SwiftObject#getEtag()} of the object, which is the etag
    *         of 0 sized object.
    *         
    * @see {@code StaticLargeObjectApi}
    */
   @Deprecated
   @Named("dynamicLargeObject:putManifest")
   @PUT
   @ResponseParser(ETagHeader.class)
   @Headers(keys = "X-Object-Manifest", values = "{containerName}/{objectName}/")
   String putManifest(@PathParam("objectName") String objectName,
         @BinderParam(BindObjectMetadataToHeaders.class) Map<String, String> metadata);
}
