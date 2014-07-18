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
package org.jclouds.glacier.blobstore.strategy.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.glacier.util.TestUtils.MiB;
import static org.jclouds.glacier.util.TestUtils.GiB;
import static org.jclouds.glacier.util.TestUtils.buildPayload;

import org.jclouds.glacier.blobstore.strategy.PayloadSlice;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.internal.BasePayloadSlicer;
import org.testng.annotations.Test;

@Test(groups = {"unit"})
public class BaseSlicingStrategyTest {
   @Test
   public void slicing100MBTest() {
      BaseSlicingStrategy slicer = new BaseSlicingStrategy(new BasePayloadSlicer());
      slicer.startSlicing(buildPayload(100 * MiB));

      long offset = 0;
      while (slicer.hasNext()) {
         PayloadSlice slice = slicer.nextSlice();
         long expectedLength = (slicer.hasNext() ? 8 : 4) * MiB;
         assertThat(slice.getPayload().getContentMetadata().getContentLength()).isEqualTo(expectedLength);
         assertThat(slice.getRange()).isEqualTo(ContentRange.build(offset, offset + expectedLength - 1));
         offset += expectedLength;
      }
   }

   @Test
   public void slicing2000MBTest() {
      BaseSlicingStrategy slicer = new BaseSlicingStrategy(new BasePayloadSlicer());
      slicer.startSlicing(buildPayload(2000 * MiB));

      long offset = 0;
      while (slicer.hasNext()) {
         PayloadSlice slice = slicer.nextSlice();
         long expectedLength = (slicer.hasNext() ? 32 : 16) * MiB;
         assertThat(slice.getPayload().getContentMetadata().getContentLength()).isEqualTo(expectedLength);
         assertThat(slice.getRange()).isEqualTo(ContentRange.build(offset, offset + expectedLength - 1));
         offset += expectedLength;
      }
   }

   @Test
   public void slicing2MBTest() {
      BaseSlicingStrategy slicer = new BaseSlicingStrategy(new BasePayloadSlicer());
      slicer.startSlicing(buildPayload(2 * MiB));

      long offset = 0;
      while (slicer.hasNext()) {
         PayloadSlice slice = slicer.nextSlice();
         long expectedLength = 1 * MiB;
         assertThat(slice.getPayload().getContentMetadata().getContentLength()).isEqualTo(expectedLength);
         assertThat(slice.getRange()).isEqualTo(ContentRange.build(offset, offset + expectedLength - 1));
         offset += expectedLength;
      }
   }

   @Test
   public void slicing40000GBTest() {
      BaseSlicingStrategy slicer = new BaseSlicingStrategy(new BasePayloadSlicer());
      slicer.startSlicing(buildPayload(40000 * GiB));

      long offset = 0;
      while (slicer.hasNext()) {
         PayloadSlice slice = slicer.nextSlice();
         long expectedLength = 4096 * MiB;
         assertThat(slice.getPayload().getContentMetadata().getContentLength()).isEqualTo(expectedLength);
         assertThat(slice.getRange()).isEqualTo(ContentRange.build(offset, offset + expectedLength - 1));
         offset += expectedLength;
      }
   }
}
