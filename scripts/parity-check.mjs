#!/usr/bin/env node

import { spawnSync } from "node:child_process";
import { existsSync, mkdirSync, readFileSync, rmSync, symlinkSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const upstreamCli = path.join(repoRoot, "workplace/upstream/miku-readfile/dist/main.js");
const javaJar = path.join(repoRoot, "target/miku-readfile.jar");
const workRoot = path.join(repoRoot, "workplace/parity-miku-readfile");
const fixtureRoot = path.join(workRoot, "fixture");
const outputRoot = path.join(workRoot, "output");
const goldenRoot = path.join(repoRoot, "docs/parity-golden");
const args = new Set(process.argv.slice(2));
const updateGolden = args.has("--update-golden");
const checkGolden = args.has("--check-golden") || existsSync(goldenRoot);

function main() {
  ensureRuntime("node upstream CLI", upstreamCli, "Run: npm install && npm run build in workplace/upstream/miku-readfile");
  ensureRuntime("Java runtime jar", javaJar, "Run: mvn package");
  prepareFixtures();

  const cases = createParityCases();
  const failures = runParityCases(cases);

  if (failures.length > 0) {
    console.error(`parity failed: ${failures.join(", ")}`);
    console.error(`details: ${path.relative(repoRoot, outputRoot)}`);
    process.exit(1);
  }

  console.log(`parity passed: ${cases.length} cases`);
  if (updateGolden) {
    console.log(`golden updated: ${path.relative(repoRoot, goldenRoot)}`);
  } else if (checkGolden) {
    console.log(`golden checked: ${path.relative(repoRoot, goldenRoot)}`);
  }
  console.log(`details: ${path.relative(repoRoot, outputRoot)}`);
}

function createParityCases() {
  return [
    {
      name: "minimal-utf8",
      intent: "reads a minimal UTF-8 request",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["README.md"],
      },
    },
    {
      name: "shift-jis-extension-and-range",
      intent: "uses extension encoding rules and returns range metadata",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [
          "src/Legacy.java",
          { path: "notes-crlf.txt", range: { startLine: 2, lineCount: 4 } },
        ],
        encoding: {
          default: "utf-8",
          extensions: {
            ".java": "shift_jis",
          },
        },
      },
    },
    {
      name: "shape-and-empty",
      intent: "handles BOM, empty files, final newline, and mixed / CR line endings",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["bom.txt", "empty.txt", "mixed.txt", "classic.txt", "nofinal.txt"],
      },
    },
    {
      name: "expected-failures",
      intent: "returns file-level diagnostics for missing, binary, decode, and directory failures",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["missing.txt", "binary.dat", "broken.txt", "src"],
      },
    },
    {
      name: "total-limit",
      intent: "enforces maxTotalBytes without returning a partial oversized result",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["small-a.txt", "small-b.txt"],
        limits: {
          maxTotalBytes: 5,
        },
      },
    },
    {
      name: "per-file-encoding-override",
      intent: "allows per-file encoding override to take precedence",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [{ path: "memo.md", encoding: "shift_jis" }],
        encoding: {
          default: "utf-8",
          extensions: {
            ".md": "utf-8",
          },
        },
      },
    },
    {
      name: "dotfile-extension-rule",
      intent: "does not treat a leading-dot filename as an extension match",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [".env"],
        encoding: {
          default: "utf-8",
          extensions: {
            ".env": "shift_jis",
          },
        },
      },
    },
    {
      name: "trailing-dot-extension-rule",
      intent: "treats a trailing dot as the dot extension",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["trailing."],
        encoding: {
          default: "utf-8",
          extensions: {
            ".": "shift_jis",
          },
        },
      },
    },
    {
      name: "leading-dot-with-extension-rule",
      intent: "uses the suffix extension for a leading-dot filename with another dot",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [".profile.txt"],
        encoding: {
          default: "utf-8",
          extensions: {
            ".txt": "shift_jis",
          },
        },
      },
    },
    {
      name: "range-no-artificial-trailing-newline",
      intent: "does not add artificial trailing newline to range output",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [{ path: "plain.txt", range: { startLine: 3, lineCount: 1 } }],
      },
    },
    {
      name: "range-after-eof",
      intent: "returns empty range text and eof metadata when startLine is after EOF",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [{ path: "notes-crlf.txt", range: { startLine: 9, lineCount: 2 } }],
      },
    },
    {
      name: "duplicate-range-requests",
      intent: "does not deduplicate duplicate file requests",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [
          { path: "same.txt", range: { startLine: 1, lineCount: 1 } },
          { path: "same.txt", range: { startLine: 2, lineCount: 1 } },
        ],
      },
    },
    {
      name: "duplicate-missing-diagnostics",
      intent: "does not deduplicate duplicate diagnostics",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["missing.txt", "missing.txt"],
      },
    },
    {
      name: "partial-failure-keeps-read-file",
      intent: "returns read files plus ok false for partial failure",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["README.md", "missing.txt"],
      },
    },
    {
      name: "file-byte-limit",
      intent: "enforces per-file maxFileBytes",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["small-a.txt"],
        limits: {
          maxFileBytes: 2,
        },
      },
    },
    {
      name: "symlink-skipped",
      intent: "skips symlinked files",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["link.txt"],
      },
    },
    {
      name: "root-not-found",
      intent: "returns root_not_found for missing root",
      request: {
        version: 1,
        root: path.join(workRoot, "missing-root"),
        files: ["README.md"],
      },
    },
    {
      name: "root-too-broad-filesystem",
      intent: "rejects filesystem root as too broad",
      request: {
        version: 1,
        root: path.parse(fixtureRoot).root,
        files: ["README.md"],
      },
    },
    {
      name: "root-is-file",
      intent: "returns root_not_accessible when root is not a directory",
      request: {
        version: 1,
        root: path.join(fixtureRoot, "README.md"),
        files: ["README.md"],
      },
    },
    {
      name: "validation-unknown-field",
      intent: "rejects unknown top-level request fields",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["README.md"],
        extra: true,
      },
    },
    {
      name: "validation-parent-path",
      intent: "rejects parent path segments during validation",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["../README.md"],
      },
    },
    {
      name: "validation-absolute-path",
      intent: "rejects absolute file paths during validation",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [path.join(fixtureRoot, "README.md")],
      },
    },
    {
      name: "validation-invalid-range",
      intent: "rejects invalid range values",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [{ path: "README.md", range: { startLine: 1, lineCount: 0 } }],
      },
    },
    {
      name: "validation-invalid-extension-key",
      intent: "rejects invalid encoding extension keys",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["README.md"],
        encoding: {
          extensions: {
            java: "shift_jis",
          },
        },
      },
    },
    {
      name: "validation-invalid-entry-type",
      intent: "rejects invalid file entry types",
      request: {
        version: 1,
        root: fixtureRoot,
        files: [123],
      },
    },
    {
      name: "validation-invalid-encoding",
      intent: "rejects unsupported encoding enum values",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["README.md"],
        encoding: {
          default: "latin1",
        },
      },
    },
    {
      name: "validation-max-files-overflow",
      intent: "rejects requests exceeding maxFiles",
      request: {
        version: 1,
        root: fixtureRoot,
        files: ["small-a.txt", "small-b.txt"],
        limits: {
          maxFiles: 1,
        },
      },
    },
  ];
}

