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

import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.hash.HashCode;
import com.google.gson.annotations.SerializedName;

public class ArchiveMetadata implements Comparable<ArchiveMetadata> {

   @SerializedName("ArchiveId")
   private final String archiveId;
   @SerializedName("ArchiveDescription")
   private final String description;
   @SerializedName("CreationDate")
   private final Date creationDate;
   @SerializedName("Size")
   private final long size;
   @SerializedName("SHA256TreeHash")
   private final HashCode treeHash;

   @ConstructorProperties({ "ArchiveId", "ArchiveDescription", "CreationDate", "Size", "SHA256TreeHash" })
   public ArchiveMetadata(String archiveId, @Nullable String description, Date creationDate, long size, String hashCode) {
      this.archiveId = checkNotNull(archiveId, "archiveId");
      this.description = description;
      this.creationDate = (Date) checkNotNull(creationDate, "creationDate").clone();
      this.size = size;
      this.treeHash = HashCode.fromString(checkNotNull(hashCode, "hashCode"));
   }

   public String getArchiveId() {
      return archiveId;
   }

   public String getDescription() {
      return description;
   }

   public Date getCreationDate() {
      return (Date) creationDate.clone();
   }

   public long getSize() {
      return size;
   }

   public HashCode getTreeHash() {
      return treeHash;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.archiveId, this.description, this.creationDate, this.size, this.treeHash);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ArchiveMetadata other = (ArchiveMetadata) obj;

      return Objects.equal(this.archiveId, other.archiveId)
            && Objects.equal(this.description, other.description)
            && Objects.equal(this.creationDate, other.creationDate)
            && Objects.equal(this.treeHash, other.treeHash)
            && Objects.equal(this.size, other.size);
   }

   @Override
   public String toString() {
      return "ArchiveMetadata [archiveId=" + archiveId + ", description=" + description
            + ", creationDate=" + creationDate + ", treeHash=" + treeHash + ", size=" + size + "]";
   }

   @Override
   public int compareTo(ArchiveMetadata o) {
      return ComparisonChain.start().compare(this.archiveId, o.archiveId).result();
   }
}
