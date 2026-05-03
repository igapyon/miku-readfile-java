# Parity Golden Files

This directory stores normalized upstream CLI expected results for
`scripts/parity-check.mjs`.

Refresh these files only when an upstream JSON contract change is intentional:

```bash
node scripts/parity-check.mjs --update-golden
```

The golden files must not contain local absolute repository paths. Local repo
paths are normalized to `<repoRoot>`.
