# Plan FKPOC-928 — Komplettering foundation in rimfrost-framework-regel

Covers FKPOC-899. Adds everything komplettering-related that lives in
`rimfrost-framework-regel`: the pre-flight check contract, the storage, the OUL task handler,
the timeout service, and the abstract REST controller that regel repos extend.

## Goal

Introduce the following types:

| Type | Layer | Kind | Purpose |
|------|-------|------|---------|
| `KompletteringKontrollInterface` | `logic` | interface (default method) | Contract all regel services inherit; default returns `List.of()` |
| `KompletteringUnderlag` | `logic/dto` | `@Value.Immutable` interface | Describes one missing attribute in the yrkande |
| `KompletteringTillstand` | `logic/dto` | `@Value.Immutable` interface | Persisted correlation state while awaiting svar |
| `KompletteringStorage` | `logic` | `@ApplicationScoped` bean | Stores and retrieves pending komplettering state |
| `KompletteringOulHandler` | `logic` | `@ApplicationScoped` bean | Creates the komplettering OUL task via `OulAdapter` |
| `KompletteringService` | `logic` | `@ApplicationScoped` bean | Exposes `handleKompletteringTimeout()` for BPMN service task |
| `KompletteringSvarServiceInterface<T, Y>` | `logic` | interface | Extension point for regel repos to implement svar handling |
| `KompletteringController<T, Y>` | `presentation` | abstract JAX-RS resource | Base REST controller regel repos extend |

`UnderlagTyp` is intentionally **not** added to the framework. The `underlagTyp` field on
`KompletteringUnderlag` is a free-form `String` — each regel repo defines its own constants
locally, matching whatever `Underlag.typ()` strings it uses. This avoids a version bump of
`rimfrost-framework-regel` every time a new regel introduces a new attribute check.

## Approach

`KompletteringKontrollInterface` carries a default implementation of `checkKomplettering()`
that returns an empty list, meaning "nothing missing — proceed". Regel services that need an
actual check override the method. The maskinell and manuell request handlers (separate tickets)
will call `regelService.checkKomplettering()` unconditionally — no `instanceof` guard needed.

`KompletteringUnderlag` follows the existing `@Value.Immutable` pattern used throughout this
repo. The `underlagTyp` string should be a stable, lowercase identifier matching the
corresponding `Underlag.typ()` value — defined as a local constant in each regel repo, never
in this framework.

## Prerequisite

Verify that `rimfrost-framework-handlaggning-adapter` is already a compile dependency in
`pom.xml` (needed for the `Handlaggning` parameter type). Add it if absent.

---

## Steps

### 1. Add `KompletteringUnderlag`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/dto/KompletteringUnderlag.java`

```java
package se.fk.rimfrost.framework.regel.logic.dto;

import org.immutables.value.Value;

/**
 * Describes one attribute in the yrkande that is missing or incomplete.
 *
 * <p>Constructed inside {@code checkKomplettering()} — one instance per missing attribute.
 * The list is persisted in {@code KompletteringStorage} and served via
 * {@code GET /{handlaggningId}/komplettering} so the handläggare client can fetch structured
 * information about what is missing. It is not embedded in the OUL task description.
 */
@Value.Immutable
public interface KompletteringUnderlag
{
   /**
    * Machine-readable type identifier matching the corresponding {@code Underlag.typ()} value.
    *
    * <p>Define this string as a local constant in the regel repo — never in this framework.
    */
   String underlagTyp();

   /** Human-readable description shown to the handläggare. */
   String beskrivning();
}
```

### 2. Add `KompletteringKontrollInterface`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringKontrollInterface.java`

