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
package org.jclouds.openstack.neutron.v2.domain;

import java.beans.ConstructorProperties;

import jakarta.inject.Named;

import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A Neutron Network Segment
 * Segments and provider values cannot both be set.
 *
 * @see <a
 *      href="http://docs.openstack.org/api/openstack-network/2.0/content/Subnets.html">api
 *      doc</a>
 */
public class NetworkSegment {

   @Named("provider:network_type")
   protected final NetworkType networkType;
   @Named("provider:physical_network")
   protected final String physicalNetwork;
   @Named("provider:segmentation_id")
   protected final Integer segmentationId;

   @ConstructorProperties({"provider:network_type", "provider:physical_network", "provider:segmentation_id"})
   protected NetworkSegment(NetworkType networkType, String physicalNetwork, Integer segmentationId) {
      this.networkType = networkType;
      this.physicalNetwork = physicalNetwork;
      this.segmentationId = segmentationId;
   }

   /**
    * @return the networkType of the NetworkSegment
    */
   @Nullable
   public NetworkType getNetworkType() {
      return networkType;
   }

   /**
    * @return the physicalNetwork of the NetworkSegment
    */
   @Nullable
   public String getPhysicalNetwork() {
      return physicalNetwork;
   }

   /**
    * @return the segmentationId of the NetworkSegment
    */
   @Nullable
   public Integer getSegmentationId() {
      return segmentationId;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(networkType, physicalNetwork, segmentationId);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null || getClass() != obj.getClass())
         return false;
      NetworkSegment that = NetworkSegment.class.cast(obj);
      return Objects.equal(this.networkType, that.networkType)
            && Objects.equal(this.physicalNetwork, that.physicalNetwork)
            && Objects.equal(this.segmentationId, that.segmentationId);
   }

   protected MoreObjects.ToStringHelper string() {
      return MoreObjects.toStringHelper(this).add("networkType", networkType).add("physicalNetwork", physicalNetwork)
            .add("segmentationId", segmentationId);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   /**
    * @return the Builder for NetworkSegment
    */
   public static Builder builder() {
      return new Builder();
   }

   /**
    * Gets a Builder configured as this object.
    */
   public Builder toBuilder() {
      return new Builder().fromNetworkSegment(this);
   }

   public static class Builder {
      protected NetworkType networkType;
      protected String physicalNetwork;
      protected Integer segmentationId;

      /**
       * Provide the networkType to the NetworkSegment's Builder.
       *
       * @return the Builder.
       * @see NetworkSegment#getNetworkType()
       */
      public Builder networkType(NetworkType networkType) {
         this.networkType = networkType;
         return this;
      }

      /**
       * Provide the physicalNetwork to the NetworkSegment's Builder.
       *
       * @return the Builder.
       * @see NetworkSegment#getPhysicalNetwork()
       */
      public Builder physicalNetwork(String physicalNetwork) {
         this.physicalNetwork = physicalNetwork;
         return this;
      }

      /**
       * Provide the segmentationId to the NetworkSegment's Builder.
       *
       * @return the Builder.
       * @see NetworkSegment#getSegmentationId()
       */
      public Builder segmentationId(int segmentationId) {
         this.segmentationId = segmentationId;
         return this;
      }

      /**
       * @return a NetworkSegment constructed with this Builder.
       */
      public NetworkSegment build() {
         return new NetworkSegment(networkType, physicalNetwork, segmentationId);
      }

      /**
       * @return a Builder from another NetworkSegment.
       */
      public Builder fromNetworkSegment(NetworkSegment in) {
         return this.networkType(in.getNetworkType()).physicalNetwork(in.getPhysicalNetwork())
               .segmentationId(in.getSegmentationId());
      }
   }
}
