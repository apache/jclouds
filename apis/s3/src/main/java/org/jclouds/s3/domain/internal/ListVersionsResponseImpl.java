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
package org.jclouds.s3.domain.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import org.jclouds.s3.domain.ListVersionsResponse;
import org.jclouds.s3.domain.ObjectMetadata;

public class ListVersionsResponseImpl extends LinkedHashSet<ObjectMetadata>
        implements ListVersionsResponse {

    protected final String name;
    protected final String prefix;
    protected final int maxKeys;
    protected final String delimiter;
    protected final String marker;
    protected final String nextMarker;
    protected final Set<String> commonPrefixes;
    protected final boolean truncated;
    protected final String nextVersionIdMarker;
    protected final String versionIdMarker;

    public ListVersionsResponseImpl(String name,
                                    Iterable<ObjectMetadata> version,
                                    String prefix, String marker,
                                    String nextMarker, int maxKeys,
                                    String delimiter, boolean truncated,
                                    Set<String> commonPrefixes
    ) {
        Iterables.addAll(this, version);
        this.name = name;
        this.prefix = prefix;
        this.maxKeys = maxKeys;
        this.delimiter = delimiter;
        this.marker = marker;
        this.nextMarker = nextMarker;
        this.commonPrefixes = commonPrefixes;
        this.truncated = truncated;
        this.nextVersionIdMarker = null;
        this.versionIdMarker = null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getCommonPrefixes() {
        return commonPrefixes;
    }

    @Override
    public String getNextVersionIdMarker() {
        return null;
    }

    @Override
    public String getVersionIdMarker() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMarker() {
        return marker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextMarker() {
        return nextMarker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxKeys() {
        return maxKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTruncated() {
        return truncated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commonPrefixes, delimiter, marker, maxKeys, name, prefix, truncated);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ListVersionsResponseImpl other = (ListVersionsResponseImpl) obj;
        return (truncated == other.truncated) &&
                maxKeys == other.maxKeys &&
                Objects.equal(name, other.name) &&
                Objects.equal(prefix, other.prefix) &&
                Objects.equal(marker, other.marker) &&
                Objects.equal(delimiter, other.delimiter) &&
                Objects.equal(commonPrefixes, other.commonPrefixes);

    }
}
