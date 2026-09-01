# Plan: FKPOC-960 — Compensate orphaned OUL task when storage fails in `KompletteringOulHandler.initiate`

## Background

`KompletteringOulHandler.initiate` performs a dual-write:

1. `oulAdapter.createOperativUppgift(...)` — creates the OUL task.
2. `storage.setKompletteringTillstand(...)` — persists correlation state.

If step 1 succeeds but step 2 fails (e.g. `PersistenceException` from Panache), the handläggare receives an OUL task that can never be correlated with the pending regel request. The task will be worked on and completed, but the reply will fail to match any stored tillstand.

The `tillstand` object requires `operativUppgift.getUppgiftId()` (only available after step 1), so persist-first ordering is not viable. Compensation is the pragmatic fix.

## Approach

Wrap the storage call in try/catch. On failure, best-effort call `oulAdapter.endOperativUppgift(uppgiftId, reason)` to close the orphaned OUL task with a descriptive reason. Log any cleanup failure at ERROR (with `uppgiftId` and `handlaggningId`) so ops can reconcile manually. Rethrow the original storage exception unchanged — it remains unchecked (`PersistenceException`) and propagates to the caller's Kafka handler.

**Known gap:** this is not atomic. A JVM crash between the two calls still leaves an orphan with no log entry. A full fix would need an outbox pattern, which is out of scope.

## New requirements

Added to `docs/krav.md` under `FRALL-FR-06`:

> **FRALL-FR-06.9** Om lagring av korrelationstillståndet misslyckas efter att OUL-uppgiften har skapats i `KompletteringOulHandler.initiate`, ska ramverket best-effort avsluta den nyskapade OUL-uppgiften via `endOperativUppgift`. Det ursprungliga persistensfelet ska alltid kastas vidare till anroparen.
>
> **FRALL-FR-06.10** Om avslutningen av OUL-uppgiften enligt FRALL-FR-06.9 också misslyckas ska felet loggas med `uppgiftId` och `handlaggningId` för manuell rekonsiliering, utan att maskera det ursprungliga persistensfelet.

## Steps

### Step 1 — Add requirement FRALL-FR-06.9 to `krav.md`

File: `docs/krav.md`

- Insert new bullet under FRALL-FR-06, after 06.8.

---

### Step 2 — Implement compensation in `KompletteringOulHandler.initiate`

File: `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringOulHandler.java`

- Add a JBoss `Logger` field (matches existing logging conventions in the module).
- Wrap `storage.setKompletteringTillstand(...)` in try/catch on `RuntimeException`.
- In the catch block:
  - Call `oulAdapter.endOperativUppgift(operativUppgift.getUppgiftId(), "Kompletteringstillstånd kunde inte sparas — avslutar uppgiften för att undvika uppgift utan korrelation")` inside a nested try/catch.
  - On cleanup failure, log at ERROR with `uppgiftId`, `handlaggningId`, and the cleanup exception. Do **not** rethrow the cleanup exception.
  - Rethrow the original storage exception.
- Update the method Javadoc: document that storage failures propagate as unchecked and that the OUL task is best-effort ended on such failures.

Method signature remains unchanged (`throws OulException` only; storage exceptions stay unchecked).

---

### Step 3 — Add tests to `KompletteringOulHandlerTest`

File: `src/test/java/se/fk/rimfrost/framework/regel/KompletteringOulHandlerTest.java`

Add three tests, all with `@DisplayName` referencing FRALL-FR-06.9:

1. **Storage failure triggers OUL end-call.** Mock storage to throw `RuntimeException`. Verify `oulAdapter.endOperativUppgift(uppgiftId, ...)` was called with the just-created `uppgiftId`, and the original exception propagates.
2. **Cleanup failure is swallowed; original exception still propagates.** Mock storage to throw `RuntimeException` and `endOperativUppgift` to also throw `OulException`. Verify the original storage exception (not the OUL one) propagates.
3. **Happy path unchanged.** Verify `endOperativUppgift` is NOT called when storage succeeds.

**Decision:** refactor the existing `KompletteringOulHandlerTest` to `@InjectMock KompletteringStorage storage` and rewrite the two existing storage-touching tests to use `verify` / `ArgumentCaptor` (integration coverage of real storage lives in `KompletteringStorageTest`). Add the three new compensation tests to the same class.

Existing tests to rewrite:
- `should_store_tillstand_after_successful_oul_call` — use `ArgumentCaptor<KompletteringTillstand>` on `verify(storage).setKompletteringTillstand(eq(handlaggningId), captor.capture())`.
- `should_propagate_oul_exception_without_storing_tillstand` — use `verify(storage, never()).setKompletteringTillstand(any(), any())`.

---

### Step 4 — Run formatting and tests

```
mvn spotless:apply
mvn test
```

Verify all tests pass, no regressions in the existing `KompletteringOulHandlerTest`.

---

### Step 5 — CHANGELOG (skipped)

`CHANGELOG.md` is auto-generated from commit messages at release time. Skipped — the commit message must reference FKPOC-960 so the release tooling picks it up.

---

## Files changed

- `docs/krav.md` (new requirement)
- `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringOulHandler.java`
- `src/test/java/se/fk/rimfrost/framework/regel/KompletteringOulHandlerTest.java`
