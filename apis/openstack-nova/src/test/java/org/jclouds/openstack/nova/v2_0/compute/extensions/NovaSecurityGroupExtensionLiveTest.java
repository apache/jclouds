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
package org.jclouds.openstack.nova.v2_0.compute.extensions;

import static org.testng.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.compute.extensions.internal.BaseSecurityGroupExtensionLiveTest;
import org.testng.annotations.Test;
import org.jclouds.compute.domain.SecurityGroup;
import com.google.common.base.Optional;

/**
 * Live test for openstack-nova {@link org.jclouds.compute.extensions.SecurityGroupExtension} implementation.
 */
@Test(groups = "live", singleThreaded = true, testName = "NovaSecurityGroupExtensionLiveTest")
public class NovaSecurityGroupExtensionLiveTest extends BaseSecurityGroupExtensionLiveTest {

   public NovaSecurityGroupExtensionLiveTest() {
      provider = "openstack-nova";
   }

    @Test(groups = { "integration", "live" }, singleThreaded = true)
    public void testListSecurityGroups() throws RunNodesException, InterruptedException, ExecutionException {
        skipIfSecurityGroupsNotSupported();

        ComputeService computeService = view.getComputeService();
        Optional<SecurityGroupExtension> securityGroupExtension = computeService.getSecurityGroupExtension();
        assertTrue(securityGroupExtension.isPresent(), "security extension was not present");

        Set<SecurityGroup> groups = securityGroupExtension.get().listSecurityGroups();
        System.out.println(groups.size());
        for (SecurityGroup group : groups) {
            System.out.println(group);
        }
    }
}
