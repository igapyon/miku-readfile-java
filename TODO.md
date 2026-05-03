# TODO

## Next

- Add JSON parity fixtures against upstream Node.js `miku-readfile`.
  - Use the same request JSON and fixture files for Node and Java.
  - Compare result JSON and classify differences as parity bugs or documented runtime differences.
  - Record findings in `docs/upstream-followup-log.md`.
- Strengthen CLI docs synchronization.
  - Compare upstream `workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md`.
  - Expand Java `docs/miku-readfile-cli-spec.md` where it is currently too brief.
  - Keep `README.md`, CLI help, and tests aligned.
- Add jar-level smoke verification.
  - Run `target/miku-readfile.jar` as an external process with stdin JSON.
  - Check stdout JSON, stderr, and exit code.
- Decide release version policy.
  - Current Java version is `0.5.0-SNAPSHOT`.
  - Upstream Node version is `0.5.0`.
  - Decide whether the first Java runtime release should be `0.5.0`.

## Current Passing Checks

```bash
mvn test
mvn clean package
java -jar target/miku-readfile.jar --version
java -jar target/miku-readfile.jar --help
printf '{"version":1,"root":".","files":["README.md" ]}' | java -jar target/miku-readfile.jar
```
