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
package org.jclouds.azurecompute.arm.domain;

import org.jclouds.azurecompute.arm.util.GetEnumValue;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Essentials {

	public enum AlertState {
		New, Acknowledged, Closed;

		public static AlertState fromValue(final String text) {
			return (AlertState) GetEnumValue.fromValueOrDefault(text, AlertState.New);
		}
	}

	public enum MonitorCondition {
		Fired, Resloved;

		public static MonitorCondition fromValue(final String text) {
			return (MonitorCondition) GetEnumValue.fromValueOrDefault(text, MonitorCondition.Fired);
		}
	}

	@Nullable
	public abstract ActionStatus actionStatus();

	@Nullable
	public abstract String alertRule();

	@Nullable
	public abstract String lastModifiedDateTime();

	@Nullable
	public abstract String lastModifiedUserName();

	@Nullable
	public abstract String sourceCreatedId();

	@Nullable
	public abstract String startDateTime();

	@Nullable
	public abstract String targetResource();

	@Nullable
	public abstract String targetResourceGroup();

	@Nullable
	public abstract String targetResourceName();

	@Nullable
	public abstract String targetResourceType();

	@Nullable
	public abstract AlertState alertState();

	@Nullable
	public abstract MonitorCondition monitorCondition();

	@Nullable
	public abstract String monitorService();

	@Nullable
	public abstract String severity();

	@Nullable
	public abstract String signalType();

	@SerializedNames({ "actionStatus", "alertRule", "lastModifiedDateTime", "lastModifiedUserName", "sourceCreatedId",
			"startDateTime", "targetResource", "targetResourceGroup", "targetResourceName", "targetResourceType",
			"alertState", "monitorCondition", "monitorService", "severity", "signalType" })
	public static Essentials create(final ActionStatus actionStatus, final String alertRule,
			final String lastModifiedDateTime, final String lastModifiedUserName, final String sourceCreatedId,
			final String startDateTime, final String targetResource, final String targetResourceGroup,
			final String targetResourceName, final String targetResourceType, final AlertState alertState,
			final MonitorCondition monitorCondition, final String monitorService, final String severity,
			final String signalType) {
		return new AutoValue_Essentials(actionStatus, alertRule, lastModifiedDateTime, lastModifiedUserName,
				sourceCreatedId, startDateTime, targetResource, targetResourceGroup, targetResourceName,
				targetResourceType, alertState, monitorCondition, monitorService, severity, signalType);

	}

}
