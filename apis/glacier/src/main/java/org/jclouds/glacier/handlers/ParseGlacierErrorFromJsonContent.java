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
package org.jclouds.glacier.handlers;

import org.jclouds.glacier.GlacierResponseException;
import org.jclouds.glacier.domain.GlacierError;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.Json;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.InsufficientResourcesException;
import org.jclouds.rest.ResourceNotFoundException;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

/**
 * Parses a GlacierError from a Json response content.
 */
public class ParseGlacierErrorFromJsonContent extends ParseJson<GlacierError> implements HttpErrorHandler {

   @Inject
   public ParseGlacierErrorFromJsonContent(Json json) {
      super(json, TypeLiteral.get(GlacierError.class));
   }

   private static Exception refineException(GlacierError error, Exception exception) {
      if ("AccessDeniedException".equals(error.getCode())) {
         return new AuthorizationException(error.getMessage(), exception);
      } else if ("InvalidParameterValueException".equals(error.getCode())) {
         return new IllegalArgumentException(error.getMessage(), exception);
      } else if ("LimitExceededException".equals(error.getCode())) {
         return new InsufficientResourcesException(error.getMessage(), exception);
      } else if ("ResourceNotFoundException".equals(error.getCode())) {
         return new ResourceNotFoundException(error.getMessage(), exception);
      }
      return exception;
   }

   @Override
   public void handleError(HttpCommand command, HttpResponse response) {
      GlacierError error = this.apply(response);
      Exception exception = error.isValid()
            ? refineException(error, new GlacierResponseException(command, response, error))
            : new HttpResponseException(command, response);
      command.setException(exception);
   }

}
