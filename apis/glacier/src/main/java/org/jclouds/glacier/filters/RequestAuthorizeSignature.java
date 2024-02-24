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
package org.jclouds.glacier.filters;

import static com.google.common.base.Preconditions.checkNotNull;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.crypto.Crypto;
import org.jclouds.date.TimeStamp;
import org.jclouds.domain.Credentials;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.glacier.util.AWSRequestSignerV4;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequestFilter;
import org.jclouds.http.HttpUtils;
import org.jclouds.logging.Logger;

import com.google.common.base.Supplier;
import com.google.common.net.HttpHeaders;

/**
 * Signs the request using the AWSRequestSignerV4.
 */
@Singleton
public class RequestAuthorizeSignature implements HttpRequestFilter {

   private final AWSRequestSignerV4 signer;

   @Resource
   @Named(Constants.LOGGER_SIGNATURE)
   Logger signatureLog = Logger.NULL;

   private final Provider<String> timeStampProvider;
   private final HttpUtils utils;

   @Inject
   public RequestAuthorizeSignature(@TimeStamp Provider<String> timeStampProvider,
         @org.jclouds.location.Provider Supplier<Credentials> creds, Crypto crypto, HttpUtils utils) {
      checkNotNull(creds, "creds");
      this.signer = new AWSRequestSignerV4(creds.get().identity, creds.get().credential, checkNotNull(crypto, "crypto"));
      this.timeStampProvider = checkNotNull(timeStampProvider, "timeStampProvider");
      this.utils = checkNotNull(utils, "utils");
   }

   @Override
   public HttpRequest filter(HttpRequest request) throws HttpException {
      request = request.toBuilder().removeHeader(HttpHeaders.DATE)
            .replaceHeader(GlacierHeaders.ALTERNATE_DATE, timeStampProvider.get())
            .replaceHeader(HttpHeaders.HOST, request.getEndpoint().getHost()).build();
      utils.logRequest(signatureLog, request, ">>");
      request = this.signer.sign(request);
      utils.logRequest(signatureLog, request, "<<");
      return request;
   }
}
