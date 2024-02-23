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

import java.util.Collection;

import javax.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;
import org.jclouds.s3.domain.AccessControlList.CanonicalUserGrantee;
import org.jclouds.s3.domain.AccessControlList.EmailAddressGrantee;
import org.jclouds.s3.domain.AccessControlList.Grant;
import org.jclouds.s3.domain.AccessControlList.GroupGrantee;
import org.jclouds.s3.domain.BucketLogging;
import org.jclouds.s3.reference.S3Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Throwables;

@Singleton
public class BindBucketLoggingToXmlPayload implements Binder {
   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object payload) {
      BucketLogging from = (BucketLogging) payload;
      try {
         request.setPayload(generatePayload(from));
         request.getPayload().getContentMetadata().setContentType(MediaType.TEXT_XML);
         return request;
      } catch (Exception e) {
         Throwables.propagateIfPossible(e);
         throw new RuntimeException("error transforming bucketLogging: " + from, e);
      }
   }

   private String generatePayload(BucketLogging bucketLogging)
         throws ParserConfigurationException, FactoryConfigurationError, TransformerException {
      Document document = createDocument();
      Element rootNode = elem(document, "BucketLoggingStatus", document);
      rootNode.setAttribute("xmlns", S3Constants.S3_REST_API_XML_NAMESPACE);
      Element loggingNode = elem(rootNode, "LoggingEnabled", document);
      elemWithText(loggingNode, "TargetBucket", bucketLogging.getTargetBucket(), document);
      elemWithText(loggingNode, "TargetPrefix", bucketLogging.getTargetPrefix(), document);
      addGrants(elem(loggingNode, "TargetGrants", document),
                bucketLogging.getTargetGrants(),
                document);
      return asString(document);
   }

   static void addGrants(Element grantsNode, Collection<Grant> grants, Document document) {
      for (Grant grant : grants) {
         Element grantNode = elem(grantsNode, "Grant", document);
         Element granteeNode = elem(grantNode, "Grantee", document);
         granteeNode.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

         if (grant.getGrantee() instanceof GroupGrantee) {
            granteeNode.setAttribute("xsi:type", "Group");
            elemWithText(granteeNode, "URI", grant.getGrantee().getIdentifier(), document);
         } else if (grant.getGrantee() instanceof CanonicalUserGrantee) {
            CanonicalUserGrantee grantee = (CanonicalUserGrantee) grant.getGrantee();
            granteeNode.setAttribute("xsi:type", "CanonicalUser");
            elemWithText(granteeNode, "ID", grantee.getIdentifier(), document);
            if (grantee.getDisplayName() != null) {
               elemWithText(granteeNode, "DisplayName", grantee.getDisplayName(), document);
            }
         } else if (grant.getGrantee() instanceof EmailAddressGrantee) {
            granteeNode.setAttribute("xsi:type", "AmazonCustomerByEmail");
            elemWithText(granteeNode, "EmailAddress", grant.getGrantee().getIdentifier(), document);
         }
         elemWithText(grantNode, "Permission", grant.getPermission(), document);
      }
   }
}
