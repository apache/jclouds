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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.sqrt;

import javax.inject.Singleton;

import org.jclouds.glacier.blobstore.strategy.PayloadSlice;
import org.jclouds.glacier.blobstore.strategy.SlicingStrategy;
import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This implementation slice a payload based on the (part size)/(number of parts) ratio. This ratio may be overriden.
 */
@Singleton
public class BaseSlicingStrategy implements SlicingStrategy {

   public static final double DEFAULT_RATIO = 0.32; // (part size/number of parts) ratio

   @Inject(optional = true)
   @Named("jclouds.mpu.part.ratio")
   private final double ratio = DEFAULT_RATIO;

   private final PayloadSlicer slicer;
   private Payload payload;
   private volatile long partSizeInMB;
   private volatile long total;
   private volatile long copied;
   private volatile int part;

   @Inject
   public BaseSlicingStrategy(PayloadSlicer slicer) {
      this.slicer = checkNotNull(slicer, "slicer");
      this.total = 0;
      this.copied = 0;
      this.partSizeInMB = 0;
      this.part = 0;
   }

   protected long calculatePartSize(long length) {
      long lengthInMB = (length / (1L << 20)) + 1;
      double fpPartSizeInMB = sqrt(ratio * lengthInMB); //Get the part size which matches the given ratio
      long partSizeInMB = Long.highestOneBit((long) fpPartSizeInMB - 1) << 1;
      if (partSizeInMB < 1) return 1;
      else if (partSizeInMB > MAX_PART_SIZE) return MAX_PART_SIZE;
      return partSizeInMB;
   }

   public long getRemaining() {
      return total - copied;
   }

   @Override
   public void startSlicing(Payload payload) {
      this.payload = checkNotNull(payload, "payload");
      this.copied = 0;
      this.total = checkNotNull(payload.getContentMetadata().getContentLength(), "contentLength");
      this.partSizeInMB = calculatePartSize(total);
      this.part = 0;
   }

   @Override
   public PayloadSlice nextSlice() {
      checkNotNull(this.payload, "payload");
      long sliceLength = Math.min(getRemaining(), partSizeInMB << 20);
      Payload slicedPayload = slicer.slice(payload, copied, sliceLength);
      ContentRange range = ContentRange.build(copied, copied + sliceLength - 1);
      copied += sliceLength;
      part++;
      return new PayloadSlice(slicedPayload, range, part);
   }

   @Override
   public boolean hasNext() {
      return this.getRemaining() != 0;
   }

   @Override
   public long getPartSizeInMB() {
      return partSizeInMB;
   }
}
