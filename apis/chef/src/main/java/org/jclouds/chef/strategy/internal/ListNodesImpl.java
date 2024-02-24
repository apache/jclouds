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

import jakarta.annotation.Resource;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.chef.domain.Node;
import org.jclouds.chef.strategy.ListNodes;
import org.jclouds.logging.Logger;

import java.util.concurrent.ExecutorService;

@Singleton
public class ListNodesImpl extends BaseListNodesImpl implements ListNodes {


   @Resource
   @Named(ChefProperties.CHEF_LOGGER)
   protected Logger logger = Logger.NULL;

   @Inject
   ListNodesImpl(ChefApi api) {
      super(api);
   }

   @Override
   public Iterable<? extends Node> execute() {
      return super.execute(api.listNodes());
   }

   @Override
   public Iterable<? extends Node> execute(ExecutorService executor) {
      return this.executeConcurrently(MoreExecutors.listeningDecorator(executor));
   }


   private Iterable<? extends Node> executeConcurrently(ListeningExecutorService executor) {
      return super.executeConcurrently(executor, api.listNodes());
   }

}
