# RouteStore swallows write errors and has read-modify-write races

Labels: bug, data-loss

## Problem

Two related defects in `app/src/main/java/com/hsl/wear/data/store/RouteStore.kt`:

1. **Every write swallows exceptions silently.** All eight mutation methods
   (`saveRouteState`, `clearRouteState`, `addFavoriteLocation`, ... `clearAllData`)
   wrap the edit in `try { ... } catch (e: Exception) { // Log error in production }`
   (e.g. lines 57-65, 96-113). A failed save of the active route means the tile and a
   process-restarted app resume a stale journey with no error anywhere, not even
   logcat. The placeholder comments are themselves leftover TODOs.

2. **List updates are read-modify-write outside the DataStore transaction.**
   `addFavoriteLocation` (lines 96-113), `removeFavoriteLocation`,
   `addFavoriteRoute`, `removeFavoriteRoute`, and `addRecentLocation` all do
   `flow.first()` then `dataStore.edit { ... }`. Two concurrent callers (e.g. the
   tile service saving refreshed realtime state while the app saves a favorite,
   both via the same singleton) can interleave and lose one write.

Also: corrupted JSON in any key decodes to `null`/`emptyList()` silently
(lines 48-54 etc.), so schema changes across app updates wipe data with no signal.

## Fix

- Move the read inside the `edit { }` lambda: decode from `preferences[KEY]`,
  mutate, re-encode. `edit` serializes writers, which removes the race.
- Replace the empty catch blocks with `Log.e` at minimum, and let callers see
  failures: return `Result<Unit>` or rethrow `IOException` so viewmodels can show
  an error instead of pretending the save happened.
- On decode failure, log the payload class and consider clearing the corrupt key
  explicitly instead of returning null forever.

## Acceptance criteria

- No `catch (e: Exception) {}` without logging remains in `RouteStore`.
- List mutations happen entirely inside `dataStore.edit`.
- Unit tests cover: concurrent `addFavoriteRoute` calls do not lose entries;
  corrupt JSON yields a logged, well-defined fallback.
