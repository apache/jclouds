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
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.googlecloudstorage.binders.MultipartUploadBinder;
import org.jclouds.googlecloudstorage.domain.GoogleCloudStorageObject;
import org.jclouds.googlecloudstorage.domain.ListPageWithPrefixes;
import org.jclouds.googlecloudstorage.domain.RewriteResponse;
import org.jclouds.googlecloudstorage.domain.templates.ComposeObjectTemplate;
import org.jclouds.googlecloudstorage.domain.templates.ObjectTemplate;
import org.jclouds.googlecloudstorage.options.ComposeObjectOptions;
import org.jclouds.googlecloudstorage.options.CopyObjectOptions;
import org.jclouds.googlecloudstorage.options.DeleteObjectOptions;
import org.jclouds.googlecloudstorage.options.GetObjectOptions;
import org.jclouds.googlecloudstorage.options.InsertObjectOptions;
import org.jclouds.googlecloudstorage.options.ListObjectOptions;
import org.jclouds.googlecloudstorage.options.RewriteObjectOptions;
import org.jclouds.googlecloudstorage.options.UpdateObjectOptions;
import org.jclouds.googlecloudstorage.parser.ParseToPayloadEnclosing;
import org.jclouds.http.options.HttpRequestOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadEnclosing;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PATCH;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * Provides access to Object entities via their REST API.
 *
 * @see <a href="https://developers.google.com/storage/docs/json_api/v1/objects"/>
 */
@RequestFilters(OAuthFilter.class)
public interface ObjectApi {

   /**
    * Check the existence of an object
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    *
    * @return a {@link Object} true if object exists
    */
   @Named("Object:Exist")
   @GET
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean objectExists(@PathParam("bucket") String bucketName, @PathParam("object") @Encoded String objectName);

