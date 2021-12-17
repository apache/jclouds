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

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.glacier.domain.GlacierError;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;

/**
 * Encapsulates a GlacierError.
 */
public class GlacierResponseException extends HttpResponseException {

   private static final long serialVersionUID = 1L;
   private final GlacierError error;

   public GlacierResponseException(HttpCommand command, HttpResponse response, GlacierError error) {
      super("request " + command.getCurrentRequest().getRequestLine() + " failed with code " + response.getStatusCode()
            + ", error: " + error.toString(), command, response);
      this.error = checkNotNull(error, "error");
   }

   public GlacierError getError() {
      return error;
   }

}
