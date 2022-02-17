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
package org.jclouds.azurecompute.arm.features;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlert;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlertProperties;
import org.jclouds.azurecompute.arm.domain.AlertRuleAllOfCondition;
import org.jclouds.azurecompute.arm.domain.AlertRuleAnyOfOrLeafCondition;
import org.jclouds.azurecompute.arm.domain.ResourceGroup;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@Test(groups = "live", testName = "ActivityLogAlertApiLiveTest", singleThreaded = true)
public class ActivityLogAlertApiLiveTest extends BaseAzureComputeApiLiveTest {

	private String alertRuleName;
	private String subscriptionid;
	private final String GLOBAL = "Global";

	@BeforeClass
	@Override
	public void setup() {
		super.setup();
		subscriptionid = getSubscriptionId();
		createTestResourceGroup();
		alertRuleName = String.format("vn-%s-%s", this.getClass().getSimpleName().toLowerCase(),
				System.getProperty("user.name"));

	}

	protected void createTestResourceGroup() {
		String name = String.format("rg-%s-%s", this.getClass().getSimpleName().toLowerCase(),
				System.getProperty("user.name"));
		ResourceGroup rg = api.getResourceGroupApi().create(name, LOCATION, ImmutableMap.<String, String>of());
		assertNotNull(rg);
		resourceGroupName = rg.name();
	}

	private ActivityLogAlertApi api() {
		return api.getActivityLogAlertApi(resourceGroupName);
	}

	@Test
	public void testCreate() {
		ActivityLogAlert activityAlert = api().createOrUpdate(alertRuleName, properties(),
				ImmutableMap.of("createdBy", "jcloud"), GLOBAL);
		assertTrue(!activityAlert.type().isEmpty());
	}

	@Test(dependsOnMethods = "testCreate")
	public void testGet() {
		final ActivityLogAlert activityLogAlertRule = api().get(alertRuleName);
		assertTrue(!activityLogAlertRule.name().isEmpty());
	}

	@Test(dependsOnMethods = "testCreate")
	public void testList() {
		List<ActivityLogAlert> list = api().list();
		final ActivityLogAlert activityLogAlertRule = api().get(alertRuleName);
		boolean alertRulePresent = Iterables.any(list, new Predicate<ActivityLogAlert>() {
			@Override
			public boolean apply(@Nullable ActivityLogAlert input) {
				return input.name().equals(activityLogAlertRule.name());
			}
		});

		assertTrue(alertRulePresent);
	}

	@Test(dependsOnMethods = "testList", alwaysRun = true)
	public void testDelete() throws Exception {
		URI uri = api().delete(alertRuleName);
		assertResourceDeleted(uri);
	}

	public ActivityLogAlertProperties properties() {
		return ActivityLogAlertProperties.create("LiveTest", true,
				Arrays.asList("/subscriptions/" + subscriptionid + "/resourceGroups/" + resourceGroupName), condition(),
				null);

	}

	public AlertRuleAllOfCondition condition() {
		final List<AlertRuleAnyOfOrLeafCondition> list1 = new ArrayList<>();
		final AlertRuleAnyOfOrLeafCondition alertRuleAnyOfOrLeafCondition0 = AlertRuleAnyOfOrLeafCondition.create(null,
				null, "Administrative", "category");
		final AlertRuleAnyOfOrLeafCondition alertRuleAnyOfOrLeafCondition1 = AlertRuleAnyOfOrLeafCondition.create(null,
				null, "Microsoft.Compute/virtualMachines", "resourceType");
		list1.add(alertRuleAnyOfOrLeafCondition0);
		list1.add(alertRuleAnyOfOrLeafCondition1);
		final AlertRuleAllOfCondition condtion = AlertRuleAllOfCondition.create(list1, null);
		return condtion;
	}

}
