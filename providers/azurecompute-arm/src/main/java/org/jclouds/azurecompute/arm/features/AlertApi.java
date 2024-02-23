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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.azurecompute.arm.domain.Alert;
import org.jclouds.azurecompute.arm.domain.AlertModification;
import org.jclouds.azurecompute.arm.domain.AlertSummary;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.azurecompute.arm.options.AlertRequestOptions;
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
	@Named("alerts:getAll")
	@Path("/providers/Microsoft.AlertsManagement/alerts")
	@GET
	@SelectJson("value")
	@Fallback(EmptyListOnNotFoundOr404.class)
	List<Alert> list(@Nullable AlertRequestOptions... getAllOptions);

	@Named("alerts:getbyid")
	@Path("/providers/Microsoft.AlertsManagement/alerts/{alertId}")
	@GET
	@Fallback(NullOnNotFoundOr404.class)
	Alert get(@PathParam("alertId") String alertId);

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
	AlertSummary getSummary(AlertRequestOptions... getSummaryOptions);
}
