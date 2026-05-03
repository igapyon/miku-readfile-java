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

```text
upstream test / intent:
  upstream CLI JSON contract compatibility
  duplicate requests, validation errors, root errors, file limits, total byte
  limits, per-file encoding override, range EOF behavior, and range output
  without artificial trailing newline
  symlink, absolute path, parent path, range after EOF, and filesystem-root
  parity cases
  dotfile extension matching parity
  documentation synchronization around README, CLI spec, parity policy, help,
  and distribution contents

java tests:
  jp.igapyon.mikureadfile.docs.DocumentationSyncTest
  scripts/parity-check.mjs

fixtures:
  dynamically generated files under workplace/parity-miku-readfile
  repository documents

focused regression:
  mvn test -Dtest=DocumentationSyncTest
  node scripts/parity-check.mjs
```

```text
upstream test / intent:
  packaged runtime jar behavior for `--version`, `--help`, normal stdin JSON,
  expected failure JSON, malformed stdin, and invalid CLI usage

java tests:
  scripts/smoke-jar.mjs

fixtures:
  dynamically generated files under workplace/smoke-miku-readfile

focused regression:
  mvn package
  node scripts/smoke-jar.mjs
```
