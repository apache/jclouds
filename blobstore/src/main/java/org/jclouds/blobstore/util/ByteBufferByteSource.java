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
package org.jclouds.blobstore.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.google.common.io.ByteSource;

public class ByteBufferByteSource extends ByteSource {
   private final ByteBuffer buffer;

   public ByteBufferByteSource(ByteBuffer buffer) {
      this.buffer = checkNotNull(buffer);
   }

   @Override
   public InputStream openStream() {
      return new ByteBufferInputStream(buffer);
   }

   private static final class ByteBufferInputStream extends InputStream {
      private final ByteBuffer buffer;
      private boolean closed = false;

      ByteBufferInputStream(ByteBuffer buffer) {
         this.buffer = buffer;
      }

      @Override
      public synchronized int read() throws IOException {
         if (closed) {
            throw new IOException("Stream already closed");
         }
         try {
            return buffer.get();
         } catch (BufferUnderflowException bue) {
            return -1;
         }
      }

      @Override
      public void close() throws IOException {
         super.close();
         closed = true;
      }
   }
}
