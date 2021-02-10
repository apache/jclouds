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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

@Test(groups = "unit")
public final class MpuChunkSizeCalculatorTest {

   @Test
   public void testWithMinPartSizeSmallerThanDefaultPartSize() {
      long minPartSize = 5L * 1024 * 1024; //5MB
      long maxPartSize = 5L * 1024 * 1024 * 1024; //5GB
      long defaultPartSize = MultipartUploadChunkSizeCalculator.DEFAULT_PART_SIZE;
      MultipartUploadChunkSizeCalculator calculator = new MultipartUploadChunkSizeCalculator(minPartSize, maxPartSize);

      assertEquals(calculator.getPartSize(), defaultPartSize);
      for (long i = 1; i < 100; i++) {
         assertEquals(calculator.getPartSize(), defaultPartSize);
      }

      for (long n = 1; n <= 10; n++) {
         for (long i = 0; i < 100; i++) {
            assertEquals(calculator.getPartSize(), Math.min(defaultPartSize * (long) Math.pow(2, n), maxPartSize));
         }
      }
   }

   @Test
   public void testWithMinPartSizeLargerThanDefaultPartSize() {
      long defaultPartSize = MultipartUploadChunkSizeCalculator.DEFAULT_PART_SIZE;
      long minPartSize = defaultPartSize + (1024 * 1024); //+ 1MB
      long maxPartSize = 5L * 1024 * 1024 * 1024; //5GB
      MultipartUploadChunkSizeCalculator calculator = new MultipartUploadChunkSizeCalculator(minPartSize, maxPartSize);

      assertEquals(calculator.getPartSize(), minPartSize);
      for (long i = 1; i < 100; i++) {
         assertEquals(calculator.getPartSize(), minPartSize);
      }

      for (long n = 1; n <= 10; n++) {
         for (long i = 0; i < 100; i++) {
            assertEquals(calculator.getPartSize(), Math.min(minPartSize * (long) Math.pow(2, n), maxPartSize));
         }
      }
   }

   @Test
   public void testMaxPartSizeIsNotExceeded() {
      long minPartSize = 5L * 1024 * 1024; //5MB
      long defaultPartSize = MultipartUploadChunkSizeCalculator.DEFAULT_PART_SIZE;
      long maxPartSize = defaultPartSize * (long) Math.pow(2, 2);
      MultipartUploadChunkSizeCalculator calculator = new MultipartUploadChunkSizeCalculator(minPartSize, maxPartSize);

      assertEquals(calculator.getPartSize(), defaultPartSize);
      for (long i = 1; i < 100; i++) {
         assertEquals(calculator.getPartSize(), defaultPartSize);
      }

      for (long n = 1; n <= 2; n++) {
         for (long i = 0; i < 100; i++) {
            assertEquals(calculator.getPartSize(), defaultPartSize * (long) Math.pow(2, n));
         }
      }

      for (long i = 0; i < 300; i++) {
         assertEquals(calculator.getPartSize(), maxPartSize);
      }
   }

}
