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
package org.tinyzip;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.tinyzip.parameters.ProgressZipParameters;
import org.tinyzip.parameters.ZipParameters;
import org.tinyzip.visitor.PathZipper;

/**
 * The main class to zip/unzip your archives.
 */
public class TinyZip {

  /**
   * Unzip.
   *
   * @param zipFilePath the path to the zip file to unzip
   * @param destDirectoryPath the destination path to unzip file content
   * @throws IOException
   */
  public static void unzip(String zipFilePath, String destDirectoryPath) throws IOException {
    unzip(zipFilePath, destDirectoryPath, new ZipParameters());
  }

  /**
   * Unzip.
   *
   * @param zipFilePath the path to the zip file to unzip.
   * @param destDirectoryPath the destination path to unzip file content.
   * @param parameters the {@link ZipParameters} to use
   * @throws IOException.
   */
  public static void unzip(String zipFilePath, String destDirectoryPath, ZipParameters parameters)
      throws IOException {
    Path destDir = Paths.get(destDirectoryPath);
    Path zipFile = Paths.get(zipFilePath);

    unzip(zipFile, destDir, parameters);
  }

  /**
   * Unzip.
   *
   * @param zipFilePath the path to the zip file to unzip.
   * @param destDirPath the destination path to unzip file content.
   * @throws IOException.
   */
  public static void unzip(Path zipFilePath, Path destDirPath) throws IOException {
    unzip(zipFilePath, destDirPath, new ZipParameters());
  }

  /**
   * Unzip.
   *
   * @param zipFilePath the path to the zip file to unzip.
   * @param destDirPath the destination path to unzip file content.
   * @param parameters the {@link ZipParameters} to use.
   * @throws IOException.
   */
  public static void unzip(Path zipFilePath, Path destDirPath, ZipParameters parameters)
      throws IOException {
    long uncompressSize = calculateUncompressSize(zipFilePath);

    try (FileChannel fileChanel = FileChannel.open(zipFilePath, StandardOpenOption.READ);
        InputStream inputStreamFile = Channels.newInputStream(fileChanel);) {

      unzip(inputStreamFile, destDirPath, new ProgressZipParameters(parameters, uncompressSize));
    }
  }

  private static long calculateUncompressSize(Path zipFilePath) throws IOException {
    long totalSize = 0;
    FileSystem newFileSystem =
        FileSystems.newFileSystem(zipFilePath, zipFilePath.getClass().getClassLoader());
    Iterator<Path> iterator = newFileSystem.getRootDirectories().iterator();

    while (iterator.hasNext()) {
      Path path = iterator.next();

      long pathSize = PathZipper.getRecursiveSize(path);
      totalSize += Math.max(0, pathSize);
    }

    return totalSize;
  }

  /**
   * Unzip.
   *
   * @param inputStream the input stream to use as source for zipped content.
   * @param destDirPath the destination path to unzip the {@code inpustream} stream source.
   * @param parameters the {@link ZipParameters} to use.
   * @throws IOException.
   */
  public static void unzip(InputStream inputStream, Path destDirPath,
      ProgressZipParameters parameters) throws IOException {

    if (Files.notExists(destDirPath)) {
      Files.createDirectories(destDirPath);
    }

    try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
      ZipEntry entry = zipIn.getNextEntry();

      byte[] bytesBuffer = new byte[parameters.getBufferSize()];

      // iterates over entries in the zip file
      while (entry != null) {
        Path filePath = destDirPath.resolve(entry.getName());

        if (!entry.isDirectory()) {
          // if the entry is a file, extracts it
          extractFile(zipIn, filePath, bytesBuffer);
        } else {
          // if the entry is a directory, make the directory
          Files.createDirectories(filePath);
        }

        long entrySize = entry.getSize();

        parameters.notifyObserver(filePath.toString(), Math.max(0, entrySize));

        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
    }
  }

