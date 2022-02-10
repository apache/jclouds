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
package org.jclouds.azurecompute.arm.options;

import static com.google.common.base.Preconditions.checkState;

import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.TARGET_RESOURCE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.TARGET_RESOURCE_GROUP;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.TARGET_RESOURCE_TYPE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.MONITOR_SERVICE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.MONITOR_CONDITION;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.SERVERITY;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.ALERT_RULE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.ALERT_STATE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.SMART_GROUP_ID;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.INCLUDE_CONTEXT;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.INCLUDE_EGRESS_CONFIG;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.PAGE_COUNT;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.SORT_BY;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.SORT_ORDER;

import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.SELECT;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.TIME_RANGE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.CUSTOM_TIME_RANGE;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.GROUP_BY;
import static org.jclouds.azurecompute.arm.reference.AlertQueryParams.INCLUDE_SMART_GROUPS_COUNT;

import org.jclouds.http.options.BaseHttpRequestOptions;

public class AlertRequestOptions extends BaseHttpRequestOptions {

	public static final AlertRequestOptions NONE = new AlertRequestOptions();

	public AlertRequestOptions withTargetResource(String targetResource) {
		checkState(!queryParameters.containsKey(TARGET_RESOURCE), "Can't have duplicate parameter of targetResource");
		queryParameters.put(TARGET_RESOURCE, targetResource);
		return this;
	}

	public AlertRequestOptions withTargetResourceGroup(String targetResourceGroup) {
		checkState(!queryParameters.containsKey(TARGET_RESOURCE_GROUP),
				"Can't have duplicate parameter of targetResourceGroup");
		queryParameters.put(TARGET_RESOURCE_GROUP, targetResourceGroup);
		return this;
	}

	public AlertRequestOptions withTargetResourceType(String targetResourceType) {
		checkState(!queryParameters.containsKey(TARGET_RESOURCE_TYPE),
				"Can't have duplicate parameter of targetResourceType");
		queryParameters.put(TARGET_RESOURCE_TYPE, targetResourceType);
		return this;
	}

	public AlertRequestOptions withMonitorService(String monitorService) {
		checkState(!queryParameters.containsKey(MONITOR_SERVICE), "Can't have duplicate parameter of monitorService");
		queryParameters.put(MONITOR_SERVICE, monitorService);
		return this;
	}

	public AlertRequestOptions withMonitorCondition(String monitorCondition) {
		checkState(!queryParameters.containsKey(MONITOR_CONDITION),
				"Can't have duplicate parameter of monitorCondition");
		queryParameters.put(MONITOR_CONDITION, monitorCondition);
		return this;
	}

	public AlertRequestOptions withSeverity(String severity) {
		checkState(!queryParameters.containsKey(SERVERITY), "Can't have duplicate parameter of severity");
		queryParameters.put(SERVERITY, severity);
		return this;
	}

	public AlertRequestOptions withAlertState(String alertState) {
		checkState(!queryParameters.containsKey(ALERT_STATE), "Can't have duplicate parameter of alertState");
		queryParameters.put(ALERT_STATE, alertState);
		return this;
	}

	public AlertRequestOptions withAlertRule(String alertRule) {
		checkState(!queryParameters.containsKey(ALERT_RULE), "Can't have duplicate parameter of alertRule");
		queryParameters.put(ALERT_RULE, alertRule);
		return this;
	}

	public AlertRequestOptions withSmartGroupId(String smartGroupId) {
		checkState(!queryParameters.containsKey(SMART_GROUP_ID), "Can't have duplicate parameter of smartGroupId");
		queryParameters.put(SMART_GROUP_ID, smartGroupId);
		return this;
	}

	public AlertRequestOptions withIncludeContext(Boolean includeContext) {
		checkState(!queryParameters.containsKey(INCLUDE_CONTEXT), "Can't have duplicate parameter of includeContext");
		queryParameters.put(INCLUDE_CONTEXT, String.valueOf(includeContext));
		return this;
	}

	public AlertRequestOptions withIncludeEgressConfig(Boolean includeEgressConfig) {
		checkState(!queryParameters.containsKey(INCLUDE_EGRESS_CONFIG),
				"Can't have duplicate parameter of includeEgressConfig");
		queryParameters.put(INCLUDE_EGRESS_CONFIG, String.valueOf(includeEgressConfig));
		return this;
	}

	public AlertRequestOptions withPageCount(Integer pageCount) {
		checkState(!queryParameters.containsKey(PAGE_COUNT), "Can't have duplicate parameter of pageCount");
		queryParameters.put(PAGE_COUNT, String.valueOf(pageCount));
		return this;
	}

	public AlertRequestOptions withSortBy(String sortBy) {
		checkState(!queryParameters.containsKey(SORT_BY), "Can't have duplicate parameter of sortBy");
		queryParameters.put(SORT_BY, sortBy);
		return this;
	}

