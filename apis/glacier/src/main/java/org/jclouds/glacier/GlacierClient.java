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
import org.jclouds.glacier.binders.BindArchiveOutputRangeToHeaders;
import org.jclouds.glacier.binders.BindArchiveSizeToHeaders;
import org.jclouds.glacier.binders.BindContentRangeToHeaders;
import org.jclouds.glacier.binders.BindDescriptionToHeaders;
import org.jclouds.glacier.binders.BindHashesToHeaders;
import org.jclouds.glacier.binders.BindJobRequestToJsonPayload;
import org.jclouds.glacier.binders.BindMultipartTreeHashToHeaders;
import org.jclouds.glacier.binders.BindPartSizeToHeaders;
import org.jclouds.glacier.domain.ArchiveMetadataCollection;
import org.jclouds.glacier.domain.JobMetadata;
import org.jclouds.glacier.domain.JobRequest;
import org.jclouds.glacier.domain.MultipartUploadMetadata;
import org.jclouds.glacier.domain.PaginatedJobCollection;
import org.jclouds.glacier.domain.PaginatedMultipartUploadCollection;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.fallbacks.FalseOnIllegalArgumentException;
import org.jclouds.glacier.filters.RequestAuthorizeSignature;
import org.jclouds.glacier.functions.GetPayloadFromHttpContent;
import org.jclouds.glacier.functions.ParseArchiveIdHeader;
import org.jclouds.glacier.functions.ParseArchiveMetadataCollectionFromHttpContent;
import org.jclouds.glacier.functions.ParseJobIdHeader;
import org.jclouds.glacier.functions.ParseJobMetadataFromHttpContent;
import org.jclouds.glacier.functions.ParseJobMetadataListFromHttpContent;
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

/** Provides access to Amazon Glacier resources via their REST API. */
@Headers(keys = GlacierHeaders.VERSION, values = "2012-06-01")
@RequestFilters(RequestAuthorizeSignature.class)
@BlobScope(CONTAINER)
public interface GlacierClient extends Closeable {

   /**
    * Creates a new vault to store archives. An account can only create up to 1,000 vaults per region, but each vault
    * can contain an unlimited number of archives.
    *
    * @param vaultName
    *           A name for the Vault being created.
    * @return A reference to an URI pointing to the resource created.
    */
   @Named("CreateVault")
   @PUT
   @Path("/-/vaults/{vault}")
   URI createVault(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * Deletes a vault. Vaults can only be deleted if there are no archives in the vault in the last inventory computed
    * by Amazon, and there are no new uploads after it. This is very important, as Amazon only updates inventories once
    * every 24 hours.
    *
    * @param vaultName
    *           Name of the Vault being deleted.
    * @return False if the vault was not empty and therefore not deleted, true otherwise.
    */
   @Named("DeleteVault")
   @DELETE
   @Path("/-/vaults/{vault}")
   @Fallback(FalseOnIllegalArgumentException.class)
   boolean deleteVault(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * Retrieves the metadata for a vault. The response include information like the vault ARN, the creation data, the
    * number of archives contained within the vault and total size of these archives. The number of archives and
    * the total size is based on the last inventory computed by amazon and the information may be outdated.
    *
    * @param vaultName
    *           Name of the Vault being described.
    * @return A VaultMetadata object containing all the information relevant to the vault if the vault exists,
    * null otherwise.
    */
   @Named("DescribeVault")
   @GET
   @Path("/-/vaults/{vault}")
   @ResponseParser(ParseVaultMetadataFromHttpContent.class)
   @Fallback(NullOnNotFoundOr404.class)
   VaultMetadata describeVault(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * Lists vaults according to specified options. By default this operation returns up to 1,000 vaults.
    *
    * @param options
    *          Options used for pagination.
    * @return A PaginatedVaultCollection object containing the list of vaults.
    */
   @Named("ListVaults")
   @GET
   @Path("/-/vaults")
   @ResponseParser(ParseVaultMetadataListFromHttpContent.class)
   PaginatedVaultCollection listVaults(PaginationOptions options);

   /**
    * @see GlacierClient#listVaults(PaginationOptions)
    */
   @Named("ListVaults")
   @GET
   @Path("/-/vaults")
   @ResponseParser(ParseVaultMetadataListFromHttpContent.class)
   PaginatedVaultCollection listVaults();

   /**
    * Stores an archive in a vault. Archives up to 4GB in size can be uploaded using this operation. Once the archive
    * is uploaded it is immutable, this means that archive or its description cannot be modified. Except for the
    * optional description Glacier does not support additional metadata.
    *
    * @param vaultName
    *           Name of the Vault where the archive is being stored.
    * @param payload
    *           Payload to be uploaded.
    * @param description
    *           Description for the archive.
    * @return A String containing the Archive identifier in Amazon Glacier.
    */
   @Named("UploadArchive")
   @POST
   @Path("/-/vaults/{vault}/archives")
   @ResponseParser(ParseArchiveIdHeader.class)
   String uploadArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload,
         @ParamValidators(DescriptionValidator.class) @BinderParam(BindDescriptionToHeaders.class) String description);

   /**
    * @see GlacierClient#uploadArchive(String, org.jclouds.io.Payload, String)
    */
   @Named("UploadArchive")
   @POST
   @Path("/-/vaults/{vault}/archives")
   @ResponseParser(ParseArchiveIdHeader.class)
   String uploadArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload);

   /**
    * Deletes an archive from a vault. Be aware that after deleting an archive it may still be listed in the
    * inventories until amazon compute a new inventory.
    *
    * @param vaultName
    *           Name of the Vault where the archive is stored.
    * @param archiveId
    *           Amazon Glacier archive identifier.
    * @return False if the archive was not deleted, true otherwise.
    */
   @Named("DeleteArchive")
   @DELETE
   @Path("/-/vaults/{vault}/archives/{archive}")
   boolean deleteArchive(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("archive") String archiveId);

   /**
    * Starts a new multipart upload. Using a multipart upload you can upload archives up to 40,000GB (10,000 parts,
    * 4GB each). The part size must be a megabyte multiplied by a power of 2 and every part must be the same size
    * except the last one, which can have a smaller size. In addition, you don't need to know the archive size to
    * initiate a multipart upload.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param partSizeInMB
    *           Content size for each part.
    * @param description
    *           The archive description.
    * @return The Multipart Upload Id.
    */
   @Named("InitiateMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadIdHeader.class)
   String initiateMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PartSizeValidator.class) @BinderParam(BindPartSizeToHeaders.class) long partSizeInMB,
         @ParamValidators(DescriptionValidator.class) @BinderParam(BindDescriptionToHeaders.class) String description);

