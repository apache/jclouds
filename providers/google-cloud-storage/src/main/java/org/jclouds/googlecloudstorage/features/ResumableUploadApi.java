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

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.jclouds.googlecloudstorage.binders.UploadBinder;
import org.jclouds.googlecloudstorage.domain.ResumableUpload;
import org.jclouds.googlecloudstorage.domain.templates.ObjectTemplate;
import org.jclouds.googlecloudstorage.options.InsertObjectOptions;
import org.jclouds.googlecloudstorage.parser.ParseToResumableUpload;
import org.jclouds.io.Payload;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * Provides Resumable Upload support via Rest API
 *
 * @see <a href="https://developers.google.com/storage/docs/json_api/v1/objects"/>
 * @see <a href="https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#resumable"/>
 */
@SkipEncoding({ '/', '=' })
@RequestFilters(OAuthFilter.class)
@Consumes(APPLICATION_JSON)
public interface ResumableUploadApi {

   /**
    * initiate a Resumable Upload Session
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#resumable
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param objectName
    *           Name of the object to upload
    * @param contentType
    *           Content type of the uploaded data
    * @param contentLength
    *           ContentLength of the uploaded object (Media part)
    *
    * @return a {@link ResumableUpload}
    */
   @Named("Object:initResumableUpload")
   @POST
   @QueryParams(keys = "uploadType", values = "resumable")
   @Path("/upload/storage/v1/b/{bucket}/o")
   @ResponseParser(ParseToResumableUpload.class)
   ResumableUpload initResumableUpload(@PathParam("bucket") String bucketName, @QueryParam("name") String objectName,
            @HeaderParam("X-Upload-Content-Type") String contentType,
            @HeaderParam("X-Upload-Content-Length") String contentLength);

   /**
    * initiate a Resumable Upload Session
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#simple
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param contentType
    *           Content type of the uploaded data (Media part)
    * @param contentLength
    *           Content length of the uploaded data (Media part)
    * @param metada
    *           Supply an {@link ObjectTemplate}
    *
    * @return a {@link ResumableUpload}
    */
   @Named("Object:resumableUpload")
   @POST
   @QueryParams(keys = "uploadType", values = "resumable")
   @Path("/upload/storage/v1/b/{bucket}/o")
   @ResponseParser(ParseToResumableUpload.class)
   ResumableUpload initResumableUpload(@PathParam("bucket") String bucketName,
            @HeaderParam("X-Upload-Content-Type") String contentType,
            @HeaderParam("X-Upload-Content-Length") Long contentLength,
            @BinderParam(BindToJsonPayload.class) ObjectTemplate metadata);

   /**
    * Stores a new object
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#resumable
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param options
    *           Supply {@link InsertObjectOptions} with optional query parameters. 'name' is mandatory.
    *
    * @return If successful, this method returns a {@link GoogleCloudStorageObject} resource.
    */
   @Named("Object:resumableUpload")
   @PUT
   @QueryParams(keys = "uploadType", values = "resumable")
   @Path("/upload/storage/v1/b/{bucket}/o")
   @MapBinder(UploadBinder.class)
   @ResponseParser(ParseToResumableUpload.class)
   ResumableUpload upload(@PathParam("bucket") String bucketName, @QueryParam("upload_id") String uploadId,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") String contentLength,
            @PayloadParam("payload") Payload payload);

   /**
    * Facilitate to use resumable upload operation to upload files in chunks
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#resumable
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param uploadId
    *           uploadId returned from initResumableUpload operation
    * @param contentType
    *           Content type of the uploaded data
    * @param contentLength
    *           Content length of the uploaded data
    * @param contentRange
    *           Range in {bytes StartingByte - Endingbyte/Totalsize } format ex: bytes 0 - 1213/2000
    * @param payload
    *           a {@link Payload} with actual data to upload
    *
    * @return a {@link ResumableUpload}
    */
   @Named("Object:Upload")
   @PUT
   @QueryParams(keys = "uploadType", values = "resumable")
   @Path("/upload/storage/v1/b/{bucket}/o")
   @MapBinder(UploadBinder.class)
   @ResponseParser(ParseToResumableUpload.class)
   ResumableUpload chunkUpload(@PathParam("bucket") String bucketName, @QueryParam("upload_id") String uploadId,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength,
            @HeaderParam("Content-Range") String contentRange, @PayloadParam("payload") Payload payload);

   /**
    * Check the status of a resumable upload
    *
    * @see https://developers.google.com/storage/docs/json_api/v1/how-tos/upload#resumable
    *
    * @param bucketName
    *           Name of the bucket in which the object to be stored
    * @param uploadId
    *           uploadId returned from initResumableUpload operation
    * @param contentRange
    *           Range in {bytes StartingByte - Endingbyte/Totalsize } format ex: bytes 0 - 1213/2000
    *
    * @return a {@link ResumableUpload}
    */

   @Named("Object:Upload")
   @PUT
   @DefaultValue("0")
   @QueryParams(keys = "uploadType", values = "resumable")
   @Path("/upload/storage/v1/b/{bucket}/o")
   @ResponseParser(ParseToResumableUpload.class)
   ResumableUpload checkStatus(@PathParam("bucket") String bucketName, @QueryParam("upload_id") String uploadId,
            @HeaderParam("Content-Range") String contentRange);

}
