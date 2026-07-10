# `parseIsoTime` silently falls back to "now" and corrupts time logic

Labels: bug, correctness

## Problem

`TimeFormatter.parseIsoTime` (`app/src/main/java/com/hsl/wear/utils/TimeFormatter.kt:31-37`)
returns `System.currentTimeMillis()` when parsing fails. Callers cannot tell a real
timestamp from the fallback, and several depend on it for control flow:

- `CurrentLegTileService.isRouteObsolete` (`CurrentLegTileService.kt:344-354`) and
  `RouteTrackingViewModel.isRouteObsolete` (`RouteTrackingViewModel.kt:308-317`):
  a malformed last-leg timestamp makes `startTime = now`, so the route is
  considered obsolete `duration + 2min` later regardless of the actual schedule,
  or conversely an active route is auto-cleared mid-journey.
- Tile timeline validity windows (`CurrentLegTileService.kt:141-159`): a parse
  failure makes every leg "depart now", producing overlapping or empty validity
  intervals and a wrong tile.
- `getStatusText` shows "Now" for unparseable departures instead of an error state.

The same pattern exists in `calculateFinalArrivalTime` (returns `now` for empty
legs) and `formatTime`'s hand-rolled string slicing (lines 59-72), which mis-parses
negative-offset timestamps like `2025-11-23T14:32:00-02:00` (the `substringBefore('-')`
also cuts on the date separator only because `substringAfter('T')` ran first, but it
truncates the offset silently and returns wrong text for fractional seconds).

## Fix

- Change `parseIsoTime` to return `Long?` (or throw) and make each caller decide:
  skip the timeline entry, keep the route, or surface an error.
- Parse with `OffsetDateTime.parse` / `Instant.parse` consistently; delete the
  string-slicing `formatTime` in favor of `DateTimeFormatter.ofPattern("HH:mm")`
  on the parsed value.
- Add unit tests with valid, offset, fractional-second, and garbage inputs.

## Acceptance criteria

- No code path treats an unparseable timestamp as "now".
- Route auto-end logic verified by tests: malformed timestamps never clear an
  active route.
