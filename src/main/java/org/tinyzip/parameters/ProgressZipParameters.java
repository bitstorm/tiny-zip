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
 * THIS CLASS IS MEANT FOR INTERNAL USE ONLY AND IS NOT PART OF THE PUBLIC API.
 */
public class ProgressZipParameters extends ZipParameters {
  
  /** The total file size. */
  private final long totalFileSize;
  
  /** The processed file size. */
  private volatile long processedFileSize;

  /**
   * Sets the processed file size.
   *
   * @param processedFileSize the new processed file size
   */
  public void setProcessedFileSize(long processedFileSize) {
    this.processedFileSize = processedFileSize;
  }

  /**
   * Instantiates a new progress zip parameters.
   *
   * @param includeBaseFolderName the include base folder name
   * @param progressObserver the progress observer
   * @param bufferSize the buffer size
   * @param totalFileSize the total file size
   */
  public ProgressZipParameters(boolean includeBaseFolderName,
      Optional<BiConsumer<Double, String>> progressObserver, int bufferSize, long totalFileSize) {
    super(progressObserver, bufferSize, includeBaseFolderName);
    this.totalFileSize = totalFileSize;
    this.processedFileSize = 0;
  }

  /**
   * Instantiates a new progress zip parameters.
   *
   * @param parameters the parameters
   * @param totalFileSize the total file size
   */
  public ProgressZipParameters(ZipParameters parameters, long totalFileSize) {
    this(parameters.isIncludeBaseFolderName(), parameters.getProgressObserver(),
        parameters.getBufferSize(), totalFileSize);
  }

  /**
   * Gets the total file size.
   *
   * @return the total file size
   */
  public long getTotalFileSize() {
    return Double.valueOf(totalFileSize).longValue();
  }

  /**
   * Gets the processed file size.
   *
   * @return the processed file size
   */
  public long getProcessedFileSize() {
    return processedFileSize;
  }

  /**
   * Notify observer.
   *
   * @param filePath the file path
   * @param entrySize the entry size
   */
  public void notifyObserver(String filePath, long entrySize) {
    processedFileSize += entrySize;
    getProgressObserver().ifPresent(
        observer -> observer.accept((double) processedFileSize / totalFileSize * 100, filePath));
  }
}
