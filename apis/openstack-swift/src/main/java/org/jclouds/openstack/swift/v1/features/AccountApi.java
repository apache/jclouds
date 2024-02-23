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
package org.jclouds.openstack.swift.v1.features;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jclouds.openstack.swift.v1.reference.SwiftHeaders.ACCOUNT_TEMPORARY_URL_KEY;

import java.util.Map;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;

import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.openstack.keystone.auth.filters.AuthenticateRequest;
import org.jclouds.openstack.swift.v1.binders.BindMetadataToHeaders.BindAccountMetadataToHeaders;
import org.jclouds.openstack.swift.v1.binders.BindMetadataToHeaders.BindRemoveAccountMetadataToHeaders;
import org.jclouds.openstack.swift.v1.domain.Account;
import org.jclouds.openstack.swift.v1.functions.ParseAccountFromHeaders;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;

import com.google.common.annotations.Beta;

/**
 * Provides access to the OpenStack Object Storage (Swift) Account API features.
 *
 * <p/>
 * Account metadata prefixed with {@code X-Account-Meta-} will be converted
 * appropriately using a binder/parser.
 * <p/>
 * This API is new to jclouds and hence is in Beta. That means we need people to use it and give us feedback. Based
 * on that feedback, minor changes to the interfaces may happen. This code will replace
 * org.jclouds.openstack.swift.SwiftClient in jclouds 2.0 and it is recommended you adopt it sooner than later.
 *
 *
 * @see {@link Account}
 */
@Beta
@RequestFilters(AuthenticateRequest.class)
@Consumes(APPLICATION_JSON)
public interface AccountApi {

   /**
    * Gets the {@link Account}.
    *
    * @return The {@link Account} object.
    */
   @Named("account:get")
   @HEAD
   @ResponseParser(ParseAccountFromHeaders.class)
   Account get();

   /**
    * Creates or updates the {@link Account} metadata.
    *
    * @param metadata  the metadata to create or update.
    */
   @Named("account:updateMetadata")
   @POST
   void updateMetadata(@BinderParam(BindAccountMetadataToHeaders.class) Map<String, String> metadata);

   /**
    * Replaces the temporary URL key for the {@link Account}.
    *
    * @param temporaryUrlKey  the temporary URL key to update.
    */
   @Named("account:updateTemporaryUrlKey")
   @POST
   void updateTemporaryUrlKey(@HeaderParam(ACCOUNT_TEMPORARY_URL_KEY) String temporaryUrlKey);

   /**
    * Deletes metadata from the {@link Account}.
    *
    * @param metadata  the metadata to delete.
    *
    * @return {@code true} if the metadata was successfully deleted,
    *         {@code false} if not.
    */
   @Named("account:deleteMetadata")
   @POST
   @Fallback(FalseOnNotFoundOr404.class)
   boolean deleteMetadata(@BinderParam(BindRemoveAccountMetadataToHeaders.class) Map<String, String> metadata);

}
