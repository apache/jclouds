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

import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AlertSummary {

	public abstract String id();

	public abstract String name();

	public abstract String type();

	public abstract AlertSummaryGroup properties();

	@SerializedNames({ "id", "name", "type", "properties" })
	public static AlertSummary create(final String id, final String name, final String type,
			final AlertSummaryGroup properties) {
		return builder().id(id).name(name).type(type).properties(properties).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_AlertSummary.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder id(String id);

		public abstract Builder name(String name);

		public abstract Builder type(String type);

		public abstract Builder properties(AlertSummaryGroup properties);

		public abstract AlertSummary build();

	}
}
