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
package org.jclouds.ec2.xml;

import jakarta.inject.Inject;

import org.jclouds.date.DateService;
import org.jclouds.ec2.domain.PasswordData;
import org.jclouds.http.functions.ParseSax;

public class GetPasswordDataResponseHandler extends ParseSax.HandlerWithResult<PasswordData> {
   protected final DateService dateService;


   @Inject
   protected GetPasswordDataResponseHandler(DateService dateService) {
      this.dateService = dateService;
   }

   private StringBuilder currentText = new StringBuilder();
   private PasswordData.Builder builder = PasswordData.builder();

   @Override
   public PasswordData getResult() {
      return builder.build();
   }

   public void endElement(String uri, String name, String qName) {
      if (qName.equals("instanceId")) {
         builder.instanceId(currentText.toString().trim());
      } else if (qName.equals("timestamp")) {
         builder.timestamp(dateService.iso8601DateOrSecondsDateParse(currentText.toString().trim()));
      } else if (qName.equals("passwordData")) {
         builder.passwordData(currentText.toString().trim());
      }
      currentText.setLength(0);
   }

   public void characters(char[] ch, int start, int length) {
      currentText.append(ch, start, length);
   }

}
