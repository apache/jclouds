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
package org.jclouds.blobstore.options;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

/**
 * Contains options supported in the list container operation. <h2>
 * Usage</h2> The recommended way to instantiate a ListOptions object is to statically import
 * ListContainerOptions.* and invoke a static creation method followed by an instance mutator (if
 * needed):
 * <p/>
 * <code>
 * import static org.jclouds.blobstore.options.ListContainerOptions.Builder.*
 * <p/>
 * BlobStore connection = // get connection
 * Future<ListResponse<ResourceMetadata>> list = connection.list("container",prefix("home/users").maxResults(1000));
 * <code>
 */
public class ListContainerOptions extends ListOptions implements Cloneable {

   public static final ImmutableListContainerOptions NONE = new ImmutableListContainerOptions(
            new ListContainerOptions());

   private String delimiter;
   private String dir;
   private String prefix;
   private boolean recursive;
   private boolean detailed;
   private boolean versions;

   public ListContainerOptions() {
   }

   ListContainerOptions(Integer maxKeys, String marker, String dir, boolean recursive,
                        boolean detailed, String prefix, String delimiter) {
      super(maxKeys, marker);
      this.dir = dir;
      this.recursive = recursive;
      this.detailed = detailed;
      this.prefix = prefix;
      this.delimiter = delimiter;
   }
   ListContainerOptions(Integer maxKeys, String marker, String dir, boolean recursive,
                        boolean detailed, String prefix, String delimiter, boolean versions) {
      this(maxKeys, marker, dir, recursive, detailed, prefix, delimiter);
      this.versions = versions;
   }

   public static class ImmutableListContainerOptions extends ListContainerOptions {
      private final ListContainerOptions delegate;

      @Override
      public ListContainerOptions afterMarker(String marker) {
         throw new UnsupportedOperationException();
      }

      public ImmutableListContainerOptions(ListContainerOptions delegate) {
         this.delegate = delegate;
      }

      @Override
      public String getDir() {
         return delegate.getDir();
      }

      @Override
      public ListContainerOptions inDirectory(String dir) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isDetailed() {
         return delegate.isDetailed();
      }

      @Override
      public boolean isRecursive() {
         return delegate.isRecursive();
      }

      @Override
      public boolean isVersions() {
         return delegate.isVersions();
      }


      @Override
      public ListContainerOptions maxResults(int maxKeys) {
         throw new UnsupportedOperationException();
      }

      @Override
      public ListContainerOptions recursive() {
         throw new UnsupportedOperationException();

      }

      @Override
      public String getMarker() {
         return delegate.getMarker();
      }

      @Override
      public Integer getMaxResults() {
         return delegate.getMaxResults();
      }

      @Override
      public String getPrefix() {
         return delegate.getPrefix();
      }

      @Override
      public ListContainerOptions prefix(String prefix) {
         throw new UnsupportedOperationException();
      }

      @Override
      public ListContainerOptions clone() {
         return delegate.clone();
      }

      @Override
      public ListContainerOptions delimiter(String delimiterString) {
         throw new UnsupportedOperationException();
      }

      @Override
      public String getDelimiter() {
         return delegate.getDelimiter();
      }

      @Override
      public String toString() {
         return delegate.toString();
      }

   }

   /**
    * @deprecated superseded by ListContainerOptions.getPrefix and ListContainerOptions.getDelimiter.
    */
   @Deprecated
   public String getDir() {
      return dir;
   }

   public String getDelimiter() {
      return delimiter;
   }

   public boolean isRecursive() {
      return recursive;
   }

   public boolean isDetailed() {
      return detailed;
   }

   public boolean isVersions() { return versions; }

   public String getPrefix() {
      return prefix;
   }

