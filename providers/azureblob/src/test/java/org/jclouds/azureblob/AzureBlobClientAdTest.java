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
package org.jclouds.azureblob;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import org.jclouds.azure.storage.filters.SharedKeyLiteAuthentication;
import org.jclouds.azure.storage.options.ListOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.internal.BaseRestAnnotationProcessingTest;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;

import static org.jclouds.azure.storage.config.AzureStorageProperties.ACCOUNT;
import static org.jclouds.azure.storage.config.AzureStorageProperties.AUTH_TYPE;
import static org.jclouds.azure.storage.config.AzureStorageProperties.TENANT_ID;
import static org.jclouds.reflect.Reflection2.method;
import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "AzureBlobClientAdTest")
public class AzureBlobClientAdTest extends BaseRestAnnotationProcessingTest<AzureBlobClient> {
    @Override
    protected void checkFilters(HttpRequest request) {
        assertEquals(request.getFilters().size(), 1);
        assertEquals(request.getFilters().get(0).getClass(), SharedKeyLiteAuthentication.class);
    }

    @Override
    public AzureBlobProviderMetadata createProviderMetadata() {
        return new AzureBlobProviderMetadata();
    }

    @Override
    protected Properties setupProperties() {
        Properties adProperties = new Properties();
        adProperties.setProperty(TENANT_ID, "tenant");
        adProperties.setProperty(ACCOUNT, "ad-account");
        adProperties.setProperty(AUTH_TYPE, "azureAd");
        return adProperties;
    }

    public void testListContainersAD() throws SecurityException, NoSuchMethodException, IOException {
        Invokable<?, ?> method = method(AzureBlobClient.class, "listContainers", ListOptions[].class);
        GeneratedHttpRequest request = processor.createRequest(method, ImmutableList.of());

        assertRequestLineEquals(request, "GET https://ad-account.blob.core.windows.net/?comp=list HTTP/1.1");
        assertNonPayloadHeadersEqual(request, "x-ms-version: 2017-11-09\n");
        assertPayloadEquals(request, null, null, false);
    }
}