```java
package se.fk.rimfrost.framework.regel.logic;

import java.util.List;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringUnderlag;

/**
 * Pre-flight completeness check included in all regel service contracts.
 *
 * <p>The default implementation returns an empty list, meaning the yrkande is complete and
 * the regel should run immediately. Regel services that require a completeness check override
 * this method and return one {@link KompletteringUnderlag} per missing attribute.
 *
 * <p>The check inspects {@code handlaggning.yrkande()} and its nested fields — not stored
 * underlag, which are not available on {@link Handlaggning}.
 *
 * <p>This interface is extended by {@code RegelMaskinellServiceInterface} and
 * {@code RegelManuellServiceInterface}. Existing regel services that do not override this
 * method inherit the default and are unaffected.
 */
public interface KompletteringKontrollInterface
{
   /**
    * Returns the list of attributes missing from the yrkande, or an empty list if the
    * yrkande is complete.
    *
    * @param handlaggning the current handlaggning; inspect {@code handlaggning.yrkande()}
    * @return missing attributes, one entry per gap; empty means proceed with the regel
    */
   default List<KompletteringUnderlag> checkKomplettering(Handlaggning handlaggning)
   {
      return List.of();
   }
}
```

### 3. Add `KompletteringTillstand` and `KompletteringStorage`

Follows the established pattern of `CloudEventDataStorage` / `ProcessTopicInfoStorage` in
`rimfrost-framework-regel-manuell`: interface + Panache implementation + JPA entity + repository.

**Files:**
- `src/main/java/se/fk/rimfrost/framework/regel/logic/dto/KompletteringTillstand.java`
- `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/KompletteringStorage.java`
- `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/internal/PanacheKompletteringStorage.java`
- `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/internal/KompletteringTillstandEntity.java`
- `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/internal/KompletteringTillstandRepository.java`

`KompletteringTillstand` is the domain DTO holding the minimum correlation state needed to
complete or cancel the round. Missing attributes are not stored here —
`GET /{handlaggningId}/komplettering` re-calls `checkKomplettering()` live, so the handläggare
always sees the current state of the yrkande rather than a snapshot from when the round was
initiated.

```java
package se.fk.rimfrost.framework.regel.logic.dto;

import java.util.UUID;
import org.immutables.value.Value;

/**
 * Correlation state persisted by {@code KompletteringStorage} while a komplettering OUL task
 * is open. Cleared on {@code POST /komplettering/done} or on timeout.
 */
@Value.Immutable
public interface KompletteringTillstand
{
   UUID oulUppgiftId();
   String replyTo();

   /** Original CloudEvent payload — re-published to retrigger the regel on done. */
   String cloudEventData();
}
```

`KompletteringStorage` is the interface; `PanacheKompletteringStorage` is the
`@ApplicationScoped @Transactional` implementation that delegates to the repository.
Method naming follows the `get/set/delete` convention used across the codebase:

```java
package se.fk.rimfrost.framework.regel.logic.storage;

/**
 * Stores pending komplettering correlation state between OUL task creation and
 * handläggare svar.
 */
public interface KompletteringStorage
{
   /**
    * Persists state for a new komplettering round.
    */
   void setKompletteringTillstand(UUID handlaggningId, KompletteringTillstand tillstand);

   /**
    * Returns the tillstand, or empty if none exists (already resolved or timed out).
    */
   Optional<KompletteringTillstand> getKompletteringTillstand(UUID handlaggningId);

   /**
    * Removes the tillstand. Safe to call even if already absent.
    */
   void deleteKompletteringTillstand(UUID handlaggningId);
}
```

`KompletteringTillstandEntity` is the JPA entity, keyed on `handlaggningId`:

```java
package se.fk.rimfrost.framework.regel.logic.storage.internal;

/**
 * JPA entity backing {@code KompletteringStorage}. Primary key is {@code handlaggningId}.
 */
@Entity
@Table(name = "komplettering_tillstand")
public class KompletteringTillstandEntity
{
   @Id
   private UUID handlaggningId;
   private UUID oulUppgiftId;
   private String replyTo;

   @Column(columnDefinition = "text")
   private String cloudEventData;

   @Version
   private long version;

   private Instant createdAt;
   private Instant updatedAt;
}
```

`KompletteringTillstandRepository` extends Panache:

