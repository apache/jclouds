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
package org.jclouds.proxy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.jclouds.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.jclouds.Constants.PROPERTY_PROXY_EXCLUDE_URI_PATTERN_PREFIX;
import static org.jclouds.util.Predicates2.startsWith;

/**
 * Proxy URI pattern excludes.
 */
public class ProxyExcludePatterns {

    @Resource
    protected Logger logger = Logger.NULL;

    private final Set<Pattern> excludeUrisPatterns;

    @Inject
    ProxyExcludePatterns(Function<Predicate<String>, Map<String, String>> filterStringsBoundByName) {
        this.excludeUrisPatterns = createPatterns(filterStringsBoundByName);
    }

    private Set<Pattern> createPatterns(Function<Predicate<String>, Map<String, String>> filterStringsBoundByName) {
        Map<String, String> proxyExcludePatternsMap = filterStringsBoundByName
                .apply(startsWith(PROPERTY_PROXY_EXCLUDE_URI_PATTERN_PREFIX));

        if (proxyExcludePatternsMap == null || proxyExcludePatternsMap.isEmpty()) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<Pattern> setBuilder = ImmutableSet.builder();
        for (Map.Entry<String, String> entry : proxyExcludePatternsMap.entrySet()) {
            try {
                Pattern excludedUriPattern = Pattern.compile(entry.getValue());
                setBuilder.add(excludedUriPattern);
            } catch (PatternSyntaxException e) {
                logger.warn("Skipped invalid proxy exclude pattern: " + entry.getValue());
            }
        }
        return setBuilder.build();
    }

    public Set<Pattern> getExcludeUrisPatterns() {
        return excludeUrisPatterns;
    }
}
