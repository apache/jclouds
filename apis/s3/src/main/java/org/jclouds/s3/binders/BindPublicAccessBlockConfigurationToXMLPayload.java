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

import static org.jclouds.s3.binders.XMLHelper.asString;
import static org.jclouds.s3.binders.XMLHelper.createDocument;
import static org.jclouds.s3.binders.XMLHelper.elem;
import static org.jclouds.s3.binders.XMLHelper.elemWithText;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;
import org.jclouds.s3.domain.PublicAccessBlockConfiguration;
import org.jclouds.s3.reference.S3Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Throwables;

@Singleton
public final class BindPublicAccessBlockConfigurationToXMLPayload implements Binder {
   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object payload) {
      PublicAccessBlockConfiguration configuration = (PublicAccessBlockConfiguration) payload;
      try {
         request.setPayload(generatePayload(configuration));
         request.getPayload().getContentMetadata().setContentType(MediaType.TEXT_XML);
         return request;
      } catch (Exception e) {
         Throwables.propagateIfPossible(e);
         throw new RuntimeException("error transforming configuration: " + configuration, e);
      }
   }

   protected String generatePayload(PublicAccessBlockConfiguration configuration)
         throws ParserConfigurationException, FactoryConfigurationError, TransformerException {
      Document document = createDocument();
      Element rootNode = elem(document, "PublicAccessBlockConfiguration", document);
      rootNode.setAttribute("xmlns", S3Constants.S3_REST_API_XML_NAMESPACE);
      elemWithText(rootNode, "BlockPublicAcls", String.valueOf(configuration.blockPublicAcls()), document);
      elemWithText(rootNode, "IgnorePublicAcls", String.valueOf(configuration.ignorePublicAcls()), document);
      elemWithText(rootNode, "BlockPublicPolicy", String.valueOf(configuration.blockPublicPolicy()), document);
      elemWithText(rootNode, "RestrictPublicBuckets", String.valueOf(configuration.restrictPublicBuckets()), document);
      return asString(document);
   }
}
