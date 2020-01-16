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
package org.jclouds.http.apachehc;

import static com.google.common.io.BaseEncoding.base64;
import static org.jclouds.Constants.PROPERTY_IDEMPOTENT_METHODS;
import static org.jclouds.Constants.PROPERTY_USER_AGENT;
import static org.jclouds.http.HttpUtils.filterOutContentHeaders;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpUtils;
import org.jclouds.http.IOExceptionRetryHandler;
import org.jclouds.http.handlers.DelegatingErrorHandler;
import org.jclouds.http.handlers.DelegatingRetryHandler;
import org.jclouds.http.internal.BaseHttpCommandExecutorService;
import org.jclouds.http.internal.HttpWire;
import org.jclouds.io.ContentMetadataCodec;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Simple implementation of a {@link HttpCommandExecutorService}, Apache Components HttpClient 4.x.
 */
public class ApacheHCHttpCommandExecutorService extends BaseHttpCommandExecutorService<HttpUriRequest> {
   private final HttpClient client;
   private final ApacheHCUtils apacheHCUtils;
   private final String userAgent;

   @Inject
   ApacheHCHttpCommandExecutorService(HttpUtils utils, ContentMetadataCodec contentMetadataCodec,
         DelegatingRetryHandler retryHandler, IOExceptionRetryHandler ioRetryHandler,
         DelegatingErrorHandler errorHandler, HttpWire wire, HttpClient client,
         @Named(PROPERTY_IDEMPOTENT_METHODS) String idempotentMethods,
         @Named(PROPERTY_USER_AGENT) String userAgent) {
      super(utils, contentMetadataCodec, retryHandler, ioRetryHandler, errorHandler, wire, idempotentMethods);
      this.client = client;
      this.apacheHCUtils = new ApacheHCUtils(contentMetadataCodec);
      this.userAgent = userAgent;
   }

   @Override
   protected HttpUriRequest convert(HttpRequest request) throws IOException {
      HttpUriRequest returnVal = apacheHCUtils.convertToApacheRequest(request);
      if (request.getPayload() != null && request.getPayload().getContentMetadata().getContentMD5() != null) {
         String md5 = base64().encode(request.getPayload().getContentMetadata().getContentMD5AsHashCode().asBytes());
         returnVal.addHeader("Content-MD5", md5);
      }

      if (!returnVal.containsHeader(HttpHeaders.USER_AGENT)) {
         returnVal.addHeader(HttpHeaders.USER_AGENT, userAgent);
      }

      return returnVal;
   }

   @Override
   protected HttpResponse invoke(HttpUriRequest nativeRequest) throws IOException {
      org.apache.http.HttpResponse apacheResponse = executeRequest(nativeRequest);

      Payload payload = null;
      if (apacheResponse.getEntity() != null) {
         try {
            payload = Payloads.newInputStreamPayload(apacheResponse.getEntity().getContent());
            if (apacheResponse.getEntity().getContentLength() >= 0)
               payload.getContentMetadata().setContentLength(apacheResponse.getEntity().getContentLength());
            if (apacheResponse.getEntity().getContentType() != null)
               payload.getContentMetadata().setContentType(apacheResponse.getEntity().getContentType().getValue());
         } catch (IOException e) {
            logger.warn(e, "couldn't receive payload for request: %s", nativeRequest.getRequestLine());
            throw e;
         }
      } else {
         // still create a payload object on no entity responses (ex: to HEAD requests) as this is apparently where JClouds is
         // exclusively looking for the content metadata (in order to fill in the BlobMetadata with correct size) 
         payload = Payloads.newStringPayload("");
      }
      
      Multimap<String, String> headers = LinkedHashMultimap.create();
      for (Header header : apacheResponse.getAllHeaders()) {
         headers.put(header.getName(), header.getValue());
      }
      
      contentMetadataCodec.fromHeaders(payload.getContentMetadata(), headers);
      headers = filterOutContentHeaders(headers);
      
      return HttpResponse.builder().statusCode(apacheResponse.getStatusLine().getStatusCode())
                                   .message(apacheResponse.getStatusLine().getReasonPhrase())
                                   .payload(payload)
                                   .headers(headers).build();
   }

   private org.apache.http.HttpResponse executeRequest(HttpUriRequest nativeRequest) throws IOException,
         ClientProtocolException {
      URI endpoint = URI.create(nativeRequest.getRequestLine().getUri());
      HttpHost host = new HttpHost(endpoint.getHost(), endpoint.getPort(), endpoint.getScheme());
      org.apache.http.HttpResponse nativeResponse = client.execute(host, nativeRequest);
      return nativeResponse;
   }

   @Override
   protected void cleanup(HttpUriRequest nativeResponse) {
      // No cleanup necessary
   }
}
