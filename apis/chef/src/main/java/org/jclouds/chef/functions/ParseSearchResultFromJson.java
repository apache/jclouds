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
package org.jclouds.chef.functions;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.chef.domain.SearchResult;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseJson;

import com.google.common.base.Function;

@Singleton
public class ParseSearchResultFromJson<T> implements Function<HttpResponse, SearchResult<T>> {

   private final ParseJson<Response<T>> json;

   static class Response<T> {
      long start;
      List<T> rows;
   }

   @Inject
   ParseSearchResultFromJson(ParseJson<Response<T>> json) {
      this.json = json;
   }

   @Override
   public SearchResult<T> apply(HttpResponse response) {
      Response<T> returnVal = json.apply(response);
      return new SearchResult<T>(returnVal.start, returnVal.rows);
   }
}
