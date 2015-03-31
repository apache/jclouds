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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import org.jclouds.predicates.Validator;

import com.google.common.base.CharMatcher;
import com.google.inject.Singleton;

/**
 * Validates Vault names according to Amazon Vault conventions.
 *
 * @see <a href="http://docs.aws.amazon.com/amazonglacier/latest/dev/api-vault-put.html" />
 */
@Singleton
public final class VaultNameValidator extends Validator<String> {

   private static final int MIN_LENGTH = 1;
   private static final int MAX_LENGTH = 255;

   private static final CharMatcher VAULT_NAME_ACCEPTABLE_RANGE = CharMatcher.inRange('a', 'z')
         .or(CharMatcher.inRange('A', 'Z'))
         .or(CharMatcher.inRange('0', '9'))
         .or(CharMatcher.anyOf("-_."));

   @Override
   public void validate(String vaultName) {
      checkArgument(!isNullOrEmpty(vaultName) && vaultName.length() <= MAX_LENGTH,
            "Can't be null or empty. Length must be %s to %s symbols.", MIN_LENGTH, MAX_LENGTH);
      checkArgument(VAULT_NAME_ACCEPTABLE_RANGE.matchesAllOf(vaultName),
            "Should contain only ASCII letters and numbers, underscores, hyphens, or periods.");
   }
}
