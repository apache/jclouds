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

import java.util.List;

import org.jclouds.azurecompute.arm.domain.Alert;
import org.jclouds.azurecompute.arm.domain.AlertModification;
import org.jclouds.azurecompute.arm.domain.AlertSummary;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiMockTest;
import org.jclouds.azurecompute.arm.options.AlertRequestOptions;
import org.testng.annotations.Test;

import static com.google.common.collect.Iterables.isEmpty;
import okhttp3.mockwebserver.MockResponse;

@Test(groups = "unit", testName = "AlertApiMockTest", singleThreaded = true)
public class AlertApiMockTest extends BaseAzureComputeApiMockTest {

	public void testGetById() throws InterruptedException {
		server.enqueue(jsonResponse("/alertsgetbyid.json"));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		Alert alert = alertApi.get("60c4d62b-xxxx-46d8-0000-b6dd8c4a769e");
		final String alertName = alert.name();
		assertEquals(alertName, "SampleAlert");
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/60c4d62b-xxxx-46d8-0000-b6dd8c4a769e?api-version=2019-03-01");
	}

	public void testGetByIdEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		Alert alert = alertApi.get("60c4d62b-xxxx-46d8-0000-b6dd8c4a769e");
		assertNull(alert);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/60c4d62b-xxxx-46d8-0000-b6dd8c4a769e?api-version=2019-03-01");
	}

	public void testGetHistory() throws InterruptedException {
		server.enqueue(jsonResponse("/alertgethistory.json"));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		AlertModification history = alertApi.getHistory("d9db1f27-ce08-4c6d-8ab6-c0a8fbd8bf64");
		final String type = history.type();
		assertEquals(type, "Microsoft.AlertsManagement/alerts");
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/d9db1f27-ce08-4c6d-8ab6-c0a8fbd8bf64/history?api-version=2019-03-01");
	}

	public void testGetHistoryEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		AlertModification history = alertApi.getHistory("d9db1f27-ce08-4c6d-8ab6-c0a8fbd8bf64");
		assertNull(history);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/d9db1f27-ce08-4c6d-8ab6-c0a8fbd8bf64/history?api-version=2019-03-01");
	}

	public void testGetSummary() throws InterruptedException {
		server.enqueue(jsonResponse("/alertgetsummary.json"));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		AlertRequestOptions groupByOption = AlertRequestOptions.Builder.groupBy("severity");
		AlertSummary summary = alertApi.getSummary(groupByOption);
		final String alertName = summary.name();
		assertEquals(alertName, "current");
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alertsSummary?groupby=severity&api-version=2019-03-01");
	}

	public void testGetSummaryEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		AlertRequestOptions groupByOption = AlertRequestOptions.Builder.groupBy("severity");
		AlertSummary summary = alertApi.getSummary(groupByOption);
		assertNull(summary);
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alertsSummary?groupby=severity&api-version=2019-03-01");
	}

	public void testGetAll() throws InterruptedException {
		server.enqueue(jsonResponse("/alertgetall.json"));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		List<Alert> list = alertApi.list();
		assertEquals(list.get(0).name(), "HostPoolAlert");
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts?api-version=2019-03-01");
	}

	public void testGetAllEmpty() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		List<Alert> list = alertApi.list();
		assertTrue(isEmpty(list));
		assertSent(server, "GET",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts?api-version=2019-03-01");
	}

	public void testAlertChangeState() throws InterruptedException {
		server.enqueue(jsonResponse("/alertchangestate.json"));
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		Alert alert = alertApi.changeState("650d5726-xxxx-4e8c-0000-504d577da210", "Closed");
		assertNotNull(alert);
		assertSent(server, "POST",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/650d5726-xxxx-4e8c-0000-504d577da210/changestate?newState=Closed&api-version=2019-03-01");
	}
	
	public void testAlertChangeStateReturns404() throws InterruptedException {
		server.enqueue(response404());
		final AlertApi alertApi = api.getAlertApi("resourceGroups/myResourceGroup");
		Alert alert = alertApi.changeState("650d5726-xxxx-4e8c-0000-504d577da210", "Closed");
		assertNull(alert);
		assertSent(server, "POST",
				"/subscriptions/SUBSCRIPTIONID/resourceGroups/myResourceGroup/providers/Microsoft.AlertsManagement/alerts/650d5726-xxxx-4e8c-0000-504d577da210/changestate?newState=Closed&api-version=2019-03-01");
		
	}	
}
