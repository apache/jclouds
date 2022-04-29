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
public abstract class MetricDimension {

	@Nullable
	public abstract String name();

	@Nullable
	public abstract String operator();

	@Nullable
	public abstract List<String> values();

	@SerializedNames({ "name", "operator", "values" })
	public static MetricDimension create(final String name, final String operator, final List<String> values) {
		return builder().name(name).operator(operator).values(values).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_MetricDimension.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder name(String name);

		public abstract Builder operator(String operator);

		public abstract Builder values(List<String> values);

		public abstract MetricDimension build();
	}
}
