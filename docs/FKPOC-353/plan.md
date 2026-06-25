# Plan: Validate config.yaml against regel_schema.yaml in CI

## Goal

Each regel-implementation repo that ships a `config.yaml` should have it validated against
`regel_schema.yaml` (owned by `rimfrost-framework-regel`) as part of its CI workflow.

## Approach

Add a reusable GitHub Actions workflow to `rimfrost-framework-regel`. Each regel-implementation
repo calls it from its `maven-ci.yaml`. The workflow uses
[`check-jsonschema`](https://github.com/python-jsonschema/check-jsonschema) (supports YAML +
JSON Schema draft 2020-12) to validate the caller's `config.yaml`.

### Schema version pinning — pin to git tag

Each impl repo's `maven-ci.yaml` pins the reusable workflow to a specific release tag of
`rimfrost-framework-regel` (e.g. `@v1.2.0`). GitHub resolves the tag at runtime and executes
that exact commit of the workflow — which also means the schema from that same commit is used.

**Upgrade flow:** when `rimfrost-framework-regel` cuts a new release, each impl repo bumps the
tag in a PR (`@v1.2.0` → `@v1.3.0`). This is a natural review point to assess whether the
schema change requires updating `config.yaml` before merging. No extra process is needed since
`rimfrost-framework-regel` already tags releases as part of its Maven publish workflow.

## In scope — regel-implementation repos with config.yaml

| Repo | config.yaml | maven-ci.yaml |
|------|-------------|---------------|
| rimfrost-regel-bekraftabeslut | yes | yes |
| rimfrost-regel-rtf-manuell | yes | yes |
| rimfrost-regel-rtf-maskinell | yes | yes |
| rimfrost-template-regel-manuell | yes | yes |
| rimfrost-template-regel-maskinell | yes | yes |

Excluded (no `config.yaml`): `rimfrost-regel-ratt-till-forsakring`,
`rimfrost-regel-rtf-manuell-subprocess`, `rimfrost-regel-rtf-maskinell-subprocess`.

---

## Steps

### 1. Add reusable workflow to rimfrost-framework-regel

Create `.github/workflows/validate-config.yaml` with `on: workflow_call`.

The workflow:
1. Checks out the caller's repo (default `actions/checkout` behaviour inside a reusable
   workflow uses `${{ github.repository }}` of the *caller*, so `config.yaml` is available).
2. Checks out `rimfrost-framework-regel` at the ref that was used to invoke the workflow
   (i.e. the pinned tag) under a separate path to obtain the schema from that same release.
3. Runs validation using the `python-jsonschema/check-jsonschema` GitHub Action (pinned to a
   specific tag, e.g. `@0.33.0`) — no pip install step needed, the action manages its own
   Python environment.

### 2. Wire up rimfrost-regel-bekraftabeslut

Add a `validate-config` job to `.github/workflows/maven-ci.yaml`:

```yaml
validate-config:
  uses: Forsakringskassan/rimfrost-framework-regel/.github/workflows/validate-config.yaml@v1.2.0
```

### 3. Wire up rimfrost-regel-rtf-manuell

Same change as step 2, in its `.github/workflows/maven-ci.yaml`.

### 4. Wire up rimfrost-regel-rtf-maskinell

Same change as step 2, in its `.github/workflows/maven-ci.yaml`.

### 5. Wire up rimfrost-template-regel-manuell

Same change as step 2, in its `.github/workflows/maven-ci.yaml`.

### 6. Wire up rimfrost-template-regel-maskinell

Same change as step 2, in its `.github/workflows/maven-ci.yaml`.

### 7. Smoke-test

Intentionally break one `config.yaml` on a feature branch and verify the CI job fails with a
clear error message. Revert and confirm green.

---

## Open questions

- **`check-jsonschema` action tag:** Dependabot can auto-bump `python-jsonschema/check-jsonschema@x.y.z`
  in the reusable workflow, but Dependabot runs in `rimfrost-framework-regel` — not in the
  impl repos. Ensure Dependabot is enabled here so the action tag stays current.
