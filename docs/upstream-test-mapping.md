# Upstream Test Mapping

This document tracks `upstream test intent -> Java test` mapping for the
`miku-readfile-java` straight conversion.

## Test Mapping

```text
upstream test / intent:
  workplace/upstream/miku-readfile/test/readfile.test.ts
  reads a minimal UTF-8 request
  Shift_JIS extension rules
  per-file encoding override
  range metadata and EOF behavior
  invalid paths
  directory, invalid UTF-8, binary, symlink, root-too-broad failures
  BOM, empty file, final newline, mixed / CR-only line endings
  file and total byte limits
  invalid request shapes
  duplicate requests and partial failure

java tests:
  jp.igapyon.mikureadfile.MikuReadfileTest

fixtures:
  temporary files from JUnit TempDir
  inline request JSON

focused regression:
  mvn test -Dtest=MikuReadfileTest
```

```text
upstream test / intent:
  workplace/upstream/miku-readfile/test/main.test.ts
  --help / --version
  malformed stdin exit code 2
  expected failure exit code 1 with JSON stdout
  invalid CLI usage exit code 2

java tests:
  jp.igapyon.mikureadfile.MikuReadfileCliTest

fixtures:
  inline stdin JSON

focused regression:
  mvn test -Dtest=MikuReadfileCliTest
```
