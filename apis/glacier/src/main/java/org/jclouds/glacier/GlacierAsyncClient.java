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

import static org.jclouds.blobstore.attr.BlobScopes.CONTAINER;

import java.io.Closeable;
import java.net.URI;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.blobstore.attr.BlobScope;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.fallbacks.FalseIfVaultNotEmpty;
import org.jclouds.glacier.filters.RequestAuthorizeSignature;
import org.jclouds.glacier.functions.ParseVaultMetadataFromHttpContent;
import org.jclouds.glacier.functions.ParseVaultMetadataListFromHttpContent;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.glacier.predicates.validators.VaultNameValidator;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.Headers;
import org.jclouds.rest.annotations.ParamValidators;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Provides asynchronous access to Amazon Glacier resources via their REST API.
 * <p/>
 *
 * @see GlacierClient
 * @see <a href="http://aws.amazon.com/documentation/glacier/" />
 */
@Headers(keys = GlacierHeaders.VERSION, values = "2012-06-01")
@RequestFilters(RequestAuthorizeSignature.class)
@BlobScope(CONTAINER)
public interface GlacierAsyncClient extends Closeable {

   /**
    * @see GlacierClient#createVault
    */
   @Named("CreateVault")
   @PUT
   @Path("/-/vaults/{vault}")
   ListenableFuture<URI> createVault(@PathParam("vault") @ParamValidators(VaultNameValidator.class) String vaultName);

   /**
    * @see GlacierClient#deleteVaultIfEmpty
    */
   @Named("DeleteVault")
   @DELETE
   @Path("/-/vaults/{vault}")
   @Fallback(FalseIfVaultNotEmpty.class)
   ListenableFuture<Boolean> deleteVault(@PathParam("vault") @ParamValidators(VaultNameValidator.class) String vaultName);

   /**
    * @see GlacierClient#describeVault
    */
   @Named("DescribeVault")
   @GET
   @Path("/-/vaults/{vault}")
   @ResponseParser(ParseVaultMetadataFromHttpContent.class)
   @Fallback(NullOnNotFoundOr404.class)
   ListenableFuture<VaultMetadata> describeVault(
         @PathParam("vault") @ParamValidators(VaultNameValidator.class) String vaultName);

   /**
    * @see GlacierClient#listVaults(PaginationOptions)
    */
   @Named("ListVaults")
   @GET
   @Path("/-/vaults")
   @ResponseParser(ParseVaultMetadataListFromHttpContent.class)
   ListenableFuture<PaginatedVaultCollection> listVaults(PaginationOptions options);

   /**
    * @see GlacierClient#listVaults
    */
   @Named("ListVaults")
   @GET
   @Path("/-/vaults")
   @ResponseParser(ParseVaultMetadataListFromHttpContent.class)
   ListenableFuture<PaginatedVaultCollection> listVaults();
}
