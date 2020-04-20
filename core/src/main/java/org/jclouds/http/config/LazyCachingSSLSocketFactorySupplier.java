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
package org.jclouds.http.config;

import com.google.common.base.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A lazy and caching {@link Supplier} of {@link SSLSocketFactory}.
 */
public class LazyCachingSSLSocketFactorySupplier implements Supplier<SSLSocketFactory> {

    private final SSLContext sslContext;
    private final AtomicReference<SSLSocketFactory> sslSocketFactoryReference;

    public LazyCachingSSLSocketFactorySupplier(SSLContext sslContext) {
        this.sslContext = sslContext;
        this.sslSocketFactoryReference = new AtomicReference<>(null);
    }

    @Override
    public SSLSocketFactory get() {
        SSLSocketFactory sslSocketFactory = sslSocketFactoryReference.get();
        if (sslSocketFactory == null) {
            sslSocketFactory = sslContext.getSocketFactory();
            sslSocketFactoryReference.compareAndSet(null, sslSocketFactory);
        }
        return sslSocketFactory;
    }
}
