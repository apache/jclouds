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
package org.jclouds.azurecompute.arm.reference;

public final class AlertQueryParams {

	public static final String TARGET_RESOURCE = "targetResource";
	public static final String TARGET_RESOURCE_GROUP = "targetResourceGroup";
	public static final String TARGET_RESOURCE_TYPE = "targetResourceGroupType";
	public static final String MONITOR_SERVICE = "monitorService";
	public static final String MONITOR_CONDITION = "monitorCondition";
	public static final String SERVERITY = "severity";
	public static final String ALERT_STATE = "alertState";
	public static final String ALERT_RULE = "alertRule";
	public static final String SMART_GROUP_ID = "smartGroupId";
	public static final String INCLUDE_SMART_GROUPS_COUNT = "includeSmartGroupsCount";
	public static final String INCLUDE_CONTEXT = "includeContext";
	public static final String INCLUDE_EGRESS_CONFIG = "includeEgressConfig";
	public static final String PAGE_COUNT = "pageCount";
	public static final String SORT_BY = "sortBy";
	public static final String SORT_ORDER = "sortOrder";
	public static final String SELECT = "select";
	public static final String TIME_RANGE = "timeRange";
	public static final String CUSTOM_TIME_RANGE = "customTimeRange";
	public static final String GROUP_BY = "groupby";

	private AlertQueryParams() {
		throw new AssertionError("intentionally unimplemented");
	}
}