   /**
    * @see GlacierClient#initiateMultipartUpload(String, long, String)
    */
   @Named("InitiateMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadIdHeader.class)
   String initiateMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @ParamValidators(PartSizeValidator.class) @BinderParam(BindPartSizeToHeaders.class) long partSizeInMB);


   /**
    * Uploads one of the multipart upload parts. The part size has to match the one specified in the multipart upload
    * request, and the range needs to be aligned. In addition, to ensure that the data is not corrupted, the tree hash
    * and the linear hash are checked by Glacier.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param uploadId
    *           Multipart upload identifier.
    * @param range
    *           The content range that this part is uploading.
    * @param payload
    *           Content for this part.
    * @return Tree-hash of the payload calculated by Amazon. This hash needs to be stored to complete the multipart
    *         upload.
    */
   @Named("UploadPart")
   @PUT
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadTreeHashHeader.class)
   HashCode uploadPart(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         @BinderParam(BindContentRangeToHeaders.class) ContentRange range,
         @ParamValidators(PayloadValidator.class) @BinderParam(BindHashesToHeaders.class) Payload payload);

   /**
    * Completes the multipart upload. After uploading all the parts this operation should be called to inform Glacier.
    * Again, the checksum and the ranges of the whole archive will be computed to verify the data.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param uploadId
    *           Multipart upload identifier.
    * @param hashes
    *           Map containing the pairs partnumber-treehash of each uploaded part.
    * @param archiveSize
    *           Size of the complete archive.
    * @return A String containing the Archive identifier in Amazon Glacier.
    */
   @Named("CompleteMultipartUpload")
   @POST
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseArchiveIdHeader.class)
   String completeMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         @BinderParam(BindMultipartTreeHashToHeaders.class) Map<Integer, HashCode> hashes,
         @BinderParam(BindArchiveSizeToHeaders.class) long archiveSize);


   /**
    * Aborts the multipart upload. Once aborted, you cannot upload any more parts to it.
    *
    * @param vaultName
    *           Name of the Vault where the archive was going to be stored.
    * @param uploadId
    *           Multipart upload identifier.
    * @return True if the multipart upload was aborted, false otherwise.
    */
   @Named("AbortMultipartUpload")
   @DELETE
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   boolean abortMultipartUpload(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId);

   /**
    * Lists the multipart upload parts. You can list the parts of an ongoing multipart upload at any time. By default
    * it returns up to 1,000 uploaded parts, but you can control this using the request options.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param uploadId
    *           Multipart upload identifier.
    * @param options
    *          Options used for pagination.
    * @return A MultipartUploadMetadata, containing an iterable part list with a marker.
    */
   @Named("ListParts")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadPartListFromHttpContent.class)
   MultipartUploadMetadata listParts(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId,
         PaginationOptions options);


   /**
    * @see GlacierClient#listParts(String, String, org.jclouds.glacier.options.PaginationOptions)
    */
   @Named("ListParts")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads/{uploadId}")
   @ResponseParser(ParseMultipartUploadPartListFromHttpContent.class)
   MultipartUploadMetadata listParts(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("uploadId") String uploadId);

   /**
    * Lists the ongoing multipart uploads in a vault. By default, this operation returns up to  1,000 multipart uploads.
    * You can control this using the request options.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param options
    *          Options used for pagination.
    * @return A PaginatedMultipartUploadCollection, containing an iterable multipart upload list with a marker.
    */
   @Named("ListMultipartUploads")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadListFromHttpContent.class)
   PaginatedMultipartUploadCollection listMultipartUploads(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         PaginationOptions options);
   
   /**
    * @see GlacierClient#listMultipartUploads(String, org.jclouds.glacier.options.PaginationOptions)
    */
   @Named("ListMultipartUploads")
   @GET
   @Path("/-/vaults/{vault}/multipart-uploads")
   @ResponseParser(ParseMultipartUploadListFromHttpContent.class)
   PaginatedMultipartUploadCollection listMultipartUploads(
         @ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * Initiates a job. The job can be an inventory retrieval or an archive retrieval. Once the job is started the
    * estimated time to complete it is ~4 hours.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param job
    *          JobRequest instance with the concrete request.
    * @return The job identifier.
    */
   @Named("InitiateJob")
   @POST
   @Path("/-/vaults/{vault}/jobs")
   @ResponseParser(ParseJobIdHeader.class)
   String initiateJob(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @BinderParam(BindJobRequestToJsonPayload.class) JobRequest job);

   /**
    * Retrieves information about an ongoing job. Among the information you will find the initiation date, the user who
    * initiated the job or the status message.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The job metadata if the job exists in the vault, null otherwise.
    */
   @Named("DescribeJob")
   @GET
   @Path("/-/vaults/{vault}/jobs/{job}")
   @ResponseParser(ParseJobMetadataFromHttpContent.class)
   @Fallback(NullOnNotFoundOr404.class)
   JobMetadata describeJob(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("job") String jobId);

   /**
    * Lists the ongoing jobs and the recently finished jobs for a vault. By default this operation returns up to
    * 1,000 jobs in the response, but you can control this using the request options. Note that this operation also
    * returns the recently finished jobs and you can still download their output.
    *
    * @param vaultName
    *           Name of the target Vault.
    * @param options
    *          Options used for pagination
    * @return A PaginatedJobCollection, containing an iterable job list with a marker.
    */
   @Named("ListJobs")
   @GET
   @Path("/-/vaults/{vault}/jobs")
   @ResponseParser(ParseJobMetadataListFromHttpContent.class)
   PaginatedJobCollection listJobs(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         PaginationOptions options);

   /**
    * Lists jobs.
    */
   @Named("ListJobs")
   @GET
   @Path("/-/vaults/{vault}/jobs")
   @ResponseParser(ParseJobMetadataListFromHttpContent.class)
   PaginatedJobCollection listJobs(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName);

   /**
    * Gets the raw job output of any kind of job. You can download the job output within the 24 hour period after
    * Glacier comlpetes a job.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @param range
    *          The range of bytes to retrieve from the output.
    * @return The content data.
    */
   @Named("GetJobOutput")
   @GET
   @Path("/-/vaults/{vault}/jobs/{job}/output")
   @ResponseParser(GetPayloadFromHttpContent.class)
   Payload getJobOutput(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("job") String jobId,
         @BinderParam(BindArchiveOutputRangeToHeaders.class) ContentRange range);

   /**
    * Downloads the output of an archive retrieval job.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The content data.
    */
   @Named("GetJobOutput")
   @GET
   @Path("/-/vaults/{vault}/jobs/{job}/output")
   @ResponseParser(GetPayloadFromHttpContent.class)
   Payload getJobOutput(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("job") String jobId);

   /**
    * Gets the job output for the given job ID. The job must be an inventory retrieval, otherwise this operation will
    * fail. This operation will parse the job output and build a collection of ArchiveMetadata.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The ArchiveMetadata collection
    */
   @Named("GetInventoryRetrievalOutput")
   @GET
   @Path("/-/vaults/{vault}/jobs/{job}/output")
   @ResponseParser(ParseArchiveMetadataCollectionFromHttpContent.class)
   ArchiveMetadataCollection getInventoryRetrievalOutput(@ParamValidators(VaultNameValidator.class) @PathParam("vault") String vaultName,
         @PathParam("job") String jobId);
}
