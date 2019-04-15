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
package org.tinyzip.visitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.tinyzip.parameters.ProgressZipParameters;

public class PathZipper implements Consumer<Path> {

  private final ZipOutputStream zipOut;
  private final Path targetFolder;
  private final ProgressZipParameters parameters;

  public PathZipper(ZipOutputStream zipOut, Path targetFolder, ProgressZipParameters parameters) {
    this.zipOut = zipOut;
    this.targetFolder = targetFolder;
    this.parameters = parameters;
  }

  @Override
  public void accept(Path path) {
    byte data[] = new byte[parameters.getBufferSize()];
    Path relativizePath = targetFolder.relativize(path);

    if (targetFolder.equals(path)) {
      return;
    }

    if (Files.isDirectory(path)) {
      addFolder(relativizePath);
    } else {
      addFile(path, relativizePath, data);
    }

    parameters.notifyObserver(path.toString(), getFileSize(path));
  }

  private long getFileSize(Path path) {
    try {
      if (Files.isDirectory(path)) {
        return 0;
      }

      return Files.size(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addFile(Path path, Path relativizePath, byte[] data) {
    try (InputStream fi = Files.newInputStream(path);
        BufferedInputStream origin = new BufferedInputStream(fi, data.length)) {
      zipOut.putNextEntry(new ZipEntry(relativizePath.toString()));

      int count;
      while ((count = origin.read(data, 0, data.length)) != -1) {
        zipOut.write(data, 0, count);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addFolder(Path path) {
    try {
      zipOut.putNextEntry(new ZipEntry(path.toString() + '/'));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }

  public static long getRecursiveSize(Path path) {
    long totalSize = 0;

    try {
      if (Files.isDirectory(path)) {
        Iterator<Path> iterator = Files.walk(path).iterator();

        while (iterator.hasNext()) {
          Path curPath = iterator.next();

          if (!Files.isDirectory(curPath)) {
            totalSize += Files.size(curPath);
          }
        }


      } else {
        totalSize = Files.size(path);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return totalSize;
  }
}
