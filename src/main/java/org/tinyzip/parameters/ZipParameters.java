/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * 
 */
package org.tinyzip.parameters;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Class ZipParameters. Contains parameters for the zip/unzip process.
 *
 * @author adelbene
 */
public class ZipParameters {

  /** The default size in bytes of the buffer used to read/write the zip stream. */
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  /**
   * The observer that will be notified about the progress of the zip/unzip process. It receives two
   * parameters: a string representing the current file being processed, and a double value
   * indicating the percentage of work done so far. The first parameter is the file we are
   * compressing during zipping operations, while it's the ZipEntry path we are extracting during
   * unzipping operations.
   * 
   */
  private final Optional<BiConsumer<Double, String>> progressObserver;

  /** The size in bytes of the buffer used to read/write the zip stream. */
  private final int bufferSize;

  /**
   * This flag says if the name of a folder will be included at the root of the zip file. The flag
   * is considered only if we are zipping a single folder.
   */
  private final boolean includeBaseFolderName;


  /**
   * Instantiates a new zip parameters.
   */
  public ZipParameters() {
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.progressObserver = Optional.ofNullable(null);
    this.includeBaseFolderName = true;
  }

  /**
   * Instantiates a new zip parameters.
   *
   * @param bufferSize the size in bytes of the buffer used to read/write the zip stream
   */
  public ZipParameters(int bufferSize) {
    this.bufferSize = bufferSize;
    this.progressObserver = Optional.ofNullable(null);
    this.includeBaseFolderName = true;
  }

  /**
   * Instantiates a new zip parameters.
   *
   * @param includeBaseFolderName says if the name of a folder will be included at the root of the
   *        zip file (true) or not (false)
   */
  public ZipParameters(boolean includeBaseFolderName) {
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.progressObserver = Optional.ofNullable(null);
    this.includeBaseFolderName = includeBaseFolderName;
  }

  /**
   * Instantiates a new zip parameters.
   *
   * @param progressObserver the observer that will be notified about the progress of the zip/unzip
   *        process.
   * @param bufferSize the size in bytes of the buffer used to read/write the zip stream
   * @param includeBaseFolderName says if the name of a folder will be included at the root of the
   *        zip file (true) or not (false)
   */
  public ZipParameters(BiConsumer<Double, String> progressObserver, int bufferSize,
      boolean includeBaseFolderName) {
    this.progressObserver = Optional.ofNullable(progressObserver);
    this.bufferSize = bufferSize;
    this.includeBaseFolderName = includeBaseFolderName;
  }

  /**
   * Instantiates a new zip parameters.
   *
   * @param progressObserver the observer that will be notified about the progress of the zip/unzip
   *        process.
   * @param bufferSize the size in bytes of the buffer used to read/write the zip stream
   * @param includeBaseFolderName says if the name of a folder will be included at the root of the
   *        zip file (true) or not (false)
   */
  public ZipParameters(Optional<BiConsumer<Double, String>> progressObserver, int bufferSize,
      boolean includeBaseFolderName) {
    this.progressObserver = progressObserver;
    this.bufferSize = bufferSize;
    this.includeBaseFolderName = includeBaseFolderName;
  }


  /**
   * Instantiates a new zip parameters.
   *
   * @param progressObserver the progress observer
   */
  public ZipParameters(BiConsumer<Double, String> progressObserver) {
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.progressObserver = Optional.ofNullable(progressObserver);
    this.includeBaseFolderName = true;
  }


  /**
   * Instantiates a new zip parameters.
   *
   * @param includeBaseFolderName the include base folder name
   * @param progressObserver the progress observer
   */
  public ZipParameters(boolean includeBaseFolderName, BiConsumer<Double, String> progressObserver) {
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.progressObserver = Optional.ofNullable(progressObserver);
    this.includeBaseFolderName = includeBaseFolderName;
  }

  /**
   * Gets the size in bytes of the buffer used to read/write the zip stream.
   *
   * @return the buffer size
   */
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   * Gets the observer that will be notified about the progress of the zip/unzip process.
   *
   * @return the progress observer
   */
  public Optional<BiConsumer<Double, String>> getProgressObserver() {
    return progressObserver;
  }

  /**
   * Checks if the name of a folder will be included (true) or not (false) at the root of the zip
   * file. The flag is considered only if we are zipping a single folder.
   * 
   * @return true, if the name of a folder will be included at the root of the zip file, false of
   *         only its content will be added to the zip file.
   */
  public boolean isIncludeBaseFolderName() {
    return includeBaseFolderName;
  }
}
