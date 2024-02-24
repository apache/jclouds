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
package org.jclouds.aws.s3.filters;

import static org.jclouds.aws.reference.AWSConstants.PROPERTY_AUTH_TAG;
import static org.jclouds.aws.reference.AWSConstants.PROPERTY_HEADER_TAG;
import static org.jclouds.http.utils.Queries.queryParser;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_SERVICE_PATH;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_VIRTUAL_HOST_BUCKETS;
import static org.jclouds.s3.reference.S3Constants.TEMPORARY_SIGNATURE_PARAM;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.jclouds.crypto.Crypto;
import org.jclouds.date.DateService;
import org.jclouds.date.TimeStamp;
import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpUtils;
import org.jclouds.http.internal.SignatureWire;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV2;

import com.google.common.base.Supplier;

/** Signs the AWS S3 request, supporting temporary signatures. */
@Singleton
public class AWSRequestAuthorizeSignature extends RequestAuthorizeSignatureV2 {

   @Inject
   public AWSRequestAuthorizeSignature(SignatureWire signatureWire, @Named(PROPERTY_AUTH_TAG) String authTag,
            @Named(PROPERTY_S3_VIRTUAL_HOST_BUCKETS) boolean isVhostStyle,
            @Named(PROPERTY_S3_SERVICE_PATH) String servicePath, @Named(PROPERTY_HEADER_TAG) String headerTag,
            @org.jclouds.location.Provider Supplier<Credentials> creds,
            @TimeStamp Provider<String> timeStampProvider, Crypto crypto, HttpUtils utils,
            DateService dateService) {
      super(signatureWire, authTag, isVhostStyle, servicePath, headerTag, creds, timeStampProvider, crypto, 
             utils, dateService);
   }

   @Override
   protected HttpRequest replaceAuthorizationHeader(HttpRequest request, String signature) {
      /* 
       * Only add the Authorization header if the query string doesn't already contain
       * the 'Signature' parameter, otherwise S3 will fail the request complaining about
       * duplicate authentication methods. The 'Signature' parameter will be added for signed URLs
       * with expiration.
       */
      if (queryParser().apply(request.getEndpoint().getQuery()).containsKey(TEMPORARY_SIGNATURE_PARAM)) {
         return request;
      }
      return super.replaceAuthorizationHeader(request, signature);
   }
}
