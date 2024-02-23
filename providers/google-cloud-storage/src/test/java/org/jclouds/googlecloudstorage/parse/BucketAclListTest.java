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
package org.jclouds.googlecloudstorage.parse;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.googlecloudstorage.domain.BucketAccessControls;
import org.jclouds.googlecloudstorage.domain.BucketAccessControls.Role;
import org.jclouds.googlecloudstorage.domain.ProjectTeam;
import org.jclouds.googlecloudstorage.domain.ProjectTeam.Team;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageParseTest;
import org.jclouds.rest.annotations.SelectJson;

import com.google.common.collect.ImmutableList;

public class BucketAclListTest extends BaseGoogleCloudStorageParseTest<List<BucketAccessControls>> {

   private BucketAccessControls item_1 = BucketAccessControls.builder().id("jcloudstestbucket/allUsers")
            .bucket("jcloudstestbucket").entity("allUsers").role(Role.READER).build();

   private BucketAccessControls item_2 = BucketAccessControls
            .builder()
            .id("jcloudstestbucket/project-owners-1082289308625")
            .projectTeam(ProjectTeam.create("1082289308625", Team.OWNERS))
            .bucket("jcloudstestbucket").entity("project-owners-1082289308625").role(Role.OWNER).build();

   @Override
   public String resource() {
      return "/bucket_acl_list.json";
   }

   @Override
   @Consumes(MediaType.APPLICATION_JSON)
   @SelectJson("items")
   public List<BucketAccessControls> expected() {
      return ImmutableList.of(item_1, item_2);
   }
}