   /**
    * This will list the contents of a virtual or real directory path.
    *
    * @deprecated superseded by ListContainerOptions.prefix and ListContainerOptions.delimiter.
    */
   @Deprecated
   public ListContainerOptions inDirectory(String dir) {
      checkNotNull(dir, "dir");
      checkArgument(!dir.equals("/"), "dir must not be a slash");
      this.dir = dir;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ListContainerOptions afterMarker(String marker) {
      return (ListContainerOptions) super.afterMarker(marker);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ListContainerOptions maxResults(int maxKeys) {
      return (ListContainerOptions) super.maxResults(maxKeys);
   }

   /**
    * return a listing of all objects inside the store, recursively.
    */
   public ListContainerOptions recursive() {
      this.recursive = true;
      return this;
   }

   /**
    * populate each result with detailed such as metadata even if it incurs extra requests to the
    * service.
    */
   public ListContainerOptions withDetails() {
      this.detailed = true;
      return this;
   }

   /**
    * Only list keys that start with the supplied prefix
    */
   public ListContainerOptions prefix(String prefix) {
      this.prefix = prefix;
      return this;
   }

   /**
    * specify the delimiter to be used when listing
    *
    */
   public ListContainerOptions delimiter(String delimiterString) {
      this.delimiter = delimiterString;
      return this;
   }

   /**
    * return a listing of all objects inside the store, recursively.
    */
   public ListContainerOptions versions() {
      // checkArgument(path == null, "path and recursive combination currently not supported");
      this.versions = true;
      return this;
   }

   public static class Builder {

      /**
       * @see ListContainerOptions#inDirectory(String)
       * @deprecated superseded by ListContainerOptions.prefix and ListContainerOptions.delimiter.
       */
      @Deprecated
      public static ListContainerOptions inDirectory(String directory) {
         ListContainerOptions options = new ListContainerOptions();
         return options.inDirectory(directory);
      }

      /**
       * @see ListContainerOptions#afterMarker(String)
       */
      public static ListContainerOptions afterMarker(String marker) {
         ListContainerOptions options = new ListContainerOptions();
         return options.afterMarker(marker);
      }

      /**
       * @see ListContainerOptions#maxResults(int)
       */
      public static ListContainerOptions maxResults(int maxKeys) {
         ListContainerOptions options = new ListContainerOptions();
         return options.maxResults(maxKeys);
      }

      /**
       * @see ListContainerOptions#recursive()
       */
      public static ListContainerOptions recursive() {
         ListContainerOptions options = new ListContainerOptions();
         return options.recursive();
      }

      /**
       * @see ListContainerOptions#versions()
       */
      public static ListContainerOptions versions() {
         ListContainerOptions options = new ListContainerOptions();
         return options.versions();
      }
      /**
       * @see ListContainerOptions#withDetails()
       */
      public static ListContainerOptions withDetails() {
         ListContainerOptions options = new ListContainerOptions();
         return options.withDetails();
      }

      /**
       * @see ListContainerOptions#prefix(String)
       */
      public static ListContainerOptions prefix(String prefix) {
         ListContainerOptions options = new ListContainerOptions();
         return options.prefix(prefix);
      }
      /**
        * @see ListContainerOptions#delimiter(String)
        */
      public static ListContainerOptions delimiter(String delimiterString) {
         ListContainerOptions options = new ListContainerOptions();
         return options.delimiter(delimiterString);
      }
   }

   @Override
   public ListContainerOptions clone() {
      return new ListContainerOptions(getMaxResults(), getMarker(), dir, recursive, detailed, prefix, delimiter, versions);
   }

   @Override
   public String toString() {
      return "[dir=" + dir + ", recursive=" + recursive + ", detailed=" + detailed
               + ", prefix=" + prefix + ", marker=" + getMarker()
               + ", delimiter=" + delimiter
               + ", maxResults=" + getMaxResults()
               + ", versions=" + versions + "]";
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(detailed, recursive, dir, getMarker(), getMaxResults(), versions);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ListContainerOptions other = (ListContainerOptions) obj;
      return (detailed == other.detailed) &&
               recursive == other.recursive &&
               versions == other.versions &&
               Objects.equal(dir, other.dir) &&
               Objects.equal(prefix, other.prefix) &&
               Objects.equal(getMarker(), other.getMarker()) &&
               Objects.equal(getMaxResults(), other.getMaxResults());
   }


}
