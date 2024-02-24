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
package org.jclouds.oauth.v2.filters;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;

import jakarta.inject.Named;

import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.location.Provider;
import org.jclouds.oauth.v2.AuthorizationApi;
import org.jclouds.oauth.v2.config.OAuthConfigFactory;
import org.jclouds.oauth.v2.config.OAuthConfigFactory.OAuthConfig;
import org.jclouds.oauth.v2.domain.ClientSecret;
import org.jclouds.oauth.v2.domain.Token;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

/**
 * Authorizes new Bearer Tokens at runtime by sending up for the http request.
 *
 * To retrieve the Bearer Token, a request of grant_type=client_credentials is
 * used.  The credential supplied is a password.
 *
 * <h3>Cache</h3>
 * This maintains a time-based Bearer Token cache. By default expires after 59 minutes
 * (the maximum time a token is valid is 60 minutes).
 */
public class ClientCredentialsSecretFlow implements OAuthFilter {
    private static final Joiner ON_SPACE = Joiner.on(" ");

    private final Supplier<Credentials> credentialsSupplier;
    private final LoadingCache<ClientSecret, Token> tokenCache;
    private final OAuthConfigFactory oauthConfigFactory;

    @Inject
    ClientCredentialsSecretFlow(AuthorizeToken loader, @Named(PROPERTY_SESSION_INTERVAL) long tokenDuration,
                                @Provider Supplier<Credentials> credentialsSupplier,
                                OAuthConfigFactory oauthConfigFactory) {
        this.credentialsSupplier = credentialsSupplier;
        this.oauthConfigFactory = oauthConfigFactory;
        // since the session interval is also the token expiration time requested to the server make the token expire a
        // bit before the deadline to make sure there aren't session expiration exceptions
        long cacheExpirationSeconds = tokenDuration > 30 ? tokenDuration - 30 : tokenDuration;
        this.tokenCache = CacheBuilder.newBuilder().expireAfterWrite(cacheExpirationSeconds, SECONDS).build(loader);
    }

    static final class AuthorizeToken extends CacheLoader<ClientSecret, Token> {
        private final AuthorizationApi api;

        @Inject AuthorizeToken(AuthorizationApi api) {
            this.api = api;
        }

        @Override public Token load(ClientSecret key) throws Exception {
            return api.authorizeClientSecret(key.clientId(), key.clientSecret(), key.resource(), key.scope());
        }
    }

    @Override public HttpRequest filter(HttpRequest request) throws HttpException {
        OAuthConfig oauthConfig = oauthConfigFactory.forRequest(request);
        ClientSecret client = ClientSecret.create(
                credentialsSupplier.get().identity,
                credentialsSupplier.get().credential,
                oauthConfig.resource(),
                oauthConfig.scopes().isEmpty() ? null : ON_SPACE.join(oauthConfig.scopes())
        );
        Token token = tokenCache.getUnchecked(client);
        String authorization = String.format("%s %s", token.tokenType(), token.accessToken());
        return request.toBuilder().addHeader("Authorization", authorization).build();
    }
}
