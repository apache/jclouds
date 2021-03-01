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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

final class XMLHelper {
   static Document createDocument()
         throws ParserConfigurationException, FactoryConfigurationError {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      disableExternalEntityParsing(factory);
      return factory.newDocumentBuilder().newDocument();
   }

   /**
    * Explicitly enable or disable the 'external-general-entities' and
    * 'external-parameter-entities' features of the underlying
    * DocumentBuilderFactory.
    *
    * TODO This is a naive approach that simply tries to apply all known
    * feature name/URL values in turn until one succeeds, or none do.
    *
    * @param factory
    * factory which will have external general and parameter entities enabled
    * or disabled.
    */
   private static void disableExternalEntityParsing(DocumentBuilderFactory factory) {
      // Feature list drawn from:
      // https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing

      /* Enable or disable external general entities */
      String[] externalGeneralEntitiesFeatures = {
            // General
            "http://xml.org/sax/features/external-general-entities",
            // Xerces 1
            "http://xerces.apache.org/xerces-j/features.html#external-general-entities",
            // Xerces 2
            "http://xerces.apache.org/xerces2-j/features.html#external-general-entities",
      };
      disableFeatures(factory, externalGeneralEntitiesFeatures);

      /* Enable or disable external parameter entities */
      String[] externalParameterEntitiesFeatures = {
            // General
            "http://xml.org/sax/features/external-parameter-entities",
            // Xerces 1
            "http://xerces.apache.org/xerces-j/features.html#external-parameter-entities",
            // Xerces 2
            "http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities",
      };
      disableFeatures(factory, externalParameterEntitiesFeatures);
   }

   private static void disableFeatures(DocumentBuilderFactory factory, String[] features) {
      for (String feature : features) {
         try {
            factory.setFeature(feature, false);
            break;
         } catch (ParserConfigurationException e) {
         }
      }
   }

   static void elemWithText(Element node, String name, String text, Document document) {
      text(elem(node, name, document),
           text,
           document);
   }

   static Element elem(Node node, String name, Document document) {
      Element newNode = document.createElement(name);
      node.appendChild(newNode);
      return newNode;
   }

   private static void text(Element node, String value, Document document) {
      if (value == null) {
          // null text values cause exceptions on subsequent call to
          // Transformer to render document, so fail-fast here on bad data.
          throw new IllegalArgumentException("Illegal null text value");
      }
      node.appendChild(document.createTextNode(value));
   }

   /** Serializes the XML document into a string. */
   static String asString(Document document) throws TransformerException {
      StringWriter writer = new StringWriter();
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.transform(new DOMSource(document), new StreamResult(writer));
      return writer.toString();
   }
}

