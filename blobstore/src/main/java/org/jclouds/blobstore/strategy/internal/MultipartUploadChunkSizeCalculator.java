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

package org.jclouds.blobstore.strategy.internal;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;

public final class MultipartUploadChunkSizeCalculator {

   @VisibleForTesting
   public static final long DEFAULT_PART_SIZE = 25 * 1024 * 1024;  //25MB

   private final long minimumPartSize;
   private final long maximumPartSize;

   private volatile int iteration = 0;
   private volatile long lastUsedPartSize;

   public MultipartUploadChunkSizeCalculator(long minimumPartSize, long maximumPartSize) {
      checkArgument(minimumPartSize > 0);
      this.minimumPartSize = minimumPartSize;
      checkArgument(maximumPartSize > 0);
      this.maximumPartSize = maximumPartSize;
   }

   public synchronized long getPartSize() {
      if (iteration == 0) {
         lastUsedPartSize = Math.max(DEFAULT_PART_SIZE, minimumPartSize);
      } else if (iteration % 100 == 0) {
         lastUsedPartSize = Math.min(lastUsedPartSize * 2, maximumPartSize);
      }
      iteration++;
      return lastUsedPartSize;
   }

}
