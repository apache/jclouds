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
package org.jclouds.openstack.neutron.v2.functions.lbaas.v1;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.http.functions.ParseJson;
import org.jclouds.json.Json;
import org.jclouds.openstack.neutron.v2.domain.lbaas.v1.VIPs;

import com.google.inject.TypeLiteral;

/**
 * Used by jclouds to provide more specific collections and fallbacks.
 */
@Singleton
public class ParseVIPs extends ParseJson<VIPs> {

   @Inject
   public ParseVIPs(Json json) {
      super(json, TypeLiteral.get(VIPs.class));
   }
}
