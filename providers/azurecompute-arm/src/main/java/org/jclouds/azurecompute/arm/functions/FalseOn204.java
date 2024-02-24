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
import com.google.common.base.Function;
import org.jclouds.http.HttpResponse;

import jakarta.inject.Singleton;

import static org.jclouds.http.HttpUtils.releasePayload;
/**
 * Parses an http response code from http responser
 */
@Singleton
public class FalseOn204 implements Function<HttpResponse, Boolean> {
   public Boolean apply(final HttpResponse from) {
      releasePayload(from);
      final int statusCode = from.getStatusCode();
      if (statusCode == 200 || statusCode == 202) {
         return true;
      }
      if (statusCode == 204) {
         return false;
      }
      throw new IllegalStateException("not expected response from: " + from);
   }
}
