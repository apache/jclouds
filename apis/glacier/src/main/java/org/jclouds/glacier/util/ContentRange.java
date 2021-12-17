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
package org.jclouds.glacier.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Objects;

/**
 * This class represents a range of bytes.
 */
public final class ContentRange {
   private final long from;
   private final long to;

   private ContentRange(long from, long to) {
      checkArgument(from < to, "\"from\" should be lower than \"to\"");
      checkArgument(from >= 0 && to > 0, "\"from\" cannot be negative and \"to\" has to be positive");
      this.from = from;
      this.to = to;
   }

   public static ContentRange fromString(String contentRangeString) {
      checkArgument(!isNullOrEmpty(contentRangeString) && contentRangeString.matches("[0-9]+-[0-9]+"),
            "The string should be two numbers separated by a hyphen (from-to)");
      String[] strings = contentRangeString.split("-", 2);
      long from = Long.parseLong(strings[0]);
      long to = Long.parseLong(strings[1]);
      return new ContentRange(from, to);
   }

   public static ContentRange fromPartNumber(long partNumber, long partSizeInMB) {
      checkArgument(partNumber >= 0, "The part number cannot be negative");
      checkArgument(partSizeInMB > 0, "The part size has to be positive");
      long from = partNumber * (partSizeInMB << 20);
      long to = from + (partSizeInMB << 20) - 1;
      return new ContentRange(from, to);
   }

   public static ContentRange build(long from, long to) {
      return new ContentRange(from, to);
   }

   public long getFrom() {
      return from;
   }

   public long getTo() {
      return to;
   }

   public String buildHeader() {
      return "bytes " + from + "-" + to + "/*";
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.from, this.to);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ContentRange other = (ContentRange) obj;
      return Objects.equal(this.from, other.from) && Objects.equal(this.to, other.to);
   }

   @Override
   public String toString() {
      return from + "-" + to;
   }
}
