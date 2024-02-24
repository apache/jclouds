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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.inject.Inject;

import org.jclouds.aws.util.AWSUtils;
import org.jclouds.date.DateService;
import org.jclouds.ec2.domain.Attachment;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.location.Region;
import org.jclouds.location.Zone;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.xml.sax.Attributes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CreateVolumeResponseHandler extends ParseSax.HandlerForGeneratedRequestWithResult<Volume> {
   protected final DateService dateService;
   protected final Supplier<String> defaultRegion;
   protected final Supplier<Map<String, Supplier<Set<String>>>> regionToZonesSupplier;
   protected final Supplier<Set<String>> zonesSupplier;

   @Inject
   protected CreateVolumeResponseHandler(DateService dateService, @Region Supplier<String> defaultRegion,
            @Zone Supplier<Map<String, Supplier<Set<String>>>> regionToZonesSupplier,
            @Zone Supplier<Set<String>> zonesSupplier) {
      this.dateService = dateService;
      this.defaultRegion = defaultRegion;
      this.regionToZonesSupplier = regionToZonesSupplier;
      this.zonesSupplier = zonesSupplier;
   }

   protected StringBuilder currentText = new StringBuilder();
   
   protected String id;
   protected int size;
   protected String snapshotId;
   protected String availabilityZone;
   protected Volume.Status volumeStatus;
   protected Date createTime;
   protected Set<Attachment> attachments = Sets.newLinkedHashSet();

   protected String volumeId;
   protected String instanceId;
   protected String device;
   protected Attachment.Status attachmentStatus;
   protected Date attachTime;
   protected String volumeType;
   protected Integer iops;
   protected boolean encrypted;

   protected boolean inAttachmentSet;

   protected String region;

   public Volume getResult() {
      return newVolume();
   }

   public void startElement(String uri, String name, String qName, Attributes attrs) {
      if (qName.equals("attachmentSet")) {
         inAttachmentSet = true;
      }
   }

   public void endElement(String uri, String name, String qName) {
      if (qName.equals("volumeId")) {
         if (inAttachmentSet) {
            volumeId = currentText.toString().trim();
         } else {
            id = currentText.toString().trim();
         }
      } else if (qName.equals("size")) {
         size = Integer.parseInt(currentText.toString().trim());
      } else if (qName.equals("availabilityZone")) {
         availabilityZone = currentText.toString().trim();
      } else if (qName.equals("volumeId")) {
         if (inAttachmentSet) {
            volumeId = currentText.toString().trim();
         } else {
            id = currentText.toString().trim();
         }
      } else if (qName.equals("status")) {
         if (inAttachmentSet) {
            attachmentStatus = Attachment.Status.fromValue(currentText.toString().trim());
         } else {
            volumeStatus = Volume.Status.fromValue(currentText.toString().trim());
         }
      } else if (qName.equals("createTime")) {
         createTime = dateService.iso8601DateOrSecondsDateParse(currentText.toString().trim());
      } else if (qName.equals("attachmentSet")) {
         inAttachmentSet = false;
      } else if (qName.equals("instanceId")) {
         instanceId = currentText.toString().trim();
      } else if (qName.equals("snapshotId")) {
         snapshotId = currentText.toString().trim();
         if (snapshotId.equals(""))
            snapshotId = null;
      } else if (qName.equals("device")) {
         device = currentText.toString().trim();
      } else if (qName.equals("attachTime")) {
         attachTime = dateService.iso8601DateOrSecondsDateParse(currentText.toString().trim());
      } else if (qName.equals("volumeType")) {
         volumeType = currentText.toString().trim();
         if (volumeType.equals(""))
            volumeType = null;
      } else if (qName.equals("iops")) {
         iops = Integer.parseInt(currentText.toString().trim());
      } else if (qName.equals("encrypted")) {
         encrypted = Boolean.parseBoolean(currentText.toString().trim());
      } else if (qName.equals("item")) {
         if (inAttachmentSet) {
            attachments.add(new Attachment(region, volumeId, instanceId, device, attachmentStatus, attachTime));
            volumeId = null;
            instanceId = null;
            device = null;
            attachmentStatus = null;
            attachTime = null;
         }

      }
      currentText.setLength(0);
   }

   private Volume newVolume() {
      Volume volume = new Volume(region, id, size, snapshotId, availabilityZone, volumeStatus, createTime,
              volumeType, iops, encrypted, attachments);
      id = null;
      size = 0;
      snapshotId = null;
      availabilityZone = null;
      volumeStatus = null;
      createTime = null;
      attachments = Sets.newLinkedHashSet();
      volumeType = null;
      iops = null;
      encrypted = false;
      return volume;
   }

   public void characters(char[] ch, int start, int length) {
      currentText.append(ch, start, length);
   }

   @Override
   public CreateVolumeResponseHandler setContext(HttpRequest request) {
      super.setContext(request);
      region = AWSUtils.findRegionInArgsOrNull(getRequest());
      if (region == null) {
         Set<String> zones = zonesSupplier.get();
         String zone = findAvailabilityZoneInArgsOrNull(getRequest(), zones);
         if (zone != null) {
            Map<String, Set<String>> regionToZones = Maps.transformValues(regionToZonesSupplier.get(), Suppliers
                     .<Set<String>> supplierFunction());
            for (Entry<String, Set<String>> entry : regionToZones.entrySet()) {
               if (entry.getValue().contains(zone)) {
                  region = entry.getKey();
                  break;
               }

            }
            checkNotNull(region, "zone %s not in %s", zone, regionToZones);
         } else {
            region = defaultRegion.get();
         }
      }
      return this;
   }

   public static String findAvailabilityZoneInArgsOrNull(GeneratedHttpRequest gRequest, Set<String> zones) {
      for (Object arg : gRequest.getInvocation().getArgs()) {
         if (arg instanceof String) {
            String zone = (String) arg;
            if (zones.contains(zone))
               return zone;
         }
      }
      return null;
   }

}
