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
package org.jclouds.openstack.keystone.auth.config;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.jclouds.openstack.keystone.config.KeystoneProperties.CREDENTIAL_TYPE;
import static org.jclouds.openstack.keystone.config.KeystoneProperties.KEYSTONE_VERSION;
import static org.jclouds.openstack.keystone.config.KeystoneProperties.SERVICE_TYPE;
import static org.jclouds.util.Suppliers2.getLastValueInMap;
import static org.testng.Assert.assertTrue;

import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.Properties;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.json.config.GsonModule.DateAdapter;
import org.jclouds.json.config.GsonModule.Iso8601DateAdapter;
import org.jclouds.location.suppliers.RegionIdToURISupplier;
import org.jclouds.openstack.keystone.catalog.config.ServiceCatalogModule;
import org.jclouds.openstack.keystone.catalog.config.ServiceCatalogModule.ProviderModule;
import org.jclouds.openstack.keystone.catalog.config.ServiceCatalogModule.RegionModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.internal.BaseKeystoneRestApiExpectTest;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.annotations.ApiVersion;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.config.HttpApiModule;
import org.jclouds.rest.internal.BaseHttpApiMetadata;
import org.jclouds.rest.internal.BaseRestApiExpectTest;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Tests configuration via {@link ProviderModule}
 */
@Test(groups = "unit", testName = "ProviderModuleExpectTest")
public class ProviderModuleExpectTest extends BaseRestApiExpectTest<ProviderModuleExpectTest.DNSApi> {

   @Retention(RUNTIME)
   @Target(METHOD)
   @Qualifier
   static @interface DNS {
   }

   @ConfiguresHttpApi
   public static class DNSHttpApiModule extends HttpApiModule<DNSApi> {

      @Override
      public void configure() {
         bind(DateAdapter.class).to(Iso8601DateAdapter.class);
         super.configure();
      }

      @Provides
      @Singleton
      @DNS
      protected final Supplier<URI> provideCDNUrl(RegionIdToURISupplier.Factory factory, @ApiVersion String apiVersion) {
         return getLastValueInMap(factory.createForApiTypeAndVersion("dns", apiVersion));
      }
   }

   @org.jclouds.rest.annotations.Endpoint(DNS.class)
   interface DNSApi extends Closeable {
      @HEAD
      @Path("/zones/{zoneName}")
      @Fallback(FalseOnNotFoundOr404.class)
      boolean zoneExists(@PathParam("zoneName") String zoneName);
   }

   public void testDNSEndpointApplied() {
      KeystoneAuth keystoneAuth = new KeystoneAuth();

      DNSApi api = requestsSendResponses(
            keystoneAuth.getKeystoneAuthWithUsernameAndPassword(),
            keystoneAuth.getResponseWithKeystoneAccess(),
            HttpRequest.builder().method("HEAD").endpoint("http://172.16.0.1:8776/v1/3456/zones/foo.com").build(),
            HttpResponse.builder().statusCode(200).build());
      assertTrue(api.zoneExists("foo.com"));
   }

   private static class DNSApiMetadata extends BaseHttpApiMetadata<DNSApi> {

      @Override
      public Builder toBuilder() {
         return new Builder().fromApiMetadata(this);
      }

      public DNSApiMetadata() {
         this(new Builder());
      }

      protected DNSApiMetadata(Builder builder) {
         super(builder);
      }

      public static Properties defaultProperties() {
         Properties properties = BaseHttpApiMetadata.defaultProperties();
         properties.setProperty(SERVICE_TYPE, "dns");
         properties.setProperty(KEYSTONE_VERSION, "2");
         properties.setProperty(CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
         return properties;
      }

      public static class Builder extends BaseHttpApiMetadata.Builder<DNSApi, Builder> {

         protected Builder() {
            id("dns")
            .name("DNS API")
            .identityName("${tenantName}:${userName} or ${userName}, if your keystone supports a default tenant")
            .credentialName("${password}")
            .endpointName("Keystone base url ending in /v2.0/")
            .documentation(URI.create("http://dns"))
            .version("1.0")
            .defaultEndpoint("http://localhost:5000/v2.0/")
            .defaultProperties(DNSApiMetadata.defaultProperties())
            .defaultModules(ImmutableSet.<Class<? extends Module>>builder()
                                        .add(AuthenticationModule.class)
                                        .add(ServiceCatalogModule.class)
                                        .add(RegionModule.class)
                                        .add(DNSHttpApiModule.class).build());
         }

         @Override
         public DNSApiMetadata build() {
            return new DNSApiMetadata(this);
         }

         @Override
         protected Builder self() {
            return this;
         }
      }
   }

   @Override
   public ApiMetadata createApiMetadata() {
      return new DNSApiMetadata();
   }
   
   public static class KeystoneAuth extends BaseKeystoneRestApiExpectTest<KeystoneApi> {
      public HttpRequest getKeystoneAuthWithUsernameAndPassword() {
         return keystoneAuthWithUsernameAndPassword;
      }
      
      public HttpResponse getResponseWithKeystoneAccess() {
         return responseWithKeystoneAccess;
      }
   }
}
