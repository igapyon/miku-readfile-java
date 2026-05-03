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
- initial normalized JSON parity smoke
- CLI specification synchronization with upstream-level contract sections
- jar-level smoke verification for stdout, stderr, and exit code
- expanded normalized JSON parity smoke covering duplicate, validation, root,
  limit, and range edge cases
- normalized JSON parity smoke expanded to 27 cases covering symlink, path
  validation, filesystem root, range-after-EOF behavior, and Node extname edge
  cases
- checked-in normalized parity golden files under docs/parity-golden
- parity script refactored into case definition, execution, output writing, and
  normalization units
- upstream documentation synchronization notes under docs/upstream-doc-sync.md
- GitHub release workflow for CLI runtime jar and sources jar assets
- core runtime responsibilities split into Root, FileReader, and
  FileResultFactory to improve upstream file traceability
- validation responsibilities split into ValidationCommon, EncodingValidator,
  FileValidator, and LimitsValidator
- text shape responsibilities split into LineEndings and LogicalLines
- diagnostic construction split into Diagnostics while Diagnostic remains the
  JSON model

## Maintenance Check

- Keep request / result JSON aligned with upstream `miku-readfile`.
- Keep docs and CLI help synchronized.
- Record Java / Node decoder differences when found.

Latest passing result:

```text
2026-05-03
  mvn test
  mvn clean package
  node scripts/smoke-jar.mjs
  node scripts/parity-check.mjs
  java -jar target/miku-readfile.jar --version
  java -jar target/miku-readfile.jar --help
  README.md stdin JSON smoke
```

## Pending

- Monitor upstream changes and refresh Java parity fixtures, golden files, and
  docs when the upstream contract changes.

## Focused Regression List

```bash
mvn test -Dtest=MikuReadfileTest
mvn test -Dtest=MikuReadfileCliTest
mvn test -Dtest=DocumentationSyncTest
mvn test
mvn package
node scripts/smoke-jar.mjs
node scripts/parity-check.mjs
```
