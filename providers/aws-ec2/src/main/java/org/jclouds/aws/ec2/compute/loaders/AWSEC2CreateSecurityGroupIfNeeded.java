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
package org.jclouds.aws.ec2.compute.loaders;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.compute.util.ComputeServiceUtils.getPortRangesFromList;

import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.aws.ec2.AWSEC2Api;
import org.jclouds.aws.ec2.features.AWSSecurityGroupApi;
import org.jclouds.aws.ec2.options.CreateSecurityGroupOptions;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.ec2.compute.domain.RegionAndName;
import org.jclouds.ec2.compute.domain.RegionNameAndIngressRules;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.logging.Logger;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Singleton
public class AWSEC2CreateSecurityGroupIfNeeded extends CacheLoader<RegionAndName, String> {
   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;
   protected final AWSSecurityGroupApi securityApi;
   protected final Predicate<RegionAndName> securityGroupEventualConsistencyDelay;
   protected final Function<String, String> groupNameToId;
   @Inject
   public AWSEC2CreateSecurityGroupIfNeeded(AWSEC2Api ec2Api,
                                            @Named("SECGROUP_NAME_TO_ID") Function<String, String> groupNameToId,
                                            @Named("SECURITY") Predicate<RegionAndName> securityGroupEventualConsistencyDelay) {
      this(checkNotNull(ec2Api, "ec2Api").getSecurityGroupApi().get(), groupNameToId, securityGroupEventualConsistencyDelay);
   }

   public AWSEC2CreateSecurityGroupIfNeeded(AWSSecurityGroupApi securityApi,
                                            @Named("SECGROUP_NAME_TO_ID") Function<String, String> groupNameToId,
                                            @Named("SECURITY") Predicate<RegionAndName> securityGroupEventualConsistencyDelay) {
      this.securityApi = checkNotNull(securityApi, "securityApi");
      this.groupNameToId = checkNotNull(groupNameToId, "groupNameToId");
      this.securityGroupEventualConsistencyDelay = checkNotNull(securityGroupEventualConsistencyDelay,
            "securityGroupEventualConsistencyDelay");
   }

   @Override
   public String load(RegionAndName from) {
      RegionNameAndIngressRules realFrom = RegionNameAndIngressRules.class.cast(from);
      return createSecurityGroupInRegion(from.getRegion(), from.getName(), realFrom.getVpcId(), realFrom.getPorts());
   }

   private String createSecurityGroupInRegion(String region, final String name, String vpcId, int... ports) {
      checkNotNull(region, "region");
      checkNotNull(name, "name");
      logger.debug(">> creating securityGroup region(%s) name(%s)", region, name);

      try {
         CreateSecurityGroupOptions options = new CreateSecurityGroupOptions();
         if (vpcId != null) {
            options.vpcId(vpcId);
         }
         String id = securityApi.createSecurityGroupInRegionAndReturnId(region, name, name, options);
         boolean created = securityGroupEventualConsistencyDelay.apply(new RegionAndName(region, name));
         if (!created)
            throw new RuntimeException(String.format("security group %s/%s is not available after creating", region,
                  name));
         logger.debug("<< created securityGroup(%s)", name);

         ImmutableSet.Builder<IpPermission> permissions = ImmutableSet.builder();

         if (ports.length > 0) {
            for (Map.Entry<Integer, Integer> range : getPortRangesFromList(ports).entrySet()) {
               permissions.add(IpPermission.builder()
                               .fromPort(range.getKey())
                               .toPort(range.getValue())
                               .ipProtocol(IpProtocol.TCP)
                               .cidrBlock("0.0.0.0/0")
                               .build());
            }

            // Use filter (as per `SecurityGroupPresent`, in securityGroupEventualConsistencyDelay)
            Set<SecurityGroup> securityGroups = securityApi.describeSecurityGroupsInRegionById(region, id);
            if (securityGroups.isEmpty()) {
               throw new IllegalStateException(String.format("security group %s/%s not found after creating", region, name));
            } else if (securityGroups.size() > 1) {
               throw new IllegalStateException(String.format("multiple security groups matching %s/%s found after creating: %s", 
                     region, name, securityGroups));
            }
            SecurityGroup securityGroup = Iterables.getOnlyElement(securityGroups);
            String myOwnerId = securityGroup.getOwnerId();
            permissions.add(IpPermission.builder()
                            .fromPort(0)
                            .toPort(65535)
                            .ipProtocol(IpProtocol.TCP)
                            .tenantIdGroupNamePair(myOwnerId, id)
                            .build());
            permissions.add(IpPermission.builder()
                            .fromPort(0)
                            .toPort(65535)
                            .ipProtocol(IpProtocol.UDP)
                            .tenantIdGroupNamePair(myOwnerId, id)
                            .build());
         }

         Set<IpPermission> perms = permissions.build();

         if (!perms.isEmpty()) {
            logger.debug(">> authorizing securityGroup region(%s) name(%s) IpPermissions(%s)", region, name, perms);
            securityApi.authorizeSecurityGroupIngressInRegion(region, id, perms);
            logger.debug("<< authorized securityGroup(%s)", name);
         }
         return id;
      } catch (IllegalStateException e) {
         logger.debug("<< reused securityGroup(%s)", name);
         return groupNameToId.apply(new RegionAndName(region, name).slashEncode());
      }
   }

}
