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
package org.jclouds.aws.s3.blobstore.integration;

import org.jclouds.s3.S3Client;
import org.jclouds.s3.blobstore.integration.S3BlobIntegrationLiveTest;
import org.jclouds.s3.domain.PublicAccessBlockConfiguration;
import org.testng.annotations.Test;
import org.testng.SkipException;

@Test(groups = "live", testName = "AWSS3BlobIntegrationLiveTest")
public class AWSS3BlobIntegrationLiveTest extends S3BlobIntegrationLiveTest {
   public AWSS3BlobIntegrationLiveTest() {
      provider = "aws-s3";
   }

   @Override
   protected void allowPublicReadable(String containerName) {
      S3Client client = view.unwrapApi(S3Client.class);
      client.putBucketOwnershipControls(containerName, "ObjectWriter");
      client.putPublicAccessBlock(containerName, PublicAccessBlockConfiguration.create(
            /*blockPublicAcls=*/ false, /*ignorePublicAcls=*/ false, /*blockPublicPolicy=*/ false, /*restrictPublicBuckets=*/ false));
   }

   @Override
   public void testCopyIfModifiedSinceNegative() throws Exception {
      throw new SkipException("S3 supports copyIfModifiedSince but test uses time in the future which Amazon does not support");
   }
}
