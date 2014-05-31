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

import org.jclouds.glacier.domain.PaginatedVaultCollection;
import org.jclouds.glacier.domain.VaultMetadata;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.Payload;

/**
 * Provides access to Amazon Glacier resources via their REST API.
 * <p/>
 *
 * @see GlacierAsyncClient
 * @see <a href="http://aws.amazon.com/documentation/glacier/" />
 */
public interface GlacierClient extends Closeable {

   /**
    * Creates a new vault to store archives.
    *
    * @param vaultName
    *           A name for the Vault being created.
    * @return A reference to an URI pointing to the resource created.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-put.html" />
    */
   URI createVault(String vaultName);

   /**
    * Deletes a vault.
    *
    * @param vaultName
    *           Name of the Vault being deleted.
    * @return False if the vault was not empty and therefore not deleted, true otherwise.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-delete.html" />
    */
   boolean deleteVault(String vaultName);

   /**
    * Retrieves the metadata for a vault.
    *
    * @param vaultName
    *           Name of the Vault being described.
    * @return A VaultMetadata object containing all the information relevant to the vault.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-get.html" />
    */
   VaultMetadata describeVault(String vaultName);

   /**
    * Lists vaults according to specified options.
    *
    * @param options
    *          Options used for pagination.
    * @return A PaginatedVaultCollection object containing the list of vaults.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vaults-get.html" />
    */
   PaginatedVaultCollection listVaults(PaginationOptions options);

   /**
    * Lists vaults.
    *
    * @see GlacierClient#listVaults(PaginationOptions)
    */
   PaginatedVaultCollection listVaults();

   /**
    * Stores an archive in a vault.
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
    * Stores an archive in a vault.
    *
    * @see GlacierClient#uploadArchive
    */
   String uploadArchive(String vaultName, Payload payload);

   /**
    * Deletes an archive from a vault.
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
    * Starts a new multipart upload.
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
    * Starts a new multipart upload.
    */
   String initiateMultipartUpload(String vaultName, long partSizeInMB);

   /**
    * Uploads one of the multipart upload parts.
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
   String uploadPart(String vaultName, String uploadId, ContentRange range, Payload payload);
}
