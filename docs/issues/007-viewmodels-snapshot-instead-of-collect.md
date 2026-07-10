# ViewModels take one-shot snapshots of DataStore instead of collecting

Labels: architecture, stability

## Problem

The store layer exposes proper `Flow`s (`RouteStore.routeStateFlow` etc.), but the
viewmodels read them once with `first()` and copy the value into their own
`MutableStateFlow`:

- `HomeViewModel.checkActiveRoute` (`HomeViewModel.kt:30`)
- `RouteTrackingViewModel.loadActiveRoute` (`RouteTrackingViewModel.kt:43`)

Consequences:

- When `CurrentLegTileService` refreshes realtime data and saves the route state
  in the background, an open RouteTracking screen keeps showing stale times until
  the user manually taps refresh.
- The workaround is visible in the code: `refreshRoute()` re-runs `loadActiveRoute`
  and `HomeViewModel` exposes `checkActiveRoute()` for screens to call at the right
  moments, which is easy to forget and racy.
- Two sources of truth (store flow vs viewmodel copy) can disagree after
  `advanceToNextLeg` and similar mutations.

## Fix

- Collect the store flow instead:
  `transitRepository.activeRouteState.map { ... }.stateIn(viewModelScope, WhileSubscribed(5000), initial)`
  and merge screen-local state (loading, error, currentTime ticker) around it.
- Mutation methods then only call the repository; the UI updates through the
  single flow, and `refreshRoute`/`checkActiveRoute` plumbing disappears.

## Acceptance criteria

- No `activeRouteState.first()` in viewmodel init/refresh paths.
- Tile-driven store updates appear on an open tracking screen without user action.
