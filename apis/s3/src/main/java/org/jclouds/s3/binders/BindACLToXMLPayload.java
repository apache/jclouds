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
package org.jclouds.s3.binders;

import java.util.Properties;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;
import org.jclouds.s3.domain.AccessControlList;
import org.jclouds.s3.reference.S3Constants;

import com.google.common.base.Throwables;
import com.jamesmurty.utils.XMLBuilder;

import static org.jclouds.s3.binders.BindBucketLoggingToXmlPayload.addGrants;

@Singleton
public class BindACLToXMLPayload implements Binder {
   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object payload) {
      AccessControlList from = (AccessControlList) payload;
      Properties outputProperties = new Properties();
      outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
      try {
         String stringPayload = generateBuilder(from).asString(outputProperties);
         request.setPayload(stringPayload);
         request.getPayload().getContentMetadata().setContentType(MediaType.TEXT_XML);
         return request;
      } catch (Exception e) {
         Throwables.propagateIfPossible(e);
         throw new RuntimeException("error transforming acl: " + from, e);
      }
   }

   protected XMLBuilder generateBuilder(AccessControlList acl) throws ParserConfigurationException,
         FactoryConfigurationError {
      XMLBuilder rootBuilder = XMLBuilder.create("AccessControlPolicy").attr("xmlns",
            S3Constants.S3_REST_API_XML_NAMESPACE);
      if (acl.getOwner() != null) {
         XMLBuilder ownerBuilder = rootBuilder.elem("Owner");
         ownerBuilder.elem("ID").text(acl.getOwner().getId());
         if (acl.getOwner().getDisplayName() != null) {
            ownerBuilder.elem("DisplayName").text(acl.getOwner().getDisplayName());
         }
      }
      addGrants(rootBuilder.elem("AccessControlList"), acl.getGrants());
      return rootBuilder;
   }
}
