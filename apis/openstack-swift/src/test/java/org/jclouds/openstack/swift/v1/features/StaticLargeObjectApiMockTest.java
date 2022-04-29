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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.OBJECT_METADATA_PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.DeleteStaticLargeObjectResponse;
import org.jclouds.openstack.swift.v1.domain.Segment;
import org.jclouds.openstack.v2_0.internal.BaseOpenStackMockTest;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;


@Test(groups = "unit", testName = "StaticLargeObjectApiMockTest")
public class StaticLargeObjectApiMockTest extends BaseOpenStackMockTest<SwiftApi> {

   public void testReplaceManifest() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().addHeader(HttpHeaders.ETAG, "\"abcd\"")));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         assertEquals(
               api.getStaticLargeObjectApi("DFW", "myContainer").replaceManifest(
                     "myObject",
                     ImmutableList
                           .<Segment> builder()
                           .add(Segment.builder().path("/mycontainer/objseg1").etag("0228c7926b8b642dfb29554cd1f00963")
                                 .sizeBytes(1468006).build())
                           .add(Segment.builder().path("/mycontainer/pseudodir/seg-obj2")
                                 .etag("5bfc9ea51a00b790717eeb934fb77b9b").sizeBytes(1572864).build())
                           .add(Segment.builder().path("/other-container/seg-final")
                                 .etag("b9c3da507d2557c1ddc51f27c54bae51").sizeBytes(256).build()).build(),
                     ImmutableMap.of("MyFoo", "Bar")), "abcd");

         assertEquals(server.getRequestCount(), 2);
         assertAuthentication(server);

         RecordedRequest replaceRequest = server.takeRequest();
         assertRequest(replaceRequest, "PUT", "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?multipart-manifest=put");
         assertEquals(replaceRequest.getHeader(OBJECT_METADATA_PREFIX + "myfoo"), "Bar");
         assertEquals(
               replaceRequest.getBody().readUtf8(),
         "[{\"path\":\"/mycontainer/objseg1\",\"etag\":\"0228c7926b8b642dfb29554cd1f00963\",\"size_bytes\":1468006}," +
          "{\"path\":\"/mycontainer/pseudodir/seg-obj2\",\"etag\":\"5bfc9ea51a00b790717eeb934fb77b9b\",\"size_bytes\":1572864}," +
          "{\"path\":\"/other-container/seg-final\",\"etag\":\"b9c3da507d2557c1ddc51f27c54bae51\",\"size_bytes\":256}]");
      } finally {
         server.shutdown();
      }
   }

   public void testReplaceManifestUnicodeUTF8() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().addHeader(HttpHeaders.ETAG, "\"abcd\"")));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         assertEquals(
             api.getStaticLargeObjectApi("DFW", "myContainer").replaceManifest(
                 "unic₪de",
                 ImmutableList
                     .<Segment> builder()
                     .add(Segment.builder().path("/mycontainer/unic₪de/slo/1").etag("0228c7926b8b642dfb29554cd1f00963")
                         .sizeBytes(1468006).build())
                     .add(Segment.builder().path("/mycontainer/unic₪de/slo/2")
                         .etag("5bfc9ea51a00b790717eeb934fb77b9b").sizeBytes(1572864).build())
                     .add(Segment.builder().path("/mycontainer/unic₪de/slo/3")
                         .etag("b9c3da507d2557c1ddc51f27c54bae51").sizeBytes(256).build()).build(),
                 ImmutableMap.of("MyFoo", "Bar")), "abcd");

         assertEquals(server.getRequestCount(), 2);
         assertAuthentication(server);

         RecordedRequest replaceRequest = server.takeRequest();
         assertRequest(replaceRequest, "PUT", "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/unic%E2%82%AAde?multipart-manifest=put");
         assertEquals(replaceRequest.getHeader(OBJECT_METADATA_PREFIX + "myfoo"), "Bar");

         String expectedManifest =
             "[{\"path\":\"/mycontainer/unic₪de/slo/1\",\"etag\":\"0228c7926b8b642dfb29554cd1f00963\",\"size_bytes\":1468006}," +
             "{\"path\":\"/mycontainer/unic₪de/slo/2\",\"etag\":\"5bfc9ea51a00b790717eeb934fb77b9b\",\"size_bytes\":1572864}," +
             "{\"path\":\"/mycontainer/unic₪de/slo/3\",\"etag\":\"b9c3da507d2557c1ddc51f27c54bae51\",\"size_bytes\":256}]";

         long characterLength = expectedManifest.length();
         long byteLength = expectedManifest.getBytes(Charsets.UTF_8).length;

         assertNotEquals(characterLength, byteLength);
         assertEquals(replaceRequest.getHeader("content-length"), Long.toString(byteLength));

         assertEquals(
             replaceRequest.getBody().readUtf8(),
             expectedManifest);
      } finally {
         server.shutdown();
      }
   }

   public void testReplaceManifestWithHeaders() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().addHeader(HttpHeaders.ETAG, "\"abcd\"")));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         assertEquals(
               api.getStaticLargeObjectApi("DFW", "myContainer").replaceManifest(
                     "myObject",
                     ImmutableList
                           .<Segment>builder()
                           .add(Segment.builder().path("/mycontainer/objseg1").etag("0228c7926b8b642dfb29554cd1f00963")
                                 .sizeBytes(1468006).build())
                           .add(Segment.builder().path("/mycontainer/pseudodir/seg-obj2")
                                 .etag("5bfc9ea51a00b790717eeb934fb77b9b").sizeBytes(1572864).build())
                           .add(Segment.builder().path("/other-container/seg-final")
                                 .etag("b9c3da507d2557c1ddc51f27c54bae51").sizeBytes(256).build()).build(),
                     ImmutableMap.of("MyFoo", "Bar"),
                     ImmutableMap.of(
                           "content-language", "en",
                           "some-header1", "some-header-value")), "abcd");

         assertEquals(server.getRequestCount(), 2);
         assertAuthentication(server);

         RecordedRequest replaceRequest = server.takeRequest();
         assertRequest(replaceRequest, "PUT", "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?multipart-manifest=put");
         assertEquals(replaceRequest.getHeader(OBJECT_METADATA_PREFIX + "myfoo"), "Bar");

         // Content-length is automatically determined based on manifest size
         // Setting it will result in an error
         assertEquals(replaceRequest.getHeader("content-language"), "en");
         assertEquals(replaceRequest.getHeader("some-header1"), "some-header-value");

         assertEquals(
               replaceRequest.getBody().readUtf8(),
               "[{\"path\":\"/mycontainer/objseg1\",\"etag\":\"0228c7926b8b642dfb29554cd1f00963\",\"size_bytes\":1468006}," +
                     "{\"path\":\"/mycontainer/pseudodir/seg-obj2\",\"etag\":\"5bfc9ea51a00b790717eeb934fb77b9b\",\"size_bytes\":1572864}," +
                     "{\"path\":\"/other-container/seg-final\",\"etag\":\"b9c3da507d2557c1ddc51f27c54bae51\",\"size_bytes\":256}]");
      } finally {
         server.shutdown();
      }
   }

   public void testGetManifest() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200).setBody(
            stringFromResource("/manifest_get_response.json")) ));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         List<Segment> manifest = api.getStaticLargeObjectApi("DFW", "myContainer").getManifest("myObject");

         // Check response
         assertEquals(manifest.size(), 3);
         assertEquals(manifest.get(1).getSizeBytes(), 1572864);
         assertEquals(manifest.get(1).getETag(), "5bfc9ea51a00b790717eeb934fb77b9b");
         assertEquals(manifest.get(1).getPath(), "/mycontainer/pseudodir/seg-obj2");

         // Check request
         assertAuthentication(server);
         RecordedRequest getRequest = server.takeRequest();
         assertRequest(getRequest, "GET",
               "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?format=json&multipart-manifest=get");
      } finally {
         server.shutdown();
      }
   }

   public void testGetManifestFail() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(404).setBody(stringFromResource("/manifest_get_response.json")) ));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         List<Segment> manifest = api.getStaticLargeObjectApi("DFW", "myContainer").getManifest("myObject");

         // Check response
         assertEquals(manifest.size(), 0);

         // Check request
         assertAuthentication(server);
         RecordedRequest getRequest = server.takeRequest();
         assertRequest(getRequest, "GET",
               "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?format=json&multipart-manifest=get");
      } finally {
         server.shutdown();
      }
   }

   public void testDelete() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200)
            .setBody("{\"Number Not Found\": 0, \"Response Status\": \"200 OK\", \"Errors\": [], \"Number Deleted\": 6, \"Response Body\": \"\"}")));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         DeleteStaticLargeObjectResponse response = api.getStaticLargeObjectApi("DFW", "myContainer").delete("myObject");

         assertEquals(server.getRequestCount(), 2);
         assertAuthentication(server);
         assertRequest(server.takeRequest(), "DELETE", "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?multipart-manifest=delete");
         assertThat(response.status()).isEqualTo("200 OK");
         assertThat(response.deleted()).isEqualTo(6);
         assertThat(response.notFound()).isZero();
         assertThat(response.errors()).isEmpty();
      } finally {
         server.shutdown();
      }
   }

   public void testAlreadyDeleted() throws Exception {
      MockWebServer server = mockOpenStackServer();
      server.enqueue(addCommonHeaders(new MockResponse().setBody(stringFromResource("/access.json"))));
      server.enqueue(addCommonHeaders(new MockResponse().setResponseCode(200)
            .setBody("{\"Number Not Found\": 1, \"Response Status\": \"200 OK\", \"Errors\": [], \"Number Deleted\": 0, \"Response Body\": \"\"}")));

      try {
         SwiftApi api = api(server.url("/").toString(), "openstack-swift");
         DeleteStaticLargeObjectResponse response = api.getStaticLargeObjectApi("DFW", "myContainer").delete("myObject");

         assertEquals(server.getRequestCount(), 2);
         assertAuthentication(server);
         assertRequest(server.takeRequest(), "DELETE", "/v1/MossoCloudFS_5bcf396e-39dd-45ff-93a1-712b9aba90a9/myContainer/myObject?multipart-manifest=delete");
         assertThat(response.status()).isEqualTo("200 OK");
         assertThat(response.deleted()).isZero();
         assertThat(response.notFound()).isEqualTo(1);
         assertThat(response.errors()).isEmpty();
      } finally {
         server.shutdown();
      }
   }
}