  /**
   * Extract a single file from the zip input stream.
   *
   * @param zipIn the zip input stream.
   * @param destFilePath the destination path of the zipped file entry.
   * @param bytesBuffer the bytes buffer to use.
   * @throws IOException .
   */
  private static void extractFile(ZipInputStream zipIn, Path destFilePath, byte[] bytesBuffer)
      throws IOException {

    Path parentPath = destFilePath.getParent();

    if (parentPath != null && Files.notExists(parentPath)) {
      Files.createDirectories(parentPath);
    }

    Files.createFile(destFilePath);

    try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(destFilePath))) {
      int read = 0;

      while ((read = zipIn.read(bytesBuffer)) != -1) {
        bos.write(bytesBuffer, 0, read);
      }
    }
  }

  /**
   * Zip.
   *
   * @param zipFilePath the path of the zip file we are creating.
   * @param paths the set of files and folders to zip.
   * @throws IOException.
   */
  public static void zip(String zipFilePath, String... paths) throws IOException {
    ZipParameters parameters = new ZipParameters();

    zip(zipFilePath, parameters, paths);
  }

  /**
   * Zip.
   *
   * @param zipFilePath the path of the zip file we are creating.
   * @param parameters the current {@link ZipParameters} we are using.
   * @param paths the set of files and folders to zip.
   * @throws IOException.
   */
  public static void zip(String zipFilePath, ZipParameters parameters, String... paths)
      throws IOException {

    List<Path> pathList = new ArrayList<>();

    for (int i = 0; i < paths.length; i++) {
      String folder = paths[i];
      pathList.add(Paths.get(folder));
    }

    zip(Paths.get(zipFilePath), parameters, pathList.toArray(new Path[] {}));
  }

  /**
   * Zip.
   *
   * @param zipFilePath the path of the zip file we are creating.
   * @param pathList the set of files and folders to zip.
   * @throws IOException.
   */
  public static void zip(Path zipFilePath, Path... pathList) throws IOException {

    ZipParameters parameters = new ZipParameters();

    zip(zipFilePath, parameters, pathList);
  }

  /**
   * Zip.
   *
   * @param zipFilePath the path of the zip file we are creating.
   * @param parameters the current {@link ZipParameters} we are using.
   * @param pathList the set of files and folders to zip.
   * @throws IOException.
   */
  public static void zip(Path zipFilePath, ZipParameters parameters, Path... pathList)
      throws IOException {
    Files.createFile(zipFilePath);

    try (FileChannel fileChannel = FileChannel.open(zipFilePath, StandardOpenOption.WRITE);
        OutputStream fileOutput = Channels.newOutputStream(fileChannel);) {

      zip(fileOutput, parameters, pathList);
    }
  }

  /**
   * Zip.
   *
   * @param outputStream the output stream that will receive the zipped content.
   * @param parameters the current {@link ZipParameters} we are using.
   * @param pathList the set of files and folders to zip.
   * @throws IOExceptions.
   */
  public static void zip(OutputStream outputStream, ZipParameters parameters, Path... pathList)
      throws IOException {
    checkForDuplicatePaths(pathList);

    long totalFileSize = Arrays.stream(pathList)
        .collect(Collectors.summingLong(path -> PathZipper.getRecursiveSize(path)));

    boolean includeBaseFolderName = includeBaseFolderName(parameters, pathList);

    ProgressZipParameters pathZipperParameters =
        new ProgressZipParameters(parameters, totalFileSize);

    try (ZipOutputStream zipOutput = new ZipOutputStream(outputStream)) {

      for (Path path : pathList) {
        Path targetPath = includeBaseFolderName ? path.getParent() : path;
        Files.walk(path).forEach(new PathZipper(zipOutput, targetPath, pathZipperParameters));
      }
    }
  }

  private static void checkForDuplicatePaths(Path[] pathList) {
    Set<Path> checkSet = new HashSet<>();

    for (int i = 0; i < pathList.length; i++) {
      Path path = pathList[i];

      if (checkSet.contains(path)) {
        String message = String.format("Duplicate path entry found for: '%s'", path);
        throw new IllegalArgumentException(message);
      }

      checkSet.add(path);
    }
  }

  /**
   * Checks if the {@link ZipParameters#isIncludeBaseFolderName()} can be applied to the current zip
   * file. Such parameter id effective only if we are zipping a single folder.
   *
   * @param parameters the current {@link ZipParameters} we are using.
   * @param the set of files and folders to zip.
   * @return true, if folder name must be included, false otherwise.
   */
  private static boolean includeBaseFolderName(ZipParameters parameters, Path... pathList) {
    int pathNum = pathList.length;
    Path parentPath = pathList[0].getParent();

    return pathNum > 1 || (parentPath != null && parameters.isIncludeBaseFolderName());
  }
}
