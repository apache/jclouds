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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.DestinationPredefinedAcl;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.PredefinedAcl;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.Projection;
import org.jclouds.googlecloudstorage.domain.templates.ComposeObjectTemplate;
import org.jclouds.googlecloudstorage.domain.templates.ObjectTemplate;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageApiMockTest;
import org.jclouds.googlecloudstorage.options.ComposeObjectOptions;
import org.jclouds.googlecloudstorage.options.CopyObjectOptions;
import org.jclouds.googlecloudstorage.options.GetObjectOptions;
import org.jclouds.googlecloudstorage.options.InsertObjectOptions;
import org.jclouds.googlecloudstorage.options.ListObjectOptions;
import org.jclouds.googlecloudstorage.options.RewriteObjectOptions;
import org.jclouds.googlecloudstorage.parse.ParseGoogleCloudStorageObject;
import org.jclouds.googlecloudstorage.parse.ParseGoogleCloudStorageObjectListTest;
import org.jclouds.googlecloudstorage.parse.ParseObjectRewriteResponse;
import org.jclouds.http.internal.PayloadEnclosingImpl;
import org.jclouds.io.PayloadEnclosing;
import org.jclouds.util.Strings2;
import org.testng.annotations.Test;

import com.google.common.net.MediaType;


@Test(groups = "unit", testName = "ObjectApiMockTest", singleThreaded = true)
public class ObjectApiMockTest extends BaseGoogleCloudStorageApiMockTest {


