# miku-readfile-java

`miku-readfile-java` is the Java CLI runtime version of
[`miku-readfile`](https://github.com/igapyon/miku-readfile).

It is maintained as a straight conversion of the Node.js / TypeScript upstream,
following the design notes under `docs/miku-soft-20-javaapp-design-v20260501.md`
and `docs/miku-soft-30-straight-conversion-v20260425.md`.

## Usage

Build and test:

```bash
mvn test
mvn package
```

Run the CLI:

```bash
java -jar target/miku-readfile.jar < request.json > result.json
```

Meta commands:

```bash
java -jar target/miku-readfile.jar --version
java -jar target/miku-readfile.jar --help
```

Minimal `request.json`:

```json
{
  "version": 1,
  "root": ".",
  "files": ["README.md"]
}
```

Range read example:

```json
{
  "version": 1,
  "root": ".",
  "files": [
    {
      "path": "src/main/java/jp/igapyon/mikureadfile/core/MikuReadfile.java",
      "range": {
        "startLine": 1,
        "lineCount": 40
      }
    }
  ]
}
```

Encoding rule example:

```json
{
  "version": 1,
  "root": ".",
  "files": [
    "README.md",
    {
      "path": "docs/legacy-memo.txt",
      "encoding": "shift_jis"
    }
  ],
  "encoding": {
    "default": "utf-8",
    "extensions": {
      ".java": "shift_jis"
    }
  }
}
```

Files must be root-relative paths using `/`. Absolute paths and `..` path
segments are validation errors. Directories, symlinks, binary files, decode
errors, and oversized files are skipped with diagnostics.

## Upstream

- Upstream Node.js / TypeScript project:
  `https://github.com/igapyon/miku-readfile`
- Local upstream checkout for development:
  `workplace/upstream/miku-readfile`

`workplace/` is local scratch space and is not tracked by Git.

## Runtime Artifacts

`mvn package` creates:

- `target/miku-readfile.jar`
- `target/miku-readfile-sources.jar`
- `target/miku-readfile-0.5.0-dist.zip`

GitHub Release publishing is handled by
`.github/workflows/release-cli-runtime.yml`. Release tags must start with `v`.
For example, tag `v0.5.0` publishes assets using version `0.5.0`; a dot-suffix
tag such as `v0.5.0.1` is also accepted for rebuilding assets from the same
`pom.xml` version.

Release assets:

- `miku-readfile-<version>.jar`
- `miku-readfile-sources-<version>.jar`
- `miku-readfile-<version>-dist.zip`

## Development Documents

- `docs/miku-readfile-cli-spec.md`
- `docs/cli-json-parity.md`
- `docs/parity-golden/`
- `docs/upstream-doc-sync.md`
- `docs/upstream-class-mapping.md`
- `docs/upstream-test-mapping.md`
- `docs/upstream-followup-log.md`
- `docs/remaining-migration-items.md`

## Verification

Focused checks:

```bash
mvn test
mvn clean package
node scripts/smoke-jar.mjs
node scripts/parity-check.mjs
```

`node scripts/parity-check.mjs` compares the Java CLI against the upstream Node
CLI and checked-in normalized golden files under `docs/parity-golden/`.
