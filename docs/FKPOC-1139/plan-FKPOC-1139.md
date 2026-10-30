# FKPOC-1139 — Migrate to updated handlaggning adapter

## Background

The `rimfrost-framework-handlaggning-adapter` was updated (commits `bb1861a` + `54e3bd0`, 2026-10-09)
to align with breaking changes in `rimfrost-service-handlaggning-openapi` (v2.0.8-SNAPSHOT).
This repo depends on adapter v1.2.5-SNAPSHOT and has several compilation errors as a result.

### Key model changes in the adapter

| Removed | Replacement |
|---|---|
| `Underlag` (typ, version, data) | `Uppgiftsdatakopia` / `Uppgiftsdatakoppling` on `Uppgift` |
| `ProduceratResultat` | `Uppgift.resultat()` → `List<Uppgiftsdatakoppling>` |
| `Yrkande.produceradeResultat()` | Moved to `Uppgift.resultat()` |
| `HandlaggningUpdate` (flat fields) | `HandlaggningUpdate.handlaggning()` + `.uppgift()` |

`Uppgiftsdata` is now a sealed interface with two permitted subtypes:
- `Uppgiftsdatakoppling` — reference only (`informationsobjektId`, `informationsobjektversion`)
- `Uppgiftsdatakopia` — copy with data (`+ informationsobjekttyp`, `data`)

---

## Steps

- [x] **1. Fix `RegelRequestHandlerBase.createHandlaggningUpdate()`**

  Replace the flat `ImmutableHandlaggningUpdate` builder with the new two-field form:
  ```java
  ImmutableHandlaggningUpdate.builder()
      .handlaggning(handlaggning)
      .uppgift(uppgift)
      .build();
  ```
  Remove the `kogitoprocInstanceId` and `version` parameters from the method signature.
  Update all callers in this repo.

- [x] **2. Fix `RegelRequestHandlerBase.createUppgift()` — field name typo**

  Rename `.skapadTs(...)` → `.skapadTS(...)` (line 138) to match `Uppgift.skapadTS()`.

- [x] **3. Fix `RegelRequestHandlerBase.updateHandlaggning()` — `handlaggningUpdate.id()`**

  `HandlaggningUpdate` no longer has `id()` directly.
  Replace `handlaggningUpdate.id()` with `handlaggningUpdate.handlaggning().id()`.

- [x] **4. Remove obsolete methods from `RegelUtils`**

  The following methods reference removed types (`Underlag`, `ProduceratResultat`,
  `Yrkande.produceradeResultat()`) and should be deleted:
  - `createYrkandeWithUpdatedProduceradeResultat()`
  - `mergeProduceradeResultat()`
  - `createUnderlag()`
  - `createProduceratResultatData()` *(no remaining callers; remove if confirmed unused)*

  Remove the three now-invalid imports (`ImmutableUnderlag`, `Underlag`, `ProduceratResultat`).

- [x] **5. Update `RegelUtilsTest` to match**

  Remove or replace tests for the deleted `RegelUtils` methods
  (`mergeProduceradeResultat`, `createYrkandeWithUpdatedProduceradeResultat`).
  Remove all references to `ProduceratResultat`, `ImmutableProduceratResultat`,
  and `Yrkande.produceradeResultat()`.

- [x] **6. Compile check and run tests**

  ```
  mvn test-compile -q
  mvn spotless:apply
  mvn test
  ```

  Verify zero compilation errors and all tests green.

---

## Deviations

- **Step 4–5 (unplanned)**: `RegelUtils` had no remaining callers after removal of its methods, so the class itself was also deleted.
- **Step 4 (unplanned)**: `WireMockHandlaggning.java` also references `PutHandlaggningRequest` (generated API class removed from spec). Replaced with the new `HandlaggningUpdate` generated class, which is now the direct PUT request body. `getUppgiftFromLastPutHandlaggning` updated: `dto.getHandlaggning().getUppgift()` → `dto.getUppgift()`.
