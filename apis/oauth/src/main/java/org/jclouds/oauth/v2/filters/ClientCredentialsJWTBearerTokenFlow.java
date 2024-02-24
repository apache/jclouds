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

import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.location.Provider;
import org.jclouds.oauth.v2.AuthorizationApi;
import org.jclouds.oauth.v2.config.OAuthConfigFactory;
import org.jclouds.oauth.v2.config.OAuthConfigFactory.OAuthConfig;
import org.jclouds.oauth.v2.domain.ClientCredentialsAuthArgs;
import org.jclouds.oauth.v2.domain.ClientCredentialsClaims;
import org.jclouds.oauth.v2.domain.Token;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Authorizes new Bearer Tokens at runtime by authorizing claims needed for the http request.
 *
 * <h3>Cache</h3>
 * This maintains a time-based Bearer Token cache. By default expires after 59 minutes
 * (the maximum time a token is valid is 60 minutes).
 * This cache and expiry period is system-wide and does not attend to per-instance expiry time
 * (e.g. "expires_in" from Google Compute -- which is set to the standard 3600 seconds).
 */
public class ClientCredentialsJWTBearerTokenFlow implements OAuthFilter {
    private static final Joiner ON_SPACE = Joiner.on(" ");

    private final Supplier<Credentials> credentialsSupplier;
    private final OAuthConfigFactory oauthConfigFactory;
    private final LoadingCache<ClientCredentialsAuthArgs, Token> tokenCache;

    @Inject
    ClientCredentialsJWTBearerTokenFlow(AuthorizeToken loader, @Named(PROPERTY_SESSION_INTERVAL) long tokenDuration,
                                        @Provider Supplier<Credentials> credentialsSupplier,
                                        OAuthConfigFactory oauthConfigFactory) {
        this.credentialsSupplier = credentialsSupplier;
        this.oauthConfigFactory = oauthConfigFactory;
        // since the session interval is also the token expiration time requested to the server make the token expire a
        // bit before the deadline to make sure there aren't session expiration exceptions
        long cacheExpirationSeconds = tokenDuration > 30 ? tokenDuration - 30 : tokenDuration;
        this.tokenCache = CacheBuilder.newBuilder().expireAfterWrite(cacheExpirationSeconds, SECONDS).build(loader);
    }

    static final class AuthorizeToken extends CacheLoader<ClientCredentialsAuthArgs, Token> {
        private final AuthorizationApi api;
        private final long tokenDuration;

        @Inject AuthorizeToken(AuthorizationApi api, @Named(PROPERTY_SESSION_INTERVAL) long tokenDuration) {
            this.api = api;
            this.tokenDuration = tokenDuration;
        }

        long currentTimeSeconds() {
            return System.currentTimeMillis() / 1000;
        }

        @Override public Token load(ClientCredentialsAuthArgs key) throws Exception {
            final long now = currentTimeSeconds();
            final ClientCredentialsClaims claims = ClientCredentialsClaims.create(
                  key.claims().iss(),
                  key.claims().sub(),
                  key.claims().aud(),
                  now + tokenDuration,
                  now,
                  UUID.randomUUID().toString()
            );
            return api.authorize(key.clientId(), claims, key.resource(), key.scope());
        }
    }

    @Override public HttpRequest filter(HttpRequest request) throws HttpException {
        OAuthConfig oauthConfig = oauthConfigFactory.forRequest(request);
        ClientCredentialsClaims claims = ClientCredentialsClaims.create( //
                credentialsSupplier.get().identity, // iss
                credentialsSupplier.get().identity, // sub
                oauthConfig.audience(), // aud
                -1, // placeholder exp for the cache
                -1, // placeholder nbf for the cache
                null // placeholder jti for the cache
        );
        ClientCredentialsAuthArgs authArgs = ClientCredentialsAuthArgs.create(
                credentialsSupplier.get().identity,
                claims,
                oauthConfig.resource(),
                oauthConfig.scopes().isEmpty() ? null : ON_SPACE.join(oauthConfig.scopes())
         );

        Token token = tokenCache.getUnchecked(authArgs);
        String authorization = String.format("%s %s", token.tokenType(), token.accessToken());
        return request.toBuilder().addHeader("Authorization", authorization).build();
    }
}

