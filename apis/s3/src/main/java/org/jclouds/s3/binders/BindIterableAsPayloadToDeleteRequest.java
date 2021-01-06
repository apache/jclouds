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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.hash.Hashing.md5;
import static org.jclouds.s3.binders.XMLHelper.asString;
import static org.jclouds.s3.binders.XMLHelper.createDocument;
import static org.jclouds.s3.binders.XMLHelper.elem;
import static org.jclouds.s3.binders.XMLHelper.elemWithText;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import org.jclouds.http.HttpRequest;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.rest.Binder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BindIterableAsPayloadToDeleteRequest implements Binder {

   @SuppressWarnings("unchecked")
   @Override
   public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      checkArgument(checkNotNull(input, "input is null") instanceof Iterable,
         "this binder is only valid for an Iterable");
      checkNotNull(request, "request is null");

      Iterable<String> keys = (Iterable<String>) input;
      checkArgument(!Iterables.isEmpty(keys), "The list of keys should not be empty.");

      String content;
      try {
         Document document = createDocument();
         Element rootNode = elem(document, "Delete", document);
         for (String key : keys) {
            Element objectNode = elem(rootNode, "Object", document);
            elemWithText(objectNode, "Key", key, document);
         }

         content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + asString(document);
      } catch (ParserConfigurationException | TransformerException pce) {
         throw Throwables.propagate(pce);
      }

      Payload payload = Payloads.newStringPayload(content);
      payload.getContentMetadata().setContentType(MediaType.TEXT_XML);
      byte[] md5 = md5().hashString(content, UTF_8).asBytes();
      payload.getContentMetadata().setContentMD5(md5);
      request.setPayload(payload);
      return request;
   }
}