function runParityCases(cases) {
  const failures = [];
  for (const testCase of cases) {
    const result = runParityCase(testCase);
    writeParityOutputs(testCase, result);
    if (!result.same) {
      failures.push(testCase.name);
    }
    if (updateGolden) {
      writeGolden(testCase, result.normalizedNode);
    } else if (checkGolden && !matchesGolden(testCase, result.normalizedNode, result.normalizedJava)) {
      failures.push(`${testCase.name}:golden`);
    }
  }
  return failures;
}

function runParityCase(testCase) {
  const nodeResult = runNode(testCase.request);
  const javaResult = runJava(testCase.request);
  const normalizedNode = normalizeRun(nodeResult);
  const normalizedJava = normalizeRun(javaResult);
  return {
    nodeResult,
    javaResult,
    normalizedNode,
    normalizedJava,
    same: JSON.stringify(normalizedNode) === JSON.stringify(normalizedJava),
  };
}

function writeParityOutputs(testCase, result) {
  const metadata = {
    name: testCase.name,
    intent: testCase.intent,
    request: testCase.request,
  };
  writeJson(path.join(outputRoot, `${testCase.name}.metadata.json`), metadata);
  writeJson(path.join(outputRoot, `${testCase.name}.node.json`), result.nodeResult);
  writeJson(path.join(outputRoot, `${testCase.name}.java.json`), result.javaResult);
  writeJson(path.join(outputRoot, `${testCase.name}.node.normalized.json`), result.normalizedNode);
  writeJson(path.join(outputRoot, `${testCase.name}.java.normalized.json`), result.normalizedJava);
}

function writeGolden(testCase, expected) {
  mkdirSync(goldenRoot, { recursive: true });
  writeJson(path.join(goldenRoot, `${testCase.name}.json`), {
    name: testCase.name,
    intent: testCase.intent,
    expected,
  });
}

function matchesGolden(testCase, normalizedNode, normalizedJava) {
  const goldenPath = path.join(goldenRoot, `${testCase.name}.json`);
  if (!existsSync(goldenPath)) {
    console.error(`missing golden: ${path.relative(repoRoot, goldenPath)}`);
    return false;
  }
  const golden = JSON.parse(readFileSync(goldenPath, "utf8"));
  const expected = JSON.stringify(golden.expected);
  const nodeSame = JSON.stringify(normalizedNode) === expected;
  const javaSame = JSON.stringify(normalizedJava) === expected;
  if (!nodeSame || !javaSame) {
    console.error(`golden mismatch: ${testCase.name}`);
    return false;
  }
  return true;
}

