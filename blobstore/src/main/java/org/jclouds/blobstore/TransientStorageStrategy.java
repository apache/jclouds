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
package org.jclouds.blobstore;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.BaseEncoding.base16;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.Blob.Factory;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.MutableStorageMetadataImpl;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.util.BlobStoreUtils;
import org.jclouds.date.DateService;
import org.jclouds.domain.Location;
import org.jclouds.http.HttpUtils;
import org.jclouds.io.ContentMetadataCodec;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.util.Closeables2;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class TransientStorageStrategy implements LocalStorageStrategy {
   private final ConcurrentMap<String, ConcurrentSkipListMap<String, Blob>> containerToBlobs = new ConcurrentHashMap<String, ConcurrentSkipListMap<String, Blob>>();
   private final ConcurrentMap<String, ConcurrentMap<String, BlobAccess>> containerToBlobAccess = new ConcurrentHashMap<String, ConcurrentMap<String, BlobAccess>>();
   private final ConcurrentMap<String, StorageMetadata> containerMetadata = new ConcurrentHashMap<String, StorageMetadata>();
   private final ConcurrentMap<String, ContainerAccess> containerAccessMap = new ConcurrentHashMap<String, ContainerAccess>();
   private final Supplier<Location> defaultLocation;
   private final DateService dateService;
   private final Factory blobFactory;
   private final ContentMetadataCodec contentMetadataCodec;

   @Inject
   TransientStorageStrategy(Supplier<Location> defaultLocation, DateService dateService, Factory blobFactory,
         ContentMetadataCodec contentMetadataCodec) {
      this.defaultLocation = defaultLocation;
      this.dateService = dateService;
      this.blobFactory = blobFactory;
      this.contentMetadataCodec = contentMetadataCodec;
   }

   @Override
   public boolean containerExists(final String containerName) {
      return containerToBlobs.containsKey(containerName);
   }

   @Override
   public Collection<String> getAllContainerNames() {
      return containerToBlobs.keySet();
   }

   @Override
   public boolean createContainerInLocation(String containerName, Location location, CreateContainerOptions options) {
      ConcurrentMap<String, Blob> origValue = containerToBlobs.putIfAbsent(
            containerName, new ConcurrentSkipListMap<String, Blob>());
      if (origValue != null) {
         return false;
      }
      containerToBlobAccess.putIfAbsent(containerName, new ConcurrentHashMap<String, BlobAccess>());

      MutableStorageMetadata metadata = new MutableStorageMetadataImpl();
      metadata.setName(containerName);
      metadata.setType(StorageType.CONTAINER);
      metadata.setLocation(location);
      metadata.setCreationDate(new Date());
      containerMetadata.put(containerName, metadata);

      containerAccessMap.put(containerName, options.isPublicRead()
            ? ContainerAccess.PUBLIC_READ : ContainerAccess.PRIVATE);
      return true;
   }

   @Override
   public ContainerAccess getContainerAccess(String container) {
      ContainerAccess access = containerAccessMap.get(container);
      return access == null ? ContainerAccess.PRIVATE : access;
   }

   @Override
   public void setContainerAccess(String container, ContainerAccess access) {
      containerAccessMap.put(container, access);
   }

   @Override
   public void deleteContainer(final String containerName) {
      containerToBlobs.remove(containerName);
      containerToBlobAccess.remove(containerName);
   }

   @Override
   public void clearContainer(final String containerName) {
      clearContainer(containerName, ListContainerOptions.Builder.recursive());
   }

   @Override
   public void clearContainer(String containerName, ListContainerOptions options) {
      // TODO implement options
      containerToBlobs.get(containerName).clear();
   }

   @Override
   public StorageMetadata getContainerMetadata(String container) {
      return containerMetadata.get(container);
   }

   @Override
   public boolean blobExists(final String containerName, final String blobName) {
      Map<String, Blob> map = containerToBlobs.get(containerName);
      return map != null && map.containsKey(blobName);
   }

   @Override
   public Iterable<String> getBlobKeysInsideContainer(final String containerName, String prefix) {
      ConcurrentSkipListMap<String, Blob> blobs = containerToBlobs.get(containerName);
      if (prefix == null) {
         return blobs.keySet();
      }
      String lastPrefix = prefix + (char) 65535;  // TODO: better sentinel?
      return blobs.subMap(prefix, /*fromInclusive=*/ true, lastPrefix, /*toInclusive=*/ false).keySet();
   }

   @Override
   public Blob getBlob(final String containerName, final String blobName) {
      Map<String, Blob> map = containerToBlobs.get(containerName);
      if (map == null) {
         return null;
      }
      Blob blob = map.get(blobName);
      if (blob == null) {
         return null;
      }

      // Deep copy Blob to make sure ByteSourcePayload does not share Closer.
      Payload payload = blob.getPayload();
      MutableContentMetadata md = payload.getContentMetadata();
      Blob newBlob = blobFactory.create(BlobStoreUtils.copy(blob.getMetadata()));
      Payload newPayload = Payloads.newPayload(payload.getRawContent());
      newBlob.setPayload(payload);
      HttpUtils.copy(md, newPayload.getContentMetadata());
      return newBlob;
   }

   @Override
   public String putBlob(final String containerName, final Blob blob) throws IOException {
      return putBlob(containerName, blob, BlobAccess.PRIVATE);
   }

   @Override
   public String putBlob(final String containerName, final Blob blob, BlobAccess access) throws IOException {
      byte[] payload;
      HashCode actualHashCode;
      HashingInputStream input = new HashingInputStream(Hashing.md5(), blob.getPayload().openStream());
      try {
         payload = ByteStreams.toByteArray(input);
         long actualSize = payload.length;
         Long expectedSize = blob.getMetadata().getContentMetadata().getContentLength();
         if (expectedSize != null && actualSize != expectedSize) {
            throw new IOException("Content-Length mismatch, actual: " + actualSize +
                  " expected: " + expectedSize);
         }
         actualHashCode = input.hash();
         HashCode expectedHashCode = blob.getPayload().getContentMetadata().getContentMD5AsHashCode();
         if (expectedHashCode != null && !actualHashCode.equals(expectedHashCode)) {
            throw new IOException("MD5 hash code mismatch, actual: " + actualHashCode +
                  " expected: " + expectedHashCode);
         }
      } finally {
         Closeables2.closeQuietly(input);
      }

      String eTag = null;
      if (blob.getMetadata() != null) {
         eTag = blob.getMetadata().getETag();
      }
      if (eTag == null) {
         eTag = base16().lowerCase().encode(actualHashCode.asBytes());
      }
      Blob newBlob = createUpdatedCopyOfBlobInContainer(containerName, blob, payload, actualHashCode, eTag);
      Map<String, Blob> map = containerToBlobs.get(containerName);
      String blobName = newBlob.getMetadata().getName();
      map.put(blobName, newBlob);
      containerToBlobAccess.get(containerName).put(blobName, access);
      return base16().lowerCase().encode(actualHashCode.asBytes());
   }

   @Override
   public void removeBlob(final String containerName, final String blobName) {
      Map<String, Blob> map = containerToBlobs.get(containerName);
      if (map != null)
         map.remove(blobName);
   }

   @Override
   public BlobAccess getBlobAccess(String containerName, String blobName) {
      Map<String, BlobAccess> map = containerToBlobAccess.get(containerName);
      if (map == null) {
         throw new ContainerNotFoundException(containerName, "in getBlobAccess");
      }
      BlobAccess access = map.get(blobName);
      if (access == null) {
         throw new KeyNotFoundException(containerName, blobName, "in getBlobAccess");
      }
      return access;
   }

   @Override
   public void setBlobAccess(String containerName, String blobName, BlobAccess access) {
      Map<String, BlobAccess> map = containerToBlobAccess.get(containerName);
      if (map == null) {
         throw new ContainerNotFoundException(containerName, "in setBlobAccess");
      }
      map.put(blobName, access);
   }

   @Override
   public Location getLocation(final String containerName) {
      return containerMetadata.get(containerName).getLocation();
   }

   @Override
   public String getSeparator() {
      return "/";
   }

   private Blob createUpdatedCopyOfBlobInContainer(String containerName, Blob in, byte[] input, HashCode contentMd5, String eTag) {
      checkNotNull(containerName, "containerName");
      checkNotNull(in, "blob");
      checkNotNull(input, "input");
      checkNotNull(contentMd5, "contentMd5");
      checkNotNull(eTag, "eTag");
      Payload payload = createPayload(input);
      MutableContentMetadata oldMd = in.getPayload().getContentMetadata();
      HttpUtils.copy(oldMd, payload.getContentMetadata());
      payload.getContentMetadata().setContentMD5(contentMd5);
      payload.getContentMetadata().setContentLength((long) input.length);
      Blob blob = blobFactory.create(BlobStoreUtils.copy(in.getMetadata()));
      blob.setPayload(payload);
      blob.getMetadata().setContainer(containerName);
      blob.getMetadata().setLastModified(new Date());
      blob.getMetadata().setSize((long) input.length);
      blob.getMetadata().setETag(eTag);
      // Set HTTP headers to match metadata
      blob.getAllHeaders().replaceValues(HttpHeaders.LAST_MODIFIED,
               ImmutableList.of(dateService.rfc822DateFormat(blob.getMetadata().getLastModified())));
      blob.getAllHeaders().replaceValues(HttpHeaders.ETAG, ImmutableList.of(eTag));
      copyPayloadHeadersToBlob(payload, blob);
      blob.getAllHeaders().putAll(Multimaps.forMap(blob.getMetadata().getUserMetadata()));
      return blob;
   }

   private static final class RepeatingByteSource extends ByteSource {
      private final byte ch;

      RepeatingByteSource(byte ch) {
         this.ch = ch;
      }

      @Override
      public InputStream openStream() {
         return new RepeatingInputStream(ch);
      }
   }

   /** @return Payload of input, possibly optimized for sparse regions (ASCII NULs) */
   private static Payload createPayload(byte[] input) {
      for (int i = 0; i < input.length; ++i) {
         if (input[i] != (byte) 0) {
            return Payloads.newByteArrayPayload(input);
         }
      }

      // all bytes are NUL
      return Payloads.newByteSourcePayload(new RepeatingByteSource((byte) 0).slice(0, input.length));
   }

   private static final class RepeatingInputStream extends InputStream {
      private final byte ch;

      RepeatingInputStream(byte ch) {
         this.ch = ch;
      }

      @Override
      public int read() throws IOException {
         return ch;
      }

      @Override
      public int read(byte[] b, int off, int len) {
         for (int i = 0; i < len; ++i) {
            b[off + i] = ch;
         }
         return len;
      }
   }

   private void copyPayloadHeadersToBlob(Payload payload, Blob blob) {
      blob.getAllHeaders().putAll(contentMetadataCodec.toHeaders(payload.getContentMetadata()));
   }
}
