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
package org.jclouds.sshj;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.or;
import static com.google.common.base.Throwables.getCausalChain;
import static com.google.common.collect.Iterables.any;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.BaseEncoding.base16;
import static org.jclouds.ssh.SshKeys.fingerprintPrivateKey;
import static org.jclouds.ssh.SshKeys.sha1PrivateKey;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Named;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.PTYMode;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.InMemorySourceFile;

import org.jclouds.compute.domain.ExecChannel;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.logging.Logger;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.ssh.SshClient;
import org.jclouds.ssh.SshException;
import org.jclouds.util.Closeables2;
import org.jclouds.util.Throwables2;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.jcraft.jsch.agentproxy.Connector;

/**
 * This class needs refactoring. It is not thread safe.
 */
@SuppressWarnings("unchecked")
public class SshjSshClient implements SshClient {

   private static final class CloseFtpChannelOnCloseInputStream extends FilterInputStream {

      private final SFTPClient sftp;

      private CloseFtpChannelOnCloseInputStream(InputStream proxy, SFTPClient sftp) {
         super(proxy);
         this.sftp = sftp;
      }

      @Override
      public void close() throws IOException {
         super.close();
         if (sftp != null)
            sftp.close();
      }
   }

   private final String toString;

   @Inject(optional = true)
   @Named("jclouds.ssh.max-retries")
   @VisibleForTesting
   int sshRetries = 5;

   @Inject(optional = true)
   @Named("jclouds.ssh.retry-auth")
   @VisibleForTesting
   boolean retryAuth;

   @Inject(optional = true)
   @Named("jclouds.ssh.retryable-messages")
   @VisibleForTesting
   String retryableMessages = "";

   @Inject(optional = true)
   @Named("jclouds.ssh.retry-predicate")
   // NOTE cannot retry io exceptions, as SSHException is a part of the chain
   private Predicate<Throwable> retryPredicate = or(instanceOf(ConnectionException.class),
            instanceOf(ConnectException.class), instanceOf(SocketTimeoutException.class),
            instanceOf(TransportException.class),
            // safe to retry sftp exceptions as they are idempotent
            instanceOf(SFTPException.class));

   @Resource
   @Named("jclouds.ssh")
   protected Logger logger = Logger.NULL;

   @VisibleForTesting
   SSHClientConnection sshClientConnection;
   
   final String user;
   final String host;

   private final BackoffLimitedRetryHandler backoffLimitedRetryHandler;

   public SshjSshClient(BackoffLimitedRetryHandler backoffLimitedRetryHandler, HostAndPort socket,
            LoginCredentials loginCredentials, int timeout, Optional<Connector> agentConnector) {
      this.user = checkNotNull(loginCredentials, "loginCredentials").getUser();
      this.host = checkNotNull(socket, "socket").getHost();
      checkArgument(socket.getPort() > 0, "ssh port must be greater then zero" + socket.getPort());
      checkArgument(loginCredentials.getOptionalPassword().isPresent() || loginCredentials.hasUnencryptedPrivateKey() || agentConnector.isPresent(),
              "you must specify a password, a key or an SSH agent needs to be available");
      this.backoffLimitedRetryHandler = checkNotNull(backoffLimitedRetryHandler, "backoffLimitedRetryHandler");
      if (loginCredentials.hasUnencryptedPrivateKey()) {
         String fingerPrint = fingerprintPrivateKey(loginCredentials.getOptionalPrivateKey().get());
         String sha1 = sha1PrivateKey(loginCredentials.getOptionalPrivateKey().get());
         this.toString = String.format("%s:rsa[fingerprint(%s),sha1(%s)]@%s:%d", loginCredentials.getUser(),
                  fingerPrint, sha1, host, socket.getPort());
      } else if (loginCredentials.getOptionalPassword().isPresent()) {
         this.toString = String.format("%s:pw[%s]@%s:%d", loginCredentials.getUser(),
               base16().lowerCase().encode(md5().hashString(loginCredentials.getOptionalPassword().get(), UTF_8).asBytes()), host,
               socket.getPort());
      } else {
          this.toString = String.format("%s:rsa[ssh-agent]@%s:%d", loginCredentials.getUser(),
                  host, socket.getPort());
      }
      sshClientConnection = SSHClientConnection.builder().hostAndPort(HostAndPort.fromParts(host, socket.getPort()))
               .loginCredentials(loginCredentials).connectTimeout(timeout).sessionTimeout(timeout).agentConnector(agentConnector).build();
   }

   @Override
   public void put(String path, String contents) {
      put(path, Payloads.newStringPayload(checkNotNull(contents, "contents")));
   }

   private void checkConnected() {
      checkState(sshClientConnection.ssh != null && sshClientConnection.ssh.isConnected(),
               "(%s) ssh not connected!", this);
   }

