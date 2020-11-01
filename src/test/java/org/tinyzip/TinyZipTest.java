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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.tinyzip.parameters.ZipParameters;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TinyZipTest {
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private static final Path OUT_PATH = Paths.get(TEMP_DIR, "ziptest");
  private static final Path ZIP_FILE = OUT_PATH.resolve("testzip.zip");

  @Before
  public void setUp() throws IOException {
    if (Files.exists(OUT_PATH)) {
      Files.walk(OUT_PATH).sorted(Comparator.reverseOrder()).map(Path::toFile)
          .forEach(File::delete);
    }

    Files.createDirectories(OUT_PATH);
  }

  @Test
  public void zipUnzipNoSplit() throws Exception {
    List<String> EXPECTED_FILES = Arrays.asList("testfiles");
    URL url = this.getClass().getClassLoader().getResource("testfiles");
    TinyZip.zip(ZIP_FILE.toString(), url.toURI().getPath());
    TinyZip.unzip(ZIP_FILE.toString(), OUT_PATH.toString());

    List<Path> actualFiles = Files.list(OUT_PATH).collect(Collectors.toList());

    for (String file : EXPECTED_FILES) {
      assertTrue(actualFiles.contains(OUT_PATH.resolve(file)));
    }
  }

  @Test
  public void excludeBaseFolderName() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testfiles/nestedfolder");
    ZipParameters params = new ZipParameters(false);

    Path unzipPath = OUT_PATH.resolve("unzip");

    TinyZip.zip(ZIP_FILE.toString(), params, url.toURI().getPath());
    TinyZip.unzip(ZIP_FILE, unzipPath);

    assertZipAndUnzipAreEqual(ZIP_FILE, unzipPath);
    assertExpectedChildrenFiles(unzipPath, "loremIpsum.txt");
  }

  @Test
  public void zipFilesAndFolder() throws Exception {
    URL folder = this.getClass().getClassLoader().getResource("testfiles/nestedfolder");
    URL file1 = this.getClass().getClassLoader().getResource("testfiles/text1.txt");
    URL file2 = this.getClass().getClassLoader().getResource("testfiles/text2.txt");

    Path unzipPath = OUT_PATH.resolve("unzip");

    TinyZip.zip(ZIP_FILE.toString(), folder.toURI().getPath(), file1.toURI().getPath(),
        file2.toURI().getPath());
    TinyZip.unzip(ZIP_FILE, unzipPath);

    assertZipAndUnzipAreEqual(ZIP_FILE, unzipPath);
    assertExpectedChildrenFiles(unzipPath, "text1.txt", "text2.txt", "nestedfolder");
  }

  @Test(expected = IllegalArgumentException.class)
  public void duplicatePaths() throws Exception {
    URL file1 = this.getClass().getClassLoader().getResource("testfiles/text1.txt");
    URL file2 = this.getClass().getClassLoader().getResource("testfiles/text1.txt");

    TinyZip.zip(ZIP_FILE.toString(), file1.toURI().getPath(), file2.toURI().getPath());

  }

  @Test
  public void progressListener() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("testfiles");
    AtomicReference<Double> doubleRef = new AtomicReference<Double>(0.0);
    
    ZipParameters params = new ZipParameters(
        (percentage, done) -> {
            doubleRef.set(percentage);
            System.out.println(String.format("%f, done %s", percentage, done));
    });

    Path unzipPath = OUT_PATH.resolve("unzip");

    TinyZip.zip(ZIP_FILE.toString(), params, url.toURI().getPath());
    
    //assert that at the end we reaced 100% of completed job
    assertEquals(100.00, doubleRef.get(), 0.0);
    
    doubleRef.set(0.0);
    
    TinyZip.unzip(ZIP_FILE, unzipPath, params);

    assertEquals(100.00, doubleRef.get(), 0.0);
  }

  private void assertExpectedChildrenFiles(Path unzipPath, String... expectedPaths)
      throws IOException {
    Set<Path> unzipChildren =
        Files.list(unzipPath).map(unzipPath::relativize).collect(Collectors.toSet());

    assertEquals(expectedPaths.length, unzipChildren.size());

    boolean containsAll = unzipChildren
        .containsAll(Arrays.stream(expectedPaths).map(Paths::get).collect(Collectors.toList()));

    assertTrue(containsAll);
  }

  private void assertZipAndUnzipAreEqual(Path zipFile, Path unzipPath) throws IOException {
    Set<Path> unzippedContent = Files.walk(unzipPath).filter(file -> !file.equals(unzipPath))
        .map(file -> unzipPath.relativize(file)).collect(Collectors.toSet());

    ZipFile name = new ZipFile(zipFile.toString());
    Enumeration<? extends ZipEntry> entries = name.entries();

    Set<Path> zippedContent = new TreeSet<>();

    while (entries.hasMoreElements()) {
      ZipEntry zipEntry = entries.nextElement();
      String zipName = zipEntry.getName();

      if (!zipName.equals("/")) {
        zippedContent.add(Paths.get(zipName));
      }
    }

    name.close();

    assertEquals(unzippedContent, zippedContent);
  }

}
