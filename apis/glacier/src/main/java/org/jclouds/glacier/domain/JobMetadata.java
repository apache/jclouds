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
package org.jclouds.glacier.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;
import java.util.Date;

import org.jclouds.glacier.util.ContentRange;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class JobMetadata {
   @SerializedName("Action")
   private final String action;
   @SerializedName("ArchiveId")
   private final String archiveId;
   @SerializedName("ArchiveSizeInBytes")
   private final Long archiveSizeInBytes;
   @SerializedName("ArchiveSHA256TreeHash")
   private final String archiveSHA256TreeHash;
   @SerializedName("Completed")
   private final boolean completed;
   @SerializedName("CompletionDate")
   private final Date completionDate;
   @SerializedName("CreationDate")
   private final Date creationDate;
   @SerializedName("InventorySizeInBytes")
   private final Long inventorySizeInBytes;
   @SerializedName("JobDescription")
   private final String jobDescription;
   @SerializedName("JobId")
   private final String jobId;
   @SerializedName("RetrievalByteRange")
   private final ContentRange retrievalByteRange;
   @SerializedName("SHA256TreeHash")
   private final String sha256TreeHash;
   @SerializedName("SNSTopic")
   private final String snsTopic;
   @SerializedName("StatusCode")
   private final JobStatus statusCode;
   @SerializedName("StatusMessage")
   private final String statusMessage;
   @SerializedName("VaultARN")
   private final String vaultArn;
   @SerializedName("InventoryRetrievalParameters")
   private final InventoryRetrievalParameters parameters;

   @ConstructorProperties({ "Action", "ArchiveId", "ArchiveSizeInBytes", "ArchiveSHA256TreeHash", "Completed",
         "CompletionDate", "CreationDate", "InventorySizeInBytes", "JobDescription", "JobId", "RetrievalByteRange",
         "SHA256TreeHash", "SNSTopic", "StatusCode", "StatusMessage", "VaultARN", "InventoryRetrievalParameters" })
   public JobMetadata(String action, @Nullable String archiveId, @Nullable Long archiveSizeInBytes,
         @Nullable String archiveSHA256TreeHash, boolean completed, @Nullable Date completionDate, Date creationDate,
         @Nullable Long inventorySizeInBytes, @Nullable String jobDescription, String jobId,
         @Nullable String retrievalByteRange, @Nullable String sha256TreeHash, @Nullable String snsTopic,
         String statusCode, @Nullable String statusMessage, String vaultArn,
         @Nullable InventoryRetrievalParameters parameters) {
      super();
      this.action = checkNotNull(action, "action");
      this.archiveId = archiveId;
      this.archiveSizeInBytes = archiveSizeInBytes;
      this.archiveSHA256TreeHash = archiveSHA256TreeHash;
      this.completed = completed;
      this.completionDate = completionDate == null ? null : (Date) completionDate.clone();
      this.creationDate = (Date) checkNotNull(creationDate, "creationDate").clone();
      this.inventorySizeInBytes = inventorySizeInBytes;
      this.jobDescription = jobDescription;
      this.jobId = checkNotNull(jobId, "jobId");
      this.retrievalByteRange = retrievalByteRange == null ? null : ContentRange.fromString(retrievalByteRange);
      this.sha256TreeHash = sha256TreeHash;
      this.snsTopic = snsTopic;
      this.statusCode = JobStatus.fromString(checkNotNull(statusCode, "statusCode"));
      this.statusMessage = statusMessage;
      this.vaultArn = checkNotNull(vaultArn, "vaultArn");
      this.parameters = parameters;
   }

   public String getAction() {
      return action;
   }

   public String getArchiveId() {
      return archiveId;
   }

   public Long getArchiveSizeInBytes() {
      return archiveSizeInBytes;
   }

   public String getArchiveSHA256TreeHash() {
      return archiveSHA256TreeHash;
   }

   public boolean isCompleted() {
      return completed;
   }

   public Date getCompletionDate() {
      return completionDate == null ? null : (Date) completionDate.clone();
   }

   public Date getCreationDate() {
      return (Date) creationDate.clone();
   }

   public Long getInventorySizeInBytes() {
      return inventorySizeInBytes;
   }

   public String getJobDescription() {
      return jobDescription;
   }

   public String getJobId() {
      return jobId;
   }

   public ContentRange getRetrievalByteRange() {
      return retrievalByteRange;
   }

   public String getSha256TreeHash() {
      return sha256TreeHash;
   }

   public String getSnsTopic() {
      return snsTopic;
   }

   public JobStatus getStatusCode() {
      return statusCode;
   }

   public String getStatusMessage() {
      return statusMessage;
   }

   public String getVaultArn() {
      return vaultArn;
   }

   public InventoryRetrievalParameters getParameters() {
      return parameters;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.action, this.archiveId, this.archiveSizeInBytes, this.archiveSHA256TreeHash,
            this.completed, this.completionDate, this.creationDate, this.inventorySizeInBytes, this.jobDescription,
            this.jobId, this.retrievalByteRange, this.sha256TreeHash, this.snsTopic, this.statusCode,
            this.statusMessage, this.vaultArn, this.parameters);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      JobMetadata other = (JobMetadata) obj;

      return Objects.equal(this.jobId, other.jobId);
   }

   @Override
   public String toString() {
      return "JobMetadata [jobId=" + jobId + ", statusCode=" + statusCode + ", statusMessage=" + statusMessage + "]";
   }
}

