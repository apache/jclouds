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
package org.jclouds.docker.features;

import java.io.InputStream;
import java.util.List;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.docker.domain.Image;
import org.jclouds.docker.domain.ImageHistory;
import org.jclouds.docker.domain.ImageSummary;
import org.jclouds.docker.options.CreateImageOptions;
import org.jclouds.docker.options.DeleteImageOptions;
import org.jclouds.docker.options.ListImageOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.Fallback;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/v{jclouds.api-version}")
public interface ImageApi {

   /**
    * @return the images available.
    */
   @Named("images:list")
   @GET
   @Path("/images/json")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<ImageSummary> listImages();

   /**
    * @param options the configuration to list images (@see ListImageOptions)
    * @return the images available.
    */
   @Named("images:list")
   @GET
   @Path("/images/json")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<ImageSummary> listImages(ListImageOptions options);

   /**
    * Return low-level information on the image with given name. Not all fields from the returned {@link Image} instance
    * are populated by this method (e.g. {@link Image#repoTags()}).
    *
    * @param imageName The id of the image to inspect.
    * @return low-level information on the image name
    */
   @Named("image:inspect")
   @GET
   @Path("/images/{name}/json")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   Image inspectImage(@PathParam("name") String imageName);

   /**
    * Create an image, either by pull it from the registry or by importing it
    *
    * @param options the configuration to create an image (@see CreateImageOptions)
    * @return a stream of the image creation.
    */
   @Named("image:create")
   @POST
   @Path("/images/create")
   InputStream createImage(CreateImageOptions options);

   /**
    * @param name the image name to be deleted
    * @return the stream of the deletion execution.
    */
   @Named("image:delete")
   @DELETE
   @Path("/images/{name}")
   InputStream deleteImage(@PathParam("name") String name);

   /**
    * @param name the name of the image to be removed
    * @param options the image deletion's options (@see DeleteImageOptions)
    * @return the stream of the deletion execution.
    */
   @Named("image:delete")
   @DELETE
   @Path("/images/{name}")
   InputStream deleteImage(@PathParam("name") String name, DeleteImageOptions options);

   /**
    * Tag the image name into a repository.
    *
    * @param name
    *           the name of the image to be tagged
    * @param repoName
    *           the repository to tag in
    * @param tag
    *           the new tag name
    * @param force
    *           force create if tag already exists
    */
   @Named("image:tag")
   @POST
   @Path("/images/{name}/tag")
   void tagImage(@PathParam("name") String name, @QueryParam("repo") String repoName,
         @QueryParam("tag") String tag, @QueryParam("force") boolean force);

   /**
    * Return the history of the image with given {@code name}.
    *
    * @param name
    *           the name of the image for which the history is retrieved
    */
   @Named("image:history")
   @GET
   @Path("/images/{name}/history")
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<ImageHistory> getHistory(@PathParam("name") String name);
}
