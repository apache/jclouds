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
import java.util.Iterator;

import org.jclouds.collect.IterableWithMarker;
import org.jclouds.glacier.options.PaginationOptions;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.gson.annotations.SerializedName;

/**
 * Defines the attributes needed for Multipart uploads. Extends IterableWithMarker to support requesting paginated
 * multipart upload parts.
 */
public class MultipartUploadMetadata extends IterableWithMarker<PartMetadata> implements Comparable<MultipartUploadMetadata> {

   @SerializedName("ArchiveDescription")
   private final String archiveDescription;
   @SerializedName("CreationDate")
   private final Date creationDate;
   @SerializedName("MultipartUploadId")
   private final String multipartUploadId;
   @SerializedName("PartSizeInBytes")
   private final long partSizeInBytes;
   @SerializedName("VaultARN")
   private final String vaultARN;
   @SerializedName("Parts")
   private final Iterable<PartMetadata> parts;
   @SerializedName("Marker")
   private final String marker;

   @ConstructorProperties({ "ArchiveDescription", "CreationDate", "MultipartUploadId", "PartSizeInBytes", "VaultARN",
         "Parts", "Marker" })
   public MultipartUploadMetadata(@Nullable String archiveDescription, Date creationDate, String multipartUploadId,
         long partSizeInBytes, String vaultARN, @Nullable Iterable<PartMetadata> parts, @Nullable String marker) {
      super();
      this.archiveDescription = archiveDescription;
      this.creationDate = (Date) checkNotNull(creationDate, "creationDate").clone();
      this.multipartUploadId = checkNotNull(multipartUploadId, "multipartUploadId");
      this.partSizeInBytes = partSizeInBytes;
      this.vaultARN = checkNotNull(vaultARN, "vaultARN");
      this.parts = parts;
      this.marker = marker;
   }

   public String getArchiveDescription() {
      return archiveDescription;
   }

   public Date getCreationDate() {
      return (Date) creationDate.clone();
   }

   public String getMultipartUploadId() {
      return multipartUploadId;
   }

   public long getPartSizeInBytes() {
      return partSizeInBytes;
   }

   public long getPartSizeInMB() {
      return partSizeInBytes >> 20;
   }

   public String getVaultARN() {
      return vaultARN;
   }

   @Override
   public Iterator<PartMetadata> iterator() {
      return parts == null ? null : parts.iterator();
   }

   @Override
   public Optional<Object> nextMarker() {
      return Optional.<Object>fromNullable(marker);
   }

   public PaginationOptions nextPaginationOptions() {
      return PaginationOptions.class.cast(nextMarker().get());
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.archiveDescription, this.creationDate, this.multipartUploadId, this.partSizeInBytes,
            this.vaultARN, this.marker, this.parts);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MultipartUploadMetadata other = (MultipartUploadMetadata) obj;

      return Objects.equal(this.archiveDescription, other.archiveDescription)
            && Objects.equal(this.creationDate, other.creationDate)
            && Objects.equal(this.multipartUploadId, other.multipartUploadId)
            && Objects.equal(this.partSizeInBytes, other.partSizeInBytes)
            && Objects.equal(this.vaultARN, other.vaultARN)
            && Objects.equal(this.marker, other.marker)
            && Objects.equal(this.parts, other.parts);
   }

   @Override
   public String toString() {
      return "MultipartUploadMetadata [archiveDescription=" + archiveDescription + ", creationDate=" + creationDate
            + ", multipartUploadId=" + multipartUploadId + ", partSizeInBytes=" + partSizeInBytes + ", vaultARN="
            + vaultARN + ", marker=" + marker + ", parts=" + parts + "]";
   }

   @Override
   public int compareTo(MultipartUploadMetadata o) {
      return ComparisonChain.start().compare(this.creationDate, o.creationDate).result();
   }
}
