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

import java.beans.ConstructorProperties;

import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class InventoryRetrievalJobRequest extends JobRequest {
   private static final String TYPE = "inventory-retrieval";

   @SerializedName("Description")
   private final String description;
   @SerializedName("Format")
   private final String format;
   @SerializedName("InventoryRetrievalParameters")
   private final InventoryRetrievalParameters parameters;

   @ConstructorProperties({ "Description", "Format" })
   private InventoryRetrievalJobRequest(@Nullable String description, @Nullable String format) {
      super(TYPE);
      this.description = description;
      this.format = format;
      this.parameters = new InventoryRetrievalParameters();
   }

   public String getDescription() {
      return description;
   }

   public String getFormat() {
      return format;
   }

   public InventoryRetrievalParameters getParameters() {
      return this.parameters;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.description, this.format, this.parameters);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      InventoryRetrievalJobRequest other = (InventoryRetrievalJobRequest) obj;

      return Objects.equal(this.description, other.description) && Objects.equal(this.format, other.format)
            && Objects.equal(this.parameters, other.parameters);
   }

   @Override
   public String toString() {
      return "InventoryRetrievalJobRequest [description=" + description + ", format=" + format + "," +
            "parameters=" + parameters + "]";
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder {
      private String description;
      private String format;
      private String startDate;
      private String endDate;
      private Integer limit;
      private String marker;

      Builder() {
      }

      public Builder description(String description) {
         this.description = description;
         return this;
      }

      public Builder format(String format) {
         this.format = format;
         return this;
      }

      public Builder startDate(String startDate) {
         this.startDate = startDate;
         return this;
      }

      public Builder endDate(String endDate) {
         this.endDate = endDate;
         return this;
      }

      public Builder limit(Integer limit) {
         this.limit = limit;
         return this;
      }

      public Builder marker(String marker) {
         this.marker = marker;
         return this;
      }

      public InventoryRetrievalJobRequest build() {
         InventoryRetrievalJobRequest request = new InventoryRetrievalJobRequest(description, format);
         request.getParameters().setEndDate(endDate);
         request.getParameters().setStartDate(startDate);
         request.getParameters().setLimit(limit);
         request.getParameters().setMarker(marker);
         return request;
      }
   }
}
