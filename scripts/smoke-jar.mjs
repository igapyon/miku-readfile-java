#!/usr/bin/env node

import { spawnSync } from "node:child_process";
import { mkdirSync, rmSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const javaJar = path.join(repoRoot, "target/miku-readfile.jar");
const workRoot = path.join(repoRoot, "workplace/smoke-miku-readfile");
const fixtureRoot = path.join(workRoot, "fixture");

function main() {
  ensureRuntime();
  prepareFixtures();

  const checks = [
    checkVersion(),
    checkHelp(),
    checkSuccessRequest(),
    checkExpectedFailure(),
    checkMalformedStdin(),
    checkInvalidUsage(),
  ];
  const failures = checks.filter((check) => !check.ok);
  if (failures.length > 0) {
    for (const failure of failures) {
      console.error(`smoke failed: ${failure.name}`);
      console.error(failure.message);
    }
    process.exit(1);
  }
  console.log(`jar smoke passed: ${checks.length} checks`);
}

function ensureRuntime() {
  const check = spawnSync("test", ["-f", javaJar], { stdio: "ignore" });
  if (check.status !== 0) {
    console.error(`Java runtime jar not found: ${path.relative(repoRoot, javaJar)}`);
    console.error("Run: mvn package");
    process.exit(2);
  }
}

function prepareFixtures() {
  rmSync(workRoot, { recursive: true, force: true });
  mkdirSync(fixtureRoot, { recursive: true });
  writeFileSync(path.join(fixtureRoot, "README.md"), "hello\n", "utf8");
}

function checkVersion() {
  const run = runJar(["--version"], "");
  return expect("version", run, {
    status: 0,
    stdoutIncludes: "miku-readfile 0.5.0\n",
    stderr: "",
  });
}

function checkHelp() {
  const run = runJar(["--help"], "");
  return expect("help", run, {
    status: 0,
    stdoutIncludes: "Usage:",
    stderr: "",
  });
}

function checkSuccessRequest() {
  const request = {
    version: 1,
    root: fixtureRoot,
    files: ["README.md"],
  };
  const run = runJar([], `${JSON.stringify(request)}\n`);
  const parsed = parseJson(run.stdout);
  return expectJson("success-request", run, parsed, {
    status: 0,
    stderr: "",
    ok: true,
    filesRead: 1,
  });
}

function checkExpectedFailure() {
  const request = {
    version: 1,
    root: fixtureRoot,
    files: ["missing.txt"],
  };
  const run = runJar([], `${JSON.stringify(request)}\n`);
  const parsed = parseJson(run.stdout);
  return expectJson("expected-failure", run, parsed, {
    status: 1,
    stderr: "",
    ok: false,
    diagnostics: 1,
  });
}

function checkMalformedStdin() {
  const run = runJar([], "{");
  return expect("malformed-stdin", run, {
    status: 2,
    stdout: "",
    stderrIncludes: "malformed stdin:",
  });
}

function checkInvalidUsage() {
  const run = runJar(["--bad"], "");
  return expect("invalid-usage", run, {
    status: 2,
    stdout: "",
    stderrIncludes: "usage: miku-readfile",
  });
}

function runJar(args, input) {
  return spawnSync("java", ["-jar", javaJar, ...args], {
    cwd: repoRoot,
    input,
    encoding: "utf8",
  });
}

function parseJson(text) {
  try {
    return { ok: true, value: JSON.parse(text) };
  } catch (error) {
    return { ok: false, error };
  }
}

function expectJson(name, run, parsed, expected) {
  const base = expect(name, run, {
    status: expected.status,
    stderr: expected.stderr,
  });
  if (!base.ok) return base;
  if (!parsed.ok) {
    return fail(name, `stdout was not JSON: ${parsed.error.message}\nstdout:\n${run.stdout}`);
  }
  if (parsed.value.ok !== expected.ok) {
    return fail(name, `expected ok ${expected.ok}, got ${parsed.value.ok}`);
  }
  if (expected.filesRead !== undefined && parsed.value.summary?.filesRead !== expected.filesRead) {
    return fail(name, `expected filesRead ${expected.filesRead}, got ${parsed.value.summary?.filesRead}`);
  }
  if (expected.diagnostics !== undefined && parsed.value.summary?.diagnostics !== expected.diagnostics) {
    return fail(name, `expected diagnostics ${expected.diagnostics}, got ${parsed.value.summary?.diagnostics}`);
  }
  return pass(name);
}

function expect(name, run, expected) {
  if (run.status !== expected.status) {
    return fail(name, `expected status ${expected.status}, got ${run.status}`);
  }
  if (expected.stdout !== undefined && run.stdout !== expected.stdout) {
    return fail(name, `unexpected stdout:\n${run.stdout}`);
  }
  if (expected.stdoutIncludes !== undefined && !run.stdout.includes(expected.stdoutIncludes)) {
    return fail(name, `stdout did not include ${JSON.stringify(expected.stdoutIncludes)}:\n${run.stdout}`);
  }
  if (expected.stderr !== undefined && run.stderr !== expected.stderr) {
    return fail(name, `unexpected stderr:\n${run.stderr}`);
  }
  if (expected.stderrIncludes !== undefined && !run.stderr.includes(expected.stderrIncludes)) {
    return fail(name, `stderr did not include ${JSON.stringify(expected.stderrIncludes)}:\n${run.stderr}`);
  }
  return pass(name);
}

function pass(name) {
  return { ok: true, name };
}

function fail(name, message) {
  return { ok: false, name, message };
}

main();
