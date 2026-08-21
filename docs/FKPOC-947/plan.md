# FKPOC-947 — Replace raw CloudEvent JSON with RegelDataRequest in komplettering storage

## Background

`KompletteringOulHandler.initiate()` currently accepts a raw `String cloudEventData`
representing the original CloudEvent payload as JSON. On `kompletteringDone()`,
`KompletteringController` deserializes that string back to `RegelRequestMessagePayload`
(an external generated type), maps it via `RegelKafkaMapper` to `RegelDataRequest`,
and calls `handleRegelRequest()`.

This introduces an unnecessary dependency on `RegelRequestMessagePayload` in the
persistence/replay path. Callers (e.g. `rimfrost-framework-regel-manuell`) that only
have a `RegelDataRequest` in scope are forced to reconstruct the raw CloudEvent JSON,
which is fragile and couples the storage format to the Kafka message format.

**Goal:** change `initiate()` to accept `RegelDataRequest` directly and store its
fields as individual columns — consistent with how `CloudEventDataEntity` and
`RegelCommonDataEntity` in this repo (`rimfrost-framework-regel`) already handle
similar correlation data (added by PR #108). On replay, the mapper reconstructs
`RegelDataRequest` from those columns and `handleRegelRequest()` is called directly —
no JSON serialization, no `ObjectMapper`, no `RegelRequestMessagePayload` in the
storage path.

As a consequence, the redundant standalone `replyTo` and `handlaggningId` parameters
on `initiate()` are removed — both are available on `RegelDataRequest`.

## Steps

### Step 1 — Update `KompletteringTillstand` DTO

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/dto/KompletteringTillstand.java`

Replace `String cloudEventData()` and `String replyTo()` with a single
`RegelDataRequest regelDataRequest()` accessor:

```java
@Value.Immutable
public interface KompletteringTillstand
{
    UUID oulUppgiftId();
    RegelDataRequest regelDataRequest();
}
```

### Step 2 — Expand `KompletteringTillstandEntity` with individual columns

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/internal/KompletteringTillstandEntity.java`

Remove `cloudEventData` and `replyTo` fields. Add one column per field of
`RegelDataRequest` (all fields except `handlaggningId`, which is already the PK):

| Field | Column | Type |
|-------|--------|------|
| `id` | `regel_request_id` | `UUID` |
| `aktivitetId` | `aktivitet_id` | `UUID` |
| `replyTo` | `reply_to` | `VARCHAR(255)` |
| `type` | `type` | `VARCHAR(255)` |
| `kogitorootprocid` | `kogitorootprocid` | `VARCHAR(255)` |
| `kogitorootprociid` | `kogitorootprociid` | `UUID` |
| `kogitoparentprociid` | `kogitoparentprociid` | `UUID` |
| `kogitoprocid` | `kogitoprocid` | `VARCHAR(255)` |
| `kogitoprocinstanceid` | `kogitoprocinstanceid` | `UUID` |
| `kogitoprocist` | `kogitoprocist` | `VARCHAR(255)` |
| `kogitoprocversion` | `kogitoprocversion` | `VARCHAR(255)` |

### Step 3 — Update `KompletteringTillstandMapper`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/storage/internal/KompletteringTillstandMapper.java`

`toEntity()`: map each `RegelDataRequest` field to the corresponding entity column.

`toDomain()`: reconstruct `ImmutableRegelDataRequest` from individual entity fields,
wrap in `ImmutableKompletteringTillstand`. No `ObjectMapper` involved.

### Step 4 — Add Flyway migration

**File:** `src/test/resources/db/migration/V005__komplettering_tillstand_expand_columns.sql`

(V002–V004 were consumed by PR #108 for `cloud_event_data`, `common_data`, and `process_topic_info`.)

Drop `cloud_event_data`, keep and repurpose `reply_to` in-place, add remaining columns:

```sql
ALTER TABLE komplettering_tillstand
    DROP COLUMN cloud_event_data,
    ADD COLUMN regel_request_id      UUID         NOT NULL,
    ADD COLUMN aktivitet_id          UUID         NOT NULL,
    ADD COLUMN type                  VARCHAR(255) NOT NULL,
    ADD COLUMN kogitorootprocid      VARCHAR(255) NOT NULL,
    ADD COLUMN kogitorootprociid     UUID         NOT NULL,
    ADD COLUMN kogitoparentprociid   UUID         NOT NULL,
    ADD COLUMN kogitoprocid          VARCHAR(255) NOT NULL,
    ADD COLUMN kogitoprocinstanceid  UUID         NOT NULL,
    ADD COLUMN kogitoprocist         VARCHAR(255) NOT NULL,
    ADD COLUMN kogitoprocversion     VARCHAR(255) NOT NULL;
```

(`reply_to` already exists as a column and maps directly to `RegelDataRequest.replyTo()`
— no rename needed.)

### Step 5 — Update `KompletteringOulHandler.initiate()`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/logic/KompletteringOulHandler.java`

New signature — `handlaggningId` and `replyTo` removed as standalone parameters:

```java
public void initiate(RegelDataRequest regelDataRequest,
      Map<String, String> cloudEventAttributes,
      RegelConfig regelConfig,
      Erbjudande erbjudande) throws OulException
```

Extract `handlaggningId` and `replyTo` from `regelDataRequest`. No `ObjectMapper`
injection needed.

### Step 6 — Update `KompletteringController.kompletteringDone()`

**File:** `src/main/java/se/fk/rimfrost/framework/regel/presentation/rest/KompletteringController.java`

Replace the `RegelRequestMessagePayload` / `RegelKafkaMapper` / `ObjectMapper`
round-trip with a direct read from the domain object:

```java
regelRequestHandler.handleRegelRequest(tillstand.get().regelDataRequest());
```

Remove injected `RegelKafkaMapper` and `ObjectMapper` if no longer used elsewhere
in the class.

### Step 7 — Tests

Update affected unit and integration tests:
- `KompletteringOulHandler` tests: pass `RegelDataRequest` instead of raw JSON string.
- `KompletteringController` tests: verify replay calls `handleRegelRequest()` with the
  `RegelDataRequest` from storage directly, no `RegelRequestMessagePayload` involved.

### Step 8 — Release

Bump to the next minor version. Record in `CHANGELOG.md`.
`rimfrost-framework-regel-manuell` (FKPOC-935) must target this new version.

## Definition of done

- `KompletteringOulHandler.initiate()` accepts `RegelDataRequest`, no raw JSON string
- `RegelRequestMessagePayload` and `RegelKafkaMapper` are absent from the replay path
- `cloud_event_data` column replaced by individual `RegelDataRequest` columns
- No `ObjectMapper` in the storage or replay path
- All tests green (`mvn test`)
- New minor version released
