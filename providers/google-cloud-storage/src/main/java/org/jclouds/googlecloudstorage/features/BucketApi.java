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

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.blobstore.BlobStoreFallbacks.NullOnKeyAlreadyExists;
import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecloudstorage.GoogleCloudStorageFallbacks.NullOnBucketAlreadyExists;
import org.jclouds.googlecloudstorage.domain.Bucket;
import org.jclouds.googlecloudstorage.domain.templates.BucketTemplate;
import org.jclouds.googlecloudstorage.options.DeleteBucketOptions;
import org.jclouds.googlecloudstorage.options.GetBucketOptions;
import org.jclouds.googlecloudstorage.options.InsertBucketOptions;
import org.jclouds.googlecloudstorage.options.ListOptions;
import org.jclouds.googlecloudstorage.options.UpdateBucketOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.PATCH;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * Provides access to Bucket entities via their REST API.
 *
 * @see <a href = "https://developers.google.com/storage/docs/json_api/v1/buckets"/>
 */

@SkipEncoding({ '/', '=' })
@RequestFilters(OAuthFilter.class)
@Consumes(APPLICATION_JSON)
public interface BucketApi {

   /**
    * Check the existence of a bucket
    *
    * @param bucketName
    *           Name of the bucket
    *
    * @return a {@link Bucket} true if bucket exist
    */
   @Named("Bucket:get")
   @GET
   @Path("/b/{bucket}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean bucketExist(@PathParam("bucket") String bucketName);

   /**
    * Returns metadata for the specified bucket.
    *
    * @param bucketName
    *           Name of the bucket
    *
    * @return a {@link Bucket} resource
    */
   @Named("Bucket:get")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Bucket getBucket(@PathParam("bucket") String bucketName);

   /**
    * Returns metadata for the specified bucket
    *
    * @param bucketName
    *           Name of the bucket
    * @param options
    *           Supply {@link GetBucketOptions} with optional query parameters
    *
    * @return a {@link Bucket} resource
    */
   @Named("Bucket:get")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Bucket getBucket(@PathParam("bucket") String bucketName, GetBucketOptions options);

   /**
    * Creates a new bucket
    *
    * @param projectId
    *           A valid API project identifier
    * @param bucketTemplate
    *           supply a {@link BucketTemplate} resource
    *
    * @return If successful, this method returns a {@link Bucket} resource.
    */
   @Named("Bucket:insert")
   @POST
   @Path("/b")
   @Fallback(NullOnBucketAlreadyExists.class)
   Bucket createBucket(@QueryParam("project") String projectId, @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate);

   /**
    * Creates a new Bucket
    *
    * @param projectId
    *           A valid API project identifier
    *
    * @param bucketTemplate
    *           Supply a {@link BucketTemplate} resource
    * @param options
    *           Supply {@link InsertBucketOptions} with optional query parameters
    *
    * @return If successful, this method returns a {@link Bucket} resource.
    */
   @Named("Bucket:insert")
   @POST
   @Path("/b")
   @Fallback(NullOnKeyAlreadyExists.class)
   Bucket createBucket(@QueryParam("project") String projectId,
            @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate, InsertBucketOptions options);

   /**
    * Permanently deletes an empty Bucket.If bucket is not empty 409 error to indicate the conflict.  
    *
    * @param bucketName
    *           Name of the bucket
    */
   @Named("Bucket:delete")
   @DELETE
   @Path("/b/{bucket}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteBucket(@PathParam("bucket") String bucketName);

   /**
    * Permanently deletes an empty Bucket.If bucket is not empty 409 error to indicate the conflict.
    *
    * @param bucketName
    *           Name of the bucket
    * @param options
    *           Supply {@link DeleteBucketOptions} with optional query parameters
    */
   @Named("Bucket:delete")
   @DELETE
   @Path("/b/{bucket}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteBucket(@PathParam("bucket") String bucketName, DeleteBucketOptions options);

   /**
    * Retrieves a list of buckets for a given project
    *
    * @param projectId
    *           A valid API project identifier
    *
    * @return a {@link ListPage<Bucket>}
    */
   @Named("Bucket:list")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b")
   ListPage<Bucket> listBucket(@QueryParam("project") String projectId);

   /**
    * Retrieves a list of buckets for a given project
    *
    * @param projectId
    *           A valid API project identifier
    * @param options
    *           Supply {@link ListOptions} with optional query parameters
    */
   @Named("Bucket:list")
   @GET
   @Produces(APPLICATION_JSON)
   @Path("/b")
   ListPage<Bucket> listBucket(@QueryParam("project") String projectId, ListOptions options);

   /**
    * Updates a bucket
    *
    * @param bucketName
    *           Name of the bucket
    * @param bucketTemplate
    *           Supply a {@link BucketTemplate} resource with list of {@link BucketAccessControls}
    *
    * @return If successful, this method returns the updated {@link Bucket} resource.
    */
   @Named("Bucket:update")
   @PUT
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   Bucket updateBucket(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate);

   /**
    * Updates a bucket
    *
    * @param bucketName
    *           Name of the bucket
    * @param bucketTemplate
    *           Supply a {@link BucketTemplate} resource with list of {@link BucketAccessControls}
    * @param options
    *           Supply {@link UpdateBucketOptions} with optional query parameters
    *
    * @return If successful,this method returns the updated {@link Bucket} resource.
    */
   @Named("Bucket:update")
   @PUT
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   Bucket updateBucket(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate, UpdateBucketOptions options);

   /**
    * Updates a bucket supporting patch semantics.
    *
    *  @see <a href = "https://developers.google.com/storage/docs/json_api/v1/how-tos/performance#patch"/>
    *
    * @param bucketName
    *           Name of the bucket
    * @param bucketTemplate
    *           Supply a {@link BucketTemplate} resource with list of {@link BucketAccessControls}
    *
    * @return If successful, this method returns the updated {@link Bucket} resource.
    */
   @Named("Bucket:patch")
   @PATCH
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   Bucket patchBucket(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate);

   /**
    * Updates a bucket supporting patch semantics.
    *
    * @see <a href = "https://developers.google.com/storage/docs/json_api/v1/how-tos/performance#patch"/>
    *
    * @param bucketName
    *           Name of the bucket
    * @param bucketTemplate
    *           Supply a {@link BucketTemplate} resource with list of {@link BucketAccessControls}
    * @param options
    *           Supply {@link UpdateBucketOptions} with optional query parameters
    *
    * @return If successful, this method returns the updated {@link Bucket} resource.
    */
   @Named("Bucket:patch")
   @PATCH
   @Produces(APPLICATION_JSON)
   @Path("/b/{bucket}")
   @Fallback(NullOnNotFoundOr404.class)
   Bucket patchBucket(@PathParam("bucket") String bucketName,
            @BinderParam(BindToJsonPayload.class) BucketTemplate bucketTemplate, UpdateBucketOptions options);
}