   /**
    * Retrieve an object metadata
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    *
    * @return a {@link Object} resource
    */
   @Named("Object:get")
   @GET
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Consumes(APPLICATION_JSON)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   GoogleCloudStorageObject getObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName);

   /**
    * Retrieves objects metadata
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param options
    *           A class that implements {@link HttpRequestOptions}
    *           such as {@link GetObjectOptions} with optional query parameters
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:get")
   @GET
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Consumes(APPLICATION_JSON)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   GoogleCloudStorageObject getObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName, HttpRequestOptions options);

   /**
    * Retrieve an object or their metadata
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    *
    * @return a {@link Object} resource
    */
   @Named("Object:get")
   @GET
   @QueryParams(keys = "alt", values = "media")
   @Path("storage/v1/b/{bucket}/o/{object}")
   @ResponseParser(ParseToPayloadEnclosing.class)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   PayloadEnclosing download(@PathParam("bucket") String bucketName, @PathParam("object") @Encoded String objectName);

   /**
    * Retrieves objects
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param options
    *           A class that implements {@link HttpRequestOptions}
    *           such as {@link GetObjectOptions} with optional query parameters
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:get")
   @GET
   @QueryParams(keys = "alt", values = "media")
   @Path("storage/v1/b/{bucket}/o/{object}")
   @ResponseParser(ParseToPayloadEnclosing.class)
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   PayloadEnclosing download(@PathParam("bucket") String bucketName, @PathParam("object") @Encoded String objectName,
         HttpRequestOptions options);

   /**
    * Stores a new object. Object metadata setting is not supported with simple uploads
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#simple
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param options
    *           Supply an {@link InsertObjectOptions}. 'name' should not null.
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:simpleUpload")
   @POST
   @QueryParams(keys = "uploadType", values = "media")
   @Consumes(APPLICATION_JSON)
   @Path("/upload/storage/v1/b/{bucket}/o")
   GoogleCloudStorageObject simpleUpload(@PathParam("bucket") String bucketName, @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength, @PayloadParam("payload") Payload payload,
            InsertObjectOptions options);

   /**
    * Deletes an object and its metadata. Deletions are permanent if versioning is not enabled.
    *
    * @param bucketName
    *           Name of the bucket in which the object to be deleted resides
    * @param objectName
    *           Name of the object
    */
   @Named("Object:delete")
   @DELETE
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteObject(@PathParam("bucket") String bucketName, @PathParam("object") @Encoded String objectName);

   /**
    * Deletes an object and its metadata. Deletions are permanent if versioning is not enabled for the bucket, or if the
    * generation parameter is used.
    *
    * @param bucketName
    *           Name of the bucket in which the object to be deleted resides
    * @param objectName
    *           Name of the object
    * @param options
    *           Supply {@link DeleteObjectOptions} with optional query parameters
    */
   @Named("Object:delete")
   @DELETE
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteObject(@PathParam("bucket") String bucketName, @PathParam("object") @Encoded String objectName,
            DeleteObjectOptions options);

   /**
    * Retrieves a list of objects matching the criteria.
    *
    * @param bucketName
    *           Name of the bucket in which to look for objects.
    */
   @Named("Object:list")
   @GET
   @Consumes(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o")
   @Fallback(NullOnNotFoundOr404.class)
   ListPageWithPrefixes<GoogleCloudStorageObject> listObjects(@PathParam("bucket") String bucketName);

   /**
    * Retrieves a list of objects matching the criteria.
    *
    * @param bucketName
    *           Name of the bucket in which to look for objects.
    * @param options
    *          Supply {@link ListObjectOptions}
    * @return a {@link ListPage<GoogleCloudStorageObject>}
    */
   @Named("Object:list")
   @GET
   @Consumes(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o")
   @Fallback(NullOnNotFoundOr404.class)
   ListPageWithPrefixes<GoogleCloudStorageObject> listObjects(@PathParam("bucket") String bucketName, ListObjectOptions options);

   /**
    * Updates an object metadata
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param objectTemplate
    *           Supply  an {@link ObjectTemplate}
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:update")
   @PUT
   @Consumes(APPLICATION_JSON)
   @Produces(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(NullOnNotFoundOr404.class)
   GoogleCloudStorageObject updateObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName,
         @BinderParam(BindToJsonPayload.class) ObjectTemplate objectTemplate);

   /**
    * Updates an object
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param objectTemplate
    *           Supply an{@link ObjectTemplate}
    * @param options
    *           Supply {@link UpdateObjectOptions} with optional query parameters
    *
    * @return a {@link GoogleCloudStorageObject} .
    */
   @Named("Object:update")
   @PUT
   @Consumes(APPLICATION_JSON)
   @Produces(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(NullOnNotFoundOr404.class)
   GoogleCloudStorageObject updateObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName,
         @BinderParam(BindToJsonPayload.class) ObjectTemplate objectTemplate, UpdateObjectOptions options);

   /**
    * Updates an object according to patch semantics
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param objectTemplate
    *           Supply {@link ObjectTemplate} with optional query parameters
    *
    * @return  a {@link GoogleCloudStorageObject}
    */
   @Named("Object:patch")
   @PATCH
   @Consumes(APPLICATION_JSON)
   @Produces(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(NullOnNotFoundOr404.class)
   GoogleCloudStorageObject patchObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName,
         @BinderParam(BindToJsonPayload.class) ObjectTemplate objectTemplate);

   /**
    * Updates an object according to patch semantics
    *
    * @param bucketName
    *           Name of the bucket in which the object resides
    * @param objectName
    *           Name of the object
    * @param objectTemplate
    *           Supply {@link ObjectTemplate} with optional query parameters
    * @param options
    *           Supply {@link UpdateObjectOptions} with optional query parameters
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:patch")
   @PUT
   @Consumes(APPLICATION_JSON)
   @Produces(APPLICATION_JSON)
   @Path("storage/v1/b/{bucket}/o/{object}")
   @Fallback(NullOnNotFoundOr404.class)
   GoogleCloudStorageObject patchObject(@PathParam("bucket") String bucketName,
         @PathParam("object") @Encoded String objectName,
         @BinderParam(BindToJsonPayload.class) ObjectTemplate objectTemplate, UpdateObjectOptions options);

   /**
    * Concatenates a list of existing objects into a new object in the same bucket.
    *
    * @param destinationBucket
    *           Name of the bucket in which the object to be stored
    * @param destinationObject
    *           The type of upload request.
    * @param composeObjectTemplate
    *           Supply a {@link ComposeObjectTemplate}
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:compose")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("storage/v1/b/{destinationBucket}/o/{destinationObject}/compose")
   GoogleCloudStorageObject composeObjects(@PathParam("destinationBucket") String destinationBucket,
            @PathParam("destinationObject") @Encoded String destinationObject,
            @BinderParam(BindToJsonPayload.class) ComposeObjectTemplate composeObjectTemplate);

   /**
    * Concatenates a list of existing objects into a new object in the same bucket.
    *
    * @param destinationBucket
    *           Name of the bucket in which the object to be stored
    * @param destinationObject
    *           The type of upload request.
    * @param composeObjectTemplate
    *           Supply a {@link ComposeObjectTemplate}
    * @param options
    *           Supply an {@link ComposeObjectOptions}
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:compose")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("storage/v1/b/{destinationBucket}/o/{destinationObject}/compose")
   GoogleCloudStorageObject composeObjects(@PathParam("destinationBucket") String destinationBucket,
            @PathParam("destinationObject") @Encoded String destinationObject,
            @BinderParam(BindToJsonPayload.class) ComposeObjectTemplate composeObjectTemplate,
            ComposeObjectOptions options);

   /**
    * Copies an object to a specified location.
    *
    * @param destinationBucket
    *           Name of the bucket in which to store the new object
    * @param destinationObject
    *           Name of the new object.
    * @param sourceBucket
    *           Name of the bucket in which to find the source object
    * @param sourceObject
    *           Name of the source object
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:copy")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("/storage/v1/b/{sourceBucket}/o/{sourceObject}/copyTo/b/{destinationBucket}/o/{destinationObject}")
   GoogleCloudStorageObject copyObject(@PathParam("destinationBucket") String destinationBucket,
         @PathParam("destinationObject") @Encoded String destinationObject,
         @PathParam("sourceBucket") String sourceBucket,
         @PathParam("sourceObject") @Encoded String sourceObject);

    /**
     * Copies an object to a specified location with updated metadata.
     *
     * @param destinationBucket
     *           Name of the bucket in which to store the new object
     * @param destinationObject
     *           Name of the new object.
     * @param sourceBucket
     *           Name of the bucket in which to find the source object
     * @param sourceObject
     *           Name of the source object
     * @param template
     *           Supply a {@link CopyObjectOptions}
     *
     * @return a {@link GoogleCloudStorageObject}
     */
    @Named("Object:copy")
    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/storage/v1/b/{sourceBucket}/o/{sourceObject}/copyTo/b/{destinationBucket}/o/{destinationObject}")
    GoogleCloudStorageObject copyObject(@PathParam("destinationBucket") String destinationBucket,
          @PathParam("destinationObject") @Encoded String destinationObject,
          @PathParam("sourceBucket") String sourceBucket,
          @PathParam("sourceObject") @Encoded String sourceObject,
          @BinderParam(BindToJsonPayload.class) ObjectTemplate template);

   /**
    * Copies an object to a specified location. Optionally overrides metadata.
    *
    * @param destinationBucket
    *           Name of the bucket in which to store the new object
    * @param destinationObject
    *           Name of the new object.
    * @param sourceBucket
    *           Name of the bucket in which to find the source object
    * @param sourceObject
    *           Name of the source object
    * @param options
    *           Supply a {@link CopyObjectOptions}
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:copy")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("/storage/v1/b/{sourceBucket}/o/{sourceObject}/copyTo/b/{destinationBucket}/o/{destinationObject}")
   GoogleCloudStorageObject copyObject(@PathParam("destinationBucket") String destinationBucket,
         @PathParam("destinationObject") @Encoded String destinationObject,
         @PathParam("sourceBucket") String sourceBucket,
         @PathParam("sourceObject") @Encoded String sourceObject, CopyObjectOptions options);

   /**
    * Stores a new object with metadata.
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#multipart
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param objectTemplate
    *           Supply an {@link ObjectTemplate}.
    *
    * @return a {@link GoogleCloudStorageObject}
    */
   @Named("Object:multipartUpload")
   @POST
   @QueryParams(keys = "uploadType", values = "multipart")
   @Consumes(APPLICATION_JSON)
   @Path("/upload/storage/v1/b/{bucket}/o")
   @MapBinder(MultipartUploadBinder.class)
   GoogleCloudStorageObject multipartUpload(@PathParam("bucket") String bucketName,
            @PayloadParam("template") ObjectTemplate objectTemplate,
            @PayloadParam("payload") Payload payload);

   /**
    * Rewrites a source object to a destination object.
    *
    * @param destinationBucket
    *           Name of the bucket in which the object to be stored
    * @param destinationObject
    *           Name of the new object.
    * @param sourceBucket
    *           Name of the bucket in which to find the source object.
    * @param sourceObject
    *           Name of the source object.
    *
    * @return a {@link RewriteResponse}
    */
   @Named("Object:rewrite")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("/storage/v1/b/{sourceBucket}/o/{sourceObject}/rewriteTo/b/{destinationBucket}/o/{destinationObject}")
   RewriteResponse rewriteObjects(@PathParam("destinationBucket") String destinationBucket,
         @PathParam("destinationObject") @Encoded String destinationObject,
         @PathParam("sourceBucket") String sourceBucket, @PathParam("sourceObject") @Encoded String sourceObject);

   /**
    * Rewrites a source object to a destination object.
    *
    * @param destinationBucket
    *           Name of the bucket in which the object to be stored
    * @param destinationObject
    *           Name of the new object.
    * @param sourceBucket
    *           Name of the bucket in which to find the source object.
    * @param sourceObject
    *           Name of the source object.
    * @param options
    *           Supply an {@link RewriteObjectOptions}
    *
    * @return a {@link RewriteResponse}
    */
   @Named("Object:rewrite")
   @POST
   @Consumes(APPLICATION_JSON)
   @Path("/storage/v1/b/{sourceBucket}/o/{sourceObject}/rewriteTo/b/{destinationBucket}/o/{destinationObject}")
   RewriteResponse rewriteObjects(@PathParam("destinationBucket") String destinationBucket,
         @PathParam("destinationObject") @Encoded String destinationObject,
         @PathParam("sourceBucket") String sourceBucket,
         @PathParam("sourceObject") @Encoded String sourceObject,
         RewriteObjectOptions options);
}
