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
package org.jclouds.glacier.config;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Named;

import org.jclouds.Constants;
import org.jclouds.date.DateService;
import org.jclouds.date.TimeStamp;
import org.jclouds.glacier.GlacierClient;
import org.jclouds.glacier.filters.RequestAuthorizeSignature;
import org.jclouds.glacier.handlers.ParseGlacierErrorFromJsonContent;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.annotation.ClientError;
import org.jclouds.http.annotation.Redirection;
import org.jclouds.http.annotation.ServerError;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.config.HttpApiModule;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/**
 * Configures the mappings. Installs the Object and Parser modules.
 */
@ConfiguresHttpApi
public class GlacierHttpApiModule extends HttpApiModule<GlacierClient> {

   @Override
   protected void configure() {
      super.configure();
      bind(RequestAuthorizeSignature.class).in(Scopes.SINGLETON);
   }

   @Provides
   @TimeStamp
   protected String provideTimeStamp(@TimeStamp Supplier<String> cache) {
      return cache.get();
   }

   @Provides
   @TimeStamp
   Supplier<String> provideTimeStampCache(@Named(Constants.PROPERTY_SESSION_INTERVAL) long seconds,
         final DateService dateService) {
      return Suppliers.memoizeWithExpiration(new Supplier<String>() {

         @Override
         public String get() {
            return dateService.iso8601SecondsDateFormat().replaceAll("[-:]", "");
         }
      }, seconds, TimeUnit.SECONDS);
   }

   @Override
   protected void bindErrorHandlers() {
      bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(ParseGlacierErrorFromJsonContent.class);
      bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(ParseGlacierErrorFromJsonContent.class);
      bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(ParseGlacierErrorFromJsonContent.class);
   }
}
