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
package org.jclouds.glacier.domain;

import java.beans.ConstructorProperties;

import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Defines attributes to describe a GlacierError
 */
public class GlacierError {

   private final String code;
   private final String message;
   private final String type;

   @ConstructorProperties({ "code", "message", "type" })
   public GlacierError(@Nullable String code, @Nullable String message, @Nullable String type) {
      this.code = code;
      this.message = message;
      this.type = type;
   }

   public String getCode() {
      return code;
   }

   public String getMessage() {
      return message;
   }

   public String getType() {
      return type;
   }

   public boolean isValid() {
      return (this.code != null) && (this.message != null) && (this.type != null);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.code, this.message, this.type);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      GlacierError other = (GlacierError) obj;

      return Objects.equal(this.code, other.code)
            && Objects.equal(this.message, other.message)
            && Objects.equal(this.type, other.type);
   }

   @Override
   public String toString() {
      return "GlacierError [code=" + this.getCode() + ", message=" + this.getMessage() + "type=" + this.getType() + "]";
   }
}
