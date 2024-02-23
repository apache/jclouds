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

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.ObjectRole;
import org.jclouds.googlecloudstorage.domain.ObjectAccessControls;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageParseTest;
import org.jclouds.rest.annotations.SelectJson;

public class ObjectAclListTest extends BaseGoogleCloudStorageParseTest<List<ObjectAccessControls>> {

   private ObjectAccessControls item1 = ObjectAccessControls
            .builder()
            .id("jcloudstestbucket/foo.txt/1394121608485000/user-00b4903a97adfde729f0650133a7379693099d8d85d6b1b18255ca70bf89e31d")
            .bucket("jcloudstestbucket").object("foo.txt").generation(Long.valueOf("1394121608485000"))
            .entity("user-00b4903a97adfde729f0650133a7379693099d8d85d6b1b18255ca70bf89e31d")
            .entityId("00b4903a97adfde729f0650133a7379693099d8d85d6b1b18255ca70bf89e31d").role(ObjectRole.OWNER)
            .build();

   @Override
   public String resource() {
      return "/object_acl_list.json";
   }

   @Override
   @Consumes(MediaType.APPLICATION_JSON)
   @SelectJson("items")
   public List<ObjectAccessControls> expected() {
      return Arrays.asList(item1);
   }
}
