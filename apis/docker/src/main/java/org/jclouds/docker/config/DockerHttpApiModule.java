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
package org.jclouds.docker.config;

import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.jclouds.docker.DockerApi;
import org.jclouds.docker.handlers.DockerErrorHandler;
import org.jclouds.docker.suppliers.DockerUntrustedSSLSocketFactorySupplier;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.annotation.ClientError;
import org.jclouds.http.annotation.Redirection;
import org.jclouds.http.annotation.ServerError;
import org.jclouds.http.config.ConfiguresHttpCommandExecutorService;
import org.jclouds.http.okhttp.OkHttpClientSupplier;
import org.jclouds.http.okhttp.config.OkHttpCommandExecutorServiceModule;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.config.HttpApiModule;

import javax.net.ssl.SSLSocketFactory;

/**
 * Configures the Docker connection.
 */
@ConfiguresHttpApi
@ConfiguresHttpCommandExecutorService
public class DockerHttpApiModule extends HttpApiModule<DockerApi> {

   @Override
   protected void bindErrorHandlers() {
      bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(DockerErrorHandler.class);
      bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(DockerErrorHandler.class);
      bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(DockerErrorHandler.class);
   }

   /**
    * This configures SSL certificate authentication when the Docker daemon is set to use an encrypted TCP socket
    */
   @Override
   protected void configure() {
      super.configure();
      install(Modules.override(new OkHttpCommandExecutorServiceModule()).with(new AbstractModule() {
         @Override
         protected void configure() {
            bind(new TypeLiteral<Supplier<SSLSocketFactory>>() {})
                    .annotatedWith(Names.named("untrusted"))
                    .to(DockerUntrustedSSLSocketFactorySupplier.class);
         }
      }));
      bind(OkHttpClientSupplier.class).to(DockerOkHttpClientSupplier.class);
   }
}
