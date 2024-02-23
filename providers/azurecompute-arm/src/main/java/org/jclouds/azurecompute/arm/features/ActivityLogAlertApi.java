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

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlert;
import org.jclouds.azurecompute.arm.domain.ActivityLogAlertProperties;
import org.jclouds.azurecompute.arm.filters.ApiVersionFilter;
import org.jclouds.azurecompute.arm.functions.URIParser;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.QueryParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.binders.BindToJsonPayload;

/**
 * The Activity Log Alert API includes operations for managing activity log type
 * alert rules in your subscription.
 *
 * @see <a href=
 *      "https://docs.microsoft.com/en-us/rest/api/monitor/activity-log-alerts">docs</a>
 */
@Path("/resourcegroups/{resourcegroup}/providers/Microsoft.Insights/activityLogAlerts")
@RequestFilters({ OAuthFilter.class, ApiVersionFilter.class })
@Consumes(MediaType.APPLICATION_JSON)
public interface ActivityLogAlertApi {

	@Named("activitylogalert:get")
	@GET
	@Path("/{name}")
	@Fallback(Fallbacks.NullOnNotFoundOr404.class)
	ActivityLogAlert get(@PathParam("name") String name);

	@Named("activitylogalert:list")
	@GET
	@SelectJson("value")
	@Fallback(Fallbacks.EmptyListOnNotFoundOr404.class)
	List<ActivityLogAlert> list();

	@Named("activitylogalert:createOrUpdate")
	@PUT
	@MapBinder(BindToJsonPayload.class)
	@Path("/{activityLogAlertName}")
	@QueryParams(keys = "validating", values = "false")
	ActivityLogAlert createOrUpdate(@PathParam("activityLogAlertName") String activityLogAlertName,
			@PayloadParam("properties") ActivityLogAlertProperties properties,
			@PayloadParam("tags") Map<String, String> tags, @PayloadParam("location") String location);

	@Named("activitylogalert:delete")
	@DELETE
	@Path("/{activityLogAlertName}")
	@ResponseParser(URIParser.class)
	@Fallback(Fallbacks.NullOnNotFoundOr404.class)
	URI delete(@PathParam("activityLogAlertName") String activityLogAlertName);

}
