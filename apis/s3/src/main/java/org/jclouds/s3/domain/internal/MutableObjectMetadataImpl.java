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
package org.jclouds.s3.domain.internal;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import org.jclouds.http.HttpUtils;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.payloads.BaseMutableContentMetadata;
import org.jclouds.s3.domain.CanonicalUser;
import org.jclouds.s3.domain.MutableObjectMetadata;
import org.jclouds.s3.domain.ObjectMetadata;

/**
 * Allows you to manipulate metadata.
 */
public class MutableObjectMetadataImpl implements MutableObjectMetadata {

   private String key;
   private String bucket;
   private URI uri;
   private Date lastModified;
   private String eTag;
   private String versionId;
   private String isLatest;
   private CanonicalUser owner;
   private StorageClass storageClass;
   private String cacheControl;
   private Map<String, String> userMetadata = Maps.newHashMap();
   private MutableContentMetadata contentMetadata;

   public MutableObjectMetadataImpl() {
      this.storageClass = StorageClass.STANDARD;
      this.contentMetadata = new BaseMutableContentMetadata();
   }

   public MutableObjectMetadataImpl(ObjectMetadata from) {
      this.storageClass = StorageClass.STANDARD;
      this.contentMetadata = new BaseMutableContentMetadata();
      HttpUtils.copy(from.getContentMetadata(), this.contentMetadata);
      this.key = from.getKey();
      this.uri = from.getUri();
      this.bucket = from.getBucket();
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public String getKey() {
      return key;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public String getBucket() {
      return bucket;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public URI getUri() {
      return uri;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setUri(URI uri) {
      this.uri = uri;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public CanonicalUser getOwner() {
      return owner;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public StorageClass getStorageClass() {
      return storageClass;
   }

   /**
    * @deprecated call getContentMetadata().getCacheControl() instead
    */
   @Deprecated
   @Override
   public String getCacheControl() {
      return contentMetadata.getCacheControl();
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public Date getLastModified() {
      return lastModified;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public String getETag() {
      return eTag;
   }

   @Override
   public String getVersionId() {
      return versionId;
   }

   @Override
   public String getIsLatest() {
      return isLatest;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public int compareTo(ObjectMetadata o) {
      return (this == o) ? 0 : getKey().compareTo(o.getKey());
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public Map<String, String> getUserMetadata() {
      return userMetadata;
   }

   /**
    * @deprecated call getContentMetadata().setCacheControl(String) instead
    */
   @Deprecated
   @Override
   public void setCacheControl(String cacheControl) {
      contentMetadata.setCacheControl(cacheControl);
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setETag(String eTag) {
      this.eTag = eTag;
   }

   @Override
   public void setVersionId(String versionId) {
      this.versionId = versionId;
   }

   @Override
   public void setIsLatest(String isLatest) {
      this.isLatest = isLatest;
   }


   /**
    *{@inheritDoc}
    */
   @Override
   public void setKey(String key) {
      this.key = key;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setBucket(String bucket) {
      this.bucket = bucket;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setLastModified(Date lastModified) {
      this.lastModified = lastModified;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setOwner(CanonicalUser owner) {
      this.owner = owner;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setStorageClass(StorageClass storageClass) {
      this.storageClass = storageClass;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public void setUserMetadata(Map<String, String> userMetadata) {
      this.userMetadata = userMetadata;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public MutableContentMetadata getContentMetadata() {
      return contentMetadata;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setContentMetadata(MutableContentMetadata contentMetadata) {
      this.contentMetadata = contentMetadata;
   }

   @Override
   public int hashCode() {
      return Objects.hash(key, bucket, uri, lastModified, eTag, versionId,
          isLatest, owner, storageClass, cacheControl, userMetadata,
          contentMetadata);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      MutableObjectMetadataImpl that = (MutableObjectMetadataImpl) o;
      return Objects.equals(key, that.key) &&
          Objects.equals(bucket, that.bucket) &&
          Objects.equals(uri, that.uri) &&
          Objects.equals(lastModified, that.lastModified) &&
          Objects.equals(eTag, that.eTag) &&
          Objects.equals(versionId, that.versionId) &&
          Objects.equals(isLatest, that.isLatest) &&
          Objects.equals(owner, that.owner) &&
          storageClass == that.storageClass &&
          Objects.equals(cacheControl, that.cacheControl) &&
          Objects.equals(userMetadata, that.userMetadata) &&
          Objects.equals(contentMetadata, that.contentMetadata);
   }

   @Override
   public String toString() {
      return String
               .format(
                        "[key=%s, bucket=%s, uri=%s, eTag=%s, cacheControl=%s, contentMetadata=%s, lastModified=%s, owner=%s, storageClass=%s, userMetadata=%s]",
                        key, bucket, uri, eTag, cacheControl, contentMetadata, lastModified, owner, storageClass,
                        userMetadata);
   }

}
