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

import org.jclouds.azurecompute.arm.util.GetEnumValue;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AlertModificationItem {

	public enum AlertModificationEvent {
		AlertCreated, MonitorConditionChange, StateChange;

		public static AlertModificationEvent fromValue(final String text) {
			return (AlertModificationEvent) GetEnumValue.fromValueOrDefault(text, AlertModificationEvent.AlertCreated);
		}
	}

	public abstract String comments();

	public abstract String description();

	public abstract AlertModificationEvent modificationEvent();

	public abstract Date modifiedAt();

	public abstract String modifiedBy();

	public abstract String newValue();

	public abstract String oldValue();

	@SerializedNames({ "comments", "description", "modificationEvent", "modifiedAt", "modifiedBy", "newValue",
			"oldValue" })
	public static AlertModificationItem create(final String comments, final String description,
			final AlertModificationEvent modificationEvent, final Date modifiedAt, final String modifiedBy,
			final String newValue, final String oldValue) {
		return builder().comments(comments).description(description).modificationEvent(modificationEvent)
				.modifiedAt(modifiedAt).modifiedBy(modifiedBy).newValue(newValue).oldValue(oldValue).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder() {
		return new AutoValue_AlertModificationItem.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder comments(String comments);

		public abstract Builder description(String description);

		public abstract Builder modificationEvent(AlertModificationEvent modificationEvent);

		public abstract Builder modifiedAt(Date modifiedAt);

		public abstract Builder modifiedBy(String modifiedBy);

		public abstract Builder newValue(String newValue);

		public abstract Builder oldValue(String oldValue);

		public abstract AlertModificationItem build();

	}
}
