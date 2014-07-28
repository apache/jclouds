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
package org.jclouds.glacier.blobstore.strategy.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import org.jclouds.glacier.GlacierClient;
import org.jclouds.glacier.blobstore.strategy.PollingStrategy;
import org.jclouds.glacier.domain.JobMetadata;
import org.jclouds.glacier.domain.JobStatus;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This implementation waits a fixed amount of time before start polling.
 */
@Singleton
public class BasePollingStrategy implements PollingStrategy {
   public static final long DEFAULT_INITIAL_WAIT = TimeUnit.HOURS.toMillis(3);
   public static final long DEFAULT_TIME_BETWEEN_POLLS = TimeUnit.MINUTES.toMillis(15);

   private final GlacierClient client;
   private final long initialWait;
   private final long timeBetweenPolls;

   public BasePollingStrategy(long initialWait, long timeBetweenPolls, GlacierClient client) {
      this.initialWait = initialWait;
      this.timeBetweenPolls = timeBetweenPolls;
      this.client = checkNotNull(client, "client");
   }

   @Inject
   public BasePollingStrategy(GlacierClient client) {
      this(DEFAULT_INITIAL_WAIT, DEFAULT_TIME_BETWEEN_POLLS, client);
   }

   private boolean inProgress(String job, String vault) {
      JobMetadata jobMetadata = client.describeJob(vault, job);
      return (jobMetadata != null) && (jobMetadata.getStatusCode() == JobStatus.IN_PROGRESS);
   }

   private void waitForJob(String job, String vault) throws InterruptedException {
      Thread.sleep(initialWait);
      while (inProgress(job, vault)) {
         Thread.sleep(timeBetweenPolls);
      }
   }

   private boolean succeeded(String job, String vault) {
      JobMetadata jobMetadata = client.describeJob(vault, job);
      return (jobMetadata != null) && (jobMetadata.getStatusCode() == JobStatus.SUCCEEDED);
   }

   @Override
   public boolean waitForSuccess(String vault, String job) throws InterruptedException {
      // Avoid waiting if the job doesn't exist
      if (client.describeJob(vault, job) == null) {
         return false;
      }
      waitForJob(job, vault);
      return succeeded(job, vault);
   }

}
