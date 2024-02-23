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
package org.jclouds.rackspace.cloudidentity.v2_0;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.openstack.keystone.auth.AuthenticationApi;
import org.jclouds.openstack.keystone.auth.domain.TenantOrDomainAndCredentials;
import org.jclouds.openstack.keystone.v2_0.binders.BindAuthToJsonPayload;
import org.jclouds.openstack.keystone.v2_0.domain.Access;
import org.jclouds.rackspace.cloudidentity.v2_0.domain.ApiKeyCredentials;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.SelectJson;

/**
 * Provides access to the Rackspace Cloud Identity API.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Path("/tokens")
public interface CloudIdentityAuthenticationApi extends AuthenticationApi {

   /**
    * Authenticate to generate a token.
    *
    * @return access with token
    */
   @Named("authenticate")
   @POST
   @SelectJson("access")
   @MapBinder(BindAuthToJsonPayload.class)
   Access authenticateApiKey(TenantOrDomainAndCredentials<ApiKeyCredentials> credentials);

}
