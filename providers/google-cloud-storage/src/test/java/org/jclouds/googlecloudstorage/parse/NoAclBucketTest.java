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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import org.jclouds.date.internal.SimpleDateFormatDateService;
import org.jclouds.googlecloudstorage.domain.Bucket;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.Location;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.StorageClass;
import org.jclouds.googlecloudstorage.domain.Owner;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageParseTest;

public class NoAclBucketTest extends BaseGoogleCloudStorageParseTest<Bucket> {

   @Override
   public String resource() {
      return "/no_acl_bucket.json";
   }

   @Override
   @Consumes(MediaType.APPLICATION_JSON)
   public Bucket expected() {
      return Bucket.create(
            "bhashbucket", // id
            "bhashbucket", // name
            1082289308625L, // projectNumber
            new SimpleDateFormatDateService().iso8601DateParse("2014-06-02T19:19:41.112z"), // timeCreated
            87L, // metageneration
            null, // acl
            null, // defaultObjectAcl
            Owner.create("project-owners-1082289308625", null), // owner
            Location.US, // location
            null, // website
            null, // logging
            null, // versioning
            null, // cors
            null, // lifeCycle
            StorageClass.STANDARD // storageClass
      );
   }
}
