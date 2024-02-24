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
package org.jclouds.atmos.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.BaseEncoding.base16;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.atmos.domain.FileType;
import org.jclouds.atmos.domain.SystemMetadata;
import org.jclouds.atmos.reference.AtmosHeaders;
import org.jclouds.date.DateService;
import org.jclouds.http.HttpResponse;

import com.google.common.base.Function;
import com.google.common.base.Splitter;

@Singleton
public class ParseSystemMetadataFromHeaders implements Function<HttpResponse, SystemMetadata> {
   private final DateService dateService;

   @Inject
   public ParseSystemMetadataFromHeaders(DateService dateService) {
      this.dateService = checkNotNull(dateService, "dateService");
   }

   public SystemMetadata apply(HttpResponse from) {
      checkNotNull(from, "http response");
      String meta = checkNotNull(from.getFirstHeaderOrNull(AtmosHeaders.META), AtmosHeaders.META);
      Map<String, String> metaMap = Splitter.on(", ").withKeyValueSeparator('=').split(meta);
      assert metaMap.size() >= 12 : String.format("Should be 12 entries in %s", metaMap);

      byte[] md5 = null;
      String wschecksum = from.getFirstHeaderOrNull(AtmosHeaders.CHECKSUM);
      if (wschecksum != null) {
         String[] parts = wschecksum.split("/");
         if (parts[0].equalsIgnoreCase("MD5") && parts.length == 3) {
            md5 = base16().lowerCase().decode(parts[2]);
         }
      }

      return new SystemMetadata(md5, dateService.iso8601SecondsDateParse(checkNotNull(metaMap.get("atime"), "atime")),
            dateService.iso8601SecondsDateParse(checkNotNull(metaMap.get("ctime"), "ctime")), checkNotNull(
                  metaMap.get("gid"), "gid"), dateService.iso8601SecondsDateParse(checkNotNull(metaMap.get("itime"),
                  "itime")), dateService.iso8601SecondsDateParse(checkNotNull(metaMap.get("mtime"), "mtime")),
            Integer.parseInt(checkNotNull(metaMap.get("nlink"), "nlink")), checkNotNull(metaMap.get("objectid"),
                  "objectid"), checkNotNull(metaMap.get("objname"), "objname"), checkNotNull(metaMap.get("policyname"),
                  "policyname"), Long.parseLong(checkNotNull(metaMap.get("size"), "size")),
            FileType.fromValue(checkNotNull(metaMap.get("type"), "type")), checkNotNull(metaMap.get("uid"), "uid"));
   }
}
