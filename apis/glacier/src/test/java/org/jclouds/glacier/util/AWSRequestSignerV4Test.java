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
package org.jclouds.glacier.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.jclouds.encryption.internal.JCECrypto;
import org.jclouds.http.HttpRequest;
import org.testng.annotations.Test;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

@Test(groups = "unit", testName = "AWSRequestSignerV4Test")
public class AWSRequestSignerV4Test {

   @Test
   public void testSignatureCalculation() throws NoSuchAlgorithmException, CertificateException {
      String auth = "AWS4-HMAC-SHA256 " + "Credential=AKIAIOSFODNN7EXAMPLE/20120525/us-east-1/glacier/aws4_request,"
            + "SignedHeaders=host;x-amz-date;x-amz-glacier-version,"
            + "Signature=3ce5b2f2fffac9262b4da9256f8d086b4aaf42eba5f111c21681a65a127b7c2a";
      String identity = "AKIAIOSFODNN7EXAMPLE";
      String credential = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
      AWSRequestSignerV4 signer = new AWSRequestSignerV4(identity, credential, new JCECrypto());
      HttpRequest request = signer.sign(createRequest());
      assertThat(request.getFirstHeaderOrNull("Authorization")).isEqualTo(auth);
   }

   private HttpRequest createRequest() {
      Multimap<String, String> headers = TreeMultimap.create();
      headers.put("Host", "glacier.us-east-1.amazonaws.com");
      headers.put("x-amz-date", "20120525T002453Z");
      headers.put("x-amz-glacier-version", "2012-06-01");
      HttpRequest request = HttpRequest.builder().method("PUT")
            .endpoint("https://glacier.us-east-1.amazonaws.com/-/vaults/examplevault").headers(headers).build();
      return request;
   }

}
