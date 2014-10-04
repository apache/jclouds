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
import org.jclouds.glacier.blobstore.config.GlacierBlobStoreContextModule;
import org.jclouds.glacier.config.GlacierHttpApiModule;
import org.jclouds.glacier.config.GlacierParserModule;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.rest.internal.BaseHttpApiMetadata;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of ApiMetadata for Amazon Glacier API
 */
public class GlacierApiMetadata extends BaseHttpApiMetadata {

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
      Properties properties = BaseHttpApiMetadata.defaultProperties();
      properties.setProperty(PROPERTY_HEADER_TAG, GlacierHeaders.DEFAULT_AMAZON_HEADERTAG);
      return properties;
   }

   public static class Builder extends BaseHttpApiMetadata.Builder<GlacierClient, Builder> {

      protected Builder() {
         super(GlacierClient.class);
         id("glacier")
               .name("Amazon Glacier API")
               .identityName("Access Key ID")
               .credentialName("Secret Access Key")
               .defaultEndpoint("https://glacier.us-east-1.amazonaws.com")
               .documentation(URI.create("http://docs.aws.amazon.com/amazonglacier/latest/dev/amazon-glacier-api.html"))
               .version("2012-06-01")
               .defaultProperties(GlacierApiMetadata.defaultProperties())
               .view(typeToken(BlobStoreContext.class))
               .defaultModules(ImmutableSet.<Class<? extends Module>> of(GlacierHttpApiModule.class,
                     GlacierParserModule.class, GlacierBlobStoreContextModule.class));
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
