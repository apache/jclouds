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
package org.jclouds.glacier.blobstore.strategy;

import org.jclouds.io.Payload;

/**
 * Strategy for payload slicing
 */
public interface SlicingStrategy {
   public static final int MAX_LIST_PARTS_RETURNED = 1000;
   public static final int MAX_LIST_MPU_RETURNED = 1000;
   public static final int MAX_NUMBER_OF_PARTS = 10000;

   public static final long MIN_PART_SIZE = 1L << 20; //1 MB, last part can be < 1 MB
   public static final long MAX_PART_SIZE = 1L << 32; //4 GB

   void startSlicing(Payload payload);
   PayloadSlice nextSlice();
   boolean hasNext();
   long getPartSizeInMB();
}
