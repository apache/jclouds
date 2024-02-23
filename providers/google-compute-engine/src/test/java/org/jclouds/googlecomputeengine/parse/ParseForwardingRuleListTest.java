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
package org.jclouds.googlecomputeengine.parse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.Consumes;

import org.jclouds.googlecloud.domain.ForwardingListPage;
import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecomputeengine.domain.ForwardingRule;
import org.jclouds.googlecomputeengine.internal.BaseGoogleComputeEngineParseTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

@Test(groups = "unit", testName = "ParseForwardingRuleListTest")
public class ParseForwardingRuleListTest extends BaseGoogleComputeEngineParseTest<ListPage<ForwardingRule>> {

   @Override
   public String resource() {
      return "/forwardingrule_list.json";
   }

   @Override @Consumes(APPLICATION_JSON)
   public ListPage<ForwardingRule> expected() {
      return expected(BASE_URL);
   }

   @Consumes(APPLICATION_JSON)
   public ListPage<ForwardingRule> expected(String baseUrl) {
      return ForwardingListPage.create( //
            ImmutableList.of(new ParseForwardingRuleTest().expected(baseUrl)), // items
            null // nextPageToken
      );
   }
}
