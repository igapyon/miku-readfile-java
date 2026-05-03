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
- `target/miku-readfile-0.5.0-SNAPSHOT-dist.zip`

## Development Documents

- `docs/miku-readfile-cli-spec.md`
- `docs/upstream-class-mapping.md`
- `docs/upstream-test-mapping.md`
- `docs/upstream-followup-log.md`
- `docs/remaining-migration-items.md`
