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
package org.jclouds.proxy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jclouds.domain.Credentials;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;

public class ProxyForURITest {

   private Optional<HostAndPort> noHostAndPort = Optional.absent();
   private Optional<Credentials> noCreds = Optional.absent();
   private Optional<HostAndPort> hostAndPort = Optional.of(HostAndPort.fromParts("proxy.example.com", 8080));
   private Optional<Credentials> creds = Optional.of(new Credentials("user", "pwd"));

   private static class MyProxyConfig implements ProxyConfig {
      private boolean useSystem;
      private boolean jvmProxyEnabled;
      private Type type;
      private Optional<HostAndPort> proxy;
      private Optional<Credentials> credentials;
      private Set<Pattern> proxyExcludeUriPatterns;

      MyProxyConfig(boolean useSystem, boolean jvmProxyEnabled, Type type, Optional<HostAndPort> proxy, Optional<Credentials> credentials) {
         this(useSystem, jvmProxyEnabled, type, proxy, credentials, Collections.<Pattern>emptySet());
      }

      MyProxyConfig(boolean useSystem, boolean jvmProxyEnabled, Type type, Optional<HostAndPort> proxy, Optional<Credentials> credentials, Set<Pattern> proxyExcludeUriPatterns) {
         this.useSystem = useSystem;
         this.jvmProxyEnabled = jvmProxyEnabled;
         this.type = type;
         this.proxy = proxy;
         this.credentials = credentials;
         this.proxyExcludeUriPatterns = proxyExcludeUriPatterns;
      }

      @Override
      public boolean useSystem() {
         return useSystem;
      }

      @Override
      public Type getType() {
         return type;
      }

      @Override
      public Optional<HostAndPort> getProxy() {
         return proxy;
      }

      @Override
      public Optional<Credentials> getCredentials() {
         return credentials;
      }

      @Override
      public Set<Pattern> getProxyExcludedPatterns() {
         return proxyExcludeUriPatterns;
      }

      @Override
      public boolean isJvmProxyEnabled() {
         return jvmProxyEnabled;
      }
   }

   @Test
   public void testDontUseProxyForSockets() throws Exception {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.HTTP, hostAndPort, creds);
      ProxyForURI proxy = new ProxyForURI(config);
      Field useProxyForSockets = proxy.getClass().getDeclaredField("useProxyForSockets");
      useProxyForSockets.setAccessible(true);
      useProxyForSockets.setBoolean(proxy, false);
      URI uri = new URI("socket://ssh.example.com:22");
      assertEquals(proxy.apply(uri), Proxy.NO_PROXY);
   }

   @Test
   public void testUseProxyForSockets() throws Exception {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.HTTP, hostAndPort, creds);
      ProxyForURI proxy = new ProxyForURI(config);
      URI uri = new URI("socket://ssh.example.com:22");
      assertEquals(proxy.apply(uri), new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)));
   }

   @Test
   public void testUseProxyForSocketsSettingShouldntAffectHTTP() throws Exception {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.HTTP, hostAndPort, creds);
      ProxyForURI proxy = new ProxyForURI(config);
      Field useProxyForSockets = proxy.getClass().getDeclaredField("useProxyForSockets");
      useProxyForSockets.setAccessible(true);
      useProxyForSockets.setBoolean(proxy, false);
      URI uri = new URI("http://example.com/file");
      assertEquals(proxy.apply(uri), new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)));
   }

   @Test
   public void testHTTPDirect() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.DIRECT, noHostAndPort, noCreds);
      URI uri = new URI("http://example.com/file");
      assertEquals(new ProxyForURI(config).apply(uri), Proxy.NO_PROXY);
   }

   @Test
   public void testHTTPSDirect() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.DIRECT, noHostAndPort, noCreds);
      URI uri = new URI("https://example.com/file");
      assertEquals(new ProxyForURI(config).apply(uri), Proxy.NO_PROXY);
   }

   @Test
   public void testFTPDirect() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.DIRECT, noHostAndPort, noCreds);
      URI uri = new URI("ftp://ftp.example.com/file");
      assertEquals(new ProxyForURI(config).apply(uri), Proxy.NO_PROXY);
   }

   @Test
   public void testSocketDirect() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.DIRECT, noHostAndPort, noCreds);
      URI uri = new URI("socket://ssh.example.com:22");
      assertEquals(new ProxyForURI(config).apply(uri), Proxy.NO_PROXY);
   }

   @Test
   public void testHTTPThroughHTTPProxy() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, false, Proxy.Type.HTTP, hostAndPort, creds);
      URI uri = new URI("http://example.com/file");
      assertEquals(new ProxyForURI(config).apply(uri), new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
            "proxy.example.com", 8080)));
   }

   @Test
   public void testThroughJvmProxy() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(false, true, Proxy.Type.HTTP, noHostAndPort, noCreds);
      URI uri = new URI("http://example.com/file");
      // could return a proxy, could return NO_PROXY, depends on the tester's environment
      assertNotNull(new ProxyForURI(config).apply(uri));
   }

   @Test
   public void testThroughSystemProxy() throws URISyntaxException {
      ProxyConfig config = new MyProxyConfig(true, false, Proxy.Type.HTTP, noHostAndPort, noCreds);
      URI uri = new URI("http://example.com/file");
      // could return a proxy, could return NO_PROXY, depends on the tester's environment
      assertNotNull(new ProxyForURI(config).apply(uri));
   }

   @Test
   public void testJcloudsProxyHostsPreferredOverJvmProxy() throws URISyntaxException {
      ProxyConfig test = new MyProxyConfig(true, true, Proxy.Type.HTTP, hostAndPort, noCreds);
      ProxyConfig jclouds = new MyProxyConfig(false, false, Proxy.Type.HTTP, hostAndPort, noCreds);
      ProxyConfig jvm = new MyProxyConfig(false, true, Proxy.Type.HTTP, noHostAndPort, noCreds);
      URI uri = new URI("http://example.com/file");
      assertEquals(new ProxyForURI(test).apply(uri), new ProxyForURI(jclouds).apply(uri));
      assertNotEquals(new ProxyForURI(test).apply(uri), new ProxyForURI(jvm).apply(uri));
   }

   @Test
   public void testJvmProxyAlwaysPreferredOverSystem() throws URISyntaxException {
      ProxyConfig test = new MyProxyConfig(true, true, Proxy.Type.HTTP, noHostAndPort, noCreds);
      ProxyConfig jvm = new MyProxyConfig(false, true, Proxy.Type.HTTP, noHostAndPort, noCreds);
      URI uri = new URI("http://example.com/file");
      assertEquals(new ProxyForURI(test).apply(uri), new ProxyForURI(jvm).apply(uri));
   }

   @Test
   public void testProxyExcludeList() throws URISyntaxException {
      Pattern excludedPattern = Pattern.compile("http://excluded\\.com.*");
      URI excludedUri = new URI("http://excluded.com/file");
      URI includedUri = new URI("http://example.com/file");
      Set<Pattern> proxyExcludeList = new HashSet<>();
      proxyExcludeList.add(excludedPattern);
      HostAndPort hostAndPort = HostAndPort.fromParts("proxy.example.com", 8080);
      ProxyConfig config = new MyProxyConfig(true, true, Proxy.Type.HTTP, Optional.of(hostAndPort), noCreds, proxyExcludeList);
      Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
      assertEquals(new ProxyForURI(config).apply(includedUri), proxy);
      assertEquals(new ProxyForURI(config).apply(excludedUri), Proxy.NO_PROXY);
   }
}
