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
package org.jclouds.glacier.predicates.validators;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.predicates.Validator;

/**
 * Validates the part size parameter used when initiating multipart uploads.
 */
public final class PartSizeValidator extends Validator<Long> {
   private static final int MIN_PART_SIZE = 1;
   private static final int MAX_PART_SIZE = 4096;

   @Override
   public void validate(Long partSizeInMB) throws IllegalArgumentException {
      checkNotNull(partSizeInMB, "partSizeInMB");
      if (partSizeInMB < MIN_PART_SIZE || partSizeInMB > MAX_PART_SIZE || (partSizeInMB & (partSizeInMB - 1)) != 0)
         throw exception(partSizeInMB, "partSizeInMB must be a power of 2 between 1 and 4096.");
   }

   protected static IllegalArgumentException exception(Long size, String reason) {
      return new IllegalArgumentException(
            String.format(
                  "Part size '%s' doesn't match Glacier Multipart upload rules. "
                        + "Reason: %s. For more info, please refer to http://http://docs.aws.amazon.com/amazonglacier/latest/dev/api-multipart-initiate-upload.html.",
                  size, reason));
   }
}
