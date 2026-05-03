# Upstream Documentation Synchronization

This document records how `docs/miku-readfile-cli-spec.md` tracks the upstream
Node.js CLI specification.

## Upstream Reference

```text
workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md
```

## Shared Sections

The Java CLI specification keeps the following upstream section structure:

- Purpose
- Design Principles
- Scope
- CLI Contract
- Exit Codes
- Request JSON
- File Entries
- Request Validation
- File Paths
- Root Validation
- Encoding
- Limits
- Result JSON
- File Result
- Summary
- Failure Results
- Diagnostics
- Range Reads
- BOM Handling
- Line Endings
- Safety
- Agent Usage Guidance
- Relationship With miku-grep

## Java-Specific Sections

The upstream Node.js spec has `Bundle Artifacts`.

The Java spec intentionally replaces that section with:

- Java Runtime Artifacts
- Parity Verification

## Java-Specific Wording

The Java spec intentionally differs from upstream wording in these areas:

- CLI command examples use `java -jar target/miku-readfile.jar`.
- JSON serialization is described as Jackson-based instead of
  `JSON.stringify`.
- Packaging is described as Maven runtime jar, sources jar, and distribution
  zip.
- Parity verification references `scripts/parity-check.mjs`,
  `scripts/smoke-jar.mjs`, and `docs/parity-golden/`.

## Synchronization Checks

When upstream documentation changes, review the Java spec and update:

- `README.md`
- `docs/miku-readfile-cli-spec.md`
- `docs/cli-json-parity.md`
- `docs/upstream-doc-sync.md`
- `src/main/java/jp/igapyon/mikureadfile/cli/HelpText.java`
- `src/test/java/jp/igapyon/mikureadfile/docs/DocumentationSyncTest.java`

Focused command:

```bash
mvn test -Dtest=DocumentationSyncTest
```
