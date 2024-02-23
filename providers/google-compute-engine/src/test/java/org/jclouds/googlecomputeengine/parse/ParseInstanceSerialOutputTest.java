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
package org.jclouds.googlecomputeengine.parse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;

import jakarta.ws.rs.Consumes;

import org.jclouds.googlecomputeengine.domain.Instance.SerialPortOutput;
import org.jclouds.googlecomputeengine.internal.BaseGoogleComputeEngineParseTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "ParseInstanceSerialOutputTest")
public class ParseInstanceSerialOutputTest extends BaseGoogleComputeEngineParseTest<SerialPortOutput> {

   @Override
   public String resource() {
      return "/instance_serial_port.json";
   }

   @Override @Consumes(APPLICATION_JSON)
   public SerialPortOutput expected() {
      return expected(BASE_URL);
   }

   @Consumes(APPLICATION_JSON)
   public SerialPortOutput expected(String baseUrl) {
      return SerialPortOutput.create(
            URI.create(baseUrl + "/party/zones/us-central1-a/instances/test-instance/serialPort"),
            "console output");
   }

   @Consumes(APPLICATION_JSON)
   public SerialPortOutput expected(String baseUrl, String contents) {
      return SerialPortOutput.create(
              URI.create(baseUrl + "/party/zones/us-central1-a/instances/test-instance/serialPort"),
              contents
      );
   }
}
