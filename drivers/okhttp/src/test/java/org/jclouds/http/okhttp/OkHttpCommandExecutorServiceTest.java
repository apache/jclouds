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
package org.jclouds.http.okhttp;

import static org.jclouds.Constants.PROPERTY_MAX_CONNECTIONS_PER_CONTEXT;
import static org.jclouds.Constants.PROPERTY_MAX_CONNECTIONS_PER_HOST;
import static org.jclouds.Constants.PROPERTY_USER_THREADS;
import static org.jclouds.util.Closeables2.closeQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Closeable;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.jclouds.http.BaseHttpCommandExecutorServiceIntegrationTest;
import org.jclouds.http.HttpResponseException;
import org.jclouds.http.config.ConfiguresHttpCommandExecutorService;
import org.jclouds.http.okhttp.config.OkHttpCommandExecutorServiceModule;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.PATCH;
import org.jclouds.rest.binders.BindToStringPayload;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;


/**
 * Tests the functionality of the {@link OkHttpCommandExecutorService}
 */
@Test
public class OkHttpCommandExecutorServiceTest extends BaseHttpCommandExecutorServiceIntegrationTest {

   @Override
   protected Module createConnectionModule() {
      return new OkHttpCommandExecutorServiceModule();
   }

   @Override
   protected void addOverrideProperties(final Properties props) {
      props.setProperty(PROPERTY_MAX_CONNECTIONS_PER_CONTEXT, 50 + "");
      props.setProperty(PROPERTY_MAX_CONNECTIONS_PER_HOST, 0 + "");
      props.setProperty(PROPERTY_USER_THREADS, 5 + "");
   }

   private interface PatchApi extends Closeable {
      @PATCH
      @Path("/objects/{id}")
      @Produces("text/plain")
      String patch(@PathParam("id") String id, @BinderParam(BindToStringPayload.class) String body);

      @PATCH
      @Path("/objects/{id}")
      String patchNothing(@PathParam("id") String id);
   }

   @Test
   public void testPatch() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse().setBody("fooPATCH"));
      PatchApi api = api(PatchApi.class, server.url("/").toString());
      try {
         String result = api.patch("", "foo");
         // Verify that the body is properly populated
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "foo");
         assertEquals(result, "fooPATCH");
         // Verify content headers are sent
         assertNotNull(request.getHeader("Content-Type"));
         assertNotNull(request.getHeader("Content-Length"));
         assertEquals(request.getHeader("Content-Type"), "text/plain");
         assertEquals(Integer.valueOf(request.getHeader("Content-Length")).intValue(), "foo".getBytes().length);
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @Test
   public void testPatchIsRetriedOnFailure() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse().setResponseCode(500),
            new MockResponse().setBody("fooPATCH"));
      PatchApi api = api(PatchApi.class, server.url("/").toString());
      try {
         String result = api.patch("", "foo");
         assertEquals(server.getRequestCount(), 2);
         assertEquals(result, "fooPATCH");
         // Verify that the body was properly sent in the two requests
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "foo");
         request = server.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "foo");
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @Test
   public void testPatchRedirect() throws Exception {
      MockWebServer redirectTarget = mockWebServer(new MockResponse().setBody("fooPATCHREDIRECT"));
      redirectTarget.useHttps(sslSocketFactory(), false);
      MockWebServer server = mockWebServer(new MockResponse().setResponseCode(302).setHeader("Location",
            redirectTarget.url("/").toString()));
      PatchApi api = api(PatchApi.class, server.url("/").toString());
      try {
         String result = api.patch("", "foo");
         assertEquals(result, "fooPATCHREDIRECT");
         assertEquals(server.getRequestCount(), 1);
         assertEquals(redirectTarget.getRequestCount(), 1);
         // Verify that the body was populated after the redirect
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "foo");
         request = redirectTarget.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "foo");
      } finally {
         closeQuietly(api);
         redirectTarget.shutdown();
         server.shutdown();
      }
   }

   @Test
   public void testZeroLengthPatch() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse());
      PatchApi api = api(PatchApi.class, server.url("/").toString());
      try {
         api.patchNothing("");
         assertEquals(server.getRequestCount(), 1);
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getMethod(), "PATCH");
         assertEquals(request.getBody().readUtf8(), "");
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @Test(expectedExceptions = HttpResponseException.class, expectedExceptionsMessageRegExp = "Failed to connect to.*")
   public void testSSLConnectionFailsIfOnlyHttpConfigured() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse());
      server.useHttps(sslSocketFactory(), false);
      Module httpConfigModule = new ConnectionSpecModule(ConnectionSpec.CLEARTEXT);
      PatchApi api = api(PatchApi.class, server.url("/").toString(), httpConfigModule);
      try {
         api.patchNothing("");
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @Test(expectedExceptions = HttpResponseException.class, expectedExceptionsMessageRegExp = "CLEARTEXT communication not enabled for client.*")
   public void testHTTPConnectionFailsIfOnlySSLConfigured() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse());
      Module httpConfigModule = new ConnectionSpecModule(ConnectionSpec.MODERN_TLS);
      PatchApi api = api(PatchApi.class, server.url("/").toString(), httpConfigModule);
      try {
         api.patchNothing("");
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @Test
   public void testBothProtocolsSucceedIfSSLAndHTTPConfigured() throws Exception {
      MockWebServer redirectTarget = mockWebServer(new MockResponse());
      MockWebServer server = mockWebServer(new MockResponse().setResponseCode(302).setHeader("Location",
            redirectTarget.url("/").toString()));
      server.useHttps(sslSocketFactory(), false);
      Module httpConfigModule = new ConnectionSpecModule(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS);
      PatchApi api = api(PatchApi.class, server.url("/").toString(), httpConfigModule);
      try {
         api.patchNothing("");
         assertEquals(server.getRequestCount(), 1);
         assertEquals(redirectTarget.getRequestCount(), 1);
      } finally {
         closeQuietly(api);
         server.shutdown();
         redirectTarget.shutdown();
      }
   }

   @Test
   public void testRestrictedSSLProtocols() throws Exception {
      MockWebServer server = mockWebServer(new MockResponse());
      server.useHttps(sslSocketFactory(), false);
      ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2)
            .build();
      PatchApi api = api(PatchApi.class, server.url("/").toString(), new ConnectionSpecModule(spec));
      try {
         api.patchNothing("");
         assertEquals(server.getRequestCount(), 1);
         RecordedRequest request = server.takeRequest();
         assertEquals(request.getTlsVersion().javaName(), "TLSv1.2");
      } finally {
         closeQuietly(api);
         server.shutdown();
      }
   }

   @ConfiguresHttpCommandExecutorService
   private static final class ConnectionSpecModule extends AbstractModule {
      private final List<ConnectionSpec> connectionSpecs;

      public ConnectionSpecModule(ConnectionSpec... connectionSpecs) {
         this.connectionSpecs = ImmutableList.copyOf(connectionSpecs);
      }

      @Override
      protected void configure() {
         install(new OkHttpCommandExecutorServiceModule());
         bind(OkHttpClientSupplier.class).toInstance(new OkHttpClientSupplier() {
            @Override
            public OkHttpClient get() {
               return new OkHttpClient.Builder()
                   .connectionSpecs(connectionSpecs)
                   .build();
            }
         });
      }
   }

}
