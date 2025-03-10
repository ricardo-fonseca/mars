# MARS - MIPS Assembler and Runtime Simulator

This is a fork of the original [MARS - MIPS Assembler and Runtime Simulator](https://dpetersanderson.github.io), developed by Pete Sanderson and Kenneth Vollmar. The main goal was to update the user interface and package it as a self-contained Java application to facilitate distribution.

The work was done in the context of the [Microprocessors](https://fenix-mais.iscte-iul.pt/courses/l0706-284502928656658) undergraduate course at [ISCTE - IUL](https://www.iscte-iul.pt/)


## Compiling

This version of MARS uses `ant` for compilation. To compile the code use `ant build`. Note that this will always recompile the complete source (some additional restructuring is required). You can clean the build using `ant clean`.

To run MARS from the source folder just do `ant run`. Again, this will always recompile the complete source.

## Distribution

To create the file `mars.jar` file wih the application do `ant dist`. The file will be placed in the `dist` directory. You can then copy this file anywhere and run it using `java -jar mars.jar` provided you have a recent JRE installed.

Additionally, you can create self-contained Java applications:

+ macOS Application (`.app`) - `ant app-image`
+ macOS Disk Image (`.dmg`) - `ant dmg`
+ Windows - Not yet supported, use the `mars.jar` file.

### macOS notes

The applications will not be signed, so users will most likely get 'Apple could not verify "MARS" is free of malware' type errors. If this is the case just open the Terminal and issue the following command:

```shell
xattr -r -c /Applications/MARS.app
```

This will clean up the Quarantine flags.

### Homebrew Java

At the time of this writing you cannot use the [Homebrew](https://brew.sh) Java distribution to build the self-contained applications on macOS. The applications will include links to other Homebrew libraries, so installation will fail on systems that don't have Homebrew installed. You will need to use the [OpenJDK distribution](https://jdk.java.net/23/) instead.


## FlatLaf

The source includes a copy of the [FlatLaf](https://www.formdev.com/flatlaf/) look and feel (version 3.5). FlatLaf is open source licensed under the [Apache 2.0 License](https://github.com/JFormDesigner/FlatLaf/blob/master/LICENSE).