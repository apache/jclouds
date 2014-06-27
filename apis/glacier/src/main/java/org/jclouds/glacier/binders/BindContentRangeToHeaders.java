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
package org.jclouds.glacier.binders;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.glacier.util.ContentRange;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

import com.google.common.net.HttpHeaders;

/**
 * Binds the ContentRange to the request headers.
 */
public class BindContentRangeToHeaders implements Binder {
   @SuppressWarnings("unchecked")
   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      checkArgument(input instanceof ContentRange, "This binder is only valid for Payload");
      checkNotNull(request, "request");
      ContentRange range = ContentRange.class.cast(input);
      return (R) request.toBuilder().addHeader(HttpHeaders.CONTENT_RANGE, range.buildHeader()).build();
   }
}
