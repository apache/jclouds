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
package org.jclouds.chef.handlers;

import static org.jclouds.http.HttpUtils.closeClientButKeepContentStream;

import jakarta.annotation.Resource;
import javax.inject.Named;

import org.jclouds.Constants;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpRetryHandler;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.logging.Logger;

import com.google.inject.Inject;

/**
 * Allow for eventual consistency on sandbox requests.
 */
public class ChefApiErrorRetryHandler implements HttpRetryHandler {

   @Inject(optional = true)
   @Named(Constants.PROPERTY_MAX_RETRIES)
   private int retryCountLimit = 5;

   @Resource
   protected Logger logger = Logger.NULL;

   private final BackoffLimitedRetryHandler backoffLimitedRetryHandler;

   @Inject
   ChefApiErrorRetryHandler(BackoffLimitedRetryHandler backoffLimitedRetryHandler) {
      this.backoffLimitedRetryHandler = backoffLimitedRetryHandler;
   }

   public boolean shouldRetryRequest(HttpCommand command, HttpResponse response) {
      if (command.getFailureCount() > retryCountLimit)
         return false;
      if (response.getStatusCode() == 400 && command.getCurrentRequest().getMethod().equals("PUT")
            && command.getCurrentRequest().getEndpoint().getPath().indexOf("sandboxes") != -1) {
         if (response.getPayload() != null) {
            String error = new String(closeClientButKeepContentStream(response));
            if (error != null && error.indexOf("was not uploaded") != -1) {
               return backoffLimitedRetryHandler.shouldRetryRequest(command, response);
            }
         }
      }
      return false;
   }

}
