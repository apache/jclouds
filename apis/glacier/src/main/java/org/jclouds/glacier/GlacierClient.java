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

import java.io.Closeable;
import java.net.URI;
import java.util.Map;

import org.jclouds.glacier.domain.ArchiveMetadataCollection;
import org.jclouds.glacier.domain.JobMetadata;
import org.jclouds.glacier.domain.JobRequest;
import org.jclouds.glacier.domain.MultipartUploadMetadata;
import org.jclouds.glacier.domain.PaginatedJobCollection;
import org.jclouds.glacier.domain.PaginatedMultipartUploadCollection;
import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.Payload;

import com.google.common.hash.HashCode;

/**
 * Provides access to Amazon Glacier resources via their REST API.
 * <p/>
 *
 * @see GlacierAsyncClient
 * @see <a href="http://aws.amazon.com/documentation/glacier/" />
 */
public interface GlacierClient extends Closeable {

   /**
    * Creates a new vault to store archives. An account can only create up to 1,000 vaults per region, but each vault
    * can contain an unlimited number of archives.
    *
    * @param vaultName
    *           A name for the Vault being created.
    * @return A reference to an URI pointing to the resource created.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-put.html" />
    */
   URI createVault(String vaultName);

   /**
    * Deletes a vault. Vaults can only be deleted if there are no archives in the vault in the last inventory computed
    * by Amazon, and there are no new uploads after it. This is very important, as Amazon only updates inventories once
    * every 24 hours.
    *
    * @param vaultName
    *           Name of the Vault being deleted.
    * @return False if the vault was not empty and therefore not deleted, true otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-delete.html" />
    */
   boolean deleteVault(String vaultName);

   /**
    * Retrieves the metadata for a vault. The response include information like the vault ARN, the creation data, the
    * number of archives contained within the vault and total size of these archives. The number of archives and
    * the total size is based on the last inventory computed by amazon and the information may be outdated.
    *
    * @param vaultName
    *           Name of the Vault being described.
    * @return A VaultMetadata object containing all the information relevant to the vault if the vault exists,
    * null otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-get.html" />
    */
   VaultMetadata describeVault(String vaultName);

   /**
    * Lists vaults according to specified options. By default this operation returns up to 1,000 vaults.
    *
    * @param options
    *          Options used for pagination.
    * @return A PaginatedVaultCollection object containing the list of vaults.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vaults-get.html" />
    */
   PaginatedVaultCollection listVaults(PaginationOptions options);

   /**
    * @see GlacierClient#listVaults(PaginationOptions)
    */
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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-archive-post.html" />
    */
   String uploadArchive(String vaultName, Payload payload, String description);

   /**
    * @see GlacierClient#uploadArchive(String, org.jclouds.io.Payload, String)
    */
   String uploadArchive(String vaultName, Payload payload);

   /**
    * Deletes an archive from a vault. Be aware that after deleting an archive it may still be listed in the
    * inventories until amazon compute a new inventory.
    *
    * @param vaultName
    *           Name of the Vault where the archive is stored.
    * @param archiveId
    *           Amazon Glacier archive identifier.
    * @return False if the archive was not deleted, true otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-archive-delete.html" />
    */
   boolean deleteArchive(String vaultName, String archiveId);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-initiate-upload.html" />
    */
   String initiateMultipartUpload(String vaultName, long partSizeInMB, String description);

   /**
    * @see GlacierClient#initiateMultipartUpload(String, long, String)
    */
   String initiateMultipartUpload(String vaultName, long partSizeInMB);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-upload-part.html" />
    */
   HashCode uploadPart(String vaultName, String uploadId, ContentRange range, Payload payload);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-complete-upload.html" />
    */
   String completeMultipartUpload(String vaultName, String uploadId, Map<Integer, HashCode> hashes, long archiveSize);

   /**
    * Aborts the multipart upload. Once aborted, you cannot upload any more parts to it.
    *
    * @param vaultName
    *           Name of the Vault where the archive was going to be stored.
    * @param uploadId
    *           Multipart upload identifier.
    * @return True if the multipart upload was aborted, false otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-abort-upload.html" />
    */
   boolean abortMultipartUpload(String vaultName, String uploadId);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-list-parts.html" />
    */
   MultipartUploadMetadata listParts(String vaultName, String uploadId, PaginationOptions options);

   /**
    * @see GlacierClient#listParts(String, String, org.jclouds.glacier.options.PaginationOptions)
    */
   MultipartUploadMetadata listParts(String vaultName, String uploadId);

   /**
    * Lists the ongoing multipart uploads in a vault. By default, this operation returns up to  1,000 multipart uploads.
    * You can control this using the request options.
    *
    * @param vaultName
    *           Name of the Vault where the archive is going to be stored.
    * @param options
    *          Options used for pagination.
    * @return A PaginatedMultipartUploadCollection, containing an iterable multipart upload list with a marker.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-list-uploads.html" />
    */
   PaginatedMultipartUploadCollection listMultipartUploads(String vaultName, PaginationOptions options);

   /**
    * @see GlacierClient#listMultipartUploads(String, org.jclouds.glacier.options.PaginationOptions)
    */
   PaginatedMultipartUploadCollection listMultipartUploads(String vaultName);

   /**
    * Initiates a job. The job can be an inventory retrieval or an archive retrieval. Once the job is started the
    * estimated time to complete it is ~4 hours.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param job
    *          JobRequest instance with the concrete request.
    * @return The job identifier.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-initiate-job-post.html" />
    */
   String initiateJob(String vaultName, JobRequest job);

   /**
    * Retrieves information about an ongoing job. Among the information you will find the initiation date, the user who
    * initiated the job or the status message.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The job metadata if the job exists in the vault, null otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-describe-job-get.html" />
    */
   JobMetadata describeJob(String vaultName, String jobId);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-jobs-get.html" />
    */
   PaginatedJobCollection listJobs(String vaultName, PaginationOptions options);

   /**
    * Lists jobs.
    */
   PaginatedJobCollection listJobs(String vaultName);

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
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-job-output-get.html" />
    */
   Payload getJobOutput(String vaultName, String jobId, ContentRange range);

   /**
    * Downloads the output of an archive retrieval job.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The content data.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-job-output-get.html" />
    */
   Payload getJobOutput(String vaultName, String jobId);

   /**
    * Gets the job output for the given job ID. The job must be an inventory retrieval, otherwise this operation will
    * fail. This operation will parse the job output and build a collection of ArchiveMetadata.
    *
    * @param vaultName
    *           Name of the target Vault for the job.
    * @param jobId
    *          Job identifier.
    * @return The ArchiveMetadata collection
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-job-output-get.html" />
    */
   ArchiveMetadataCollection getInventoryRetrievalOutput(String vaultName, String jobId);
}
