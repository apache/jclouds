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

import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;

import java.util.Arrays;

import org.jclouds.io.ByteSources;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.ByteSourcePayload;

import com.google.common.io.ByteSource;

public class TestUtils {
   public static final long MiB = 1L << 20;
   public static final long GiB = 1L << 30;
   public static final long TiB = 1L << 40;

   public static Payload buildPayload(long size) {
      ByteSource data = buildData(size);
      Payload payload = new ByteSourcePayload(data);
      payload.getContentMetadata().setContentType(PLAIN_TEXT_UTF_8.toString());
      payload.getContentMetadata().setContentLength(size);
      return payload;
   }

   public static ByteSource buildData(long size) {
      byte[] array = new byte[1024];
      Arrays.fill(array, (byte) 'a');
      return ByteSources.repeatingArrayByteSource(array).slice(0, size);
   }
}
