# LocationProvider ignores coroutine cancellation and can hang

Labels: bug, stability

## Problem

`LocationProvider.getCurrentLocation` (`app/src/main/java/com/hsl/wear/location/LocationProvider.kt:51-97`):

1. **Cancellation is not wired up.** A `CancellationTokenSource` is created
   (line 75) but never connected to `continuation.invokeOnCancellation`. When the
   calling coroutine is cancelled (user leaves the screen), the high-accuracy GPS
   request keeps running to completion, wasting battery on a watch, and the result
   is dropped.
2. **No timeout.** `getCurrentLocation` with `PRIORITY_HIGH_ACCURACY` can take a
   long time indoors; nothing bounds the wait, so a caller without its own timeout
   shows a spinner indefinitely.
3. **Dead code.** `tryGetLastKnownLocation` (lines 132-157) duplicates
   `tryLastLocation` and is never called.
4. `requestLocationUpdates` (lines 196-226) is declared `suspend` but never
   suspends, and returns a raw callback the caller must remember to remove.
   A `callbackFlow` that unregisters in `awaitClose` would make leaks impossible.

## Fix

- Add `continuation.invokeOnCancellation { cancellationTokenSource.cancel() }`.
- Wrap the fresh-location attempt in `withTimeoutOrNull` (10-15 s) before falling
  back to `lastLocation`.
- Delete `tryGetLastKnownLocation`; drop the unused `suspend` or convert
  `requestLocationUpdates` to `callbackFlow<Location>`.

## Acceptance criteria

- Cancelling the calling coroutine cancels the underlying fused-location request.
- A location request never blocks a caller for longer than the timeout.
- No unused location-fallback methods remain.
