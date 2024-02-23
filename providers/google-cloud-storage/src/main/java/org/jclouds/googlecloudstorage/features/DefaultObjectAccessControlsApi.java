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

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.ObjectRole;
import org.jclouds.googlecloudstorage.domain.ObjectAccessControls;
import org.jclouds.googlecloudstorage.domain.templates.ObjectAccessControlsTemplate;
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
 * Provides access to DefaultObjectAccessControl entities via their REST API.
 *
 * @see <a href = " https://developers.google.com/storage/docs/json_api/v1/defaultObjectAccessControls"/>
 */

@SkipEncoding({ '/', '=' })
@RequestFilters(OAuthFilter.class)
@Consumes(APPLICATION_JSON)
public interface DefaultObjectAccessControlsApi {

   /**
    * Returns the ACL entry for the specified entity on the specified object.
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    * @param entity
    *           The entity holding the permission. Can be user-userId, user-emailAddress, group-groupId,
    *           group-emailAddress, allUsers, or allAuthenticatedUsers
    *
    * @return an DefaultObjectAccessControls resource
    */
   @Named("DefaultObjectAccessControls:get")
   @GET
   @Path("/b/{bucket}/defaultObjectAcl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   ObjectAccessControls getDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
         @PathParam("entity") String entity);

   /**
    * Creates a new ACL entry for specified object
    *
    * @param bucketName
    *           Name of the bucket of that ACL to be created In the request body, supply a DefaultObjectAccessControls
    *           resource with the following properties
    *
    * @return If successful, this method returns a DefaultObjectAccessControls resource
    */
   @Named("DefaultObjectAccessControls:insert")
   @POST
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/defaultObjectAcl")
   ObjectAccessControls createDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) ObjectAccessControlsTemplate template);

   /**
    * Permanently deletes the DefaultObjectAcessControl entry for the specified entity on the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    * @param entity
    *           The entity holding the permission. Can be user-userId, user-emailAddress, group-groupId,
    *           group-emailAddress, allUsers, or allAuthenticatedUsers
    *
    * @return If successful, this method returns an empty response body
    */
   @Named("DefaultObjectAccessControls:delete")
   @DELETE
   @Path("/b/{bucket}/defaultObjectAcl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   HttpResponse deleteDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity);

   /**
    * Retrieves ACL entries on a specified object
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    *
    * @return ListObjectAccessControls resource
    *
    */
   @Named("DefaultObjectAccessControls:list")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/defaultObjectAcl")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   @SelectJson("items")
   List<ObjectAccessControls> listDefaultObjectAccessControls(@PathParam("bucket") String bucketName);

   /**
    * Retrieves ACL entries on a specified object
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    *
    * @return DefaultObjectAccessControls resource
    *
    */
   @Named("DefaultObjectAccessControls:update")
   @PUT
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/defaultObjectAcl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   ObjectAccessControls updateDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity,
            @BinderParam(BindToJsonPayload.class) ObjectAccessControls payload);

   /**
    * Retrieves ACL entries on a specified object
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    */
   @Named("DefaultObjectAccessControls:update")
   @PUT
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/defaultObjectAcl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   ObjectAccessControls updateDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity,
            @BinderParam(BindToJsonPayload.class) ObjectAccessControls payload,
            @QueryParam("role") ObjectRole role);

   /**
    * Retrieves ACL entries on a specified object
    *
    * @param bucketName
    *           Name of the bucket which contains the object
    */
   @Named("DefaultObjectAccessControls:patch")
   @PATCH
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}/defaultObjectAcl/{entity}")
   @Fallback(NullOnNotFoundOr404.class)
   ObjectAccessControls patchDefaultObjectAccessControls(@PathParam("bucket") String bucketName,
            @PathParam("entity") String entity,
            @BinderParam(BindToJsonPayload.class) ObjectAccessControls payload);
}
