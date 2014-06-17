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

import static com.google.common.base.Strings.isNullOrEmpty;

import org.jclouds.predicates.Validator;

import com.google.common.base.CharMatcher;
import com.google.inject.Singleton;

/**
 * Validates the archive description string.
 */
@Singleton
public final class DescriptionValidator extends Validator<String> {

   private static final int MAX_DESC_LENGTH = 1024;

   private static final CharMatcher DESCRIPTION_ACCEPTABLE_RANGE = CharMatcher.inRange(' ', '~');

   @Override
   public void validate(String description) {
      if (isNullOrEmpty(description))
         return;
      if (description.length() > MAX_DESC_LENGTH)
         throw exception("Description can't be longer than " + MAX_DESC_LENGTH + " characters" + " but was " + description.length());
      if (!DESCRIPTION_ACCEPTABLE_RANGE.matchesAllOf(description))
         throw exception("Description should have ASCII values between 32 and 126.");
   }

   protected static IllegalArgumentException exception(String reason) {
      return new IllegalArgumentException(
            String.format(
                  "Description doesn't match Glacier archive description rules. "
                        + "Reason: %s. For more info, please refer to http://docs.aws.amazon.com/amazonglacier/latest/dev/api-archive-post.html.",
                  reason));
   }
}
