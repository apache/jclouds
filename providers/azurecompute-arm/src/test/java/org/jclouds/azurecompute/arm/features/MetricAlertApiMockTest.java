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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jclouds.azurecompute.arm.domain.Criteria;
import org.jclouds.azurecompute.arm.domain.MetricAlert;
import org.jclouds.azurecompute.arm.domain.MetricAlertAction;
import org.jclouds.azurecompute.arm.domain.MetricAlertCriteria;
import org.jclouds.azurecompute.arm.domain.MetricAlertCriteria.Operator;
import org.jclouds.azurecompute.arm.domain.MetricAlertProperties;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiMockTest;
import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

@Test(groups = "unit", testName = "MetricAlertApiMockTest", singleThreaded = true)
public class MetricAlertApiMockTest extends BaseAzureComputeApiMockTest {

	public void testList() throws InterruptedException {
		server.enqueue(jsonResponse("/metricalertgetbyresource.json"));
		final MetricAlertApi metricAlertApi = api.getMetricAlertApi("myResourceGroup");
		List<MetricAlert> list = metricAlertApi.list();
		assertEquals(list, getMetricAlertRuleList());
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts?api-version=2018-03-01");
	}

	private List<MetricAlert> getMetricAlertRuleList() {
		List<MetricAlert> metricAlertRules = new ArrayList<MetricAlert>();
		metricAlertRules.add(MetricAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/metricalerts/MetricAlert1",
				"MetricAlert1", "global", "Microsoft.Insights/metricAlerts", Collections.emptyMap(),
				myMetricAlertProperties0()));
		metricAlertRules.add(MetricAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/metricalerts/MetricAlert2",
				"MetricAlert2", "global", "Microsoft.Insights/metricAlerts", Collections.emptyMap(),
				myMetricAlertProperties1()));
		return metricAlertRules;
	}

	public MetricAlertProperties myMetricAlertProperties0() {
		return MetricAlertProperties.create(Collections.emptyList(), true, criteria0("Available Memory Bytes"),
				"MetricAlert1", true, "PT1M", false, null, scope0(), 0, "eastus", "Microsoft.Compute/virtualMachines",
				"PT1M");
	}

	public MetricAlertProperties myMetricAlertProperties1() {
		return MetricAlertProperties.create(actions(), true, criteria1("Percentage CPU"), "", false, "PT1M", false,
				null, scope1(), 3, "eastus", "Microsoft.Compute/virtualMachines", "PT5M");
	}

	public List<String> scope0() {
		List<String> list = new ArrayList<String>();
		list.add(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/LinuxVM");
		list.add(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/CentOSVM");
		return list;
	}

	public List<String> scope1() {
		List<String> list = new ArrayList<String>();
		list.add(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/LinuxVM");
		return list;
	}

	public Criteria criteria0(String metricName) {
		List<MetricAlertCriteria> lstMetricAlertCriteria = new ArrayList<>();
		lstMetricAlertCriteria.add(MetricAlertCriteria.create("StaticThresholdCriterion", null, metricName,
				"Microsoft.Compute/virtualMachines", "criteria1", Operator.GreaterThan, false, 0,
				MetricAlertCriteria.AggregationTypeEnum.Total, null, null, null, null, 0, null));
		final Criteria criteria = Criteria.create(lstMetricAlertCriteria,
				"Microsoft.Azure.Monitor.MultipleResourceMultipleMetricCriteria");
		return criteria;
	}

	public Criteria criteria1(String metricName) {
		List<MetricAlertCriteria> lstMetricAlertCriteria = new ArrayList<>();
		lstMetricAlertCriteria.add(MetricAlertCriteria.create("StaticThresholdCriterion", null, metricName,
				"Microsoft.Compute/virtualMachines", "criteria1", Operator.GreaterThan, false, 0,
				MetricAlertCriteria.AggregationTypeEnum.Average, null, null, null, null, 0, null));
		final Criteria criteria = Criteria.create(lstMetricAlertCriteria,
				"Microsoft.Azure.Monitor.MultipleResourceMultipleMetricCriteria");
		return criteria;
	}

	public List<MetricAlertAction> actions() {
		List<MetricAlertAction> list = new ArrayList<>();
		MetricAlertAction actionGroup = MetricAlertAction.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/actionGroups/metricaction",
				null);
		list.add(actionGroup);
		return list;
	}

	public void testListEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final MetricAlertApi metricAlertApi = api.getMetricAlertApi("myResourceGroup");
		List<MetricAlert> list = metricAlertApi.list();
		assertTrue(list.isEmpty());
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts?api-version=2018-03-01");
	}

	public void testGet() throws InterruptedException {
		server.enqueue(jsonResponse("/metricalertget.json"));
		final MetricAlertApi metricAlerApi = api.getMetricAlertApi("myResourceGroup");
		MetricAlert actual = metricAlerApi.get("MetricAlertTest");

		MetricAlert expected = MetricAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Insights/metricalerts/MetricAlertTest",
				"MetricAlertTest", "global", "Microsoft.Insights/metricAlerts", tags(), getMetricAlertProperties());

		assertEquals(expected, actual);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts/MetricAlertTest?api-version=2018-03-01");
	}

	public Map<String, String> tags() {
		Map<String, String> tags = new HashMap<>();
		tags.put("createdBy", "jclouds");
		return tags;
	}

	public MetricAlertProperties getMetricAlertProperties() {
		final List<MetricAlertAction> actionList = new ArrayList<>();
		final MetricAlertAction action = MetricAlertAction.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/microsoft.insights/actiongroups/actionemail",
				null);
		actionList.add(action);
		final List<MetricAlertCriteria> lstMetricAlertCriteria = new ArrayList<>();
		lstMetricAlertCriteria.add(MetricAlertCriteria.create("StaticThresholdCriterion", null, "CPU Credits Consumed",
				"Microsoft.Compute/virtualMachines", "Metric1", Operator.GreaterThan, false, 0,
				MetricAlertCriteria.AggregationTypeEnum.Average, null, null, null, null, 0, null));
		final Criteria criteria = Criteria.create(lstMetricAlertCriteria,
				"Microsoft.Azure.Monitor.MultipleResourceMultipleMetricCriteria");
		return MetricAlertProperties.create(actionList, true, criteria, "", false, "PT5M", false, null, scope1(), 3,
				"eastus", "Microsoft.Compute/virtualMachines", "PT5M");
	}

	public void testGetReturns404() throws InterruptedException {
		server.enqueue(response404());
		final MetricAlertApi metricAlerApi = api.getMetricAlertApi("myResourceGroup");
		MetricAlert alert = metricAlerApi.get("MetricAlertTest");
		assertNull(alert);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts/MetricAlertTest?api-version=2018-03-01");
	}

	public void testDelete() throws Exception {
		server.enqueue(response202WithHeader());
		final MetricAlertApi metricAlerApi = api.getMetricAlertApi("myResourceGroup");
		URI uri = metricAlerApi.delete("MetricAlertTest");
		assertEquals(server.getRequestCount(), 1);
		assertNotNull(uri);
		assertSent(server, "DELETE",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts/MetricAlertTest?api-version=2018-03-01");
	}

	public void testDeleteReturns404() throws Exception {
		server.enqueue(response404());
		final MetricAlertApi metricAlerApi = api.getMetricAlertApi("myResourceGroup");
		URI uri = metricAlerApi.delete("MetricAlertTest");
		assertEquals(server.getRequestCount(), 1);
		assertNull(uri);
		assertSent(server, "DELETE",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts/MetricAlertTest?api-version=2018-03-01");
	}

	public void testCreate() throws Exception {
		server.enqueue(jsonResponse("/metricalertcreateorupdate.json"));
		final MetricAlertApi metricAlertApi = api.getMetricAlertApi("myResourceGroup");
		MetricAlert alertRule = metricAlertApi.createOrUpdate("MetricAlertTest", getMetricAlertProperties(), tags(),
				"global");

		MetricAlert expected = MetricAlert.create(
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.Insights/metricalerts/MetricAlertTest",
				"MetricAlertTest", "global", "Microsoft.Insights/metricAlerts", tags(), getMetricAlertProperties());

		assertEquals(alertRule, expected);
		assertSent(server, "PUT",
				"/subscriptions/SUBSCRIPTIONID/resourcegroups/myResourceGroup/providers/Microsoft.Insights/metricAlerts/MetricAlertTest?validating=false&api-version=2018-03-01");
	}

}
