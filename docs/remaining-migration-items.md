# Remaining Migration Items

## Current Position

Initial Java runtime skeleton and MVP behavior are implemented.

## Done

- Java 1.8 Maven project foundation
- executable runtime jar configuration
- sources jar configuration
- distribution zip configuration
- core request validation
- UTF-8 / Shift_JIS file read path
- line ending metadata and range reads
- CLI `--help`, `-h`, `--version`, stdin JSON, stdout JSON, and exit codes
- upstream class mapping
- upstream test mapping

## Maintenance Check

- Keep request / result JSON aligned with upstream `miku-readfile`.
- Keep docs and CLI help synchronized.
- Record Java / Node decoder differences when found.

Latest passing result:

```text
2026-05-03
  mvn test
  mvn clean package
  java -jar target/miku-readfile.jar --version
  java -jar target/miku-readfile.jar --help
  README.md stdin JSON smoke
```

## Pending

- Add selected byte-level JSON parity fixtures against upstream Node CLI.
- Decide whether package version should become `0.5.0` for release.
- Expand README with more examples after first package verification.

## Focused Regression List

```bash
mvn test -Dtest=MikuReadfileTest
mvn test -Dtest=MikuReadfileCliTest
mvn test
mvn package
```
