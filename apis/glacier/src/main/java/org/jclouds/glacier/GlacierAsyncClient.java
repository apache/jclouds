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
import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.blobstore.attr.BlobScope;
import org.jclouds.glacier.binders.BindArchiveSizeToHeaders;
import org.jclouds.glacier.binders.BindContentRangeToHeaders;
import org.jclouds.glacier.binders.BindDescriptionToHeaders;
import org.jclouds.glacier.binders.BindHashesToHeaders;
import org.jclouds.glacier.binders.BindJobRequestToJsonPayload;
import org.jclouds.glacier.binders.BindMultipartTreeHashToHeaders;
import org.jclouds.glacier.binders.BindPartSizeToHeaders;
import org.jclouds.glacier.domain.JobRequest;
import org.jclouds.glacier.domain.MultipartUploadMetadata;
import org.jclouds.glacier.domain.PaginatedMultipartUploadCollection;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.fallbacks.FalseOnIllegalArgumentException;
import org.jclouds.glacier.filters.RequestAuthorizeSignature;
import org.jclouds.glacier.functions.ParseArchiveIdHeader;
import org.jclouds.glacier.functions.ParseJobIdHeader;
import org.jclouds.glacier.functions.ParseMultipartUploadIdHeader;
import org.jclouds.glacier.functions.ParseMultipartUploadListFromHttpContent;
import org.jclouds.glacier.functions.ParseMultipartUploadPartListFromHttpContent;
import org.jclouds.glacier.functions.ParseMultipartUploadTreeHashHeader;
import org.jclouds.glacier.functions.ParseVaultMetadataFromHttpContent;
import org.jclouds.glacier.functions.ParseVaultMetadataListFromHttpContent;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.glacier.predicates.validators.DescriptionValidator;
import org.jclouds.glacier.predicates.validators.PartSizeValidator;
import org.jclouds.glacier.predicates.validators.PayloadValidator;
import org.jclouds.glacier.predicates.validators.VaultNameValidator;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.Payload;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.Headers;
import org.jclouds.rest.annotations.ParamValidators;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;

import com.google.common.hash.HashCode;
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
   ListenableFuture<URI> createVault(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * @see GlacierClient#deleteVaultIfEmpty
    */
   @Named("DeleteVault")
   @DELETE
   @Path("/-/vaults/{vault}")
   @Fallback(FalseOnIllegalArgumentException.class)
   ListenableFuture<Boolean> deleteVault(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * @see GlacierClient#describeVault
    */
   @Named("DescribeVault")
   @GET
   @Path("/-/vaults/{vault}")
   @ResponseParser(ParseVaultMetadataFromHttpContent.class)
   @Fallback(NullOnNotFoundOr404.class)
   ListenableFuture<VaultMetadata> describeVault(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

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

   /**
    * @see GlacierClient#uploadArchive
    */
   @Named("UploadArchive")
   @POST
   @Path("/-/vaults/{vault}/archives")
   @ResponseParser(ParseArchiveIdHeader.class)
   ListenableFuture<String> uploadArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload,
         @ParamValidators(DescriptionValidator.class) @BinderParam(BindDescriptionToHeaders.class) String description);

   /**
    * @see GlacierClient#uploadArchive
    */
   @Named("UploadArchive")
   @POST
   @Path("/-/vaults/{vault}/archives")
   @ResponseParser(ParseArchiveIdHeader.class)
   ListenableFuture<String> uploadArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload);

   /**
    * @see GlacierClient#deleteArchive
    */
   @Named("DeleteArchive")
   @DELETE
   @Path("/-/vaults/{vault}/archives/{archive}")
   ListenableFuture<Boolean> deleteArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("archive") String archiveId);

   /**
    * @see GlacierClient#initiateMultipartUpload
    */
   @Named("InitiateMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadIdHeader.class)
   ListenableFuture<String> initiateMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PartSizeValidator.class) @BinderParam(BindPartSizeToHeaders.class) long partSizeInMB,
         @ParamValidators(DescriptionValidator.class) @BinderParam(BindDescriptionToHeaders.class) String description);

   /**
    * @see GlacierClient#initiateMultipartUpload
    */
   @Named("InitiateMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadIdHeader.class)
   ListenableFuture<String> initiateMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PartSizeValidator.class) @BinderParam(BindPartSizeToHeaders.class) long partSizeInMB);

   /**
    * @see GlacierClient#uploadPart
    */
   @Named("UploadPart")
   @PUT
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadTreeHashHeader.class)
   ListenableFuture<String> uploadPart(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         @BinderParam(BindContentRangeToHeaders.class) ContentRange range,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload);

   /**
    * @see GlacierClient#completeMultipartUpload
    */
   @Named("CompleteMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseArchiveIdHeader.class)
   ListenableFuture<String> completeMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         @BinderParam(BindMultipartTreeHashToHeaders.class) Map<Integer, HashCode> hashes,
         @BinderParam(BindArchiveSizeToHeaders.class) long archiveSizeInMB);

   /**
    * @see GlacierClient#abortMultipartUpload
    */
   @Named("AbortMultipartUpload")
   @DELETE
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   ListenableFuture<Boolean> abortMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId);

   /**
    * @see GlacierClient#listParts
    */
   @Named("ListParts")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadPartListFromHttpContent.class)
   ListenableFuture<MultipartUploadMetadata> listParts(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         PaginationOptions options);

   /**
    * @see GlacierClient#listParts
    */
   @Named("ListParts")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadPartListFromHttpContent.class)
   ListenableFuture<MultipartUploadMetadata> listParts(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId);

   /**
    * @see GlacierClient#listMultipartUploads
    */
   @Named("ListMultipartUploads")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadListFromHttpContent.class)
   ListenableFuture<PaginatedMultipartUploadCollection> listMultipartUploads(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         PaginationOptions options);

   /**
    * @see GlacierClient#listMultipartUploads
    */
   @Named("ListMultipartUploads")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadListFromHttpContent.class)
   ListenableFuture<PaginatedMultipartUploadCollection> listMultipartUploads(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * @see GlacierClient#initiateJob
    */
   @Named("InitiateJob")
   @POST
   @Path("/-/vaults/{vault}/jobs")
   @ResponseParser(ParseJobIdHeader.class)
   ListenableFuture<String> initiateJob(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @BinderParam(BindJobRequestToJsonPayload.class) JobRequest job);
}
