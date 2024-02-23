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

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jakarta.ws.rs.core.MediaType;

import org.jclouds.googlecloudstorage.domain.BucketAccessControls.Role;
import org.jclouds.googlecloudstorage.domain.templates.BucketAccessControlsTemplate;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageApiExpectTest;
import org.jclouds.googlecloudstorage.parse.BucketAclGetTest;
import org.jclouds.googlecloudstorage.parse.BucketAclInsertTest;
import org.jclouds.googlecloudstorage.parse.BucketAclListTest;
import org.jclouds.googlecloudstorage.parse.BucketAclUpdateTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "BucketAccessControlsApiExpectTest")
public class BucketAccessControlsApiExpectTest extends BaseGoogleCloudStorageApiExpectTest {

   private static final String EXPECTED_TEST_BUCKET = "jcloudstestbucket";

   public static final HttpRequest GET_BUCKETACL_REQUEST = HttpRequest.builder().method("GET")
            .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl/allUsers")
            .addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + TOKEN).build();

   private final HttpResponse GET_BUCKETACL_RESPONSE = HttpResponse.builder().statusCode(200)
            .payload(staticPayloadFromResource("/bucket_acl_get.json")).build();

   private final HttpResponse CREATE_BUCKETACL_RESPONSE = HttpResponse.builder().statusCode(200)
            .payload(staticPayloadFromResource("/bucket_acl_insert_response.json")).build();

   private final HttpRequest LIST_BUCKETACL_REQUEST = HttpRequest.builder().method("GET")
            .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl")
            .addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + TOKEN).build();

   private final HttpResponse LIST_BUCKETACL_RESPONSE = HttpResponse.builder().statusCode(200)
            .payload(staticPayloadFromResource("/bucket_acl_list.json")).build();

   // Test getBucketAccessControls
   public void testGetBucketAclResponseIs2xx() throws Exception {

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               GET_BUCKETACL_REQUEST, GET_BUCKETACL_RESPONSE).getBucketAccessControlsApi();

      assertEquals(api.getBucketAccessControls(EXPECTED_TEST_BUCKET, "allUsers"), new BucketAclGetTest().expected());
   }

   public void testGetBucketAclResponseIs4xx() throws Exception {

      HttpResponse getResponse = HttpResponse.builder().statusCode(404).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               GET_BUCKETACL_REQUEST, getResponse).getBucketAccessControlsApi();

      assertNull("404", api.getBucketAccessControls(EXPECTED_TEST_BUCKET, "allUsers"));

   }

   // Test listBucketAccessControls
   public void testListBucketAclResponseIs2xx() throws Exception {

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               LIST_BUCKETACL_REQUEST, LIST_BUCKETACL_RESPONSE).getBucketAccessControlsApi();

      assertEquals(api.listBucketAccessControls(EXPECTED_TEST_BUCKET), new BucketAclListTest().expected());

   }

   public void testListBucketAclResponseIs4xx() throws Exception {
      HttpResponse listResponse = HttpResponse.builder().statusCode(404).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               LIST_BUCKETACL_REQUEST, listResponse).getBucketAccessControlsApi();

      assertNull(api.listBucketAccessControls("jcloudstestbucket"));
   }

   // Test insertBucketAccessControls
   public void testInsertBucketAclResponseIs2xx() throws Exception {
      HttpRequest insertRequest = HttpRequest
               .builder()
               .method("POST")
               .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl")
               .addHeader("Accept", "application/json")
               .addHeader("Authorization", "Bearer " + TOKEN)
               .payload(payloadFromResourceWithContentType("/bucket_acl_insert_initial.json",
                        MediaType.APPLICATION_JSON)).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               insertRequest, CREATE_BUCKETACL_RESPONSE).getBucketAccessControlsApi();

      BucketAccessControlsTemplate template = BucketAccessControlsTemplate.create("allAuthenticatedUsers", Role.WRITER);

      assertEquals(api.createBucketAccessControls(EXPECTED_TEST_BUCKET, template), new BucketAclInsertTest().expected());
   }

   // Test deleteBucketAccessControls
   public void testDeleteBucketAclResponseIs2xx() throws Exception {
      HttpRequest delete = HttpRequest.builder().method("DELETE")
               .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl/allAuthenticatedUsers")
               .addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse deleteResponse = HttpResponse.builder().statusCode(204).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               delete, deleteResponse).getBucketAccessControlsApi();

      assertEquals(api.deleteBucketAccessControls(EXPECTED_TEST_BUCKET, "allAuthenticatedUsers"), deleteResponse);
   }

   public void testDeleteBucketAclResponseIs4xx() throws Exception {
      HttpRequest delete = HttpRequest.builder().method("DELETE")
               .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl/allAuthenticatedUsers")
               .addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + TOKEN).build();

      HttpResponse deleteResponse = HttpResponse.builder().statusCode(404).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               delete, deleteResponse).getBucketAccessControlsApi();

      assertNull(api.deleteBucketAccessControls(EXPECTED_TEST_BUCKET, "allAuthenticatedUsers"));
   }

   // Test updateBucketAccessControls
   public void testUpdateBucketAclResponseIs2xx() throws Exception {
      HttpRequest update = HttpRequest
               .builder()
               .method("PUT")
               .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl/allUsers")
               .addHeader("Accept", "application/json")
               .addHeader("Authorization", "Bearer " + TOKEN)
               .payload(payloadFromResourceWithContentType("/bucket_acl_update_initial.json",
                        MediaType.APPLICATION_JSON)).build();

      HttpResponse updateResponse = HttpResponse.builder().statusCode(200)
               .payload(staticPayloadFromResource("/bucket_acl_update_response.json")).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               update, updateResponse).getBucketAccessControlsApi();

      BucketAccessControlsTemplate template = BucketAccessControlsTemplate.create("allUsers", Role.OWNER);

      assertEquals(api.updateBucketAccessControls(EXPECTED_TEST_BUCKET, "allUsers", template),
               new BucketAclUpdateTest().expected());
   }

   // Test updateBucketAccessControls
   public void testPatchBucketAclResponseIs2xx() throws Exception {
      HttpRequest patchRequest = HttpRequest
               .builder()
               .method("PATCH")
               .endpoint("https://www.googleapis.com/storage/v1/b/jcloudstestbucket/acl/allUsers")
               .addHeader("Accept", "application/json")
               .addHeader("Authorization", "Bearer " + TOKEN)
               .payload(payloadFromResourceWithContentType("/bucket_acl_update_initial.json",
                        MediaType.APPLICATION_JSON)).build();

      HttpResponse patchResponse = HttpResponse.builder().statusCode(200)
               .payload(staticPayloadFromResource("/bucket_acl_update_response.json")).build();

      BucketAccessControlsApi api = requestsSendResponses(requestForScopes(STORAGE_FULLCONTROL_SCOPE), TOKEN_RESPONSE,
               patchRequest, patchResponse).getBucketAccessControlsApi();

      BucketAccessControlsTemplate template = BucketAccessControlsTemplate.create("allUsers", Role.OWNER);

      assertEquals(api.patchBucketAccessControls(EXPECTED_TEST_BUCKET, "allUsers", template),
               new BucketAclUpdateTest().expected());
   }
}
