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
package org.jclouds.azurecompute.arm.handlers;

import static org.jclouds.azurecompute.arm.handlers.AzureRateLimitRetryHandler.isRateLimitError;

import java.io.IOException;

import jakarta.inject.Singleton;

import org.jclouds.azurecompute.arm.exceptions.AzureComputeRateLimitExceededException;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.ResourceNotFoundException;
import org.jclouds.util.Closeables2;
import org.jclouds.util.Strings2;

/**
 * This will parse and set an appropriate exception on the command object.
 */
@Singleton
public class AzureComputeErrorHandler implements HttpErrorHandler {

   @Override
   public void handleError(final HttpCommand command, final HttpResponse response) {
      // It is important to always read fully and close streams
      // For 429 errors the response body might have already been consumed as
      // some errors report information in the response body that needs to be
      // handled by the retry handlers.
      String message = parseMessage(response);
      Exception exception = message == null
              ? new HttpResponseException(command, response)
              : new HttpResponseException(command, response, message);
      try {
         message = message == null
                 ? String.format("%s -> %s", command.getCurrentRequest().getRequestLine(), response.getStatusLine())
                 : message;
         switch (response.getStatusCode()) {
            case 400:
               if (message.contains("unauthorized_client")) {
                  exception = new AuthorizationException(message, exception);
               }
               else {
                  exception = new IllegalArgumentException(message, exception);
               }
               break;
            case 401:
            case 403:
               exception = new AuthorizationException(message, exception);
               break;

            case 404:
               if (!command.getCurrentRequest().getMethod().equals("DELETE")) {
                  exception = new ResourceNotFoundException(message, exception);
               }
               break;
            case 409:
               exception = new IllegalStateException(message, exception);
               break;
            case 429:
               if (isRateLimitError(response)) {
                  exception = new AzureComputeRateLimitExceededException(response, exception);
               } else {
                  exception = new IllegalStateException(message, exception);
               }
               break;
            default:
         }
      } finally {
         Closeables2.closeQuietly(response.getPayload());
         command.setException(exception);
      }
   }

   public String parseMessage(final HttpResponse response) {
      if (response.getPayload() == null) {
         return null;
      }
      try {
         return Strings2.toStringAndClose(response.getPayload().openStream());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
