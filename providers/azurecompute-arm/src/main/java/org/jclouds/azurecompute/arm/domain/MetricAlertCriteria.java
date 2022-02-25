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

import org.jclouds.azurecompute.arm.util.GetEnumValue;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MetricAlertCriteria {

	@Nullable
	public abstract String criterionType();

	@Nullable
	public abstract List<MetricDimension> dimensions();

	@Nullable
	public abstract String metricName();

	@Nullable
	public abstract String metricNamespace();

	@Nullable
	public abstract String name();

	@Nullable
	public abstract Operator operator();

	public abstract boolean skipMetricValidation();

	public abstract int threshold();

	@Nullable
	public abstract AggregationTypeEnum timeAggregation();

	@Nullable
	public abstract DynamicThresholdSensitivity alertSensitivity();

	@Nullable
	public abstract DynamicThresholdFailingPeriods failingPeriods();

	@Nullable
	public abstract String ignoreDataBefore();

	@Nullable
	public abstract String componentId();

	public abstract int failedLocationCount();

	@Nullable
	public abstract String webTestId();

	@SerializedNames({ "criterionType", "dimensions", "metricName", "metricNamespace", "name", "operator",
			"skipMetricValidation", "threshold", "timeAggregation", "alertSensitivity", "failingPeriods",
			"ignoreDataBefore", "componentId", "failedLocationCount", "webTestId" })
	public static MetricAlertCriteria create(final String criterionType, List<MetricDimension> dimensions,
			final String metricName, final String metricNamespace, final String name, final Operator operator,
			final boolean skipMetricValidation, final int threshold, final AggregationTypeEnum timeAggregation,
			final DynamicThresholdSensitivity alertSensitivity, final DynamicThresholdFailingPeriods failingPeriods,
			final String ignoreDataBefore, final String componentId, final int failedLocationCount,
			final String webTestId) {
		return new AutoValue_MetricAlertCriteria(criterionType, dimensions, metricName, metricNamespace, name, operator,
				skipMetricValidation, threshold, timeAggregation, alertSensitivity, failingPeriods, ignoreDataBefore,
				componentId, failedLocationCount, webTestId);

	}

	public enum AggregationTypeEnum {
		Average, Count, Maximum, Minimum, Total;
		public static AggregationTypeEnum fromValue(final String text) {
			return (AggregationTypeEnum) GetEnumValue.fromValueOrDefault(text, AggregationTypeEnum.Average);
		}
	}

	public enum Operator {
		Equals, GreaterThan, GreaterThanOrEqual, LessThan, LessThanOrEqual, GreaterOrLessThan;
		public static Operator fromValue(final String text) {
			return (Operator) GetEnumValue.fromValueOrDefault(text, Operator.Equals);
		}
	}

	public enum DynamicThresholdSensitivity {
		High, Low, Medium;
		public static DynamicThresholdSensitivity fromValue(final String text) {
			return (DynamicThresholdSensitivity) GetEnumValue.fromValueOrDefault(text,
					DynamicThresholdSensitivity.High);
		}
	}

	public enum DynamicThresholdOperator {
		GreaterOrLessThan, GreaterThan, LessThan;
		public static DynamicThresholdOperator fromValue(final String text) {
			return (DynamicThresholdOperator) GetEnumValue.fromValueOrDefault(text,
					DynamicThresholdOperator.GreaterOrLessThan);
		}
	}
}
