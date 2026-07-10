# Expand test coverage beyond the single mapper test

Labels: testing

## Problem

The entire test suite is one file, `GraphQLResponseMapperTest.kt` (176 lines).
CLAUDE.md describes a testing strategy (unit, UI, integration, tile tests) that
does not exist. The logic most likely to break is pure and cheap to test:

- `TimeFormatter`: ISO parsing edge cases, status text, journey state, arrival
  calculations (see issue 003 for known bugs to pin down first).
- `RouteState` navigation invariants: `moveToNextLeg`/`moveToPreviousLeg`/
  `jumpToLeg` boundary behavior via `TransitRepository`.
- `RouteStore`: round-trip serialization, corrupt-JSON handling, list cap
  (10/20 items), concurrent mutation (issue 002) - runs on JVM with an in-memory
  or tmp-file DataStore.
- `GraphQLClient`/`GeocodingClient` against OkHttp `MockWebServer`: success,
  HTTP error, GraphQL error payload, malformed JSON, empty body.
- `GraphQLQueries`: locale-stable query generation (issue 004).
- `LocationRanker` / `ZoneUtils` / `DistanceFormatter`: pure functions, trivial
  to cover.

There is also no test infrastructure: no coroutines-test, mockk/turbine, or
MockWebServer dependencies.

## Fix

- Add `kotlinx-coroutines-test`, `turbine`, `mockwebserver`, and (if needed)
  `mockk` as `testImplementation`.
- Write the suites above; target the data and utils layers first (highest value,
  no Android dependencies).
- Wire into CI (issue 009) so coverage is enforced going forward. Optional:
  kover for a coverage floor on `data/` and `utils/`.

## Acceptance criteria

- `./gradlew testDebugUnitTest` runs green with meaningful suites for
  TimeFormatter, RouteStore, TransitRepository, and both network clients.
- Known bugs from issues 002-004 have regression tests.
