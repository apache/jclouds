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
package org.jclouds.azurecompute.arm.functions;
import java.net.URI;

import jakarta.inject.Singleton;

import org.jclouds.http.HttpResponse;

import com.google.common.base.Function;
/**
 * Parses job status from http response
 */
@Singleton
public class URIParser implements Function<HttpResponse, URI> {
   public URI apply(final HttpResponse from) {
      String locationUri;
      if (from.getStatusCode() == 202 && (locationUri = from.getFirstHeaderOrNull("Location")) != null){
         return URI.create(locationUri);

      } else if (from.getStatusCode() == 200 || from.getStatusCode() == 204){
         return null;
      }
      throw new IllegalStateException("did not receive expected response code and header in: " + from);
   }
}
