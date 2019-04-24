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

package org.jclouds.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.util.Closeables2.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.annotations.Beta;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;

@Beta
public class ByteStreams2 {
   private static final int INPUT_STREAM_READ_END_OF_STREAM_INDICATOR = -1;

   public static HashCode hashAndClose(InputStream input, HashFunction hashFunction) throws IOException {
      checkNotNull(input, "input");
      checkNotNull(hashFunction, "hashFunction");
      try {
         HashingInputStream his = new HashingInputStream(hashFunction, input);
         ByteStreams.copy(his, ByteStreams.nullOutputStream());
         return his.hash();
      } finally {
         closeQuietly(input);
      }
   }

   public static byte[] toByteArrayAndClose(InputStream input) throws IOException {
      checkNotNull(input, "input");
      try {
         return ByteStreams.toByteArray(input);
      } finally {
         closeQuietly(input);
      }
   }

   public static long copy(InputStream from, OutputStream to, int bufferSize) throws IOException {
      checkNotNull(from, "from");
      checkNotNull(to, "to");
      checkArgument(bufferSize >= 1, "bufferSize must be >= 1");

      byte[] buf = new byte[bufferSize];
      long total = 0L;

      while (true) {
         int len = from.read(buf);
         if (len == INPUT_STREAM_READ_END_OF_STREAM_INDICATOR) {
            return total;
         }

         to.write(buf, 0, len);
         total += (long)len;
      }
   }
}
