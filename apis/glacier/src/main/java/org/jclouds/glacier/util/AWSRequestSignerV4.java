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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.util.Closeables2.closeQuietly;

import java.io.IOException;
import java.util.Locale;
import java.util.Map.Entry;

import javax.crypto.Mac;

import org.jclouds.crypto.Crypto;
import org.jclouds.glacier.reference.GlacierHeaders;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

// TODO: Query parameters, not necessary for Glacier
// TODO: Endpoint on buildCredentialScope is being read from the static string. Uncool.
/**
 * Signs requests using the AWSv4 signing algorithm
 *
 * @see <a href="http://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html" />
 */
public final class AWSRequestSignerV4 {

   public static final String AUTH_TAG = "AWS4";
   public static final String HEADER_TAG = "x-amz-";
   public static final String ALGORITHM = AUTH_TAG + "-HMAC-SHA256";
   public static final String TERMINATION_STRING = "aws4_request";
   public static final String REGION = "us-east-1";
   public static final String SERVICE = "glacier";

   private final Crypto crypto;
   private final String identity;
   private final String credential;

   public AWSRequestSignerV4(String identity, String credential, Crypto crypto) {
      this.crypto = checkNotNull(crypto, "crypto");
      this.identity = checkNotNull(identity, "identity");
      this.credential = checkNotNull(credential, "credential");
   }

   private static HashCode buildHashedCanonicalRequest(String method, String endpoint, HashCode hashedPayload,
         String canonicalizedHeadersString, String signedHeaders) {
      return Hashing.sha256().newHasher()
            .putString(method, UTF_8)
            .putString("\n", UTF_8)
            .putString(endpoint, UTF_8)
            .putString("\n", UTF_8)
            .putString("\n", UTF_8)
            .putString(canonicalizedHeadersString, UTF_8)
            .putString("\n", UTF_8)
            .putString(signedHeaders, UTF_8)
            .putString("\n", UTF_8)
            .putString(hashedPayload.toString(), UTF_8)
            .hash();
   }

   private static String createStringToSign(String date, String credentialScope, HashCode hashedCanonicalRequest) {
      return ALGORITHM + "\n" + date + "\n" + credentialScope + "\n" + hashedCanonicalRequest.toString();
   }

   private static String formatDateWithoutTimestamp(String date) {
      return date.substring(0, 8);
   }

   private static String buildCredentialScope(String dateWithoutTimeStamp) {
      return dateWithoutTimeStamp + "/" + REGION + "/" + SERVICE + "/" + TERMINATION_STRING;
   }

   private static Multimap<String, String> buildCanonicalizedHeadersMap(HttpRequest request) {
      Multimap<String, String> headers = request.getHeaders();
      SortedSetMultimap<String, String> canonicalizedHeaders = TreeMultimap.create();
      for (Entry<String, String> header : headers.entries()) {
         if (header.getKey() == null)
            continue;
         String key = header.getKey().toString().toLowerCase(Locale.getDefault());
         // Ignore any headers that are not particularly interesting.
         if (key.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE) || key.equalsIgnoreCase(HttpHeaders.CONTENT_MD5)
               || key.equalsIgnoreCase(HttpHeaders.HOST) || key.startsWith(HEADER_TAG)) {
            canonicalizedHeaders.put(key, header.getValue());
         }
      }
      return canonicalizedHeaders;
   }

   private static String buildCanonicalizedHeadersString(Multimap<String, String> canonicalizedHeadersMap) {
      StringBuilder canonicalizedHeadersBuffer = new StringBuilder();
      for (Entry<String, String> header : canonicalizedHeadersMap.entries()) {
         String key = header.getKey();
         canonicalizedHeadersBuffer.append(key.toLowerCase()).append(':').append(header.getValue()).append('\n');
      }
      return canonicalizedHeadersBuffer.toString();
   }

   private static String buildSignedHeaders(Multimap<String, String> canonicalizedHeadersMap) {
      return Joiner.on(';').join(Iterables.transform(canonicalizedHeadersMap.keySet(), new Function<String, String>() {

         @Override
         public String apply(String input) {
            return input.toLowerCase();
         }
      }));
   }

   private static HashCode buildHashedPayload(HttpRequest request) {
      HashingInputStream his = null;
      try {
         his = new HashingInputStream(Hashing.sha256(),
               request.getPayload() == null ? ByteSource.empty().openStream() : request.getPayload().openStream());
         ByteStreams.copy(his, ByteStreams.nullOutputStream());
         return his.hash();
      } catch (IOException e) {
         throw new HttpException("Error signing request", e);
      } finally {
         closeQuietly(his);
      }
   }

   private static String buildAuthHeader(String accessKey, String credentialScope, String signedHeaders,
         String signature) {
      return ALGORITHM + " " + "Credential=" + accessKey + "/" + credentialScope + "," + "SignedHeaders="
            + signedHeaders + "," + "Signature=" + signature;
   }

   private byte[] hmacSha256(byte[] key, String s) {
      try {
         Mac hmacSHA256 = crypto.hmacSHA256(key);
         return hmacSHA256.doFinal(s.getBytes());
      } catch (Exception e) {
         throw new HttpException("Error signing request", e);
      }
   }

   private String buildSignature(String dateWithoutTimestamp, String stringToSign) {
      byte[] kSecret = (AUTH_TAG + credential).getBytes(UTF_8);
      byte[] kDate = hmacSha256(kSecret, dateWithoutTimestamp);
      byte[] kRegion = hmacSha256(kDate, REGION);
      byte[] kService = hmacSha256(kRegion, SERVICE);
      byte[] kSigning = hmacSha256(kService, TERMINATION_STRING);
      return BaseEncoding.base16().encode(hmacSha256(kSigning, stringToSign)).toLowerCase();
   }

   public HttpRequest sign(HttpRequest request) {
      // Grab the needed data to build the signature
      Multimap<String, String> canonicalizedHeadersMap = buildCanonicalizedHeadersMap(request);
      String canonicalizedHeadersString = buildCanonicalizedHeadersString(canonicalizedHeadersMap);
      String signedHeaders = buildSignedHeaders(canonicalizedHeadersMap);
      String date = request.getFirstHeaderOrNull(GlacierHeaders.ALTERNATE_DATE);
      String dateWithoutTimestamp = formatDateWithoutTimestamp(date);
      String method = request.getMethod();
      String endpoint = request.getEndpoint().getRawPath();
      String credentialScope = buildCredentialScope(dateWithoutTimestamp);
      HashCode hashedPayload = buildHashedPayload(request);

      // Task 1: Create a Canonical Request For Signature Version 4.
      HashCode hashedCanonicalRequest = buildHashedCanonicalRequest(method, endpoint, hashedPayload,
            canonicalizedHeadersString, signedHeaders);

      // Task 2: Create a String to Sign for Signature Version 4.
      String stringToSign = createStringToSign(date, credentialScope, hashedCanonicalRequest);

      // Task 3: Calculate the AWS Signature Version 4.
      String signature = buildSignature(dateWithoutTimestamp, stringToSign);

      // Sign the request
      String authHeader = buildAuthHeader(identity, credentialScope, signedHeaders, signature);
      request = request.toBuilder().replaceHeader(HttpHeaders.AUTHORIZATION, authHeader).build();
      return request;
   }
}
