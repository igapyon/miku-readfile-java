# CLI JSON Parity Policy

This document fixes the compatibility policy for the `miku-readfile-java`
straight conversion.

## Scope

The Java runtime should preserve the stdin / stdout JSON contract of the
upstream Node.js `miku-readfile` CLI as the primary compatibility target.

The Java implementation is a single-module Maven runtime jar with a
distribution zip created during packaging.

Fixed Java-side identity:

- base package: `jp.igapyon.mikureadfile`
- Maven artifactId: `miku-readfile`
- CLI class: `jp.igapyon.mikureadfile.cli.MikuReadfileCli`

## Parity Targets

The Java CLI should match the Node CLI as closely as practical for the
following items.

- stdin request JSON
- stdout result JSON
- stderr role
- exit codes
- result top-level shape
- request and result top-level `version: 1`
- field names
- default values
- limit values
- diagnostic codes
- diagnostic severities
- `ok`, `files`, `summary`, and `diagnostics` structure
- root-relative result paths
- `/` path separators in result JSON
- no absolute paths in result JSON
- unknown request fields as validation errors
- 2-space pretty-printed JSON on stdout
- trailing newline after stdout JSON
- `--help`, `-h`, and `--version` as stdin-free meta commands
- Java runtime CLI documentation in `README.md` and
  `docs/miku-readfile-cli-spec.md`

Stdout must not contain progress logs, warnings, or runtime chatter when the
CLI is returning request results. Agents and scripts should be able to parse
stdout as JSON.

## Known Runtime Differences

The following runtime difference is known and acceptable when documented:

- Shift_JIS decoder behavior differences between upstream Node dependencies and
  Java charset handling

This difference should be documented as a runtime difference when it affects
observable behavior. It should not be used as a reason to change JSON field
names, result shape, default values, diagnostic codes, or exit code policy.

For Shift_JIS, the Java runtime uses standard charset handling instead of
bringing over the upstream Node-side decoder dependency. This is an
implementation difference to record when malformed byte handling or other edge
cases become observably different.

## JSON Ordering

The Java implementation should preserve stable JSON object ordering where the
upstream contract or tests make ordering visible.

Use ordered data structures or explicit serialization order for result objects.
In particular, `files`, `range`, `summary`, and diagnostics should not depend
on unordered map iteration.

## JSON Implementation Policy

The Java implementation should use Jackson for request JSON parsing and result
JSON serialization.

Jackson is an implementation detail. The observable contract remains the
upstream Node.js CLI JSON contract.

Jackson dependencies should be treated as Apache License 2.0 dependencies,
aligned with this repository's license.

Expected Maven dependency:

- `com.fasterxml.jackson.core:jackson-databind`

Expected transitive dependencies:

- `com.fasterxml.jackson.core:jackson-core`
- `com.fasterxml.jackson.core:jackson-annotations`

Do not rely on Jackson defaults when they would change field names, field
ordering, null handling, unknown-field behavior, pretty printing, or diagnostic
codes.

## Boundary Rule

Java runtime convenience should not leak into the JSON contract.

Path objects, Java exception names, platform-specific separators, absolute
working paths, and Java-specific diagnostic wording should stay inside the Java
implementation unless the upstream Node contract already exposes equivalent
information.

## Parity Smoke

The local parity smoke command is:

```bash
node scripts/parity-check.mjs
```

This command compares normalized Node and Java CLI JSON results for selected
fixtures, including validation, duplicate requests, limits, root errors, range
reads, Shift_JIS, BOM, and line-ending cases. It writes details under
`workplace/`, which remains local scratch space.

The command also checks normalized golden files under `docs/parity-golden/`
when that directory exists. These golden files contain upstream-normalized
expected CLI results with local repository paths replaced by `<repoRoot>`.

To refresh the checked-in golden files after an intentional upstream contract
change, run:

```bash
node scripts/parity-check.mjs --update-golden
```

## Documentation Synchronization

The following Java-side documents and command output describe the same public
CLI contract and should be updated together:

- `README.md`
- `docs/miku-readfile-cli-spec.md`
- `docs/cli-json-parity.md`
- `java -jar target/miku-readfile.jar --help`

Java-side runtime differences should be explicit in these documents instead of
silently inheriting Node.js-only wording.
