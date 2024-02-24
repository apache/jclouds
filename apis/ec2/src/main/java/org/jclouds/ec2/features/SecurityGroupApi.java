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
package org.jclouds.ec2.features;

import static org.jclouds.aws.reference.FormParameters.ACTION;

import java.util.Set;

import jakarta.inject.Named;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.aws.filters.FormSigner;
import org.jclouds.ec2.binders.BindFiltersToIndexedFormParams;
import org.jclouds.ec2.binders.BindGroupNamesToIndexedFormParams;
import org.jclouds.ec2.binders.BindUserIdGroupPairToSourceSecurityGroupFormParams;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.domain.UserIdGroupPair;
import org.jclouds.ec2.xml.DescribeSecurityGroupsResponseHandler;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.location.functions.RegionToEndpointOrProviderIfNull;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.VirtualHost;
import org.jclouds.rest.annotations.XMLResponseParser;

import com.google.common.collect.Multimap;

/**
 * Provides access to EC2 via their REST API.
 * <p/>
 */
@RequestFilters(FormSigner.class)
@VirtualHost
public interface SecurityGroupApi {

   /**
    * Creates a new security group. Group names must be unique per identity.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param name
    *           Name of the security group. Accepts alphanumeric characters, spaces, dashes, and
    *           underscores.
    * @param description
    *           Description of the group. This is informational only. If the description contains
    *           spaces, you must enc lose it in single quotes (') or URL-encode it. Accepts
    *           alphanumeric characters, spaces, dashes, and underscores.
    * @see #runInstances
    * @see #describeSecurityGroups
    * @see #authorizeSecurityGroupIngress
    * @see #revokeSecurityGroupIngress
    * @see #deleteSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-CreateSecurityGroup.html"
    *      />
    */
   @Named("CreateSecurityGroup")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "CreateSecurityGroup")
   void createSecurityGroupInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
            @FormParam("GroupName") String name, @FormParam("GroupDescription") String description);


   // Supported by
   //  * AWS
   //  * Openstack - https://github.com/openstack/ec2-api/blob/61daf6a80fd6cc9ab800e6b6a2cd3d1d827e2527/ec2api/api/security_group.py#L130
   //  * Eucalyptus - https://docs.eucalyptus.com/eucalyptus/4.4.0/#euca2ools-guide/euca-delete-group.html
   //                 https://github.com/eucalyptus/euca2ools/blob/096d97ef2729da976759657d6d6f645a6e959e05/euca2ools/commands/ec2/deletesecuritygroup.py#L37
   /**
    * Deletes a security group by ID.
    *
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param id
    *           ID of the security group to delete.
    *
    * @see #describeSecurityGroups
    * @see #authorizeSecurityGroupIngress
    * @see #revokeSecurityGroupIngress
    * @see #createSecurityGroup
    *
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DeleteSecurityGroup.html"
    *      />
    */
   @Named("DeleteSecurityGroup")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "DeleteSecurityGroup")
   @Fallback(VoidOnNotFoundOr404.class)
   void deleteSecurityGroupInRegionById(
           @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
           @FormParam("GroupId") String id);
   
   /**
    * Deletes a security group that you own.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param name
    *           Name of the security group to delete.
    * 
    * @see #describeSecurityGroups
    * @see #authorizeSecurityGroupIngress
    * @see #revokeSecurityGroupIngress
    * @see #createSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DeleteSecurityGroup.html"
    *      />
    */
   @Named("DeleteSecurityGroup")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "DeleteSecurityGroup")
   @Fallback(VoidOnNotFoundOr404.class)
   void deleteSecurityGroupInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region, @FormParam("GroupName") String name);

   /**
    * Returns information about security groups that you own.
    * <p><em>NOTE</em> Works with groups in default VPC only</p>
    *
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param securityGroupNames
    *           Name of the security groups
    *
    * @see #createSecurityGroup
    * @see #authorizeSecurityGroupIngress
    * @see #revokeSecurityGroupIngress
    * @see #deleteSecurityGroup
    *
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeSecurityGroups.html"
    *      />
    */
   @Named("DescribeSecurityGroups")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "DescribeSecurityGroups")
   @XMLResponseParser(DescribeSecurityGroupsResponseHandler.class)
   @Fallback(EmptySetOnNotFoundOr404.class)
   Set<SecurityGroup> describeSecurityGroupsInRegion(
           @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
           @BinderParam(BindGroupNamesToIndexedFormParams.class) String... securityGroupNames);

   /**
    * Returns information about security groups that you own.
    *
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param filter
    *           Multimap of filter key/values.
    *
    * @see #createSecurityGroup
    * @see #authorizeSecurityGroupIngress
    * @see #revokeSecurityGroupIngress
    * @see #deleteSecurityGroup
    *
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeSecurityGroups.html"
    *      />
    */
   @Named("DescribeSecurityGroups")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "DescribeSecurityGroups")
   @XMLResponseParser(DescribeSecurityGroupsResponseHandler.class)
   @Fallback(EmptySetOnNotFoundOr404.class)
   Set<SecurityGroup> describeSecurityGroupsInRegionWithFilter(
           @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
           @BinderParam(BindFiltersToIndexedFormParams.class) Multimap<String, String> filter);

   /**
    * 
    * Adds permissions to a security group based on another group.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param groupName
    *           Name of the group to modify. The name must be valid and belong to the identity
    * @param sourceSecurityGroup
    *           group to associate with this group.
    * 
    * @see #createSecurityGroup
    * @see #describeSecurityGroups
    * @see #revokeSecurityGroupIngress
    * @see #deleteSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-AuthorizeSecurityGroupIngress.html"
    * 
    */
   @Named("AuthorizeSecurityGroupIngress")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "AuthorizeSecurityGroupIngress")
   void authorizeSecurityGroupIngressInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
            @FormParam("GroupName") String groupName,
            @BinderParam(BindUserIdGroupPairToSourceSecurityGroupFormParams.class) UserIdGroupPair sourceSecurityGroup);

   /**
    * 
    * Adds permissions to a security group.
    * <p/>
    * Permissions are specified by the IP protocol (TCP, UDP or ICMP), the source of the request (by
    * IP range or an Amazon EC2 user-group pair), the source and destination port ranges (for TCP
    * and UDP), and the ICMP codes and types (for ICMP). When authorizing ICMP, -1 can be used as a
    * wildcard in the type and code fields. Permission changes are propagated to instances within
    * the security group as quickly as possible. However, depending on the number of instances, a
    * small delay might occur.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param groupName
    *           Name of the group to modify. The name must be valid and belong to the identity
    * @param ipProtocol
    *           IP protocol.
    * @param fromPort
    *           Start of port range for the TCP and UDP protocols, or an ICMP type number. An ICMP
    *           type number of -1 indicates a wildcard (i.e., any ICMP type number).
    * @param toPort
    *           End of port range for the TCP and UDP protocols, or an ICMP code. An ICMP code of -1
    *           indicates a wildcard (i.e., any ICMP code).
    * @param cidrIp
    *           CIDR range.
    * 
    * @see #createSecurityGroup
    * @see #describeSecurityGroups
    * @see #revokeSecurityGroupIngress
    * @see #deleteSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-AuthorizeSecurityGroupIngress.html"
    * 
    */
   @Named("AuthorizeSecurityGroupIngress")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "AuthorizeSecurityGroupIngress")
   void authorizeSecurityGroupIngressInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
            @FormParam("GroupName") String groupName, @FormParam("IpProtocol") IpProtocol ipProtocol,
            @FormParam("FromPort") int fromPort, @FormParam("ToPort") int toPort, @FormParam("CidrIp") String cidrIp);

   /**
    * 
    * Revokes permissions from a security group. The permissions used to revoke must be specified
    * using the same values used to grant the permissions.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param groupName
    *           Name of the group to modify. The name must be valid and belong to the identity
    * @param sourceSecurityGroup
    *           group to associate with this group.
    * 
    * @see #createSecurityGroup
    * @see #describeSecurityGroups
    * @see #authorizeSecurityGroupIngress
    * @see #deleteSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-RevokeSecurityGroupIngress.html"
    * 
    */
   @Named("RevokeSecurityGroupIngress")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "RevokeSecurityGroupIngress")
   void revokeSecurityGroupIngressInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
            @FormParam("GroupName") String groupName,
            @BinderParam(BindUserIdGroupPairToSourceSecurityGroupFormParams.class) UserIdGroupPair sourceSecurityGroup);

   /**
    * 
    * Revokes permissions from a security group. The permissions used to revoke must be specified
    * using the same values used to grant the permissions.
    * <p/>
    * Permissions are specified by IP protocol (TCP, UDP, or ICMP), the source of the request (by IP
    * range or an Amazon EC2 user-group pair), the source and destination port ranges (for TCP and
    * UDP), and the ICMP codes and types (for ICMP).
    * 
    * Permission changes are quickly propagated to instances within the security group. However,
    * depending on the number of instances in the group, a small delay is might occur.
    * 
    * @param region
    *           Security groups are not copied across Regions. Instances within the Region cannot
    *           communicate with instances outside the Region using group-based firewall rules.
    *           Traffic from instances in another Region is seen as WAN bandwidth.
    * @param groupName
    *           Name of the group to modify. The name must be valid and belong to the identity
    * @param ipProtocol
    *           IP protocol.
    * @param fromPort
    *           Start of port range for the TCP and UDP protocols, or an ICMP type number. An ICMP
    *           type number of -1 indicates a wildcard (i.e., any ICMP type number).
    * @param toPort
    *           End of port range for the TCP and UDP protocols, or an ICMP code. An ICMP code of -1
    *           indicates a wildcard (i.e., any ICMP code).
    * @param cidrIp
    *           CIDR range.
    * 
    * @see #createSecurityGroup
    * @see #describeSecurityGroups
    * @see #authorizeSecurityGroupIngress
    * @see #deleteSecurityGroup
    * 
    * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-RevokeSecurityGroupIngress.html"
    * 
    */
   @Named("RevokeSecurityGroupIngress")
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "RevokeSecurityGroupIngress")
   void revokeSecurityGroupIngressInRegion(
            @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
            @FormParam("GroupName") String groupName, @FormParam("IpProtocol") IpProtocol ipProtocol,
            @FormParam("FromPort") int fromPort, @FormParam("ToPort") int toPort, @FormParam("CidrIp") String cidrIp);
}
