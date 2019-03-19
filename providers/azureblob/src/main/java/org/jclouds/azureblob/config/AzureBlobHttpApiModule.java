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
package org.jclouds.azureblob.config;

import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;

import static com.google.common.base.Predicates.in;

import static com.google.common.collect.Iterables.all;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;

import javax.inject.Named;

import org.jclouds.azure.storage.handlers.AzureStorageClientErrorRetryHandler;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.handlers.ParseAzureBlobErrorFromXmlContent;
import org.jclouds.date.DateService;
import org.jclouds.date.TimeStamp;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpRetryHandler;
import org.jclouds.http.annotation.ClientError;
import org.jclouds.http.annotation.Redirection;
import org.jclouds.http.annotation.ServerError;
import org.jclouds.json.config.GsonModule.DateAdapter;
import org.jclouds.json.config.GsonModule.Iso8601DateAdapter;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.config.HttpApiModule;
import org.jclouds.domain.Credentials;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Provides;

/**
 * Configures the Azure Blob Service connection, including logging and http transport.
 */
@ConfiguresHttpApi
public class AzureBlobHttpApiModule extends HttpApiModule<AzureBlobClient> {

   @Override
   protected void configure() {
      install(new AzureBlobModule());
      bind(DateAdapter.class).to(Iso8601DateAdapter.class);
      super.configure();
   }

   @Provides
   @TimeStamp
   protected final String guiceProvideTimeStamp(@TimeStamp Supplier<String> cache) {
      return provideTimeStamp(cache);
   }

   protected String provideTimeStamp(@TimeStamp Supplier<String> cache) {
      return cache.get();
   }
   
   /** 
    * checks which Authentication type is used 
    */
   @Named("sasAuth")
   @Provides 
   protected boolean authSAS(@org.jclouds.location.Provider Supplier<Credentials> creds) {
      String credential = creds.get().credential;
      String formattedCredential = credential.startsWith("?") ? credential.substring(1) : credential;
      List<String> required = ImmutableList.of("sv", "sig"); 
      try {
         Map<String, String> tokens = Splitter.on('&').withKeyValueSeparator('=').split(formattedCredential);
         return all(required, in(tokens.keySet()));
      } catch (Exception ex) {
         return false;
      }
   }

   /**
    * borrowing concurrency code to ensure that caching takes place properly
    */
   @Provides
   @TimeStamp
   protected Supplier<String> provideTimeStampCache(@Named(PROPERTY_SESSION_INTERVAL) long seconds,
         final DateService dateService) {
      return Suppliers.memoizeWithExpiration(new Supplier<String>() {
         @Override
         public String get() {
            return dateService.rfc822DateFormat();
         }
      }, seconds, TimeUnit.SECONDS);
   }

   @Override
   protected void bindRetryHandlers() {
      bind(HttpRetryHandler.class).annotatedWith(ClientError.class).to(AzureStorageClientErrorRetryHandler.class);
   }

   @Override
   protected void bindErrorHandlers() {
      bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(ParseAzureBlobErrorFromXmlContent.class);
      bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(ParseAzureBlobErrorFromXmlContent.class);
      bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(ParseAzureBlobErrorFromXmlContent.class);
   }
}
