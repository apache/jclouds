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
package org.jclouds.glacier.options;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.http.options.BaseHttpRequestOptions;

/**
 * Pagination options used to specify the collection responses.
 *
 * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vaults-get.html" />
 */
public class PaginationOptions extends BaseHttpRequestOptions {

   private static final int MIN_LIMIT = 1;
   private static final int MAX_LIMIT = 1000;

   public PaginationOptions marker(String marker) {
      queryParameters.put("marker", checkNotNull(marker, "marker"));
      return this;
   }

   public PaginationOptions limit(int limit) {
      checkArgument(limit >= MIN_LIMIT, "limit must be >= " + MIN_LIMIT + " but was " + limit);
      checkArgument(limit <= MAX_LIMIT, "limit must be <= " + MAX_LIMIT + " but was " + limit);
      queryParameters.put("limit", Integer.toString(limit));
      return this;
   }

   public static class Builder {

      public static PaginationOptions marker(String marker) {
         PaginationOptions options = new PaginationOptions();
         return options.marker(marker);
      }

      public static PaginationOptions limit(int limit) {
         PaginationOptions options = new PaginationOptions();
         return options.limit(limit);
      }
   }
}
