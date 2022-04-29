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

import org.jclouds.glacier.util.ContentRange;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class ArchiveRetrievalJobRequest extends JobRequest {
   private static final String TYPE = "archive-retrieval";

   @SerializedName("ArchiveId")
   private final String archiveId;
   @SerializedName("Description")
   private final String description;
   @SerializedName("RetrievalByteRange")
   private final ContentRange range;

   private ArchiveRetrievalJobRequest(String archiveId, @Nullable String description, @Nullable ContentRange range) {
      super(TYPE);
      this.archiveId = checkNotNull(archiveId, "archiveId");
      this.description = description;
      this.range = range;
   }

   @ConstructorProperties({ "ArchiveId", "Description", "RetrievalByteRange" })
   private ArchiveRetrievalJobRequest(String archiveId, @Nullable String description, @Nullable String range) {
      this(archiveId, description, range == null ? null : ContentRange.fromString(range));
   }

   public String getDescription() {
      return description;
   }

   public ContentRange getRange() {
      return range;
   }

   public String getArchiveId() {
      return archiveId;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.archiveId, this.description, this.range);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ArchiveRetrievalJobRequest other = (ArchiveRetrievalJobRequest) obj;

      return Objects.equal(this.archiveId, other.archiveId) && Objects.equal(this.description, other.description)
            && Objects.equal(this.range, other.range);
   }

   @Override
   public String toString() {
      return "InventoryRetrievalParameters [archiveId=" + archiveId + ", description=" + description + ", range="
            + range + "]";
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder {
      private String archiveId;
      private String description;
      private ContentRange range;

      Builder() {
      }

      public Builder archiveId(String archiveId) {
         this.archiveId = archiveId;
         return this;
      }

      public Builder description(String description) {
         this.description = description;
         return this;
      }

      public Builder range(ContentRange range) {
         this.range = range;
         return this;
      }

      public ArchiveRetrievalJobRequest build() {
         return new ArchiveRetrievalJobRequest(archiveId, description, range);
      }
   }
}
