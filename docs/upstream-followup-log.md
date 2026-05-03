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

## JSON Parity Smoke

```text
upstream file:
  workplace/upstream/miku-readfile/src/main.ts
  workplace/upstream/miku-readfile/src/readfile.ts
  workplace/upstream/miku-readfile/src/file-reader.ts
  workplace/upstream/miku-readfile/src/file-result.ts
  workplace/upstream/miku-readfile/src/text-shape.ts
  workplace/upstream/miku-readfile/src/range-text.ts

java classes:
  jp.igapyon.mikureadfile.cli.MikuReadfileCli
  jp.igapyon.mikureadfile.core.MikuReadfile
  jp.igapyon.mikureadfile.core.TextShape
  jp.igapyon.mikureadfile.core.RangeText

tests:
  scripts/parity-check.mjs
  DocumentationSyncTest

diff summary:
  behavior differences:
    - No normalized JSON parity differences were found in the current smoke
      fixture set.
  naming differences:
    - none
  unported differences:
    - none for the checked CLI JSON result paths
  Java-side original extensions:
    - `scripts/parity-check.mjs` is a Java-side verification helper.

follow-up:
  - checks performed:
      npm install
      npm run build
      node scripts/parity-check.mjs
  - fixture:
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      Expand fixture coverage and decide whether selected normalized outputs
      should become checked-in golden files.
```

## CLI Specification Synchronization

```text
upstream file:
  workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md
  workplace/upstream/miku-readfile/src/help.ts

java classes:
  jp.igapyon.mikureadfile.cli.HelpText

tests:
  DocumentationSyncTest

diff summary:
  behavior differences:
    - none
  naming differences:
    - Java examples use `java -jar target/miku-readfile.jar`.
  unported differences:
    - Node bundle artifact notes are represented as Java runtime artifact notes.
  Java-side original extensions:
    - `docs/cli-json-parity.md` is included as Java-side parity policy.

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/parity-check.mjs
  - fixture:
      README.md, docs/miku-readfile-cli-spec.md, docs/cli-json-parity.md,
      HelpText, and src/assembly/dist.xml
  - next check viewpoint:
      Keep CLI help compact while maintaining detailed contract coverage in
      docs/miku-readfile-cli-spec.md.
```

## Release Version Policy

```text
upstream file:
  workplace/upstream/miku-readfile/package.json

java classes:
  jp.igapyon.mikureadfile.cli.MikuReadfileCli

tests:
  MikuReadfileCliTest

diff summary:
  behavior differences:
    - none
  naming differences:
    - Java Maven project version is fixed to `0.5.0`.
  unported differences:
    - none
  Java-side original extensions:
    - Maven package and distribution artifact names use version `0.5.0`.

follow-up:
  - checks performed:
      mvn clean package
      java -jar target/miku-readfile.jar --version
  - fixture:
      target/miku-readfile.jar
  - next check viewpoint:
      Keep Java release version aligned with the intended upstream runtime
      version when receiving future upstream changes.
```

## Jar-Level Smoke Verification

```text
upstream file:
  workplace/upstream/miku-readfile/src/main.ts
  workplace/upstream/miku-readfile/src/help.ts

java classes:
  jp.igapyon.mikureadfile.cli.MikuReadfileCli
  jp.igapyon.mikureadfile.cli.HelpText

tests:
  scripts/smoke-jar.mjs

diff summary:
  behavior differences:
    - none
  naming differences:
    - Java runtime is invoked as `java -jar target/miku-readfile.jar`.
  unported differences:
    - none for the checked jar-level CLI paths
  Java-side original extensions:
    - `scripts/smoke-jar.mjs` is a Java-side packaged-runtime smoke helper.

follow-up:
  - checks performed:
      mvn clean package
      node scripts/smoke-jar.mjs
  - fixture:
      dynamically generated files under workplace/smoke-miku-readfile
  - next check viewpoint:
      Keep this smoke focused on packaged jar process behavior; keep broader
      Node/Java result parity in scripts/parity-check.mjs.
```

## Expanded JSON Parity Smoke

```text
upstream file:
  workplace/upstream/miku-readfile/test/readfile.test.ts
  workplace/upstream/miku-readfile/src/readfile.ts
  workplace/upstream/miku-readfile/src/validation.ts
  workplace/upstream/miku-readfile/src/root.ts

java classes:
  jp.igapyon.mikureadfile.core.MikuReadfile
  jp.igapyon.mikureadfile.core.RequestValidator
  jp.igapyon.mikureadfile.core.TextShape
  jp.igapyon.mikureadfile.core.RangeText

tests:
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - `root` pointing to a file initially differed only in diagnostic message.
      Java was aligned to upstream message `root is not a directory`.
    - No normalized JSON parity differences remain in the expanded fixture set.
  naming differences:
    - none
  unported differences:
    - none for the checked CLI JSON result paths
  Java-side original extensions:
    - `scripts/parity-check.mjs` remains a Java-side verification helper.

follow-up:
  - checks performed:
      mvn test
      mvn package
      node scripts/parity-check.mjs
      node scripts/smoke-jar.mjs
  - fixture:
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      Decide whether selected normalized outputs should become checked-in
      golden files.
```

