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
package org.jclouds.openstack.neutron.v2;

import java.io.Closeable;
import java.util.Set;

import jakarta.ws.rs.Path;

import org.jclouds.location.Region;
import org.jclouds.openstack.neutron.v2.features.FloatingIPApi;
import org.jclouds.openstack.neutron.v2.extensions.RouterApi;
import org.jclouds.openstack.neutron.v2.features.SecurityGroupApi;
import org.jclouds.openstack.neutron.v2.extensions.lbaas.v1.LBaaSApi;
import org.jclouds.openstack.neutron.v2.extensions.FWaaSApi;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.neutron.v2.features.PortApi;
import org.jclouds.openstack.neutron.v2.features.SubnetApi;
import org.jclouds.openstack.neutron.v2.functions.VersionAwareRegionToEndpoint;
import org.jclouds.openstack.v2_0.features.ExtensionApi;
import org.jclouds.rest.annotations.Delegate;
import org.jclouds.rest.annotations.EndpointParam;

import com.google.common.base.Optional;
import com.google.inject.Provides;

/**
 * Provides access to the OpenStack Networking (Neutron) v2 API.
 *
 * The service-side API will always have a v2.0 in the path.
 * However, the endpoint will sometimes contain a v2.0 and sometimes it will not.
 * The @Path annotation here ensures the path is always added. The VersionAwareRegionToEndpoint ensures that the
 * endpoint will always look the same.
 *
 * Cannot leave labs until fixed:
 * TODO: https://issues.apache.org/jira/browse/JCLOUDS-773
 */
@Path("v2.0")
public interface NeutronApi extends Closeable {
   /**
    * @return the Region codes configured
    */
   @Provides
   @Region
   Set<String> getConfiguredRegions();

   /**
    * Provides access to Extension features.
    */
   @Delegate
   ExtensionApi getExtensionApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to Network features.
    */
   @Delegate
   NetworkApi getNetworkApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to Subnet features.
    */
   @Delegate
   SubnetApi getSubnetApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to Port features.
    */
   @Delegate
   PortApi getPortApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to SecurityGroup features.
    */
   @Delegate
   SecurityGroupApi getSecurityGroupApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to Floating IP features.
    */
   @Delegate
   FloatingIPApi getFloatingIPApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to Router features.
    *
    * <h3>NOTE</h3>
    * This API is an extension that may or may not be present in your OpenStack cloud. Use the Optional return type
    * to determine if it is present.
    */
   @Delegate
   Optional<RouterApi> getRouterApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to LBaaS features.
    *
    * <h3>NOTE</h3>
    * This API is an extension that may or may not be present in your OpenStack cloud. Use the Optional return type
    * to determine if it is present.
    */
   @Delegate
   Optional<LBaaSApi> getLBaaSApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);

   /**
    * Provides access to FWaaS features.
    *
    * <h3>NOTE</h3>
    * This API is an extension that may or may not be present in your OpenStack cloud. Use the Optional return type
    * to determine if it is present.
    */
   @Delegate
   Optional<FWaaSApi> getFWaaSApi(@EndpointParam(parser = VersionAwareRegionToEndpoint.class) String region);
}
