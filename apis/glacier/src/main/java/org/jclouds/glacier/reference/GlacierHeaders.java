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
package org.jclouds.glacier.reference;

/**
 * Headers used by Amazon Glacier.
 */
public final class GlacierHeaders {

   public static final String DEFAULT_AMAZON_HEADERTAG = "amz";
   public static final String HEADER_PREFIX = "x-" + DEFAULT_AMAZON_HEADERTAG + "-";
   public static final String REQUEST_ID = HEADER_PREFIX + "RequestId";
   public static final String VERSION = HEADER_PREFIX + "glacier-version";
   public static final String ALTERNATE_DATE = HEADER_PREFIX + "date";
   public static final String ARCHIVE_DESCRIPTION = HEADER_PREFIX + "archive-description";
   public static final String LINEAR_HASH = HEADER_PREFIX + "content-sha256";
   public static final String TREE_HASH = HEADER_PREFIX + "sha256-tree-hash";
   public static final String ARCHIVE_ID = HEADER_PREFIX + "archive-id";
   public static final String MULTIPART_UPLOAD_ID = HEADER_PREFIX + "multipart-upload-id";
   public static final String PART_SIZE = HEADER_PREFIX + "part-size";
   public static final String ARCHIVE_SIZE = HEADER_PREFIX + "archive-size";
   public static final String JOB_ID = HEADER_PREFIX + "job-id";

   private GlacierHeaders() {
   }
}
