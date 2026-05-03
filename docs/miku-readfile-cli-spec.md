# miku-readfile Java CLI Specification

This document follows the upstream Node.js CLI contract in
`workplace/upstream/miku-readfile/docs/miku-readfile-cli-spec.md`.

The Java runtime keeps the same primary contract.

```text
stdin
  request JSON

stdout
  result JSON

stderr
  malformed stdin, invalid CLI usage, or unexpected runtime-level messages
```

Meta commands:

```bash
java -jar target/miku-readfile.jar --version
java -jar target/miku-readfile.jar --help
java -jar target/miku-readfile.jar -h
```

Normal command:

```bash
java -jar target/miku-readfile.jar < request.json > result.json
```

Exit codes:

```text
0  ok: true
1  ok: false expected failure
2  malformed stdin or invalid CLI usage
3  unexpected runtime error
```

The Java implementation is a straight-conversion runtime. Observable request
and result JSON should stay aligned with the upstream Node.js implementation
unless a runtime difference is documented in the mapping or follow-up logs.
