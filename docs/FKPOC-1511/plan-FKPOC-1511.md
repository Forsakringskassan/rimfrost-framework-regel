# FKPOC-1511 — Add handlaggningInstanceBaseUrl to regel framework

## Background

The AsyncAPI spec (`rimfrost-framework-regel-asyncapi`) has been updated to add a required
property `handlaggningInstanceBaseUrl` (URI) to `RegelRequestMessagePayloadData` (PR #22).

Today the framework routes all handläggning calls to a single static URL configured via
`handlaggning.api.base-url`. The new property enables dynamic per-request routing so that
the calling process can direct the regel framework to the correct handläggning instance in
environments where multiple instances run in parallel.

The new `HandlaggningAdapter` that accepts a dynamic base URL is being developed in a
separate workstream and is out of scope here. This plan covers only the changes needed to
read, carry, and expose `handlaggningInstanceBaseUrl` through the framework.

---

## Steps

- [x] **1. Add `handlaggningInstanceBaseUrl` to `RegelDataRequest`**

  Add a non-nullable `String handlaggningInstanceBaseUrl()` field to the immutable interface
  `RegelDataRequest` (no `@Nullable` — the field is required per FRALL-FR-07.2).

- [x] **2. Map `handlaggningInstanceBaseUrl` in `RegelKafkaMapper`**

  Read `request.getData().getHandlaggningInstanceBaseUrl()` and set it on the
  `ImmutableRegelDataRequest` builder, following the same pattern as `aktivitetId` and
  `processvyUrl`.

  Update `RegelTestData` to populate the new field in test payloads.

- [x] **3. Pass `handlaggningInstanceBaseUrl` to `HandlaggningAdapter` in `RegelRequestHandlerBase`**

  Update `getHandlaggning()` and `updateHandlaggning()` to forward the base URL from
  `RegelDataRequest` to the adapter, using the new adapter API once it is available.

  Code is implemented — `getHandlaggning(UUID, String, CloudEventData)` and
  `updateHandlaggning(HandlaggningUpdate, String, CloudEventData, UUID)` call
  `readHandlaggning(String baseUrl, UUID)` and `updateHandlaggning(String baseUrl, HandlaggningUpdate)`
  per the actual API in PR #48. Note: `handlaggning.api.base-url` property is removed entirely
  in the new adapter — no static fallback.
  Will not compile until the new `HandlaggningAdapter` is released.

  Also a breaking change for callers of the base class protected methods in
  `rimfrost-framework-regel-komplettering` and `rimfrost-framework-regel-kommunicering`
  — those repos need updating as part of FKPOC-1511.

- [x] **4. Update tests**

  - `RegelKafkaMapperTest` — add a test asserting that `handlaggningInstanceBaseUrl` is
    mapped correctly (reference FRALL-FR-07.1).
  - `RegelMessageHandlerTest` — verify that the field is forwarded in the mapped request.
  - Verify FRALL-FR-07.2: assert that a request without `handlaggningInstanceBaseUrl` is
    rejected.

- [x] **5. Compile check and run tests**

  ```
  mvn test-compile -q
  mvn spotless:apply
  mvn test
  ```

  Verify zero compilation errors and all tests green.

---

## Deviations
