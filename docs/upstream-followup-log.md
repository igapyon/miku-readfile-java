# Upstream Follow-Up Log

This document records upstream-following checks for `miku-readfile-java`.

## Initial Straight Conversion

```text
upstream file:
  workplace/upstream/miku-readfile/src/*.ts

java classes:
  jp.igapyon.mikureadfile.core.*
  jp.igapyon.mikureadfile.model.*
  jp.igapyon.mikureadfile.cli.*

tests:
  MikuReadfileTest
  MikuReadfileCliTest

diff summary:
  behavior differences:
    - Shift_JIS decoding uses Java Charset `Shift_JIS`; upstream uses
      iconv-lite. Differences should be checked with fixtures when found.
  naming differences:
    - Java package base is `jp.igapyon.mikureadfile`.
    - The Java core facade is `MikuReadfile`.
  unported differences:
    - Node bundle entry is not ported; Java runtime jar is the packaging path.
  Java-side original extensions:
    - Maven package, executable jar, sources jar, and distribution zip.

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      java -jar target/miku-readfile.jar --version
      java -jar target/miku-readfile.jar --help
      printf '{"version":1,"root":".","files":["README.md" ]}' | java -jar target/miku-readfile.jar
  - fixture:
      JUnit TempDir and inline JSON
  - next check viewpoint:
      Add byte-level JSON parity fixtures against upstream CLI output.
```
