# FKPOC-963 — Consolidate `ErbjudandeReferensdataTestService` in CORE test-jar + close MASKINELL coverage gap

## Scope

Two related pieces of work bundled into one ticket:

- **Consolidate the duplicated fixture.** Move a unified `ErbjudandeReferensdataTestService` into CORE's test sources so both children can share it. Expose a `public static final String DEFAULT_ERBJUDANDE_NAMN` on the class and update the MANUELL assertion to reference it (no magic strings). Delete both child copies.
- **Close the MASKINELL coverage gap.** Add a MASKINELL test that mirrors MANUELL's existing `should_send_correct_erbjudande_values_with_oul_create_request` — captures `CreateOperativUppgiftRequest` and asserts the erbjudande name (using the shared constant).

## Background

- MASKINELL and MANUELL each have a near-duplicate `ErbjudandeReferensdataTestService` CDI fixture (differs only in the returned string and one `@SuppressWarnings`).
- CORE already publishes a test-jar (`maven-jar-plugin` `test-jar`, excludes `**/*Test.class`), and both children already consume it — so the infrastructure for sharing this fixture is in place.
- MANUELL asserts the returned string via `RegelManuellOulTest.java:39` (`assertEquals("Test", oulRequest.getErbjudande().getNamn())`). MASKINELL has **no** equivalent assertion despite the same production wiring (`RegelMaskinellRequestHandler.java:104`) — a real coverage gap.

## Goal

1. One canonical `ErbjudandeReferensdataTestService` in CORE test sources, with a `public static final String DEFAULT_ERBJUDANDE_NAMN`.
2. Delete the two child copies.
3. Update MANUELL's assertion to reference the shared constant (no magic string).
4. Add a MASKINELL test mirroring MANUELL's assertion to close the coverage gap.

## Design

### New file (CORE)

Location: `rimfrost-framework-regel/src/test/java/se/fk/rimfrost/framework/regel/ErbjudandeReferensdataTestService.java`

Package: `se.fk.rimfrost.framework.regel` — same as `RegelTestData`, `WireMockHandlaggning`, `KafkaConnector`.

```java
package se.fk.rimfrost.framework.regel;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.referensdata.ErbjudandeReferensdataInterface;

@ApplicationScoped
@DefaultBean
public class ErbjudandeReferensdataTestService implements ErbjudandeReferensdataInterface
{
   public static final String DEFAULT_ERBJUDANDE_NAMN = "Test erbjudande";

   @Override
   public String getErbjudandeNamn(String id)
   {
      return DEFAULT_ERBJUDANDE_NAMN;
   }
}
```

### Locked decisions

1. **Constant value:** `"Test erbjudande"`. MANUELL's assertion (currently `"Test"`) will be updated to match.
2. **CORE release version:** `1.3.5`.
3. **New MASKINELL requirement:** add `FRMASK-FR-03.3 — Erbjudandenamn slås upp från referensdata och inkluderas i uppgiftsobjektet` to `rimfrost-framework-regel-maskinell/docs/krav.md` and reference it in the new test's `@DisplayName`.

## Steps

Steps 1–4 are CORE. Step 5 unblocks children. Steps 6–7 are the child cleanups and run against the released CORE.

1. **CORE — create branch `feat/FKPOC-963-shared-erbjudande-referensdata-fixture`.** Log to Obsidian per user convention.
2. **CORE — add** `src/test/java/se/fk/rimfrost/framework/regel/ErbjudandeReferensdataTestService.java` (see Design). Run `mvn spotless:apply && mvn test`.
3. **CORE — verify test-jar contents.** Run `mvn package -DskipTests` and confirm `target/rimfrost-framework-regel-*-tests.jar` contains the new class (`unzip -l | grep ErbjudandeReferensdataTestService`). Fixture is a plain non-`*Test.class` file, so it should be included by the existing test-jar filter — verify to be safe.
4. **CORE — PR + review + merge.** Standard flow. Version stays `1.0.0-SNAPSHOT` on the branch; release happens on `main` via existing release process.
5. **CORE — release `1.3.5`** (or agreed version) via the normal release workflow. **Blocker for steps 6–7.**
6. **MANUELL — branch `fix/FKPOC-963-use-shared-erbjudande-fixture`.**
   - Bump `rimfrost-framework-regel` (both regular and `<classifier>tests</classifier>` deps) `1.3.4` → `1.3.5` in `pom.xml`.
   - Delete `src/test/java/se/fk/rimfrost/framework/regel/manuell/logic/ErbjudandeReferensdataTestService.java`.
   - Update `RegelManuellOulTest.java:39` from `assertEquals("Test", …)` to `assertEquals(ErbjudandeReferensdataTestService.DEFAULT_ERBJUDANDE_NAMN, …)` (import from `se.fk.rimfrost.framework.regel`).
   - Run `mvn clean test` — Quarkus CDI must discover the `@ApplicationScoped @DefaultBean` from the test-jar. If discovery fails, add `quarkus.index-dependency.regel-tests.group-id=se.fk.rimfrost.framework.regel` and `.artifact-id=rimfrost-framework-regel` + `.classifier=tests` to `src/test/resources/application.properties`.
   - PR + merge.
7. **MASKINELL — branch `fix/FKPOC-963-use-shared-erbjudande-fixture`.**
   - Bump `rimfrost-framework-regel` (both deps) to `1.3.5` in `pom.xml`.
   - Delete `src/test/java/se/fk/rimfrost/framework/regel/maskinell/ErbjudandeReferensdataTestService.java`.
   - Update `rimfrost-framework-regel-maskinell/docs/krav.md`: add `FRMASK-FR-03.3` under section `FRMASK-FR-03 — Skapande av uppgifter`.
   - Add new test — mirrors MANUELL's `should_send_correct_erbjudande_values_with_oul_create_request` — captures `CreateOperativUppgiftRequest`, asserts `oulRequest.getErbjudande().getNamn()` equals `ErbjudandeReferensdataTestService.DEFAULT_ERBJUDANDE_NAMN`. `@DisplayName("FRMASK-FR-03.3: Erbjudandenamn slås upp från referensdata och inkluderas i OUL-skapandeanropet")`. Location: probably new `RegelMaskinellOulTest` or extend an existing OUL-focused test class (TBD after inspecting MASKINELL test structure).
   - CDI discovery same caveat as step 6.
   - Run `mvn clean test`.
   - PR + merge.

## Verification

- After each `mvn clean test` in child repos, confirm zero failures and that the `Erbjudande…` fixture bean is being resolved (no `UnsatisfiedResolutionException`).
- Confirm MANUELL's `should_send_correct_erbjudande_values_with_oul_create_request` still passes with the new constant value.
- Confirm the new MASKINELL test fails when the fixture is stubbed to return a different value (sanity check the wiring is actually being asserted).

## Rollback

Each step lands on its own branch and can be reverted independently. CORE step 4 is the point of no return for the release; step 5 (the release itself) is only strictly required to unblock 6/7 — CORE can sit at 1.3.5-SNAPSHOT indefinitely if we defer child cleanup.
