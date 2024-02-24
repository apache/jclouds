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
package org.jclouds.docker.config;

import java.io.File;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import org.jclouds.docker.suppliers.DockerSSLContextSupplier;
import org.jclouds.domain.Credentials;
import org.jclouds.http.okhttp.OkHttpClientSupplier;
import org.jclouds.location.Provider;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;


@Singleton
public class DockerOkHttpClientSupplier implements OkHttpClientSupplier {

    private final DockerSSLContextSupplier dockerSSLContextSupplier;
    private final Supplier<Credentials> creds;

    @Inject
    DockerOkHttpClientSupplier(DockerSSLContextSupplier dockerSSLContextSupplier, @Provider Supplier<Credentials> creds) {
        this.dockerSSLContextSupplier = dockerSSLContextSupplier;
        this.creds = creds;
    }

    @Override
    public OkHttpClient get() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        ConnectionSpec tlsSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
                .build();
        ConnectionSpec cleartextSpec = new ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT)
                .build();
        clientBuilder.connectionSpecs(ImmutableList.of(tlsSpec, cleartextSpec));
        // check if identity and credential are files, to set up sslContext
        if (new File(creds.get().identity).isFile() && new File(creds.get().credential).isFile()) {
           clientBuilder.sslSocketFactory(dockerSSLContextSupplier.get().getSocketFactory());
        }
        return clientBuilder.build();
    }

}
