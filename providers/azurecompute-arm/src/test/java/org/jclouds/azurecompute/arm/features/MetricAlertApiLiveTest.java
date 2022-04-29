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

import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jclouds.azurecompute.arm.domain.Criteria;
import org.jclouds.azurecompute.arm.domain.MetricAlert;
import org.jclouds.azurecompute.arm.domain.MetricAlertCriteria;
import org.jclouds.azurecompute.arm.domain.MetricAlertCriteria.Operator;
import org.jclouds.azurecompute.arm.domain.MetricAlertProperties;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@Test(groups = "live", testName = "MetricAlertApiLiveTest", singleThreaded = true)
public class MetricAlertApiLiveTest extends BaseAzureComputeApiLiveTest {

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

	private MetricAlertApi api() {
		return api.getMetricAlertApi(resourceGroupName);
	}

	@Test
	public void testCreate() {
		MetricAlert metricAlert = api().createOrUpdate(alertRuleName, getMetricAlertProperties(),
				ImmutableMap.of("createdBy", "jclouds"), GLOBAL);
		assertTrue(!metricAlert.type().isEmpty());
	}
	
	@Test(dependsOnMethods = "testCreate")
	public void testGet() {
		final MetricAlert rule = api().get(alertRuleName);
		assertTrue(!rule.name().isEmpty());
	}

	@Test(dependsOnMethods = "testCreate")
	public void testList() {
		List<MetricAlert> list = api().list();
		final MetricAlert rule = api().get(alertRuleName);
		boolean alertRulePresent = Iterables.any(list, new Predicate<MetricAlert>() {
			@Override
			public boolean apply(@Nullable MetricAlert input) {
				return input.name().equals(rule.name());
			}
		});
		assertTrue(alertRulePresent);
	}
	
	@Test(dependsOnMethods = "testList", alwaysRun = true)
	public void testDelete() throws Exception {
		URI uri = api().delete(alertRuleName);
		assertResourceDeleted(uri);
	}

	public MetricAlertProperties getMetricAlertProperties() {
		final List<MetricAlertCriteria> lstMetricAlertCriteria = new ArrayList<>();
		lstMetricAlertCriteria.add(MetricAlertCriteria.create("StaticThresholdCriterion", null, "CPU Credits Consumed",
				"Microsoft.Compute/virtualMachines", "Metric1", Operator.GreaterThan, false, 0,
				MetricAlertCriteria.AggregationTypeEnum.Average, null, null, null, null, 0, null));
		final Criteria criteria = Criteria.create(lstMetricAlertCriteria,
				"Microsoft.Azure.Monitor.MultipleResourceMultipleMetricCriteria");
		return MetricAlertProperties.create(Collections.emptyList(), true, criteria, "", true, "PT5M", false, null,
				Arrays.asList("/subscriptions/" + subscriptionid + "/resourceGroups/" + resourceGroupName), 3, "eastus",
				"Microsoft.Compute/virtualMachines", "PT5M");
	}
}
