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

import static com.google.common.collect.Iterables.isEmpty;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jclouds.azurecompute.arm.domain.ActionGroup;
import org.jclouds.azurecompute.arm.domain.Actions;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlert;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlertProperties;
import org.jclouds.azurecompute.arm.domain.AlertRuleAllOfCondition;
import org.jclouds.azurecompute.arm.domain.AlertRuleAnyOfOrLeafCondition;
import org.jclouds.azurecompute.arm.domain.AlertRuleLeafCondition;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiMockTest;
import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

@Test(groups = "unit", testName = "ActivityLogAlertApiMockTest", singleThreaded = true)
public class ActivityLogAlertApiMockTest extends BaseAzureComputeApiMockTest {

	public void testList() throws InterruptedException {
		server.enqueue(jsonResponse("/activitylogalertresourcegroup.json"));
		final ActivityLogAlertApi activityLogAlertApi = api.getActivityLogAlertApi("myResourceGroup");
		assertEquals(activityLogAlertApi.list(), getActivityLogRuleList());
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts?api-version=2020-10-01");
	}

	private List<ActivityLogAlert> getActivityLogRuleList() {
		List<ActivityLogAlert> activityLogAlertRules = new ArrayList<ActivityLogAlert>();
		activityLogAlertRules.add(ActivityLogAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/activityLogAlerts/myActivityLog",
				"myActivityLog", "Global", "Microsoft.Insights/ActivityLogAlerts", tags(),
				myActivityLogAlertProperties()));
		activityLogAlertRules.add(ActivityLogAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/activityLogAlerts/simpleActivityLog",
				"simpleActivityLog", "Global", "Microsoft.Insights/ActivityLogAlerts", null,
				sampleActivityLogAlertProperties()));
		return activityLogAlertRules;
	}

	public ActivityLogAlertProperties myActivityLogAlertProperties() {
		return ActivityLogAlertProperties.create("", false, scope0(), condition0(), actions());
	}

	public ActivityLogAlertProperties sampleActivityLogAlertProperties() {
		return ActivityLogAlertProperties.create("", true, scope1(), condition1(), actions());

	}

	public void testListEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("groupname");
		List<ActivityLogAlert> list = activityApi.list();
		assertTrue(isEmpty(list));
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/groupname/providers/Microsoft.Insights/activityLogAlerts?api-version=2020-10-01");
	}

	public void testGet() throws InterruptedException {
		server.enqueue(jsonResponse("/activitylogalertget.json"));
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("myResourceGroup");
		ActivityLogAlert alert = activityApi.get("myActivityLogAlert");
		assertEquals(alert.location(), "Global");
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts/myActivityLogAlert?api-version=2020-10-01");
	}
	
	public void testGetReturns404() throws InterruptedException {
		server.enqueue(response404());
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("myResourceGroup");
		ActivityLogAlert alert = activityApi.get("myActivityLogAlert");
		assertNull(alert);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts/myActivityLogAlert?api-version=2020-10-01");
	}

	public void testDelete() throws Exception {
		server.enqueue(response202WithHeader());
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("myResourceGroup");
		URI uri = activityApi.delete("myActivityLogAlert");
		assertEquals(server.getRequestCount(), 1);
		assertNotNull(uri);
		assertSent(server, "DELETE",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts/myActivityLogAlert?api-version=2020-10-01");
	}

	public void testDeleteReturns404() throws Exception {
		server.enqueue(response404());
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("myResourceGroup");
		URI uri = activityApi.delete("myActivityLogAlert");
		assertEquals(server.getRequestCount(), 1);
		assertNull(uri);
		assertSent(server, "DELETE",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts/myActivityLogAlert?api-version=2020-10-01");
	}

	public Map<String, String> tags() {
		Map<String, String> tags = new HashMap<>();
		tags.put("key1", "value1");
		tags.put("key2", "value2");
		return tags;
	}

	public List<String> scope0() {
		List<String> list = new ArrayList<String>();
		list.add(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVM");
		return list;
	}

	public List<String> scope1() {
		List<String> list = new ArrayList<String>();
		list.add(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/simpleVM");

		return list;
	}

	public AlertRuleAllOfCondition condition0() {
		final List<AlertRuleAnyOfOrLeafCondition> list1 = new ArrayList<>();
		final AlertRuleAnyOfOrLeafCondition alertRuleAnyOfOrLeafCondition = AlertRuleAnyOfOrLeafCondition.create(null,
				null, "ServiceHealth", "category");
		list1.add(alertRuleAnyOfOrLeafCondition);
		final AlertRuleAllOfCondition condtion = AlertRuleAllOfCondition.create(list1, "");
		return condtion;
	}

	public AlertRuleAllOfCondition condition1() {
		final List<AlertRuleAnyOfOrLeafCondition> list1 = new ArrayList<>();
		final AlertRuleAnyOfOrLeafCondition alertRuleAnyOfOrLeafCondition0 = AlertRuleAnyOfOrLeafCondition.create(null,
				null, "Administrative", "category");
		final AlertRuleAnyOfOrLeafCondition alertRuleAnyOfOrLeafCondition1 = AlertRuleAnyOfOrLeafCondition.create(null,
				null, "Microsoft.Compute/virtualMachines/write", "operationName");
		list1.add(alertRuleAnyOfOrLeafCondition0);
		list1.add(alertRuleAnyOfOrLeafCondition1);
		final AlertRuleAllOfCondition condtion = AlertRuleAllOfCondition.create(list1, null);
		return condtion;
	}

	public List<AlertRuleAnyOfOrLeafCondition> allOf() {
		List<AlertRuleAnyOfOrLeafCondition> list = new ArrayList<>();
		list.add(anyOfOrLeafCondition());
		return list;
	}

	public AlertRuleAnyOfOrLeafCondition anyOfOrLeafCondition() {
		AlertRuleAnyOfOrLeafCondition alertRule = AlertRuleAnyOfOrLeafCondition.create(leafCondition(), null, null,
				null);
		return alertRule;
	}

	public List<AlertRuleLeafCondition> leafCondition() {
		final List<AlertRuleLeafCondition> list = new ArrayList<>();
		final AlertRuleLeafCondition alertRuleLeafCondition = AlertRuleLeafCondition.create(null, "ServiceHealth",
				"category");
		list.add(alertRuleLeafCondition);
		return list;
	}

	public Actions actions() {
		List<ActionGroup> list = new ArrayList<>();
		ActionGroup actionGroup = ActionGroup.create(
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/microsoft.insights/actiongroups/myAction",
				null);
		list.add(actionGroup);
		Actions action = Actions.create(list);
		return action;
	}

	public void testCreate() throws Exception {
		server.enqueue(jsonResponse("/activitylogalertcreate.json"));
		final ActivityLogAlertApi activityApi = api.getActivityLogAlertApi("myResourceGroup");
		ActivityLogAlert activityAlert = activityApi.createOrUpdate("myActivityLogAlertRule",
				myActivityLogAlertProperties(), new HashMap<>(), "Global");
		final ActivityLogAlert expected = ActivityLogAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/activityLogAlerts/myActivityLogAlertRule",
				"myActivityLogAlertRule", "Global", "Microsoft.Insights/ActivityLogAlerts", tags(),
				myActivityLogAlertProperties());
		assertEquals(activityAlert.id(), expected.id());
		assertSent(server, "PUT",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/activityLogAlerts/myActivityLogAlertRule?validating=false&api-version=2020-10-01");

	}
}
