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
package org.jclouds.googlecomputeengine.features;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.binders.RouteBinder;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.domain.Route;
import org.jclouds.googlecomputeengine.internal.BaseToIteratorOfListPage;
import org.jclouds.googlecomputeengine.options.ListOptions;
import org.jclouds.googlecomputeengine.options.RouteOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.filters.OAuthFilter;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.MapBinder;
import org.jclouds.rest.annotations.PayloadParam;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.annotations.Transform;

import com.google.common.base.Function;

@SkipEncoding({'/', '='})
@RequestFilters(OAuthFilter.class)
@Path("/routes")
@Consumes(APPLICATION_JSON)
public interface RouteApi {

   /** Returns a route type by name or null if not found. */
   @Named("Routes:get")
   @GET
   @Path("/{route}")
   @Fallback(NullOnNotFoundOr404.class)
   Route get(@PathParam("route") String routeName);

   /** Deletes a route by name and returns the operation in progress, or null if not found. */
   @Named("Routes:delete")
   @DELETE
   @Path("/{route}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Operation delete(@PathParam("route") String routeName);

   /**
    * Creates a route resource in the specified project using the data included in the request.
    *
    * @param name            the name of the route to be inserted.
    * @param network         the network to which to add the route
    * @param routeOptions the options of the route to add
    * @return an Operation resource. To check on the status of an operation, poll the Operations resource returned to
    *         you, and look for the status field.
    */
   @Named("Routes:insert")
   @POST
   @Produces(APPLICATION_JSON)
   @MapBinder(RouteBinder.class)
   Operation createInNetwork(@PayloadParam("name") String name,
                             @PayloadParam("network") URI network,
                             @PayloadParam("options") RouteOptions routeOptions);

   /**
    * Retrieves the list of route resources available to the specified project.
    * By default the list as a maximum size of 100, if no options are provided or ListOptions#getMaxResults() has not
    * been set.
    *
    * @param pageToken   marks the beginning of the next list page
    * @param listOptions listing options
    * @return a page of the list
    */
   @Named("Routes:list")
   @GET
   ListPage<Route> listPage(@Nullable @QueryParam("pageToken") String pageToken, ListOptions listOptions);

   /** @see #listPage(String, ListOptions) */
   @Named("Routes:list")
   @GET
   @Transform(RoutePages.class)
   Iterator<ListPage<Route>> list();

   /** @see #listPage(String, ListOptions) */
   @Named("Routes:list")
   @GET
   @Transform(RoutePages.class)
   Iterator<ListPage<Route>> list(ListOptions options);

   static final class RoutePages extends BaseToIteratorOfListPage<Route, RoutePages> {

      private final GoogleComputeEngineApi api;

      @Inject RoutePages(GoogleComputeEngineApi api) {
         this.api = api;
      }

      @Override protected Function<String, ListPage<Route>> fetchNextPage(final ListOptions options) {
         return new Function<String, ListPage<Route>>() {
            @Override public ListPage<Route> apply(String pageToken) {
               return api.routes().listPage(pageToken, options);
            }
         };
      }
   }
}
