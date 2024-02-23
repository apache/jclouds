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
package org.jclouds.chef.strategy.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.getUnchecked;

import java.util.List;
import java.util.concurrent.Callable;

import jakarta.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.chef.domain.Client;
import org.jclouds.chef.strategy.ListClients;
import org.jclouds.logging.Logger;

import java.util.concurrent.ExecutorService;


@Singleton
public class ListClientsImpl implements ListClients {

   protected final ChefApi api;
   @Resource
   @Named(ChefProperties.CHEF_LOGGER)
   protected Logger logger = Logger.NULL;

   @Inject
   ListClientsImpl(ChefApi api) {
      this.api = checkNotNull(api, "api");
   }

   @Override
   public Iterable<? extends Client> execute() {

      Iterable<String> toGet = api.listClients();
      Iterable<? extends Client> clients = transform(toGet,
            new Function<String, Client>() {
               @Override
               public Client apply(final String input) {

                  return api.getClient(input);
               }

            }
      );

      logger.trace(String.format("getting clients: %s", Joiner.on(',').join(toGet)));
      return clients;

   }

   @Override
   public Iterable<? extends Client> execute(ExecutorService executorService) {
      return this.execute(MoreExecutors.listeningDecorator(executorService));
   }


   private Iterable<? extends Client> execute(ListeningExecutorService listeningExecutor) {
      return executeConcurrently(listeningExecutor, api.listClients());
   }

   private Iterable<? extends Client> executeConcurrently(final ListeningExecutorService executor,
         Iterable<String> toGet) {
      ListenableFuture<List<Client>> futures = allAsList(transform(toGet,
            new Function<String, ListenableFuture<Client>>() {
               @Override
               public ListenableFuture<Client> apply(final String input) {
                  return executor.submit(new Callable<Client>() {
                     @Override
                     public Client call() throws Exception {
                        return api.getClient(input);
                     }
                  });
               }
            }
      ));

      logger.trace(String.format("getting clients: %s", Joiner.on(',').join(toGet)));
      return getUnchecked(futures);
   }

}
