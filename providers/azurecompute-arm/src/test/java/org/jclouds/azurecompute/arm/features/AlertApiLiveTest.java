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

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jclouds.azurecompute.arm.domain.Alert;
import org.jclouds.azurecompute.arm.domain.AlertModification;
import org.jclouds.azurecompute.arm.domain.AlertSummary;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiLiveTest;
import org.jclouds.azurecompute.arm.options.AlertRequestOptions;
import org.testng.annotations.Test;

@Test(groups = "live", testName = "AlertApiLiveTest", singleThreaded = true)
public class AlertApiLiveTest extends BaseAzureComputeApiLiveTest {

	private String alertId;

	private AlertApi alertApi() {
		return api.getAlertApi("");
	}

	@Test
	public void testList() {
		AlertRequestOptions pageCount = AlertRequestOptions.Builder.pageCount(1);
		List<Alert> result = alertApi().list(pageCount);
		System.out.println(result.size());
		assertNotNull(result);
		assertTrue(result.size() > 0);
		final String id = result.get(0).id();
		alertId = id.substring(id.lastIndexOf("/") + 1);
	}

	@Test(dependsOnMethods = "testList")
	public void testGetById() {
		Alert alert = alertApi().get(alertId);
		assertNotNull(alert);
	}

	@Test(dependsOnMethods = "testList")
	public void testGetHistory() {
		AlertModification history = alertApi().getHistory(alertId);
		assertNotNull(history);
	}

	@Test(dependsOnMethods = "testList")
	public void testGetSummary() {
		AlertRequestOptions groupByOption = AlertRequestOptions.Builder.groupBy("severity");
		AlertSummary summary = alertApi().getSummary(groupByOption);
		assertNotNull(summary);
	}

	@Test(dependsOnMethods = "testList")
	public void testAlertChangeState() {
		Alert alert = alertApi().changeState(alertId, "Closed");
		assertNotNull(alert);
		assertEquals("Closed", alertApi().get(alertId).properties().essentials().alertState().name());
	}
}
