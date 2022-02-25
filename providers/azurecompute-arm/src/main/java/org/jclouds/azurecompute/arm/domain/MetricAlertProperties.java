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

import java.util.Date;
import java.util.List;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MetricAlertProperties {

	@Nullable
	public abstract List<MetricAlertAction> actions();

	public abstract boolean autoMitigate();

	@Nullable
	public abstract Criteria criteria();

	@Nullable
	public abstract String description();

	public abstract boolean enabled();

	@Nullable
	public abstract String evaluationFrequency();

	public abstract boolean isMigrated();

	@Nullable
	public abstract Date lastUpdatedTime();

	@Nullable
	public abstract List<String> scopes();

	public abstract int severity();

	@Nullable
	public abstract String targetResourceRegion();

	@Nullable
	public abstract String targetResourceType();

	@Nullable
	public abstract String windowSize();

	@SerializedNames({ "actions", "autoMitigate", "criteria", "description", "enabled", "evaluationFrequency",
			"isMigrated", "lastUpdatedTime", "scopes", "severity", "targetResourceRegion", "targetResourceType",
			"windowSize" })
	public static MetricAlertProperties create(final List<MetricAlertAction> actions, final boolean autoMitigate,
			final Criteria criteria, final String description, final boolean enabled, final String evaluationFrequency,
			final boolean isMigrated, final Date lastUpdatedTime, final List<String> scopes, final int severity,
			final String targetResourceRegion, final String targetResourceType, final String windowSize) {
		return builder().actions(actions).criteria(criteria).autoMitigate(autoMitigate).description(description)
				.enabled(enabled).evaluationFrequency(evaluationFrequency).isMigrated(isMigrated)
				.lastUpdatedTime(lastUpdatedTime).scopes(scopes).severity(severity)
				.targetResourceRegion(targetResourceRegion).targetResourceType(targetResourceType)
				.windowSize(windowSize).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_MetricAlertProperties.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder autoMitigate(boolean autoMitigate);

		public abstract Builder description(String description);

		public abstract Builder enabled(boolean enabled);

		public abstract Builder evaluationFrequency(String evaluationFrequency);

		public abstract Builder isMigrated(boolean isMigrated);

		public abstract Builder lastUpdatedTime(Date lastUpdatedTime);

		public abstract Builder severity(int severity);

		public abstract Builder targetResourceRegion(String targetResourceRegion);

		public abstract Builder targetResourceType(String targetResourceType);

		public abstract Builder windowSize(String windowSize);

		public abstract Builder scopes(List<String> scopes);

		public abstract Builder actions(List<MetricAlertAction> actions);

		public abstract Builder criteria(Criteria criteria);

		public abstract MetricAlertProperties build();

	}
}
