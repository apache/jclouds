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
package org.jclouds.googlecloudstorage.features;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.googlecloudstorage.domain.BucketAccessControls;
import org.jclouds.googlecloudstorage.domain.templates.BucketAccessControlsTemplate;
import org.jclouds.http.HttpResponse;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.PATCH;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * Provides access to BucketAccessControl entities via their REST API.
 *
 * @see <a href = " https://developers.google.com/storage/docs/json_api/v1/bucketAccessControls "/>
 */

@SkipEncoding({ '/', '=' })
@RequestFilters(OAuthFilter.class)
@Consumes(APPLICATION_JSON)
public interface BucketAccessControlsApi {

   /**
    * Returns the ACL entry for the specified entity on the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket which ACL is related
    * @param entity
    *           The entity holding the permission. Can be user-userId, user-emailAddress, group-groupId,
    *           group-emailAddress, allUsers, or allAuthenticatedUsers.
    *
    * @return a BucketAccessControls resource
    */

   @Named("BucketAccessControls:get")
   @GET
   @Path("/b/{bucket}/acl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   BucketAccessControls getBucketAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity);

   /**
    * Creates a new ACL entry on the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket of which ACL to be created
    *
    * @param template
    *           In the request body,supply a {@link BucketAccessControlsTemplate} resource with role and entity
    *
    * @return If successful, this method returns a BucketAccessControls resource in the response body
    */

   @Named("BucketAccessControls:insert")
   @POST
   @Path("/b/{bucket}/acl")
   BucketAccessControls createBucketAccessControls(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) BucketAccessControlsTemplate template);

   /**
    * Permanently deletes the ACL entry for the specified entity on the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket of that ACL is related
    * @return If successful, this method returns an empty response body.
    */

   @Named("BucketAccessControls:delete")
   @DELETE
   @Path("/b/{bucket}/acl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   HttpResponse deleteBucketAccessControls(@PathParam("bucket") String bucketName, @PathParam("entity") String entity);

   /**
    * Retrieves all ACL entries on a specified bucket
    *
    * @param bucketName
    *           Name of the bucket which ACL is related
    *
    * @return ListBucketAccessControls resource
    */

   @Named("BucketAccessControls:list")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/acl")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   @SelectJson("items")
   List<BucketAccessControls> listBucketAccessControls(@PathParam("bucket") String bucketName);

   /**
    * Updates an ACL entry on the specified bucket
    *
    * @param bucketName
    *           Name of the bucket which ACL to be created
    * @param entity
    *           The entity holding the permission. Can be user-userId, user-emailAddress, group-groupId,
    *           group-emailAddress, allUsers, or allAuthenticatedUsers. In the request body, supply a
    *           {@link BucketAccessControlsTemplate} resource with role
    *
    * @return If successful, this method returns a {@link BucketAccessControlsTemplate} resource in the response body
    */
   @Named("BucketAccessControls:update")
   @PUT
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/acl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   BucketAccessControls updateBucketAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity,
            @BinderParam(BindToJsonPayload.class) BucketAccessControlsTemplate template);

   /**
    * Updates an ACL entry on the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket which ACL to be created
    * @param entity
    *           The entity holding the permission. Can be user-userId, user-emailAddress, group-groupId,
    *           group-emailAddress, allUsers, or allAuthenticatedUsers
    *
    * @param template
    *           In the request body, supply a {@link BucketAccessControlsTemplate} resource with role
    *
    * @return If successful, this method returns a BucketAccessControls resource in the response body
    */
   @Named("BucketAccessControls:patch")
   @PATCH
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/acl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   BucketAccessControls patchBucketAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity,
            @BinderParam(BindToJsonPayload.class) BucketAccessControlsTemplate template);
}