   public interface Connection<T> {
      void clear() throws Exception;

      T create() throws Exception;
   }

   private void backoffForAttempt(int retryAttempt, String message) {
      backoffLimitedRetryHandler.imposeBackoffExponentialDelay(200L, 2, retryAttempt, sshRetries, message);
   }

   protected <T, C extends Connection<T>> T acquire(C connection) {
      String errorMessage = String.format("(%s) error acquiring %s", toString(), connection);
      for (int i = 0; i < sshRetries; i++) {
         try {
            connection.clear();
            logger.debug(">> (%s) acquiring %s", toString(), connection);
            T returnVal = connection.create();
            logger.debug("<< (%s) acquired %s", toString(), returnVal);
            return returnVal;
         } catch (Exception from) {
            try {
               disconnect();
            } catch (Exception e1) {
               logger.warn(from, "<< (%s) error closing connection", toString());
            }
            if (i + 1 == sshRetries) {
               throw propagate(from, errorMessage + " (out of retries - max " + sshRetries + ")");
            } else if (shouldRetry(from)
                     || (Throwables2.getFirstThrowableOfType(from, IllegalStateException.class) != null)) {
               logger.info("<< " + errorMessage + " (attempt " + (i + 1) + " of " + sshRetries + "): "
                        + from.getMessage());
               backoffForAttempt(i + 1, errorMessage + ": " + from.getMessage());
               if (connection != sshClientConnection)
                  connect();
               continue;
            } else {
               throw propagate(from, errorMessage + " (not retryable)");
            }
         }
      }
      throw new AssertionError("should not reach here");
   }

