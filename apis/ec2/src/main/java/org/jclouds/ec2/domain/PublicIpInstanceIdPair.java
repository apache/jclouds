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
package org.jclouds.ec2.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.jclouds.javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-ItemType-DescribeAddressesResponseInfoType.html"
 *      />
 */
public class PublicIpInstanceIdPair implements Comparable<PublicIpInstanceIdPair> {

   private final String region;
   @Nullable
   private final String instanceId;
   private final String allocationId;
   private final String publicIp;
   private final Map<String, String> tags;

   public PublicIpInstanceIdPair(final String region, final String publicIp, @Nullable final String instanceId,
         @Nullable final String allocationId, @Nullable final Map<String, String> tags) {
      this.region = checkNotNull(region, "region");
      this.instanceId = instanceId;
      this.allocationId = allocationId;
      this.publicIp = checkNotNull(publicIp, "publicIp");
      this.tags = tags == null ? ImmutableMap.<String, String> of() : ImmutableMap.copyOf(tags);
   }

   /**
    * To be removed in jclouds 1.6 <h4>Warning</h4>
    * 
    * Especially on EC2 clones that may not support regions, this value is fragile. Consider
    * alternate means to determine context.
    */
   @Deprecated
   public String getRegion() {
      return region;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int compareTo(final PublicIpInstanceIdPair o) {
      return this == o ? 0 : getPublicIp().compareTo(o.getPublicIp());
   }

   /**
    * The ID of the instance.
    */
   public String getInstanceId() {
      return instanceId;
   }

   /**
    * The ID of the IP allocation (e.g., eipalloc-0ca038968f2a2c986).
    */
   public String getAllocationId() {
      return allocationId;
   }

   /**
    * The public IP address.
    */
   public String getPublicIp() {
      return publicIp;
   }
   
   public Map<String, String> getTags() {
      return tags;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
      result = prime * result + ((allocationId == null) ? 0 : allocationId.hashCode());
      result = prime * result + ((publicIp == null) ? 0 : publicIp.hashCode());
      result = prime * result + ((region == null) ? 0 : region.hashCode());
      result = prime * result + ((tags == null) ? 0 : tags.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PublicIpInstanceIdPair other = (PublicIpInstanceIdPair) obj;
      if (instanceId == null) {
         if (other.instanceId != null)
            return false;
      } else if (!instanceId.equals(other.instanceId))
         return false;
      if (allocationId == null) {
         if (other.allocationId != null)
            return false;
      } else if (!allocationId.equals(other.allocationId))
         return false;
      if (publicIp == null) {
         if (other.publicIp != null)
            return false;
      } else if (!publicIp.equals(other.publicIp))
         return false;
      if (region == null) {
         if (other.region != null)
            return false;
      } else if (!region.equals(other.region))
         return false;
      if (tags == null) {
         if (other.tags != null)
            return false;
      } else if (!tags.equals(other.tags))
         return false;
      return true;
   }
}
