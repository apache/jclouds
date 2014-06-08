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
package org.jclouds.glacier;

import static org.jclouds.aws.reference.AWSConstants.PROPERTY_HEADER_TAG;
import static org.jclouds.reflect.Reflection2.typeToken;

import java.net.URI;
import java.util.Properties;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.glacier.config.GlacierParserModule;
import org.jclouds.glacier.config.GlacierRestClientModule;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.rest.internal.BaseRestApiMetadata;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.Module;

/**
 * Implementation of ApiMetadata for Amazon Glacier API
 */
public class GlacierApiMetadata extends BaseRestApiMetadata {

   @Deprecated
   public static final TypeToken<org.jclouds.rest.RestContext<GlacierClient, GlacierAsyncClient>> CONTEXT_TOKEN = new TypeToken<org.jclouds.rest.RestContext<GlacierClient, GlacierAsyncClient>>() {

      private static final long serialVersionUID = 1L;
   };

   private static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return builder().fromApiMetadata(this);
   }

   public GlacierApiMetadata() {
      this(builder());
   }

   protected GlacierApiMetadata(Builder builder) {
      super(new Builder());
   }

   public static Properties defaultProperties() {
      Properties properties = BaseRestApiMetadata.defaultProperties();
      properties.setProperty(PROPERTY_HEADER_TAG, GlacierHeaders.DEFAULT_AMAZON_HEADERTAG);
      return properties;
   }

   public static class Builder extends BaseRestApiMetadata.Builder<Builder> {

      @SuppressWarnings("deprecation")
      protected Builder() {
         super(GlacierClient.class, GlacierAsyncClient.class);
         id("glacier")
               .name("Amazon Glacier API")
               .identityName("Access Key ID")
               .credentialName("Secret Access Key")
               .defaultEndpoint("https://glacier.us-east-1.amazonaws.com")
               .documentation(URI.create("http://docs.aws.amazon.com/amazonglacier/latest/dev/amazon-glacier-api.html"))
               .version("2012-06-01")
               .defaultProperties(GlacierApiMetadata.defaultProperties())
               .context(CONTEXT_TOKEN)
               .view(typeToken(BlobStoreContext.class))
               .defaultModules(ImmutableSet.<Class<? extends Module>> of(GlacierRestClientModule.class, GlacierParserModule.class));
      }

      @Override
      public GlacierApiMetadata build() {
         return new GlacierApiMetadata(this);
      }

      @Override
      protected Builder self() {
         return this;
      }
   }
}
