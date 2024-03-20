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
package org.jclouds.aws.s3.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.jclouds.aws.s3.filters.AWSRequestAuthorizeSignatureV4;
import org.jclouds.domain.Credentials;
import org.jclouds.http.HttpRequest;
import org.jclouds.reflect.Invocation;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.jclouds.s3.S3Client;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV4;
import org.jclouds.s3.filters.RequestAuthorizeSignatureV4Test;
import org.jclouds.s3.options.ListBucketOptions;
import org.testng.annotations.Test;

import static org.jclouds.reflect.Reflection2.method;
import static org.testng.Assert.assertEquals;

/**
 * Tests behavior of {@code AWSRequestAuthorizeSignatureV4}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "AwsRequestAuthorizeSignatureV4Test")
public class AwsRequestAuthorizeSignatureV4Test extends RequestAuthorizeSignatureV4Test {

    private static final String LIST_BUCKET_AUTHORIZATION_HEADER_RESULT = "AWS4-HMAC-SHA256 Credential=AKIAPAEBI3QI4EXAMPLE/20150203/cn-north-1/s3/aws4_request," +
            " SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=ec72ac5f67bf86e3b95d122f690a2898224f28328d39131c48221a5dcf0c2cee";

    @Override
    public RequestAuthorizeSignatureV4 filter(Credentials creds) {
        return injector(creds).getInstance(AWSRequestAuthorizeSignatureV4.class);
    }


    // JCLOUDS-1631
    @Test
    void testListBucketWithSpecialChars() {
        Invocation invocation = Invocation.create(method(S3Client.class, "listBucket", String.class,
                        ListBucketOptions[].class),
                // Simulating ListBucketOptions.Builder.withPrefix("Folder (`~!@#$%^&*-_+[]'|<>.?) Name/") with manual endpoint:
                ImmutableList.<Object>of(RequestAuthorizeSignatureV4Test.BUCKET_NAME, new ListBucketOptions[0]));

        HttpRequest getObject = GeneratedHttpRequest.builder().method("GET")
                .invocation(invocation)
                .endpoint("https://" + BUCKET_NAME + ".s3.cn-north-1.amazonaws.com.cn/?delimiter=/&prefix=Folder%20%28%60%7E%21%40%23%24%25%5E%26%2A-_%2B%5B%5D%27%7C%3C%3E.%3F%29%20Name/")
                .addHeader(HttpHeaders.HOST, BUCKET_NAME + ".s3.cn-north-1.amazonaws.com.cn")
                .build();


        HttpRequest filtered = filter(temporaryCredentials).filter(getObject);
        assertEquals(filtered.getFirstHeaderOrNull("Authorization"), LIST_BUCKET_AUTHORIZATION_HEADER_RESULT);
    }
}
