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
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.jclouds.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

public class SSLModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(HostnameVerifier.class).annotatedWith(Names.named("untrusted")).to(LogToMapHostnameVerifier.class);
      bind(new TypeLiteral<Supplier<SSLSocketFactory>>() {
      }).annotatedWith(Names.named("untrusted")).to(new TypeLiteral<UntrustedSSLSocketFactorySupplier>() {
      });
   }

   /**
    * 
    * Used to get more information about HTTPS hostname wrong errors.
    * 
    */
   @Singleton
   public static class LogToMapHostnameVerifier implements HostnameVerifier {
      @Resource
      private Logger logger = Logger.NULL;
      private final Map<String, String> sslMap = new MapMaker().makeMap();

      public boolean verify(String hostname, SSLSession session) {
         String peerHost = session.getPeerHost();
         if (!hostname.equals(peerHost)) {
             String oldPeerHost = sslMap.get(hostname);
             if (oldPeerHost == null || !oldPeerHost.equals(peerHost)) {
                 logger.warn("hostname was %s while session was %s", hostname, peerHost);
                 sslMap.put(hostname, peerHost);
             }
         }
         return true;
      }
   }

   @Singleton
   public static class UntrustedSSLSocketFactorySupplier implements Supplier<SSLSocketFactory> {
      private final Supplier<SSLSocketFactory> sslSocketFactorySupplier;

      @Inject
      UntrustedSSLSocketFactorySupplier(TrustAllCerts trustAllCerts) {
         this.sslSocketFactorySupplier = createSSLSocketFactorySupplier(trustAllCerts);
      }

      private Supplier<SSLSocketFactory> createSSLSocketFactorySupplier(TrustAllCerts trustAllCerts) {
         try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { trustAllCerts }, new SecureRandom());
            return new LazyCachingSSLSocketFactorySupplier(sslContext);
         } catch (Exception e) {
            throw Throwables.propagate(e);
         }
      }

      @Override
      public SSLSocketFactory get() {
         return sslSocketFactorySupplier.get();
      }
   }

   /**
    * 
    * Used to trust all certs
    * 
    */
   @Singleton
   public static class TrustAllCerts implements X509TrustManager {
      public X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
         return;
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
         return;
      }
   }
}
