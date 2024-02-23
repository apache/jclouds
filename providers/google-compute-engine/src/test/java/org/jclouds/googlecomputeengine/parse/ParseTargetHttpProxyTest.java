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
package org.jclouds.googlecomputeengine.parse;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.date.internal.SimpleDateFormatDateService;
import org.jclouds.googlecomputeengine.domain.TargetHttpProxy;
import org.jclouds.googlecomputeengine.internal.BaseGoogleComputeEngineParseTest;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class ParseTargetHttpProxyTest extends BaseGoogleComputeEngineParseTest<TargetHttpProxy> {

   @Override
   public String resource() {
      return "/target_http_proxy_get.json";
   }

   @Override @Consumes(MediaType.APPLICATION_JSON)
   public TargetHttpProxy expected() {
      return expected(BASE_URL);
   }

   @Consumes(MediaType.APPLICATION_JSON)
   public TargetHttpProxy expected(String baseUrl) {
      return TargetHttpProxy.create("13050421646334304115", // id
            new SimpleDateFormatDateService().iso8601DateParse("2012-11-25T01:38:48.306"), // creationTimestamp
            URI.create(baseUrl + "/myproject/global/targetHttpProxies/jclouds-test"), // selfLink
            "jclouds-test", // name
            null,
            URI.create(baseUrl + "/myproject/global/urlMaps/jclouds-test")); // urlMap
   }
}
