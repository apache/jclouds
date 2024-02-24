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
package org.jclouds.chef.binders;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.BaseEncoding.base16;
import static com.google.common.primitives.Bytes.toArray;

import java.util.List;
import java.util.Set;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.http.HttpRequest;
import org.jclouds.rest.binders.BindToStringPayload;

@Singleton
public class BindChecksumsToJsonPayload extends BindToStringPayload {

   @SuppressWarnings("unchecked")
   public HttpRequest bindToRequest(HttpRequest request, Object input) {
      checkArgument(checkNotNull(input, "input") instanceof Set, "this binder is only valid for Set!");

      Set<List<Byte>> md5s = (Set<List<Byte>>) input;

      StringBuilder builder = new StringBuilder();
      builder.append("{\"checksums\":{");

      for (List<Byte> md5 : md5s)
         builder.append(String.format("\"%s\":null,", base16().lowerCase().encode(toArray(md5))));
      builder.deleteCharAt(builder.length() - 1);
      builder.append("}}");
      super.bindToRequest(request, builder.toString());
      request.getPayload().getContentMetadata().setContentType(MediaType.APPLICATION_JSON);
      return request;
   }

}
