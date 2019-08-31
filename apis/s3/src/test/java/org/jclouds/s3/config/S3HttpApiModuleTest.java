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
package org.jclouds.s3.config;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.jclouds.location.config.LocationModule;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.providers.JcloudsTestBlobStoreProviderMetadata;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.config.CredentialStoreModule;
import org.jclouds.rest.internal.BaseRestApiTest;
import org.jclouds.s3.blobstore.config.S3BlobStoreContextModule;
import org.jclouds.s3.filters.RequestAuthorizeSignature;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV2;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV4;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_V4_REQUEST_SIGNATURES;
import static org.testng.Assert.assertTrue;

public class S3HttpApiModuleTest {

    @Test
    public void testRequestAuthorizeSignatureV2() {
        RequestAuthorizeSignature requestAuthorizeSignature = getRequestAuthorizeSignature("false");
        assertTrue(requestAuthorizeSignature instanceof RequestAuthorizeSignatureV2);
    }

    @Test
    public void testRequestAuthorizeSignatureV4() {
        RequestAuthorizeSignature requestAuthorizeSignature = getRequestAuthorizeSignature("true");
        assertTrue(requestAuthorizeSignature instanceof RequestAuthorizeSignatureV4);
    }

    private RequestAuthorizeSignature getRequestAuthorizeSignature(final String s3V4Value) {
        AbstractModule abstractModule = new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named(PROPERTY_S3_V4_REQUEST_SIGNATURES)).to(s3V4Value);
                bind(String.class).annotatedWith(Names.named(PROPERTY_SESSION_INTERVAL)).toInstance("60");
                bind(ProviderMetadata.class).to(JcloudsTestBlobStoreProviderMetadata.class);
            }
        };
        List<Module> modules = ImmutableList.<Module>of(new BaseRestApiTest.MockModule(),
                new CredentialStoreModule(new ConcurrentHashMap<String, ByteSource>()),
                new S3BlobStoreContextModule(),
                new NullLoggingModule(),
                new S3HttpApiModule<>(),
                new LocationModule(),
                abstractModule);
        return Guice.createInjector(modules).getInstance(RequestAuthorizeSignature.class);
    }
}
