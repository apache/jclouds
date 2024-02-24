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
package org.jclouds.b2.features;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jclouds.http.filters.BasicAuthentication;
import org.jclouds.b2.domain.Authorization;
import org.jclouds.rest.annotations.RequestFilters;

public interface AuthorizationApi {
   @Named("b2_authorize_account")
   @GET
   @Path("/b2api/v2/b2_authorize_account")
   @RequestFilters(BasicAuthentication.class)
   @Consumes(APPLICATION_JSON)
   Authorization authorizeAccount();
}
