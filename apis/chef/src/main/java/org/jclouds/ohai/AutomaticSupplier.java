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
package org.jclouds.ohai;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.domain.JsonBall;
import org.jclouds.ohai.functions.NestSlashKeys;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;

@Singleton
public class AutomaticSupplier implements Supplier<Map<String, JsonBall>> {
   private final Multimap<String, Supplier<JsonBall>> autoAttrs;
   private final NestSlashKeys nester;

   @Inject
   AutomaticSupplier(@Automatic Multimap<String, Supplier<JsonBall>> autoAttrs, NestSlashKeys nester) {
      this.autoAttrs = checkNotNull(autoAttrs, "autoAttrs");
      this.nester = checkNotNull(nester, "nester");
   }

   @Override
   public Map<String, JsonBall> get() {
      return nester.apply(autoAttrs);
   }

}
