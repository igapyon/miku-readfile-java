# Upstream Class Mapping

This document tracks `upstream file -> Java class` mapping for the
`miku-readfile-java` straight conversion.

The stable upstream reference for development is:

```text
workplace/upstream/miku-readfile
```

## Source Mapping

```text
upstream file:
  workplace/upstream/miku-readfile/src/types.ts

java classes:
  jp.igapyon.mikureadfile.model.Diagnostic
  jp.igapyon.mikureadfile.model.EffectiveFileRequest
  jp.igapyon.mikureadfile.model.EffectiveRequest
  jp.igapyon.mikureadfile.model.FileResult
  jp.igapyon.mikureadfile.model.MikuReadfileResult
  jp.igapyon.mikureadfile.model.RangeRequest
  jp.igapyon.mikureadfile.model.RangeResult
  jp.igapyon.mikureadfile.model.Summary

notes:
  - Model field names preserve the upstream JSON contract.
  - Simple public fields are intentional during initial straight conversion.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/constants.ts

java classes:
  jp.igapyon.mikureadfile.core.Constants

notes:
  - Holds schema version, default limits, and maximum limits.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/validation.ts

java classes:
  jp.igapyon.mikureadfile.core.RequestValidator
  jp.igapyon.mikureadfile.core.ValidationException

notes:
  - Converts request JSON into EffectiveRequest.
  - Coordinates encoding, limits, and file-entry validators.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/validation-common.ts

java classes:
  jp.igapyon.mikureadfile.core.ValidationCommon

notes:
  - Holds common unknown-field, integer, encoding, and validation failure
    helpers.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/validate-encoding.ts

java classes:
  jp.igapyon.mikureadfile.core.EncodingValidator

notes:
  - Validates default encoding and extension-specific encoding overrides.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/validate-file.ts

java classes:
  jp.igapyon.mikureadfile.core.FileValidator

notes:
  - Validates string and object file entries, relative paths, ranges, and
    per-file encoding overrides.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/validate-limits.ts

java classes:
  jp.igapyon.mikureadfile.core.LimitsValidator

notes:
  - Validates max file bytes, file count, and total bytes limits.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/path-security.ts
  workplace/upstream/miku-readfile/src/root.ts

java classes:
  jp.igapyon.mikureadfile.core.PathSecurity
  jp.igapyon.mikureadfile.core.Root

notes:
  - Covers root-boundary checks and root-too-broad policy.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/readfile.ts

java classes:
  jp.igapyon.mikureadfile.core.MikuReadfile

notes:
  - Core API is `MikuReadfile.runRequest(JsonNode, Path)`.
  - The facade coordinates validation, root checks, per-file reads, and final
    result aggregation.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/file-reader.ts

java classes:
  jp.igapyon.mikureadfile.core.FileReader

notes:
  - Handles file path resolution, symlink / boundary checks, binary skipping,
    byte reading, and decode invocation.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/file-result.ts

java classes:
  jp.igapyon.mikureadfile.core.FileResultFactory

notes:
  - Creates file result JSON model fields from decoded text and metadata.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/diagnostics.ts

java classes:
  jp.igapyon.mikureadfile.core.Diagnostics
  jp.igapyon.mikureadfile.model.Diagnostic

notes:
  - `Diagnostic` is the JSON model.
  - `Diagnostics` creates root, file, validation, and total-byte diagnostics.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/result.ts

java classes:
  jp.igapyon.mikureadfile.core.ResultFactory
  jp.igapyon.mikureadfile.model.MikuReadfileResult
  jp.igapyon.mikureadfile.model.Summary

notes:
  - Creates summaries, validation failure results, and final aggregated
    results outside the CLI boundary.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/encoding.ts

java classes:
  jp.igapyon.mikureadfile.core.Encoding

notes:
  - Supports `utf-8` and `shift_jis`.
  - Extension selection follows Node `path.posix.extname` behavior for
    leading-dot filenames.
  - Decoder differences between iconv-lite and Java Charset should be treated
    as runtime differences if found.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/text-shape.ts

java classes:
  jp.igapyon.mikureadfile.core.TextShape

notes:
  - Builds normalized text shape, BOM metadata, line ending metadata, and
    logical line metadata by coordinating line-ending and logical-line helpers.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/line-endings.ts

java classes:
  jp.igapyon.mikureadfile.core.LineEndings

notes:
  - Normalizes line endings, collects raw line ending kinds, and classifies the
    line ending summary value.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/logical-lines.ts

java classes:
  jp.igapyon.mikureadfile.core.LogicalLines

notes:
  - Splits raw decoded text into logical lines and records whether each line
    originally had an ending.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/range-text.ts

java classes:
  jp.igapyon.mikureadfile.core.RangeText

notes:
  - Extracts normalized range text from TextShape logical line metadata.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/main.ts
  workplace/upstream/miku-readfile/src/help.ts

java classes:
  jp.igapyon.mikureadfile.cli.MikuReadfileCli
  jp.igapyon.mikureadfile.cli.HelpText

notes:
  - CLI uses `run(String[], InputStream, PrintStream, PrintStream)` for tests.
  - `System.exit` is confined to the outer `main` boundary.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/bundle-entry.ts

java classes:
  none

notes:
  - Node single-file bundle entry.
  - Java packaging is handled by the executable runtime jar.
```