## Parity Script Refactoring

```text
upstream file:
  workplace/upstream/miku-readfile/test/readfile.test.ts

java classes:
  none

tests:
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none
  naming differences:
    - none
  unported differences:
    - none
  Java-side original extensions:
    - `scripts/parity-check.mjs` now separates case definition, execution,
      output writing, and normalization. The case set is currently expanded to
      27 parity cases.

follow-up:
  - checks performed:
      node scripts/parity-check.mjs
  - fixture:
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      If checked-in golden files are adopted, reuse the case metadata emitted by
      the parity script.
```

## Path and Range Parity Expansion

```text
upstream file:
  workplace/upstream/miku-readfile/test/readfile.test.ts
  workplace/upstream/miku-readfile/src/validate-file.ts
  workplace/upstream/miku-readfile/src/root.ts
  workplace/upstream/miku-readfile/src/file-reader.ts
  workplace/upstream/miku-readfile/src/range-text.ts

java classes:
  jp.igapyon.mikureadfile.core.FileValidator
  jp.igapyon.mikureadfile.core.Root
  jp.igapyon.mikureadfile.core.FileReader
  jp.igapyon.mikureadfile.core.RangeText

tests:
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none found
  naming differences:
    - none
  unported differences:
    - unreadable file chmod behavior remains covered by upstream tests but is
      not included in parity smoke because it is platform-permission dependent.
  Java-side original extensions:
    - parity fixture metadata for the expanded cases

follow-up:
  - checks performed:
      node scripts/parity-check.mjs
  - fixture:
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      Consider checked-in normalized golden outputs if parity review becomes
      repetitive.
```

## Diagnostics and Result Refactoring

```text
upstream file:
  workplace/upstream/miku-readfile/src/diagnostics.ts
  workplace/upstream/miku-readfile/src/result.ts

java classes:
  jp.igapyon.mikureadfile.core.Diagnostics
  jp.igapyon.mikureadfile.core.ResultFactory
  jp.igapyon.mikureadfile.model.Diagnostic
  jp.igapyon.mikureadfile.model.MikuReadfileResult
  jp.igapyon.mikureadfile.model.Summary

tests:
  MikuReadfileTest
  MikuReadfileCliTest
  DocumentationSyncTest
  scripts/smoke-jar.mjs
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none intended
  naming differences:
    - Java uses `Diagnostics` for diagnostic construction and keeps
      `Diagnostic` as the JSON model.
  unported differences:
    - none for diagnostics and result aggregation
  Java-side original extensions:
    - none

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/smoke-jar.mjs
      node scripts/parity-check.mjs
  - fixture:
      existing JUnit fixtures and dynamically generated files under
      workplace/smoke-miku-readfile and workplace/parity-miku-readfile
  - next check viewpoint:
      Keep diagnostics helper methods aligned with upstream when adding new
      diagnostic codes.
```

## Encoding Extension Parity

```text
upstream file:
  workplace/upstream/miku-readfile/src/encoding.ts

java classes:
  jp.igapyon.mikureadfile.core.Encoding

tests:
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - Java extension selection was aligned with Node `path.posix.extname` for
      leading-dot filenames, trailing-dot filenames, and leading-dot filenames
      with suffix extensions.
  naming differences:
    - none
  unported differences:
    - Shift_JIS decoding still uses Java Charset while upstream uses
      iconv-lite; keep checking fixture-level differences when found.
  Java-side original extensions:
    - Node extname edge-case parity fixtures

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/smoke-jar.mjs
      node scripts/parity-check.mjs
  - fixture:
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      Keep extension selection aligned with `path.posix.extname` edge cases.
```

## Normalized Golden Parity

