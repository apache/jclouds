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
package org.jclouds.googlecomputeengine.binders;

import java.net.URI;
import java.util.Map;

import jakarta.inject.Inject;

import org.jclouds.googlecomputeengine.options.RouteOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.MapBinder;
import org.jclouds.rest.binders.BindToJsonPayload;

public class RouteBinder implements MapBinder {

   private final BindToJsonPayload jsonBinder;

   @Inject RouteBinder(BindToJsonPayload jsonBinder){
      this.jsonBinder = jsonBinder;
   }

   @Override public <R extends HttpRequest> R bindToRequest(R request, Map<String, Object> postParams) {
      RouteOptions options = (RouteOptions) postParams.get("options");
      options.name(postParams.get("name").toString());
      options.network((URI) postParams.get("network"));
      return bindToRequest(request, options);
   }

   @Override public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      return jsonBinder.bindToRequest(request, input);
   }
}
