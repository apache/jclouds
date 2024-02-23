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
package org.jclouds.googlecloudstorage;

import java.io.Closeable;

import jakarta.ws.rs.Path;

import org.jclouds.googlecloudstorage.features.BucketAccessControlsApi;
import org.jclouds.googlecloudstorage.features.BucketApi;
import org.jclouds.googlecloudstorage.features.DefaultObjectAccessControlsApi;
import org.jclouds.googlecloudstorage.features.ObjectAccessControlsApi;
import org.jclouds.googlecloudstorage.features.ObjectApi;
import org.jclouds.googlecloudstorage.features.ResumableUploadApi;
import org.jclouds.rest.annotations.Delegate;

/**
 * Provide access to GoogleCloudStorage
 *
 * @see <a href="https://developers.google.com/storage/docs/json_api/v1/">api doc /a>
 */
public interface GoogleCloudStorageApi extends Closeable {

   /**
    * Provides access to Default Object Access Control features on bucket
    */
   @Delegate
   @Path("/storage/v1")
   DefaultObjectAccessControlsApi getDefaultObjectAccessControlsApi();

   /**
    * Provides access to Bucket Access Control features
    */
   @Delegate
   @Path("/storage/v1")
   BucketAccessControlsApi getBucketAccessControlsApi();

   /**
    * Provides access to Bucket features
    */
   @Delegate
   @Path("/storage/v1")
   BucketApi getBucketApi();

   /**
    * Provides access to Object Access Control features
    */
   @Delegate
   @Path("/storage/v1")
   ObjectAccessControlsApi getObjectAccessControlsApi();

   /**
    * Provides access to Google Cloud Storage Object features
    */
   @Delegate
   ObjectApi getObjectApi();

   /**
    * Provides access to Google Cloud Storage ResumableUpload features
    */
   @Delegate
   ResumableUploadApi getResumableUploadApi();
}