```text
upstream file:
  workplace/upstream/miku-readfile/test/readfile.test.ts
  workplace/upstream/miku-readfile/src/main.ts

java classes:
  none

tests:
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none found
  naming differences:
    - none
  unported differences:
    - none
  Java-side original extensions:
    - `docs/parity-golden/*.json` stores upstream-normalized expected CLI
      results with local repository paths replaced by `<repoRoot>`.

follow-up:
  - checks performed:
      node scripts/parity-check.mjs --update-golden
      node scripts/parity-check.mjs
  - fixture:
      docs/parity-golden
      dynamically generated files under workplace/parity-miku-readfile
  - next check viewpoint:
      Refresh golden files only when an upstream contract change is intentional.
```

## Upstream Documentation Synchronization

```text
upstream file:
  workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md

java files:
  README.md
  docs/miku-readfile-cli-spec.md
  docs/cli-json-parity.md
  docs/upstream-doc-sync.md
  src/main/java/jp/igapyon/mikureadfile/cli/HelpText.java
  src/test/java/jp/igapyon/mikureadfile/docs/DocumentationSyncTest.java

diff summary:
  behavior differences:
    - none
  naming differences:
    - Java examples use `java -jar target/miku-readfile.jar`.
  unported differences:
    - upstream `Bundle Artifacts` is represented as Java runtime artifacts.
  Java-side original extensions:
    - `docs/upstream-doc-sync.md` records shared sections and intentional
      Java-specific documentation differences.

follow-up:
  - checks performed:
      rg "^## " workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md
      rg "^## " docs/miku-readfile-cli-spec.md
  - fixture:
      upstream and Java CLI specification documents
  - next check viewpoint:
      Update Java CLI docs, README, help text, and DocumentationSyncTest when
      upstream CLI documentation changes.
```

## Core Responsibility Refactoring

```text
upstream file:
  workplace/upstream/miku-readfile/src/readfile.ts
  workplace/upstream/miku-readfile/src/root.ts
  workplace/upstream/miku-readfile/src/file-reader.ts
  workplace/upstream/miku-readfile/src/file-result.ts

java classes:
  jp.igapyon.mikureadfile.core.MikuReadfile
  jp.igapyon.mikureadfile.core.Root
  jp.igapyon.mikureadfile.core.FileReader
  jp.igapyon.mikureadfile.core.FileResultFactory

tests:
  MikuReadfileTest
  MikuReadfileCliTest
  DocumentationSyncTest
  scripts/smoke-jar.mjs
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none intended
  naming differences:
    - Java uses `FileResultFactory` for the upstream file result creation
      responsibility.
  unported differences:
    - none for the refactored responsibilities
  Java-side original extensions:
    - none

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/smoke-jar.mjs
      node scripts/parity-check.mjs
  - fixture:
      existing JUnit fixtures and dynamically generated files under
      workplace/smoke-miku-readfile and workplace/parity-miku-readfile
  - next check viewpoint:
      Continue keeping facade responsibilities small when porting future
      upstream changes.
```

## Validation Responsibility Refactoring

```text
upstream file:
  workplace/upstream/miku-readfile/src/validation.ts
  workplace/upstream/miku-readfile/src/validation-common.ts
  workplace/upstream/miku-readfile/src/validate-encoding.ts
  workplace/upstream/miku-readfile/src/validate-file.ts
  workplace/upstream/miku-readfile/src/validate-limits.ts

java classes:
  jp.igapyon.mikureadfile.core.RequestValidator
  jp.igapyon.mikureadfile.core.ValidationCommon
  jp.igapyon.mikureadfile.core.EncodingValidator
  jp.igapyon.mikureadfile.core.FileValidator
  jp.igapyon.mikureadfile.core.LimitsValidator
  jp.igapyon.mikureadfile.core.ValidationException

tests:
  MikuReadfileTest
  MikuReadfileCliTest
  DocumentationSyncTest
  scripts/smoke-jar.mjs
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none intended
  naming differences:
    - Java uses `EncodingValidator`, `FileValidator`, and `LimitsValidator`
      class names for the upstream `validate-*` modules.
  unported differences:
    - none for the refactored validation responsibilities
  Java-side original extensions:
    - none

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/smoke-jar.mjs
      node scripts/parity-check.mjs
  - fixture:
      existing JUnit fixtures and dynamically generated files under
      workplace/smoke-miku-readfile and workplace/parity-miku-readfile
  - next check viewpoint:
      Keep validation error messages and paths aligned when expanding parity
      fixtures.
```

## Text Shape Responsibility Refactoring

```text
upstream file:
  workplace/upstream/miku-readfile/src/text-shape.ts
  workplace/upstream/miku-readfile/src/line-endings.ts
  workplace/upstream/miku-readfile/src/logical-lines.ts
  workplace/upstream/miku-readfile/src/range-text.ts

java classes:
  jp.igapyon.mikureadfile.core.TextShape
  jp.igapyon.mikureadfile.core.LineEndings
  jp.igapyon.mikureadfile.core.LogicalLines
  jp.igapyon.mikureadfile.core.RangeText

tests:
  MikuReadfileTest
  MikuReadfileCliTest
  DocumentationSyncTest
  scripts/smoke-jar.mjs
  scripts/parity-check.mjs

diff summary:
  behavior differences:
    - none intended
  naming differences:
    - Java uses `LineEndings` and `LogicalLines` class names for the
      upstream helper modules.
  unported differences:
    - none for the refactored text-shape responsibilities
  Java-side original extensions:
    - none

follow-up:
  - checks performed:
      mvn test
      mvn clean package
      node scripts/smoke-jar.mjs
      node scripts/parity-check.mjs
  - fixture:
      existing JUnit fixtures and dynamically generated files under
      workplace/smoke-miku-readfile and workplace/parity-miku-readfile
  - next check viewpoint:
      Keep range extraction parity focused on line-ending and final-newline
      edge cases.
```
