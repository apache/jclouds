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
package org.jclouds.s3.xml;

import static com.google.common.io.BaseEncoding.base16;
import static org.jclouds.http.Uris.uriBuilder;
import static org.jclouds.util.SaxUtils.currentOrNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.jclouds.date.DateService;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.s3.domain.CanonicalUser;
import org.jclouds.s3.domain.ListBucketResponse;
import org.jclouds.s3.domain.ObjectMetadata;
import org.jclouds.s3.domain.ObjectMetadataBuilder;
import org.jclouds.s3.domain.internal.ListBucketResponseImpl;
import org.jclouds.util.Strings2;
import org.xml.sax.Attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * Parses the following XML document:
 * <p/>
 * ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01"
 */
public class ListBucketHandler extends ParseSax.HandlerWithResult<ListBucketResponse> {
   private Builder<ObjectMetadata> contents = ImmutableSet.builder();
   private Builder<String> commonPrefixes = ImmutableSet.builder();
   private CanonicalUser currentOwner;
   private StringBuilder currentText = new StringBuilder();

   private ObjectMetadataBuilder builder = new ObjectMetadataBuilder();

   private final DateService dateParser;

   private String bucketName;
   private String prefix;
   private String marker;
   private int maxResults;
   private String delimiter;
   private boolean isTruncated;

   /** Some blobs have a non-hex suffix when created by multi-part uploads such Amazon S3. */
   private static final Pattern ETAG_CONTENT_MD5_PATTERN = Pattern.compile("\"([0-9a-f]+)\"");

   @Inject
   public ListBucketHandler(DateService dateParser) {
      this.dateParser = dateParser;
      this.currentOwner = new CanonicalUser();
   }

   public ListBucketResponse getResult() {
      return new ListBucketResponseImpl(bucketName, contents.build(), prefix, marker,
               (isTruncated && nextMarker == null) ? currentKey : nextMarker, maxResults, delimiter, isTruncated,
               commonPrefixes.build());
   }

   private boolean inCommonPrefixes;
   private String currentKey;
   private String nextMarker;

   public void startElement(String uri, String name, String qName, Attributes attrs) {
      if (qName.equals("CommonPrefixes")) {
         inCommonPrefixes = true;
      }
      currentText.setLength(0);
   }

   public void endElement(String uri, String name, String qName) {
      if (qName.equals("ID")) {
    	 currentOwner.setId(currentOrNull(currentText));
      } else if (qName.equals("DisplayName")) {
         currentOwner.setDisplayName(currentOrNull(currentText));
      } else if (qName.equals("Key")) { // content stuff
         if (currentText.length() == 0) {
            throw new RuntimeException("Object store returned empty key name");
         }
         currentKey = currentText.toString();
         builder.key(currentKey);
         builder.uri(uriBuilder(getRequest().getEndpoint()).clearQuery().appendPath(Strings2.urlEncode(currentKey))
               .build());
      } else if (qName.equals("LastModified")) {
         builder.lastModified(dateParser
               .iso8601DateOrSecondsDateParse(currentOrNull(currentText)));
      } else if (qName.equals("ETag")) {
         String currentETag = currentOrNull(currentText);
         builder.eTag(currentETag);
         Matcher matcher = ETAG_CONTENT_MD5_PATTERN.matcher(currentETag);
         if (matcher.matches()) {
            builder.contentMD5(base16().lowerCase().decode(matcher.group(1)));
         }
      } else if (qName.equals("Size")) {
         builder.contentLength(Long.valueOf(currentOrNull(currentText)));
      } else if (qName.equals("Owner")) {
         builder.owner(currentOwner);
         currentOwner = new CanonicalUser();
      } else if (qName.equals("StorageClass")) {
         builder.storageClass(ObjectMetadata.StorageClass.valueOf(currentOrNull(currentText)));
      } else if (qName.equals("Contents")) {
         contents.add(builder.build());
         builder = new ObjectMetadataBuilder().bucket(bucketName);
      } else if (qName.equals("Name")) {
         this.bucketName = currentOrNull(currentText);
         builder.bucket(bucketName);
      } else if (qName.equals("Prefix")) {
         String prefix = currentOrNull(currentText);
         if (inCommonPrefixes)
            commonPrefixes.add(prefix);
         else
            this.prefix = prefix;
      } else if (qName.equals("Delimiter")) {
         this.delimiter = currentOrNull(currentText);
      } else if (qName.equals("Marker")) {
         this.marker = currentOrNull(currentText);
      } else if (qName.equals("NextMarker")) {
         this.nextMarker = currentOrNull(currentText);
      } else if (qName.equals("MaxKeys")) {
         this.maxResults = Integer.parseInt(currentOrNull(currentText));
      } else if (qName.equals("IsTruncated")) {
         this.isTruncated = Boolean.parseBoolean(currentOrNull(currentText));
      }
   }

   public void characters(char[] ch, int start, int length) {
      currentText.append(ch, start, length);
   }
}