	public AlertRequestOptions withSortOrder(String sortOrder) {
		checkState(!queryParameters.containsKey(SORT_ORDER), "Can't have duplicate parameter of sortOrder");
		queryParameters.put(SORT_ORDER, sortOrder);
		return this;
	}

	public AlertRequestOptions withSelect(String select) {
		checkState(!queryParameters.containsKey(SELECT), "Can't have duplicate parameter of select");
		queryParameters.put(SELECT, select);
		return this;
	}

	public AlertRequestOptions withTimeRange(String timeRange) {
		checkState(!queryParameters.containsKey(TIME_RANGE), "Can't have duplicate parameter of timeRange");
		queryParameters.put(TIME_RANGE, timeRange);
		return this;
	}

	public AlertRequestOptions withCustomTimeRange(String customTimeRange) {
		checkState(!queryParameters.containsKey(CUSTOM_TIME_RANGE),
				"Can't have duplicate parameter of customTimeRange");
		queryParameters.put(CUSTOM_TIME_RANGE, customTimeRange);
		return this;
	}

	public AlertRequestOptions withGroupBy(String groupby) {
		checkState(!queryParameters.containsKey(GROUP_BY), "Can't have duplicate parameter of groupby");
		queryParameters.put(GROUP_BY, groupby);
		return this;
	}

	public AlertRequestOptions withIncludeSmartGroupsCount(Boolean includeSmartGroupsCount) {
		checkState(!queryParameters.containsKey(INCLUDE_SMART_GROUPS_COUNT),
				"Can't have duplicate parameter of includeSmartGroupsCount");
		queryParameters.put(INCLUDE_SMART_GROUPS_COUNT, String.valueOf(includeSmartGroupsCount));
		return this;
	}

	/*
	 * This method is intended for testing
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		AlertRequestOptions options = (AlertRequestOptions) o;

		return buildQueryParameters().equals(options.buildQueryParameters());
	}

	@Override
	public int hashCode() {
		return buildQueryParameters().hashCode();
	}

	public static class Builder {

		public static AlertRequestOptions targetResource(String targetResource) {
			return new AlertRequestOptions().withTargetResource(targetResource);
		}

		public static AlertRequestOptions targetResourceGroup(String targetResourceGroup) {
			return new AlertRequestOptions().withTargetResourceGroup(targetResourceGroup);
		}

		public static AlertRequestOptions targetResourceGroupType(String targetResourceGroupType) {
			return new AlertRequestOptions().withTargetResourceType(targetResourceGroupType);
		}

		public static AlertRequestOptions monitorService(String monitorService) {
			return new AlertRequestOptions().withMonitorService(monitorService);
		}

		public static AlertRequestOptions monitorCondition(String monitorCondition) {
			return new AlertRequestOptions().withMonitorCondition(monitorCondition);
		}

		public static AlertRequestOptions severity(String severity) {
			return new AlertRequestOptions().withSeverity(severity);
		}

		public static AlertRequestOptions alertState(String alertState) {
			return new AlertRequestOptions().withAlertState(alertState);
		}

		public static AlertRequestOptions alertRule(String alerRule) {
			return new AlertRequestOptions().withAlertRule(alerRule);
		}

		public static AlertRequestOptions smartGroupId(String smartGroupId) {
			return new AlertRequestOptions().withSmartGroupId(smartGroupId);
		}

		public static AlertRequestOptions includeContext(Boolean includeContext) {
			return new AlertRequestOptions().withIncludeContext(includeContext);
		}

		public static AlertRequestOptions includeEgressConfig(Boolean includeEgressConfig) {
			return new AlertRequestOptions().withIncludeEgressConfig(includeEgressConfig);
		}

		public static AlertRequestOptions pageCount(Integer pageCount) {
			return new AlertRequestOptions().withPageCount(pageCount);
		}

		public static AlertRequestOptions sortBy(String sortBy) {
			return new AlertRequestOptions().withSortBy(sortBy);
		}

		public static AlertRequestOptions sortOrder(String sortOrder) {
			return new AlertRequestOptions().withSortOrder(sortOrder);
		}

		public static AlertRequestOptions select(String select) {
			return new AlertRequestOptions().withSelect(select);
		}

		public static AlertRequestOptions timeRange(String timeRange) {
			return new AlertRequestOptions().withTimeRange(timeRange);
		}

		public static AlertRequestOptions customTimeRange(String customTimeRange) {
			return new AlertRequestOptions().withCustomTimeRange(customTimeRange);
		}

		public static AlertRequestOptions groupBy(String groupBy) {
			return new AlertRequestOptions().withGroupBy(groupBy);
		}

		public static AlertRequestOptions includeSmartGroupsCount(Boolean includeSmartGroupsCount) {
			return new AlertRequestOptions().withIncludeSmartGroupsCount(includeSmartGroupsCount);
		}

	}

}
