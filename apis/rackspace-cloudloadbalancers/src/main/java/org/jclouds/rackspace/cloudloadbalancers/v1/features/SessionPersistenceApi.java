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
package org.jclouds.rackspace.cloudloadbalancers.v1.features;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.SessionPersistence;
import org.jclouds.rackspace.cloudloadbalancers.v1.functions.ParseSessionPersistence;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.Payload;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;


/**
 * Session persistence is a feature of the load balancing service that forces multiple requests from clients to be 
 * directed to the same node. This is common with many web applications that do not inherently share application 
 * state between back-end servers. Two session persistence modes are available, HTTP Cookie and Source IP.
 */
@RequestFilters(AuthenticateRequest.class)
public interface SessionPersistenceApi {
   /**
    * Get the current session persistence.
    * 
    * @see SessionPersistence
    */
   @Named("sessionpersistence:get")
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @ResponseParser(ParseSessionPersistence.class)
   @Fallback(NullOnNotFoundOr404.class)
   @Path("/sessionpersistence")
   SessionPersistence get();
   
   /**
    * Create session persistence.
    * 
    * @see SessionPersistence
    */
   @Named("sessionpersistence:create")
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(VoidOnNotFoundOr404.class)
   @Payload("%7B\"sessionPersistence\":%7B\"persistenceType\":\"{sessionPersistence}\"%7D%7D")
   @Path("/sessionpersistence")
   void create(@PayloadParam("sessionPersistence") SessionPersistence sessionPersistence);
   
   /**
    * Delete session persistence.
    * 
    * @see SessionPersistence
    */
   @Named("sessionpersistence:delete")
   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Fallback(VoidOnNotFoundOr404.class)
   @Path("/sessionpersistence")
   void delete();
}
