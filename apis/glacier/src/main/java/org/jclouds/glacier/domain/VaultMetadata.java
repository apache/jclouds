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
import com.google.gson.annotations.SerializedName;

/**
 * Defines the attributes needed to describe a vault.
 */
public class VaultMetadata implements Comparable<VaultMetadata> {

   @SerializedName("VaultName")
   private final String vaultName;
   @SerializedName("VaultARN")
   private final String vaultARN;
   @SerializedName("CreationDate")
   private final Date creationDate;
   @SerializedName("LastInventoryDate")
   private final Date lastInventoryDate;
   @SerializedName("NumberOfArchives")
   private final long numberOfArchives;
   @SerializedName("SizeInBytes")
   private final long sizeInBytes;

   @ConstructorProperties({ "VaultName", "VaultARN", "CreationDate", "LastInventoryDate", "NumberOfArchives",
         "SizeInBytes" })
   public VaultMetadata(String vaultName, String vaultARN, Date creationDate, @Nullable Date lastInventoryDate,
         long numberOfArchives, long sizeInBytes) {
      this.vaultName = checkNotNull(vaultName, "vaultName");
      this.vaultARN = checkNotNull(vaultARN, "vaultARN");
      this.creationDate = (Date) checkNotNull(creationDate, "creationDate").clone();
      this.lastInventoryDate = lastInventoryDate;
      this.numberOfArchives = numberOfArchives;
      this.sizeInBytes = sizeInBytes;
   }

   public String getVaultName() {
      return vaultName;
   }

   public String getVaultARN() {
      return vaultARN;
   }

   public Date getCreationDate() {
      return (Date) creationDate.clone();
   }

   public Date getLastInventoryDate() {
      return lastInventoryDate;
   }

   public long getNumberOfArchives() {
      return numberOfArchives;
   }

   public long getSizeInBytes() {
      return sizeInBytes;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.vaultName, this.vaultARN, this.creationDate, this.lastInventoryDate,
            this.numberOfArchives, this.sizeInBytes);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      VaultMetadata other = (VaultMetadata) obj;

      return Objects.equal(this.vaultName, other.vaultName) && Objects.equal(this.vaultARN, other.vaultARN)
            && Objects.equal(this.creationDate, other.creationDate)
            && Objects.equal(this.lastInventoryDate, other.lastInventoryDate)
            && Objects.equal(this.numberOfArchives, other.numberOfArchives)
            && Objects.equal(this.sizeInBytes, other.sizeInBytes);
   }

   @Override
   public String toString() {
      return "VaultMetadata [vaultName=" + vaultName + ", vaultARN=" + vaultARN + ", creationDate=" + creationDate
            + ", lastInventoryDate=" + lastInventoryDate + ", numberOfArchives=" + numberOfArchives + ", sizeInBytes="
            + sizeInBytes + "]";
   }

   @Override
   public int compareTo(VaultMetadata o) {
      return ComparisonChain.start().compare(this.vaultName, o.getVaultName()).result();
   }
}
