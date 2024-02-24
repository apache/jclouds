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
package org.jclouds.packet.filters;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequestFilter;
import org.jclouds.location.Provider;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class AddXAuthTokenToRequest implements HttpRequestFilter {

    private final Supplier<Credentials> creds;

    @Inject
    AddXAuthTokenToRequest(@Provider Supplier<Credentials> creds) {
        this.creds = creds;
    }

    @Override
    public HttpRequest filter(HttpRequest request) throws HttpException {
        Credentials currentCreds = checkNotNull(creds.get(), "credential supplier returned null");
        return request.toBuilder().replaceHeader("X-Auth-Token", currentCreds.credential).build();
    }
}
