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
package org.jclouds.s3.options;

import org.jclouds.http.options.BaseHttpRequestOptions;

public class BucketConfigOptions extends BaseHttpRequestOptions {
    public BucketConfigOptions versioning() {
        this.queryParameters.put("versioning", "true");
        return this;
    }
    public BucketConfigOptions encryption() {
        this.queryParameters.put("encryption", "");
        return this;
    }
    public BucketConfigOptions lifecycle() {
        this.queryParameters.put("lifecycle", "");
        return this;
    }


    public static class Builder {
        public BucketConfigOptions versioning() {
            return new BucketConfigOptions().versioning();
        }
        public BucketConfigOptions encryption() {
            return new BucketConfigOptions().encryption();
        }
        public BucketConfigOptions lifecycle() {
            return new BucketConfigOptions().lifecycle();
        }
    }
}
