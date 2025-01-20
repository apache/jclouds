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
package org.jclouds.s3.domain;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.Beta;

@AutoValue
@Beta
public abstract class PublicAccessBlockConfiguration {
   public abstract boolean blockPublicAcls();
   public abstract boolean ignorePublicAcls();
   public abstract boolean blockPublicPolicy();
   public abstract boolean restrictPublicBuckets();

   public static PublicAccessBlockConfiguration create(boolean blockPublicAcls, boolean ignorePublicAcls, boolean blockPublicPolicy, boolean restrictPublicBuckets) {
      return new AutoValue_PublicAccessBlockConfiguration(blockPublicAcls, ignorePublicAcls, blockPublicPolicy, restrictPublicBuckets);
   }
}
