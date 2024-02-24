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
package org.jclouds.rest.annotationparsing;

import static org.testng.Assert.assertEquals;

import java.io.Closeable;
import java.util.NoSuchElementException;
import java.util.Set;

import jakarta.inject.Named;

import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.providers.AnonymousProviderMetadata;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.config.HttpApiModule;
import org.jclouds.rest.internal.BaseRestApiExpectTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Tests that we can add {@link Provides} methods on interfaces
 */
@Test(groups = "unit", testName = "ProvidesAnnotationExpectTest")
public class ProvidesAnnotationExpectTest extends BaseRestApiExpectTest<ProvidesAnnotationExpectTest.ProvidingApi> {

   interface ProvidingApi extends Closeable {
      @Provides
      Set<String> set();

      @Named("bar")
      @Provides
      Set<String> foo();

      @Named("exception")
      @Provides
      Set<String> exception();

      @Named("NoSuchElementException")
      @Provides
      Set<String> noSuchElementException();
   }

   @Test
   public void testProvidesWithGeneric() {
      ProvidingApi client = requestsSendResponses(ImmutableMap.<HttpRequest, HttpResponse> of());
      assertEquals(client.set(), ImmutableSet.of("foo"));
   }

   @Test
   public void testProvidesWithGenericQualified() {
      ProvidingApi client = requestsSendResponses(ImmutableMap.<HttpRequest, HttpResponse> of());
      assertEquals(client.foo(), ImmutableSet.of("bar"));
   }

   @Test(expectedExceptions = AuthorizationException.class)
   public void testProvidesWithGenericQualifiedAuthorizationException() {
      ProvidingApi client = requestsSendResponses(ImmutableMap.<HttpRequest, HttpResponse> of());
      client.exception();
   }

   @Test(expectedExceptions = NoSuchElementException.class)
   public void testProvidesWithGenericQualifiedNoSuchElementException() {
      ProvidingApi client = requestsSendResponses(ImmutableMap.<HttpRequest, HttpResponse> of());
      client.noSuchElementException();
   }

   // crufty junk until we inspect delegating api classes for all their client
   // mappings and make a test helper for random classes.

   @Override
   public ProviderMetadata createProviderMetadata() {
      return AnonymousProviderMetadata.forApiOnEndpoint(ProvidingApi.class, "http://mock");
   }

   @Override
   protected Module createModule() {
      return new ProvidingHttpApiModule();
   }

   @ConfiguresHttpApi
   static class ProvidingHttpApiModule extends HttpApiModule<ProvidingApi> {

      @Override
      protected void configure() {
         super.configure();
         bind(new TypeLiteral<Set<String>>() {
         }).toInstance(ImmutableSet.of("foo"));
         bind(new TypeLiteral<Set<String>>() {
         }).annotatedWith(Names.named("bar")).toInstance(ImmutableSet.of("bar"));
      }

      @Provides
      @Named("exception")
      Set<String> exception() {
         throw new AuthorizationException();
      }

      @Provides
      @Named("NoSuchElementException")
      Set<String> noSuchElementException() {
         throw new NoSuchElementException();
      }
   }
}
