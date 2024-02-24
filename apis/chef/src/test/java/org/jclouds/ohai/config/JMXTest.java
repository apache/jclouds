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
package org.jclouds.ohai.config;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

import java.lang.management.RuntimeMXBean;
import java.util.Map;

import jakarta.inject.Inject;

import org.jclouds.chef.ChefApiMetadata;
import org.jclouds.chef.config.ChefParserModule;
import org.jclouds.domain.JsonBall;
import org.jclouds.json.Json;
import org.jclouds.json.config.GsonModule;
import org.jclouds.ohai.Automatic;
import org.jclouds.rest.annotations.ApiVersion;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests behavior of {@code JMX}
 */
@Test(groups = { "unit" })
public class JMXTest {

   @Test
   public void test() {

      final RuntimeMXBean runtime = createMock(RuntimeMXBean.class);

      expect(runtime.getUptime()).andReturn(69876000L);

      replay(runtime);

      Injector injector = Guice.createInjector(new AbstractModule() {
         @Override
         protected void configure() {
            bind(String.class).annotatedWith(ApiVersion.class).toInstance(ChefApiMetadata.DEFAULT_API_VERSION);
         }
      }, new ChefParserModule(), new GsonModule(), new JMXOhaiModule() {
         @Override
         protected RuntimeMXBean provideRuntimeMXBean() {
            return runtime;
         }
      });
      Json json = injector.getInstance(Json.class);
      Ohai ohai = injector.getInstance(Ohai.class);
      assertEquals(json.toJson(ohai.ohai.get().get("uptime_seconds")), "69876");
   }

   static class Ohai {
      private Supplier<Map<String, JsonBall>> ohai;

      @Inject
      public Ohai(@Automatic Supplier<Map<String, JsonBall>> ohai) {
         this.ohai = ohai;
      }
   }
}