   public void connect() {
      try {
         acquire(sshClientConnection);
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }

   Connection<SFTPClient> sftpConnection = new Connection<SFTPClient>() {

      private SFTPClient sftp;

      @Override
      public void clear() {
         if (sftp != null)
            try {
               sftp.close();
            } catch (IOException e) {
               Throwables.propagate(e);
            }
      }

      @Override
      public SFTPClient create() throws IOException {
         checkConnected();
         sftp = sshClientConnection.ssh.newSFTPClient();
         return sftp;
      }

      @Override
      public String toString() {
         return "SFTPClient()";
      }
   };

   class GetConnection implements Connection<Payload> {
      private final String path;
      private SFTPClient sftp;

      GetConnection(String path) {
         this.path = checkNotNull(path, "path");
      }

      @Override
      public void clear() throws IOException {
         if (sftp != null)
            sftp.close();
      }

      @Override
      public Payload create() throws Exception {
         sftp = acquire(sftpConnection);
         final RemoteFile remoteFile = sftp.getSFTPEngine().open(path);
         final InputStream in = remoteFile.new RemoteFileInputStream() {
            @Override
            public void close() throws IOException {
               try {
                  super.close();
               } finally {
                  remoteFile.close();
               }
            }
         };
         return Payloads.newInputStreamPayload(new CloseFtpChannelOnCloseInputStream(in, sftp));
      }

      @Override
      public String toString() {
         return "Payload(path=[" + path + "])";
      }
   };

   public Payload get(String path) {
      return acquire(new GetConnection(path));
   }

   class PutConnection implements Connection<Void> {
      private final String path;
      private final Payload contents;
      private SFTPClient sftp;

      PutConnection(String path, Payload contents) {
         this.path = checkNotNull(path, "path");
         this.contents = checkNotNull(contents, "contents");
      }

      @Override
      public void clear() {
         if (sftp != null)
            try {
               sftp.close();
            } catch (IOException e) {
               Throwables.propagate(e);
            }
      }

      @Override
      public Void create() throws Exception {
         sftp = acquire(sftpConnection);
         try {
            sftp.put(new InMemorySourceFile() {

               @Override
               public String getName() {
                  return path;
               }

               @Override
               public long getLength() {
                  Long length = contents.getContentMetadata().getContentLength();
                  return length != null ? length : -1;
               }

               @Override
               public InputStream getInputStream() throws IOException {
                  return checkNotNull(contents.getInput(), "inputstream for path %s", path);
               }

            }, path);
         } finally {
            contents.release();
         }
         return null;
      }

      @Override
      public String toString() {
         return "Put(path=[" + path + "])";
      }
   };

   @Override
   public void put(String path, Payload contents) {
      acquire(new PutConnection(path, contents));
   }

   @VisibleForTesting
   boolean shouldRetry(Exception from) {
      Predicate<Throwable> predicate = retryAuth ? Predicates.<Throwable> or(retryPredicate,
               instanceOf(AuthorizationException.class), instanceOf(UserAuthException.class)) : retryPredicate;
      if (any(getCausalChain(from), predicate))
         return true;
      if (!retryableMessages.equals(""))
         return any(Splitter.on(",").split(retryableMessages), causalChainHasMessageContaining(from));
      return false;
   }

   @VisibleForTesting
   Predicate<String> causalChainHasMessageContaining(final Exception from) {
      return new Predicate<String>() {

         @Override
         public boolean apply(final String input) {
            return any(getCausalChain(from), new Predicate<Throwable>() {

               @Override
               public boolean apply(Throwable arg0) {
                  return (arg0.toString().indexOf(input) != -1)
                           || (arg0.getMessage() != null && arg0.getMessage().indexOf(input) != -1);
               }

            });
         }

      };
   }

   @VisibleForTesting
   SshException propagate(Exception e, String message) {
      message += ": " + e.getMessage();
      logger.error(e, "<< " + message);
      if (e instanceof UserAuthException)
         throw new AuthorizationException("(" + toString() + ") " + message, e);
      throw e instanceof SshException ? SshException.class.cast(e) : new SshException(
               "(" + toString() + ") " + message, e);
   }

   @Override
   public String toString() {
      return toString;
   }

   @PreDestroy
   public void disconnect() {
      try {
         sshClientConnection.clear();
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }

   @Override
   public boolean isConnected() {
      try {
         return sshClientConnection.getSSHClient().isConnected();
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
   }

   protected Connection<Session> execConnection() {

      return new Connection<Session>() {

         private Session session = null;

         @Override
         public void clear() throws TransportException, ConnectionException {
            if (session != null)
               session.close();
         }

         @Override
         public Session create() throws Exception {
            checkConnected();
            session = sshClientConnection.ssh.startSession();
            session.allocatePTY("vt100", 80, 24, 0, 0, ImmutableMap.<PTYMode, Integer> of());
            return session;
         }

         @Override
         public String toString() {
            return "Session()";
         }
      };

   }

   class ExecConnection implements Connection<ExecResponse> {
      private final String command;
      private Session session;

      ExecConnection(String command) {
         this.command = checkNotNull(command, "command");
      }

      @Override
      public void clear() throws TransportException, ConnectionException {
         if (session != null)
            session.close();
      }

      @Override
      public ExecResponse create() throws Exception {
         try {
            session = acquire(execConnection());
            Command output = session.exec(checkNotNull(command, "command"));
            String outputString = IOUtils.readFully(output.getInputStream()).toString();
            output.join(sshClientConnection.getSessionTimeout(), TimeUnit.MILLISECONDS);
            String errorString = IOUtils.readFully(output.getErrorStream()).toString();
            return new ExecResponse(outputString, errorString, output.getExitStatus());
         } finally {
            clear();
         }
      }

      @Override
      public String toString() {
         return "ExecResponse(command=[" + command + "])";
      }
   }

   public ExecResponse exec(String command) {
      return acquire(new ExecConnection(command));
   }

   protected Connection<Session> noPTYConnection() {

      return new Connection<Session>() {

         private Session session = null;
         private SSHClient sshClientConnection;

         @Override
         public void clear() throws TransportException, ConnectionException {
            if (session != null)
               session.close();
            if (sshClientConnection != null)
               Closeables2.closeQuietly(sshClientConnection);
         }

         @Override
         public Session create() throws Exception {
            this.sshClientConnection = acquire(SSHClientConnection.builder().fromSSHClientConnection(
                     SshjSshClient.this.sshClientConnection).sessionTimeout(0).build());
            session = sshClientConnection.startSession();
            return session;
         }

         @Override
         public String toString() {
            return "Session()";
         }
      };

   }

   class ExecChannelConnection implements Connection<ExecChannel> {
      private final String command;
      private Command output;
      private Connection<Session> connection;

      ExecChannelConnection(String command) {
         this.command = checkNotNull(command, "command");
      }

      @Override
      public void clear() {
         Closeables2.closeQuietly(output);
         try {
             if (connection != null) {
                 connection.clear();
             }
         } catch (Throwable e) {
             Throwables.propagate(e);
         }
      }

      @Override
      public ExecChannel create() throws Exception {
         connection = noPTYConnection();
         output = SessionChannel.class.cast(acquire(connection)).exec(command);
         return new ExecChannel(output.getOutputStream(), output.getInputStream(), output.getErrorStream(),
                  new Supplier<Integer>() {

                     @Override
                     public Integer get() {
                        return output.getExitStatus();
                     }

                  }, new Closeable() {

                     @Override
                     public void close() throws IOException {
                        clear();
                     }

                  });

      }

      @Override
      public String toString() {
         return "ExecChannel(command=[" + command + "])";
      }
   }

   @Override
   public ExecChannel execChannel(String command) {
      return acquire(new ExecChannelConnection(command));
   }

   @Override
   public String getHostAddress() {
      return this.host;
   }

   @Override
   public String getUsername() {
      return this.user;
   }

}
