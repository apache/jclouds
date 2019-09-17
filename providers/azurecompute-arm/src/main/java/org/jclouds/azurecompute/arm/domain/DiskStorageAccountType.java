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
package org.jclouds.azurecompute.arm.domain;

/**
 * DiskStorageAccountTypes used in Azure.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/compute/disks/createorupdate#diskstorageaccounttypes">this page</a>
 */
public enum DiskStorageAccountType
{
    STANDARD_HDD("Standard_LRS"),
    STANDARD_SSD("StandardSSD_LRS"),
    PREMIUM_SSD("Premium_LRS"),
    ULTRA_SSD("UltraSSD_LRS"); // Available only if your subscription is enabled for ultra disks

    private final String name;

    DiskStorageAccountType(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
