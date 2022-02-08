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

import java.util.List;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ActivityLogAlertProperties {

	@Nullable
	public abstract String description();

	public abstract boolean enabled();

	@Nullable
	public abstract List<String> scopes();

	@Nullable
	public abstract AlertRuleAllOfCondition condition();

	@Nullable
	public abstract Actions actions();

	@SerializedNames({ "description", "enabled", "scopes", "condition", "actions" })
	public static ActivityLogAlertProperties create(final String description, final boolean enabled,
			final List<String> scopes, final AlertRuleAllOfCondition condition, final Actions actions) {
		return builder().description(description).enabled(enabled).scopes(scopes).condition(condition).actions(actions)
				.build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_ActivityLogAlertProperties.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder description(String description);

		public abstract Builder enabled(boolean enabled);

		public abstract Builder scopes(List<String> scopes);

		public abstract Builder condition(AlertRuleAllOfCondition condition);

		public abstract Builder actions(Actions actions);

		public abstract ActivityLogAlertProperties build();

	}
}
