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
package org.jclouds.azure.storage.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jclouds.http.HttpRequest;
import org.jclouds.oauth.v2.config.OAuthConfigFactory;
import org.jclouds.oauth.v2.config.OAuthScopes;

import static org.jclouds.azure.storage.config.AzureStorageProperties.ACCOUNT;
import static org.jclouds.oauth.v2.config.OAuthProperties.AUDIENCE;
import static org.jclouds.oauth.v2.config.OAuthProperties.RESOURCE;

public class AzureStorageOAuthConfigFactory implements OAuthConfigFactory {
    private final OAuthScopes scopes;

    @Named(AUDIENCE)
    @Inject(optional = true)
    private String audience;

    @Named(RESOURCE)
    @Inject(optional = true)
    private String resource;

    @Named(ACCOUNT)
    @Inject
    private String account;

    @Inject
    AzureStorageOAuthConfigFactory(OAuthScopes scopes) { this.scopes = scopes; }

    @Override
    public OAuthConfig forRequest(HttpRequest input) {
        String authResource = resource;
        if (authResource == null) {
            authResource = "https://" + account + ".blob.core.windows.net";
        }
        String authAudience = audience;
        if (authAudience == null) {
            authAudience = "https://" + account + ".blob.core.windows.net";
        }
        return OAuthConfig.create(scopes.forRequest(input), authAudience, authResource);
    }
}
