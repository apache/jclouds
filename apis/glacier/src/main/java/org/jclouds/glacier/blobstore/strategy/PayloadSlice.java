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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.glacier.util.ContentRange;
import org.jclouds.io.Payload;

/**
 * Represents an ordered part of data from a payload
 */
public class PayloadSlice {
   private final Payload payload;
   private final ContentRange range;
   private final int part;

   public PayloadSlice(Payload payload, ContentRange range, int part) {
      this.payload = checkNotNull(payload, "payload");
      this.range = checkNotNull(range, "range");
      checkArgument(part >= 0, "The part number cannot be negative");
      this.part = part;
   }

   public Payload getPayload() {
      return payload;
   }

   public ContentRange getRange() {
      return range;
   }

   public int getPart() {
      return part;
   }
}
