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

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class InventoryRetrievalParameters {
      @SerializedName("StartDate")
      private String startDate;
      @SerializedName("EndDate")
      private String endDate;
      @SerializedName("Limit")
      private Integer limit;
      @SerializedName("Marker")
      private String marker;

      public InventoryRetrievalParameters() {
      }

      public String getStartDate() {
         return startDate;
      }

      public void setStartDate(String startDate) {
         this.startDate = startDate;
      }

      public String getEndDate() {
         return endDate;
      }

      public void setEndDate(String endDate) {
         this.endDate = endDate;
      }

      public Integer getLimit() {
         return limit;
      }

      public void setLimit(Integer limit) {
         this.limit = limit;
      }

      public String getMarker() {
         return marker;
      }

      public void setMarker(String marker) {
         this.marker = marker;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.startDate, this.endDate, this.limit, this.marker);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         InventoryRetrievalParameters other = (InventoryRetrievalParameters) obj;

         return Objects.equal(this.startDate, other.startDate) && Objects.equal(this.endDate, other.endDate)
               && Objects.equal(this.limit, other.limit)
               && Objects.equal(this.marker, other.marker);
      }

      @Override
      public String toString() {
         return "InventoryRetrievalParameters [startDate=" + startDate + ", endDate=" + endDate + ", limit=" + limit
               + ", marker=" + marker + "]";
      }
}
