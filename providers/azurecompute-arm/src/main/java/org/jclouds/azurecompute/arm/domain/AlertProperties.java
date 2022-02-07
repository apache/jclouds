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

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AlertProperties {

	@Nullable
	public abstract Object context();

	@Nullable
	public abstract Object egressConfig();

	@Nullable
	public abstract Essentials essentials();

	@SerializedNames({ "context", "egressConfig", "essentials" })
	public static AlertProperties create(final Object context, final Object egressConfig, final Essentials essentials) {
		return builder().context(context).egressConfig(egressConfig).essentials(essentials).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_AlertProperties.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder context(Object provisioningState);

		public abstract Builder egressConfig(Object egressConfig);

		public abstract Builder essentials(Essentials essentials);

		public abstract AlertProperties build();

	}
}
