# Plan: FKPOC-956 — Unified data structure for readSvarData and registerSvar

## Background

`KompletteringSvarServiceInterface` currently has two independent type parameters: `T` for the GET response body (`readSvarData`) and `Y` for the PATCH request body (`registerSvar`). In practice the same data structure should be used for both directions, since the handläggare reads the current svar data and then patches it back. Collapsing the two parameters into one (`T`) removes an unnecessary distinction and simplifies regel-repo implementations.

---

## Approach

Mechanical refactor — change the type signatures in the interface and the abstract controller, update the test stubs, and verify with `mvn test`.

---

## Steps

### Step 1 — Collapse type parameters in `KompletteringSvarServiceInterface`

File: `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringSvarServiceInterface.java`

- Change `interface KompletteringSvarServiceInterface<T, Y>` → `KompletteringSvarServiceInterface<T>`
- Change `registerSvar(Handlaggning handlaggning, Y request)` → `registerSvar(Handlaggning handlaggning, T request)`
- Update class-level Javadoc: remove the `@param Y` entry, update the example snippet to show a single type argument
- Update requirement reference in Javadoc to mention FRALL-FR-07.7

---

### Step 2 — Collapse type parameters in `KompletteringController`

File: `src/main/java/se/fk/rimfrost/framework/regel/presentation/rest/KompletteringController.java`

- Change `class KompletteringController<T, Y>` → `KompletteringController<T>`
- Change `KompletteringSvarServiceInterface<T, Y> svarService` → `KompletteringSvarServiceInterface<T> svarService`
- Change `patchKomplettering(... Y request)` → `patchKomplettering(... T request)`
- Update class-level Javadoc: remove `@param Y`, update the example snippet to a single type argument

---

### Step 3 — Update test stubs in `KompletteringControllerTest`

File: `src/test/java/se/fk/rimfrost/framework/regel/KompletteringControllerTest.java`

- Change `KompletteringController<Object, Object>` → `KompletteringController<Object>`
- Change `KompletteringSvarServiceInterface<Object, Object>` → `KompletteringSvarServiceInterface<Object>`
- Change `registerSvar(Handlaggning handlaggning, Object request)` — signature stays the same (parameter type is already `Object`)
- Add `@DisplayName("FRALL-FR-07.7: ...")` to relevant tests if applicable

---

### Step 4 — Run tests and apply formatting

```
mvn spotless:apply
mvn test
```

Verify all existing tests pass with no compilation errors.

---

## Execution order

1 → 2 → 3 → 4
