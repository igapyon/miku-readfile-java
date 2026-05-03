# TODO

## Done

- Add JSON parity fixtures against upstream Node.js `miku-readfile`.
  - Use the same request JSON and fixture files for Node and Java.
  - Compare result JSON and classify differences as parity bugs or documented runtime differences.
  - Record findings in `docs/upstream-followup-log.md`.
  - Current smoke command: `node scripts/parity-check.mjs` with 27 cases.
  - Case metadata is written beside parity outputs under `workplace/`.
  - Normalized golden files are checked in under `docs/parity-golden/`.
- Add jar-level smoke verification.
  - Run `target/miku-readfile.jar` as an external process with stdin JSON.
  - Check stdout JSON, stderr, and exit code.
  - Current smoke command: `node scripts/smoke-jar.mjs`.
- Release version policy is fixed.
  - Current Java version is `0.5.0`.
  - Upstream Node version is `0.5.0`.

## Next

- Strengthen CLI docs synchronization.
  - Compare upstream `workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md`.
  - Java `docs/miku-readfile-cli-spec.md` has been expanded; keep it synchronized with future upstream changes.
  - Keep `README.md`, CLI help, and tests aligned.
- Expand README with more examples.

## Current Passing Checks

```bash
mvn test
mvn clean package
node scripts/smoke-jar.mjs
node scripts/parity-check.mjs
java -jar target/miku-readfile.jar --version
java -jar target/miku-readfile.jar --help
printf '{"version":1,"root":".","files":["README.md" ]}' | java -jar target/miku-readfile.jar
```
