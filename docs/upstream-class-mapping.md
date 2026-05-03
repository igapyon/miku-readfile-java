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
  workplace/upstream/miku-readfile/src/validate-encoding.ts
  workplace/upstream/miku-readfile/src/validate-file.ts
  workplace/upstream/miku-readfile/src/validate-limits.ts
  workplace/upstream/miku-readfile/src/validation-common.ts

java classes:
  jp.igapyon.mikureadfile.core.RequestValidator
  jp.igapyon.mikureadfile.core.ValidationException

notes:
  - Converts request JSON into EffectiveRequest.
  - Unknown fields, default values, limits, paths, and validation diagnostics
    should match upstream.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/path-security.ts
  workplace/upstream/miku-readfile/src/root.ts

java classes:
  jp.igapyon.mikureadfile.core.PathSecurity
  jp.igapyon.mikureadfile.core.MikuReadfile

notes:
  - Covers root-boundary checks and root-too-broad policy.
  - Java root handling is implemented inside the core facade for now.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/readfile.ts
  workplace/upstream/miku-readfile/src/file-reader.ts
  workplace/upstream/miku-readfile/src/file-result.ts
  workplace/upstream/miku-readfile/src/diagnostics.ts
  workplace/upstream/miku-readfile/src/result.ts

java classes:
  jp.igapyon.mikureadfile.core.MikuReadfile
  jp.igapyon.mikureadfile.core.ResultFactory
  jp.igapyon.mikureadfile.model.Diagnostic

notes:
  - Core API is `MikuReadfile.runRequest(JsonNode, Path)`.
  - File IO remains at the core runtime boundary and CLI only handles stdin /
    stdout / stderr.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/encoding.ts

java classes:
  jp.igapyon.mikureadfile.core.Encoding

notes:
  - Supports `utf-8` and `shift_jis`.
  - Decoder differences between iconv-lite and Java Charset should be treated
    as runtime differences if found.
```

```text
upstream file:
  workplace/upstream/miku-readfile/src/text-shape.ts
  workplace/upstream/miku-readfile/src/line-endings.ts
  workplace/upstream/miku-readfile/src/logical-lines.ts
  workplace/upstream/miku-readfile/src/range-text.ts

java classes:
  jp.igapyon.mikureadfile.core.TextShape
  jp.igapyon.mikureadfile.core.RangeText

notes:
  - Covers LF normalization, BOM metadata, final newline, line ending
    classification, logical line count, and range text extraction.
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
