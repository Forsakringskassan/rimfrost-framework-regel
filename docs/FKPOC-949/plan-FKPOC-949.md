# Plan — FKPOC-949

**Se över vad som testas i rimfrost-framework-regel, rimfrost-framework-regel-manuell/maskinell**

## Background

After the refactoring of `rimfrost-framework-regel`, tests in the manuell and maskinell repos should
be audited. Tests that verify core framework logic should live in the core repo, not in variant repos.

## Current state (as of 2026-08-31)

### rimfrost-framework-regel (core) — 10 test classes

| Class | Requirements covered |
|---|---|
| `KompletteringControllerTest` | FRALL-FR-07.6, FR-07.7, FR-05.2 |
| `KompletteringOulHandlerTest` | FRALL-FR-06.1, FR-06.3 |
| `KompletteringServiceTest` | FRALL-FR-06.6, FR-06.7, FR-06.8 |
| `KompletteringStorageTest` | FRALL-FR-06.3, FR-06.4, FR-06.5 |
| `RegelConfigProviderYamlTest` | FRALL-FR-02.1, FR-02.2, FR-02.3 |
| `RegelConsumerTest` | FRALL-FR-01.1, FR-01.2 |
| `RegelFireAndForgetTest` | FRALL-FR-04.3 |
| `RegelMessageHandlerTest` | FRALL-FR-01.2 |
| `RegelRequestHandlerBaseTest` | FRALL-FR-04.1, FR-03.2, FR-03.3, FR-03.4, FR-04.2 |
| `RegelUtilsTest` | FRALL-FR-05.1, FR-05.2 |

### rimfrost-framework-regel-manuell — 17 test classes (+ 4 abstract)

All tests are correctly placed: they cover REST delegation (GET/PATCH/POST), SID detection, OUL
pipeline null safety, exception mapping, storage cleanup, middleware service, and manuell-specific
komplettering and handläggning flows.

### rimfrost-framework-regel-maskinell — 7 test classes (+ 3 abstract)

Most tests are correctly placed. One exception:

- `RegelConfigLoadingTest` — tests `RegelConfigProvider` (a **core** class) for file-not-found,
  malformed YAML, and missing classpath resource error scenarios. This is generic framework
  behaviour and belongs in core alongside `RegelConfigProviderYamlTest`.

## Identified actions

### Move to core

1. `RegelConfigLoadingTest` from maskinell → core
   - The class under test (`RegelConfigProvider`) lives in core
   - Requirement mapping: `FRMASK-NFR-03.2` → `FRALL-NFR-02.1` / `FRALL-NFR-02.2`
   - Delete the class from maskinell after adding it to core
   - Verify test still passes in both repos after the change

### No changes needed

- All other maskinell tests are correctly placed (retry utility, sync processing, komplettering skip)
- All manuell tests are correctly placed (REST delegation, SID, null safety, exception mapping)

### Gaps in core (to fill)

2. `RegelConfigProviderYamlTest` currently covers schema validation only (FR-02.1–FR-02.3).
   After step 1, core will also cover file/resource loading error paths (NFR-02.1/NFR-02.2).
   Assess whether `krav.md` needs an additional requirement entry for loading errors once the
   test is in core (e.g. `FRALL-NFR-02.2` may need rewording to cover this explicitly).

3. Health endpoint — core exposes a health endpoint per `FRALL-NFR-01.1` but there is no
   `RegelHealthTest` in core (manuell has one). Add a basic health test to core.

## Steps

1. **Move `RegelConfigLoadingTest` to core**
   - Repos: **rimfrost-framework-regel** (add), **rimfrost-framework-regel-maskinell** (delete)
   - Copy class from maskinell to core; update package declaration and `@DisplayName` requirement
     IDs from `FRMASK-NFR-03.2` to `FRALL-NFR-02.x`
   - Update `krav.md` in core if needed to precisely cover loading-error scenarios
   - Delete the class from maskinell
   - Run `mvn test` in both affected repos

2. **Add `RegelHealthTest` to core**
   - Repo: **rimfrost-framework-regel** (add)
   - Verify `/q/health/live` returns `UP`
   - Tag with `@DisplayName("FRALL-NFR-01.1: ...")`
   - Run `mvn test` in core

3. **Final audit pass**
   - Repos: **rimfrost-framework-regel**, **rimfrost-framework-regel-manuell**, **rimfrost-framework-regel-maskinell**
   - Run full test suite in all three repos
   - Confirm no duplicate tests remain across repos
   - Update this plan with any deviations found

## Out of scope

- Moving manuell tests to core (all correctly placed)
- Splitting or refactoring existing test classes
- Changes to production code
