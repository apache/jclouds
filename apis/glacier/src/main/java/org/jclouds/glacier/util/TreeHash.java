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
import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.util.Closeables2.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.jclouds.io.Payload;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;

public final class TreeHash {
   private static final int CHUNK_SIZE = 1024 * 1024;

   private final HashCode treeHash;
   private final HashCode linearHash;

   private TreeHash(HashCode treeHash, HashCode linearHash) {
      this.treeHash = checkNotNull(treeHash, "treeHash");
      this.linearHash = checkNotNull(linearHash, "linearHash");
   }

   public HashCode getLinearHash() {
      return linearHash;
   }

   public HashCode getTreeHash() {
      return treeHash;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.treeHash, this.linearHash);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      TreeHash other = (TreeHash) obj;
      return Objects.equal(this.treeHash, other.treeHash) && Objects.equal(this.linearHash, other.linearHash);
   }

   @Override
   public String toString() {
      return "TreeHash [treeHash=" + treeHash + ", linearHash=" + linearHash + "]";
   }

   private static HashCode hashList(Collection<HashCode> hashList) {
      Builder<HashCode> result = ImmutableList.builder();
      while (hashList.size() > 1) {
         //Hash pairs of values and add them to the result list.
         for (Iterator<HashCode> it = hashList.iterator(); it.hasNext();) {
            HashCode hc1 = it.next();
            if (it.hasNext()) {
                HashCode hc2 = it.next();
                result.add(Hashing.sha256().newHasher()
                      .putBytes(hc1.asBytes())
                      .putBytes(hc2.asBytes())
                      .hash());
            } else {
               result.add(hc1);
            }
         }
         hashList = result.build();
         result = ImmutableList.builder();
      }
      return hashList.iterator().next();
   }

   /**
    * Builds the Hash and the TreeHash values of the payload.
    *
    * @return The calculated TreeHash.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/checksum-calculations.html" />
    */
   public static TreeHash buildTreeHashFromPayload(Payload payload) throws IOException {
      InputStream is = null;
      try {
         is = checkNotNull(payload, "payload").openStream();
         Builder<HashCode> list = ImmutableList.builder();
         HashingInputStream linearHis = new HashingInputStream(Hashing.sha256(), is);
         while (true) {
             HashingInputStream chunkedHis = new HashingInputStream(
                     Hashing.sha256(), ByteStreams.limit(linearHis, CHUNK_SIZE));
             long count = ByteStreams.copy(chunkedHis, ByteStreams.nullOutputStream());
             if (count == 0) {
                 break;
             }
             list.add(chunkedHis.hash());
         }
         //The result list contains exactly one element now.
         return new TreeHash(hashList(list.build()), linearHis.hash());
      } finally {
         closeQuietly(is);
      }
   }

   /**
    * Builds a TreeHash based on a map of hashed chunks.
    *
    * @return The calculated TreeHash.
    * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/checksum-calculations.html" />
    */
   public static HashCode buildTreeHashFromMap(Map<Integer, HashCode> map) {
      checkArgument(!map.isEmpty(), "The map cannot be empty.");
      return hashList(ImmutableSortedMap.copyOf(map).values());
   }
}
