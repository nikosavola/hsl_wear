# Clean up leftover refactor artifacts and dead code

Labels: tech-debt

## Problem

The codebase carries artifacts of an unfinished refactor ("pt.1" in git history).
None are TODO-tagged, so they do not show up in searches, but they are pending work:

- **Stale "next step" comments, never done:** `GraphQLClient.kt:20-22` and
  `GeocodingClient.kt:25-27` hardcode timeouts with the comment
  `// Will be replaced with NetworkConstants in next step`. `NetworkConstants`
  exists but has no timeout constants.
- **Placeholder error handling:** eight `// Log error in production` empty catch
  blocks in `RouteStore.kt` (tracked in issue 002).
- **Dead code:**
  - `HslRepository.getCurrentTimeString` (`HslRepository.kt:95-99`), unused.
  - `HslRepository.getTripStatus` result chain (`HslRepository.kt:120-124`):
    `.onSuccess { response }.onFailure { Result.failure(...) }` builds values and
    discards them; it is a no-op around `result`.
  - `LocationProvider.tryGetLastKnownLocation` (`LocationProvider.kt:132-157`),
    duplicate of `tryLastLocation`, never called.
  - `CurrentLegTileService`: unused `tileDispatcher` executor and `TILE_SIZE`
    constant; a block of blank lines at 339-343.
  - `GeocodingClient.searchLocations` just forwards to
    `searchLocationsWithBoundaries`, which no longer takes boundaries.
- **Parallel in-memory store still wired in:** `UserLocationStore` holds hardcoded
  sample "Home"/"Work" favorites in memory (`UserLocationStore.kt:23-41`) and is
  injected into `RouteInputViewModel`, alongside the persistent
  `RouteStore` favorites path used elsewhere. Two favorites systems, one fake.
- **Root-level scripts:** `test_hsl_api.py` (exploratory API script) and mac-only
  `build.sh` (see issue 010) live at the repo root.

## Fix

- Add timeout constants to `NetworkConstants` and use them; delete the comments.
- Delete the dead methods/fields listed above.
- Decide the fate of `UserLocationStore`: either back it with DataStore and remove
  the sample data, or delete it and route `RouteInputViewModel` through
  `TransitRepository` favorites/recents.
- Move `test_hsl_api.py` under `scripts/` or delete it.

## Acceptance criteria

- `grep -rn "next step\|Log error in production"` returns nothing.
- No hardcoded sample locations ship in release builds.
- Build passes; no unused-symbol warnings for the touched files.
