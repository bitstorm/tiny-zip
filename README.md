# TinyZip, the missing Java ZIP library

Java offers support for the ZIP file format through package `java.util.zip` package since its very first versions. However this kind of support is limited to I/O streams and algorithm implementation, without any utility class for the file system.
That's why so many projects (both open and closed source) have their own version of `ZipUtils` class that try to fill the gap!  
This library aims to finally offer a light, not over-bloated solution that avoids to reinvent the wheel each time we need to work with ZIP files.  
In addition, as its name suggest TinyZip has been designed to be as small as possible: it weights less than 15 kb, isn't tiny enough for you :-)?

## Technical specs and license

The library is entirely based on Java NIO package and it has no additional dependencies. Java 8 is required as minimum version.  
TinyZip is released under under the terms of the Apache Software Foundation license, version 2.0. The text is included in the file LICENSE in the root of the project.

## Maven dependency
```
<dependency>
	<groupId>io.github.bitstorm</groupId>
	<artifactId>tinyzip-core</artifactId>
	<version>1.0.0</version>
</dependency>	
```

## Basic usage

#### Zipping files

The basic usage requires only the path to the zip file you want to create along with a list of file/folders you want to zip.

```
TinyZip.zip("/path/to/my/zip/myzip.zip", "/foo/", "/bar/fooBar.txt", "baz.java");
```

All previous parameters can be also expressed as `Path` instances:

```
Path zipPath = ...
Path fooPath = ...
Path barPath = ...
Path bazPath = ...

TinyZip.zip(zipPath, fooPath, barPath, bazPath);
```

#### Unzipping files

Unzipping just requires the path to a zip file and the path to a destination folder:

```
TinyZip.unzip("/path/to/my/zip/myzip.zip", "/dest");
```

Just like `zip` method also `unzip` can be used with class `Path` instead of `String`

```
Path pathTozip = ...
Path destZip = ...

TinyZip.unzip(pathTozip, destZip);
```
## Advanced usage

Zipping/unzipping process can be customized through class `ZipParameters`. Here is a list of its properties along with a short description:

*  __bufferSize__: The size in bytes of the buffer used to read/write the zip streams (ZipInputStream and ZipOutputStream). Its default value is 4096.
*  __includeBaseFolderName__: This flag says if the name of a folder will be included at the root of the zip file. _The flag is considered only if we are zipping a single folder_, i.e.:

```
   ZipParameters params = new ZipParameters(false);
   TinyZip.zip("/path/of/my.zip", params, "/path/to/folder")
```

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Its default value is __true__


*  __progressObserver__: The observer that will be notified about the progress of the zip/unzip process. It receives two
parameters: a string representing the current file being processed, and a double value indicating the percentage of work done so far. The first parameter is the file we are compressing during zipping operations, while it's the ZipEntry path we are extracting during unzipping operations.

In the next section we will see an example for __progressObserver__.

#### Progress monitoring
TinyZip allows to keep track of the progress for the current zip/unzip operation. This can be done specifying an _observer_ in `ZipParameters`. This observer is a standard Java `BiConsumer` that takes in input the following two parameters: 

* A double value representing the percentage of work completed so far.
* A string value representing the path of the last file processed. When we are zipping this value is the path of the last file we have compressed. When we are unzipping this value is the path of the last `ZipEntry` we have extracted. 

For example:

```
//set a simple observer that prints progress informations on standard output
ZipParameters params = new ZipParameters((percentage, currentFile) 
	-> System.out.println(String.format("%f, done %s", percentage, currentFile)));
	
TinyZip.zip("/path/to/my/zip/myzip.zip", params, "/foo/", "/bar/fooBar.txt", "baz.java");
// do some stuff...
TinyZip.unzip("/path/to/my/zip/myzip.zip", "/foo", params);		
```
#### Using streams
Zipping and unzipping operations can also be performed on IO streams object rather than directly on file. This allows to use custom streams to implement advanced functionalities like data encryption or splitting output over multiple files. For example:

```
//inizialize output stream
CipherOutputStream myChiperOutputStream = ...

TinyZip.zip(myChiperOutputStream, "/foo/", "/bar/fooBar.txt", "baz.java");

//inizialize input stream
CipherInputStream myChiperInputStream = ...

TinyZip.unzip(myChiperInputStream, "/dest");
```