```java
package se.fk.rimfrost.framework.regel.logic.storage.internal;

@ApplicationScoped
public class KompletteringTillstandRepository
        extends PanacheRepositoryBase<KompletteringTillstandEntity, UUID>
{
}
```

#### Tests for `KompletteringStorage`

**File:** `src/test/java/se/fk/rimfrost/framework/regel/KompletteringStorageTest.java`

`@QuarkusTest` against the in-memory H2 database (same setup as other tests in this repo).
Injects `KompletteringStorage` directly and exercises the Panache implementation end-to-end.

```java
package se.fk.rimfrost.framework.regel;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class KompletteringStorageTest
{
   @Inject
   KompletteringStorage storage;

   @Test
   @DisplayName("FR-KS-01: Stored tillstand can be retrieved by handlaggningId")
   void should_return_stored_tillstand()
   {
      var handlaggningId = UUID.randomUUID();
      var tillstand = ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(UUID.randomUUID())
            .replyTo("reply-topic")
            .cloudEventData("{}")
            .build();

      storage.setKompletteringTillstand(handlaggningId, tillstand);

      var result = storage.getKompletteringTillstand(handlaggningId);
      assertTrue(result.isPresent());
      assertEquals(tillstand, result.get());
   }

   @Test
   @DisplayName("FR-KS-02: get returns empty for unknown handlaggningId")
   void should_return_empty_for_unknown_handlaggning_id()
   {
      var result = storage.getKompletteringTillstand(UUID.randomUUID());
      assertFalse(result.isPresent());
   }

   @Test
   @DisplayName("FR-KS-03: Deleted tillstand is no longer retrievable")
   void should_return_empty_after_delete()
   {
      var handlaggningId = UUID.randomUUID();
      var tillstand = ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(UUID.randomUUID())
            .replyTo("reply-topic")
            .cloudEventData("{}")
            .build();

      storage.setKompletteringTillstand(handlaggningId, tillstand);
      storage.deleteKompletteringTillstand(handlaggningId);

      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }

   @Test
   @DisplayName("FR-KS-04: delete on absent handlaggningId does not throw")
   void should_not_throw_on_delete_of_absent_entry()
   {
      storage.deleteKompletteringTillstand(UUID.randomUUID());
   }
}
```

### 4. Add `KompletteringOulHandler`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringOulHandler.java`

Central entry point called by both maskinell and manuell request handler hooks when
`checkKomplettering()` returns a non-empty list.

No separate `KompletteringConfig` YAML class is needed. The OUL task metadata is derived
entirely from the regel's existing `RegelConfig`:

| OUL field | Derived from |
|-----------|-------------|
| `namn` | `"Hantera komplettering för " + specifikation.getNamn()` |
| `beskrivning` | Fixed framework string |
| `roll` | `specifikation.getRoll()` |
| `url` | `uppgift.getPath()` with last segment replaced by `komplettering` |
| `verksamhetslogik` | `specifikation.getVerksamhetslogik()` |

```java
package se.fk.rimfrost.framework.regel.logic;

/**
 * Creates the komplettering OUL task and persists correlation state.
 *
 * <p>OUL task metadata ({@code namn}, {@code beskrivning}, {@code roll}, {@code url},
 * {@code verksamhetslogik}) is derived from the regel's own {@code RegelConfig} —
 * no separate komplettering YAML section is required in regel repos.
 *
 * <p>Missing attribute details are served via
 * {@code GET /{handlaggningId}/komplettering} (pull model), not embedded in the OUL task.
 */
@ApplicationScoped
public class KompletteringOulHandler
{
   /**
    * Creates the komplettering OUL task and stores the full correlation state.
    * Returns without sending a Kafka reply — the BPMN process instance keeps waiting.
    *
    * @param handlaggningId   the handlaggning being processed
    * @param cloudEventData   original CloudEvent payload, re-published on done
    * @param replyTo          Kafka reply topic from the original CloudEvent
    * @param regelConfig      the regel's own config; used to derive all OUL task metadata
    */
   public void initiate(UUID handlaggningId,
                        String cloudEventData,
                        String replyTo,
                        RegelConfig regelConfig) { ... }
}
```

