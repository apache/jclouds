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
package org.jclouds.azurecompute.arm.features;

import java.util.List;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.azurecompute.arm.domain.Alert;
import org.jclouds.azurecompute.arm.domain.AlertModification;
import org.jclouds.azurecompute.arm.domain.AlertSummary;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;

/**
 * This Azure Resource Manager API provides all the alerts available for a given
 * resource
 * <p/>
 *
 * @see <a href=
 *      "https://docs.microsoft.com/en-us/rest/api/monitor/alertsmanagement/alerts">docs</a>
 */
@Path("/{resourceid}")
@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(MediaType.APPLICATION_JSON)
public interface AlertApi {
	@Named("alerts:list")
	@Path("/providers/Microsoft.AlertsManagement/alerts")
	@GET
	@SelectJson("value")
	@Fallback(EmptyListOnNotFoundOr404.class)
	List<Alert> getAll(@Nullable @QueryParam("targetResource") String targetResource,
			@Nullable @QueryParam("targetResourceGroup") String targetResourceGroup,
			@Nullable @QueryParam("monitorService") String monitorService,
			@Nullable @QueryParam("monitorCondition") String monitorCondition,
			@Nullable @QueryParam("severity") String severity, @Nullable @QueryParam("alertState") String alertState,
			@Nullable @QueryParam("alertRule") String alertRule,
			@Nullable @QueryParam("smartGroupId") String smartGroupId,
			@Nullable @QueryParam("includeContext") String includeContext,
			@Nullable @QueryParam("includeEgressConfig") String includeEgressConfig,
			@Nullable @QueryParam("pageCount") String pageCount, @Nullable @QueryParam("sortBy") String sortBy,
			@Nullable @QueryParam("sortOrder") String sortOrder, @Nullable @QueryParam("select") String select,
			@Nullable @QueryParam("timeRange") String timeRange,
			@Nullable @QueryParam("customTimeRange") String customTimeRange);

	@Named("alerts:getbyid")
	@Path("/providers/Microsoft.AlertsManagement/alerts/{alertId}")
	@GET
	@Fallback(NullOnNotFoundOr404.class)
	Alert getById(@PathParam("alertId") String alertId);

	@Named("alerts:changestate")
	@Path("/providers/Microsoft.AlertsManagement/alerts/{alertId}/changestate")
	@POST
	@Fallback(NullOnNotFoundOr404.class)
	Alert changeState(@PathParam("alertId") String alertId, @QueryParam("newState") String newState);

	@Named("alerts:history")
	@Path("/providers/Microsoft.AlertsManagement/alerts/{alertId}/history")
	@GET
	@Fallback(NullOnNotFoundOr404.class)
	AlertModification getHistory(@PathParam("alertId") String alertId);

	@Named("alerts:summary")
	@Path("providers/Microsoft.AlertsManagement/alertsSummary")
	@GET
	@Fallback(NullOnNotFoundOr404.class)
	AlertSummary getSummary(@QueryParam("groupby") String groupby, @Nullable @QueryParam("alertRule") String alertRule,
			@Nullable @QueryParam("alertState") String alertState,
			@Nullable @QueryParam("customTimeRange") String customTimeRange,
			@Nullable @QueryParam("includeSmartGroupsCount") String includeSmartGroupsCount,
			@Nullable @QueryParam("monitorCondition") String monitorCondition,
			@Nullable @QueryParam("monitorService") String monitorService,
			@Nullable @QueryParam("severity") String severity,
			@Nullable @QueryParam("targetResource") String targetResource,
			@Nullable @QueryParam("targetResourceGroup") String targetResourceGroup,
			@Nullable @QueryParam("targetResourceType") String targetResourceType,
			@Nullable @QueryParam("timeRange") String timeRange);

}
