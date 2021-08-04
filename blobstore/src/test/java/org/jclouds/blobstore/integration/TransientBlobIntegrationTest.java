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
package org.jclouds.blobstore.integration;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.integration.internal.BaseBlobIntegrationTest;
import org.testng.annotations.Test;
import org.testng.SkipException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = { "integration" })
public class TransientBlobIntegrationTest extends BaseBlobIntegrationTest {
   public TransientBlobIntegrationTest() {
      provider = "transient";
   }

   @Override
   @Test(groups = { "integration", "live" })
   public void testSetBlobAccess() throws Exception {
      throw new SkipException("transient does not support anonymous access");
   }

   @Override
   protected void checkMPUParts(Blob blob, List<MultipartPart> parts) {
      assertThat(blob.getMetadata().getETag()).endsWith(String.format("-%d\"", parts.size()));
      Hasher eTagHasher = Hashing.md5().newHasher();
      for (MultipartPart part : parts) {
         eTagHasher.putBytes(BaseEncoding.base16().lowerCase().decode(part.partETag()));
      }
      String expectedETag = new StringBuilder("\"")
              .append(eTagHasher.hash())
              .append("-")
              .append(parts.size())
              .append("\"")
              .toString();
      assertThat(blob.getMetadata().getETag()).isEqualTo(expectedETag);
   }
}