   public void exists() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      assertTrue(objectApi().objectExists("test", "file_name"));
      assertSent(server, "GET", "/storage/v1/b/test/o/file_name", null);
   }

   public void existsEncoded() throws Exception {
      server.enqueue(jsonResponse("/object_encoded_get.json"));

      assertTrue(objectApi().objectExists("test", Strings2.urlEncode("dir/file name")));
      assertSent(server, "GET", "/storage/v1/b/test/o/dir%2Ffile%20name", null);
   }

   public void exists_4xx() throws Exception {
      server.enqueue(response404());

      assertFalse(objectApi().objectExists("test", "file_name"));
      assertSent(server, "GET", "/storage/v1/b/test/o/file_name", null);
   }

   public void get() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      assertEquals(objectApi().getObject("test", "file_name"),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "GET", "/storage/v1/b/test/o/file_name");
   }

   public void get_4xx() throws Exception {
      server.enqueue(response404());

      assertNull(objectApi().getObject("test", "file_name"));
      assertSent(server, "GET", "/storage/v1/b/test/o/file_name");
   }

   public void get_with_options() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      GetObjectOptions options = new GetObjectOptions().ifGenerationMatch((long) 1000);
      options.range(0, 1023);

      assertEquals(objectApi().getObject("test", "file_name", options),
            new ParseGoogleCloudStorageObject().expected());
      RecordedRequest request = assertSent(server, "GET", "/storage/v1/b/test/o/file_name?ifGenerationMatch=1000");
      assertEquals(request.getHeader("Range"), "bytes=0-1023");
   }

   public void simpleUpload() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      PayloadEnclosing p = new PayloadEnclosingImpl();
      String testPayload = "this is a test payload for upload!";
      p.setPayload(testPayload.getBytes());

      InsertObjectOptions options = new InsertObjectOptions()
         .name("new_object")
         .predefinedAcl(PredefinedAcl.PUBLIC_READ_WRITE);

      assertEquals(objectApi().simpleUpload("bucket_name", "text/plain",
            p.getPayload().getContentMetadata().getContentLength(), p.getPayload(), options),
            new ParseGoogleCloudStorageObject().expected());

      RecordedRequest request = assertSent(server, "POST", "/upload/storage/v1/b/bucket_name/o" +
         "?uploadType=media&name=new_object&predefinedAcl=publicReadWrite", null);
      assertEquals(request.getHeader("Content-Type"), "text/plain");
      assertEquals(request.getBody().readUtf8(), testPayload);
   }

   public void delete() throws Exception {
      server.enqueue(new MockResponse());

      // TODO: Should this be returning True on not found?
      assertTrue(objectApi().deleteObject("test", "object_name"));
      assertSent(server, "DELETE", "/storage/v1/b/test/o/object_name", null);
   }

   public void delete_encoded() throws Exception {
      server.enqueue(new MockResponse());

      // TODO: Should this be returning True on not found?
      assertTrue(objectApi().deleteObject("test", Strings2.urlEncode("dir/object name")));
      assertSent(server, "DELETE", "/storage/v1/b/test/o/dir%2Fobject%20name", null);
   }

   public void list() throws Exception {
      server.enqueue(jsonResponse("/object_list.json"));

      assertEquals(objectApi().listObjects("test"),
            new ParseGoogleCloudStorageObjectListTest().expected());
      assertSent(server, "GET", "/storage/v1/b/test/o");
   }

   public void list_with_options() throws Exception {
      server.enqueue(jsonResponse("/object_list.json"));
      ListObjectOptions options = new ListObjectOptions()
         .delimiter("-")
         .prefix("test")
         .versions(Boolean.TRUE)
         .pageToken("asdf")
         .maxResults(4)
         .projection(Projection.FULL);
      assertEquals(objectApi().listObjects("test", options),
            new ParseGoogleCloudStorageObjectListTest().expected());
      assertSent(server, "GET", "/storage/v1/b/test/o?" +
        "delimiter=-&prefix=test&versions=true&pageToken=asdf&maxResults=4&projection=full");
   }

   public void update() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) 1000).crc32c("crc32c");

      assertEquals(objectApi().updateObject("test", "file_name", template),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "PUT", "/storage/v1/b/test/o/file_name", APPLICATION_JSON,
            "{" +
            "  \"name\": \"file_name\"," +
            "  \"size\": 1000," +
            "  \"crc32c\": \"crc32c\"" +
            "}");
   }

   public void patch() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) 1000).crc32c("crc32c");

      assertEquals(objectApi().patchObject("test", "file_name", template),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "PATCH", "/storage/v1/b/test/o/file_name", APPLICATION_JSON,
            "{" +
            "  \"name\": \"file_name\"," +
            "  \"size\": 1000," +
            "  \"crc32c\": \"crc32c\"" +
            "}");
   }

   public void compose() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) 1000).crc32c("crc32c");

      ComposeObjectTemplate composeTemplate = ComposeObjectTemplate.builder()
      .fromGoogleCloudStorageObject(new ParseGoogleCloudStorageObjectListTest().expected())
      .destination(template).build();

      assertEquals(objectApi().composeObjects("destination_bucket", "destination_object", composeTemplate),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "POST", "/storage/v1/b/destination_bucket/o/destination_object/compose", APPLICATION_JSON,
            stringFromResource("/object_compose_request.json"));
   }

   public void compose_with_options() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) 1000).crc32c("crc32c");
      ComposeObjectTemplate composeTemplate = ComposeObjectTemplate.builder()
      .fromGoogleCloudStorageObject(new ParseGoogleCloudStorageObjectListTest().expected())
      .destination(template).build();

      ComposeObjectOptions options = new ComposeObjectOptions()
         .destinationPredefinedAcl(DestinationPredefinedAcl.BUCKET_OWNER_FULLCONTROL)
         .ifMetagenerationMatch((long) 15);

      assertEquals(objectApi().composeObjects("destination_bucket", "destination_object", composeTemplate, options),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "POST", "/storage/v1/b/destination_bucket/o/destination_object/compose" +
            "?destinationPredefinedAcl=bucketOwnerFullcontrol&ifMetagenerationMatch=15", APPLICATION_JSON,
            stringFromResource("/object_compose_request.json"));
   }

   public void copy() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      assertEquals(objectApi().copyObject("destination_bucket", "destination_object", "source_bucket", "source_object"),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "POST", "/storage/v1/b/source_bucket/o/source_object/copyTo" +
              "/b/destination_bucket/o/destination_object", APPLICATION_JSON);
   }

    public void copy_update_metadata() throws Exception {
        server.enqueue(jsonResponse("/object_get.json"));

        ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) 1000).crc32c("crc32c");

        assertEquals(objectApi().copyObject("destination_bucket", "destination_object", "source_bucket", "source_object", template),
                new ParseGoogleCloudStorageObject().expected());
        assertSent(server, "POST", "/storage/v1/b/source_bucket/o/source_object/copyTo" +
                "/b/destination_bucket/o/destination_object", APPLICATION_JSON, "{" +
                "  \"name\": \"file_name\"," +
                "  \"size\": 1000," +
                "  \"crc32c\": \"crc32c\"" +
                "}");
    }

   public void copy_with_options() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      CopyObjectOptions options = new CopyObjectOptions().ifGenerationMatch((long) 50);

      assertEquals(objectApi().copyObject("destination_bucket", "destination_object", "source_bucket", "source_object", options),
            new ParseGoogleCloudStorageObject().expected());
      assertSent(server, "POST", "/storage/v1/b/source_bucket/o/source_object/copyTo" +
              "/b/destination_bucket/o/destination_object?ifGenerationMatch=50", APPLICATION_JSON);
   }

   public void multipartUpload() throws Exception {
      server.enqueue(jsonResponse("/object_get.json"));

      PayloadEnclosing p = new PayloadEnclosingImpl();
      String testPayload = "this is a test payload for upload!";
      p.setPayload(testPayload.getBytes());

      ObjectTemplate template = new ObjectTemplate().name("file_name").size((long) testPayload.length()).crc32c("crc32c").contentType(MediaType.ANY_TEXT_TYPE);

      assertEquals(objectApi().multipartUpload("bucket_name", template, p.getPayload()),
            new ParseGoogleCloudStorageObject().expected());

      RecordedRequest request = assertSent(server, "POST", "/upload/storage/v1/b/bucket_name/o?uploadType=multipart", null);
      assertTrue(request.getBody().readUtf8().contains(testPayload));

      //TODO: this should be a more robust assertion about the formatting of the payload
   }

   public void rewrite() throws Exception {
      server.enqueue(jsonResponse("/object_rewrite.json"));

      assertEquals(objectApi().rewriteObjects("destinationBucket", "destinationObject", "sourceBucket", "sourceObject"),
            new ParseObjectRewriteResponse().expected());

      assertSent(server, "POST", "/storage/v1/b/sourceBucket/o/sourceObject/rewriteTo/b/destinationBucket/o/destinationObject");
   }

   public void rewriteWithOptions() throws Exception {
      server.enqueue(jsonResponse("/object_rewrite.json"));

      RewriteObjectOptions options = new RewriteObjectOptions.Builder().rewriteToken("rewriteToken");
      assertEquals(objectApi().rewriteObjects("destinationBucket", "destinationObject", "sourceBucket", "sourceObject", options),
            new ParseObjectRewriteResponse().expected());

      assertSent(server, "POST",
            "/storage/v1/b/sourceBucket/o/sourceObject/rewriteTo/b/destinationBucket/o/destinationObject?rewriteToken=rewriteToken");
   }

   public ObjectApi objectApi(){
      return api().getObjectApi();
   }
}
