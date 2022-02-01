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
public abstract class AlertRuleAnyOfOrLeafCondition {

	@Nullable
	public abstract List<AlertRuleLeafCondition> anyOf();

	@Nullable
	public abstract List<String> containsAny();

	@Nullable
	public abstract String equals();

	@Nullable
	public abstract String field();

	@SerializedNames({ "anyOf", "containsAny", "equals", "field" })
	public static AlertRuleAnyOfOrLeafCondition create(final List<AlertRuleLeafCondition> anyOf,
			final List<String> containsAny, final String equals, final String field) {
		return builder().anyOf(anyOf).containsAny(containsAny).equals(equals).field(field).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_AlertRuleAnyOfOrLeafCondition.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder anyOf(List<AlertRuleLeafCondition> anyOf);

		public abstract Builder containsAny(List<String> containsAny);

		public abstract Builder equals(String equals);

		public abstract Builder field(String field);

		public abstract AlertRuleAnyOfOrLeafCondition build();

	}

}