function ensureRuntime(label, runtimePath, hint) {
  const check = spawnSync("test", ["-f", runtimePath], { stdio: "ignore" });
  if (check.status !== 0) {
    console.error(`${label} not found: ${path.relative(repoRoot, runtimePath)}`);
    console.error(hint);
    process.exit(2);
  }
}

function prepareFixtures() {
  rmSync(workRoot, { recursive: true, force: true });
  mkdirSync(fixtureRoot, { recursive: true });
  mkdirSync(outputRoot, { recursive: true });
  mkdirSync(path.join(fixtureRoot, "src"), { recursive: true });

  writeFileSync(path.join(fixtureRoot, "README.md"), "hello\nworld\n", "utf8");
  writeFileSync(path.join(fixtureRoot, "src/Legacy.java"), Buffer.from([0x82, 0xb1, 0x82, 0xf1, 0x82, 0xc9, 0x82, 0xbf, 0x82, 0xcd, 0x0a]));
  writeFileSync(path.join(fixtureRoot, "memo.md"), Buffer.from([0x83, 0x81, 0x83, 0x82, 0x0a]));
  writeFileSync(path.join(fixtureRoot, ".env"), Buffer.from([0x83, 0x81, 0x83, 0x82, 0x0a]));
  writeFileSync(path.join(fixtureRoot, "trailing."), Buffer.from([0x83, 0x81, 0x83, 0x82, 0x0a]));
  writeFileSync(path.join(fixtureRoot, ".profile.txt"), Buffer.from([0x83, 0x81, 0x83, 0x82, 0x0a]));
  writeFileSync(path.join(fixtureRoot, "notes-crlf.txt"), "a\r\nb\r\nc\r\n", "utf8");
  writeFileSync(path.join(fixtureRoot, "bom.txt"), "\ufeffhello", "utf8");
  writeFileSync(path.join(fixtureRoot, "empty.txt"), "");
  writeFileSync(path.join(fixtureRoot, "mixed.txt"), "a\r\nb\nc\rd", "utf8");
  writeFileSync(path.join(fixtureRoot, "classic.txt"), "a\rb\r", "utf8");
  writeFileSync(path.join(fixtureRoot, "nofinal.txt"), "no final", "utf8");
  writeFileSync(path.join(fixtureRoot, "plain.txt"), "a\nb\nc", "utf8");
  writeFileSync(path.join(fixtureRoot, "same.txt"), "one\ntwo\n", "utf8");
  writeFileSync(path.join(fixtureRoot, "target.txt"), "target\n", "utf8");
  symlinkSync("target.txt", path.join(fixtureRoot, "link.txt"));
  writeFileSync(path.join(fixtureRoot, "binary.dat"), Buffer.from([0x61, 0x00, 0x62]));
  writeFileSync(path.join(fixtureRoot, "broken.txt"), Buffer.from([0x80]));
  writeFileSync(path.join(fixtureRoot, "small-a.txt"), "aaa", "utf8");
  writeFileSync(path.join(fixtureRoot, "small-b.txt"), "bbb", "utf8");
}

function runNode(request) {
  return runRuntime("node", [upstreamCli], request);
}

function runJava(request) {
  return runRuntime("java", ["-jar", javaJar], request);
}

function runRuntime(command, args, request) {
  const child = spawnSync(command, args, {
    cwd: repoRoot,
    input: `${JSON.stringify(request)}\n`,
    encoding: "utf8",
  });
  const parsed = child.stdout.trim().length === 0 ? null : JSON.parse(child.stdout);
  return {
    status: child.status,
    stdout: parsed,
    stderr: child.stderr,
  };
}

function normalizeRun(run) {
  return {
    status: run.status,
    stdout: normalizeValue(run.stdout),
    stderr: normalizeStderr(run.stderr),
  };
}

function normalizeValue(value) {
  if (Array.isArray(value)) {
    return value.map(normalizeValue);
  }
  if (value && typeof value === "object") {
    const normalized = {};
    for (const [key, child] of Object.entries(value)) {
      if (key === "modifiedTime") {
        normalized[key] = "<modifiedTime>";
      } else if (key === "path" && typeof child === "string") {
        normalized[key] = normalizePathString(child);
      } else {
        normalized[key] = normalizeValue(child);
      }
    }
    return normalized;
  }
  return value;
}

function normalizePathString(value) {
  const normalizedRepoRoot = repoRoot.replace(/\\/g, "/");
  const normalizedValue = value.replace(/\\/g, "/");
  if (normalizedValue === normalizedRepoRoot) {
    return "<repoRoot>";
  }
  if (normalizedValue.startsWith(`${normalizedRepoRoot}/`)) {
    return `<repoRoot>/${normalizedValue.slice(normalizedRepoRoot.length + 1)}`;
  }
  return normalizedValue;
}

function normalizeStderr(stderr) {
  return stderr.replace(/\r\n/g, "\n");
}

function writeJson(filePath, value) {
  writeFileSync(filePath, `${JSON.stringify(value, null, 2)}\n`, "utf8");
}

main();
