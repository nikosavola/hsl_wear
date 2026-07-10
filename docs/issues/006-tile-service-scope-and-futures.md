# Tile service leaks its coroutine scope and a dedicated thread

Labels: bug, stability, wear-os

## Problem

`app/src/main/java/com/hsl/wear/tiles/CurrentLegTileService.kt`:

1. **Scope never cancelled.** `serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`
   (line 54) has no matching `onDestroy` override, so coroutines launched in
   `onTileRequest` (including the fire-and-forget realtime refresh at lines 77-84)
   outlive the service and hold references after the system destroys it.
2. **Unused single-thread executor.** `tileDispatcher` (lines 55-59) spins up a
   dedicated daemon thread per service instance and is never used anywhere.
3. **Deprecated future bridging.** `JdkFutureAdapters.listenInPoolThread(future)`
   (line 98) is deprecated Guava API that parks a pool thread per request. The
   supported patterns are `CallbackToFutureAdapter` or, better, Horologist's
   `SuspendingTileService` which exposes `suspend fun tileRequest(...)` directly.
4. **Refresh result is discarded for this render.** The async
   `refreshRouteStateRealTimeData` saves updated state to the store after the tile
   was already built from the stale state, and it races with the app process
   writing the same key (see issue 002). If intentional (fresh data on next
   refresh), a comment should say so; otherwise await it with a short timeout.
5. Minor: `TILE_SIZE` (line 377) is unused; `buildTimeline` calls
   `timelineBuilder.build()` mid-construction (line 191) just to count entries,
   which is wasteful; track a counter instead.

## Fix

- Migrate to `com.google.android.horologist:horologist-tiles` `SuspendingTileService`,
  which manages the lifecycle scope and futures. Failing that: cancel the scope in
  `onDestroy`, delete `tileDispatcher`, and replace `JdkFutureAdapters` with
  `CallbackToFutureAdapter`.
- Decide and document the refresh-staleness behavior.

## Acceptance criteria

- No coroutine scope or executor outlives the service.
- No deprecated Guava future adapters.
- Tile still renders, auto-switches legs, and refreshes on the emulator.