### 5. Add `KompletteringService`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringService.java`

Called by the BPMN timeout service task when the komplettering timer fires.

```java
package se.fk.rimfrost.framework.regel.logic;

/**
 * Handles the komplettering timeout triggered by the BPMN event-based gateway timer branch.
 */
@ApplicationScoped
public class KompletteringService
{
   /**
    * Ends the open OUL task and clears storage. Safe to call if storage is already empty
    * (handläggare completed just before timeout) — logs and returns without throwing.
    *
    * @param handlaggningId the handlaggning whose komplettering round timed out
    */
   public void handleKompletteringTimeout(UUID handlaggningId) { ... }
}
```

The two race conditions to handle inside this method:

- **Timeout wins:** `find()` returns a tillstand → end OUL task → `remove()`
- **Done wins first:** `find()` returns empty → log and return, do not throw

### 6. Add `KompletteringSvarServiceInterface`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringSvarServiceInterface.java`

Extension point for regel repos. `T` is the GET response type, `Y` is the PATCH request type —
both generated from the regel repo's OpenAPI spec.

```java
package se.fk.rimfrost.framework.regel.logic;

/**
 * Regel-specific extension point for handling the handläggare's svar during komplettering.
 *
 * @param <T> response type for {@code GET /{handlaggningId}/komplettering}
 * @param <Y> request type for {@code PATCH /{handlaggningId}/komplettering}
 */
public interface KompletteringSvarServiceInterface<T, Y>
{
   /**
    * Returns the data the handläggare needs to register the sökande's svar.
    */
   T readSvarData(Handlaggning handlaggning);

   /**
    * Applies the svar to the handlaggning and returns the update.
    */
   HandlaggningUpdate registerSvar(Handlaggning handlaggning, Y request);
}
```

### 7. Add `KompletteringController`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/presentation/rest/KompletteringController.java`

Abstract JAX-RS resource following the same pattern as `RegelManuellController`. Regel repos
subclass it with their own generated types:

```java
public class RtfKompletteringController
        extends KompletteringController<RtfKompletteringResponse, RtfPatchKompletteringRequest> {}
```

```java
package se.fk.rimfrost.framework.regel.presentation.rest;

/**
 * Abstract base controller for komplettering endpoints. Extend with the regel's own
 * OpenAPI-generated request and response types.
 *
 * @param <T> GET response type — data shown to the handläggare
 * @param <Y> PATCH request type — the handläggare's registered svar
 */
public abstract class KompletteringController<T, Y>
{
   /**
    * Returns structured information about what is missing.
    * Called by the handläggare client when the komplettering OUL task is opened (pull model).
    */
   @GET
   @Path("/{handlaggningId}/komplettering")
   public Response getKomplettering(@PathParam("handlaggningId") UUID handlaggningId) { ... }

   /**
    * Registers the sökande's svar for one or more missing attributes.
    */
   @PATCH
   @Path("/{handlaggningId}/komplettering")
   public void patchKomplettering(@PathParam("handlaggningId") UUID handlaggningId,
                                  @Valid @NotNull Y request) { ... }

   /**
    * Marks komplettering as complete. Re-publishes the original CloudEvent to retrigger
    * the regel. Returns 409 Conflict if the timeout has already cleared storage.
    */
   @POST
   @Path("/{handlaggningId}/komplettering/done")
   public void kompletteringDone(@PathParam("handlaggningId") UUID handlaggningId) { ... }
}
```

---

## Verification

- `mvn test-compile -q` passes with no new warnings
- `mvn spotless:apply` produces no diff
- No existing tests break (no behaviour changes — only new types added)
- `KompletteringStorage.find()` returns empty for an unknown `handlaggningId`
- `KompletteringService.handleKompletteringTimeout()` logs and returns cleanly when storage is already empty
