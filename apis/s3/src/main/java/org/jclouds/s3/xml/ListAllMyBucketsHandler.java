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

import static org.jclouds.util.SaxUtils.currentOrNull;

import java.util.Date;
import java.util.Set;

import jakarta.inject.Inject;

import org.jclouds.date.DateService;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.s3.domain.BucketMetadata;
import org.jclouds.s3.domain.CanonicalUser;

import com.google.common.collect.Sets;

/**
 * Parses the following XML document:
 * <p/>
 * SetAllMyBucketsResult xmlns="http://doc.s3.amazonaws.com/2006-03-01"
 */
public class ListAllMyBucketsHandler extends ParseSax.HandlerWithResult<Set<BucketMetadata>> {

   private Set<BucketMetadata> buckets = Sets.newLinkedHashSet();
   private CanonicalUser currentOwner;
   private String currentDisplayName;
   private StringBuilder currentText = new StringBuilder();

   private final DateService dateParser;
   private String currentName;
   private Date currentCreationDate;

   @Inject
   public ListAllMyBucketsHandler(DateService dateParser) {
      this.dateParser = dateParser;
      this.currentOwner =  new CanonicalUser();
   }

   public Set<BucketMetadata> getResult() {
      return buckets;
   }

   public void endElement(String uri, String name, String qName) {
      if (qName.equals("ID")) { // owner stuff
         currentOwner.setId(currentOrNull(currentText));
      } else if (qName.equals("DisplayName")) {
         currentOwner.setDisplayName(currentOrNull(currentText));
      } else if (qName.equals("Bucket")) {
         buckets.add(new BucketMetadata(currentName, currentCreationDate, currentOwner));
      } else if (qName.equals("Name")) {
         currentName = currentOrNull(currentText);
      } else if (qName.equals("CreationDate")) {
         currentCreationDate = dateParser
               .iso8601DateOrSecondsDateParse(currentOrNull(currentText));
      }
      currentText.setLength(0);
   }

   public void characters(char[] ch, int start, int length) {
      currentText.append(ch, start, length);
   }
}
