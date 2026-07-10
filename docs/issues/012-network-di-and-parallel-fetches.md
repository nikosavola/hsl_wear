# Share one OkHttpClient/Json via DI and parallelize trip status fetches

Labels: performance

## Problem

1. **Two full HTTP stacks.** `GraphQLClient` (`GraphQLClient.kt:19-36`) and
   `GeocodingClient` (`GeocodingClient.kt:24-41`) each build their own
   `OkHttpClient` and `Json`. Two connection pools, two dispatchers, and no shared
   connection reuse against the same host (`api.digitransit.fi`). On a watch,
   the extra TLS handshakes cost real latency and battery.
2. **Sequential batch fetch.** `HslRepository.getMultipleTripStatuses`
   (`HslRepository.kt:133-158`) loops trip IDs one request at a time; a 4-transit-leg
   route pays 4x round-trip latency on every realtime refresh. The comment says
   "avoid overwhelming the API", but 3-5 concurrent requests is nowhere near that.
3. **DI redundancy/leakage.** `NetworkModule` provides both clients manually even
   though they have `@Inject` constructors, and `GraphQLClient.client`/`json` are
   `public` only because `executeQuery` is `inline` (`GraphQLClient.kt:19,32`).

## Fix

- Provide a single `OkHttpClient` and a single `Json` from `NetworkModule`; inject
  them into both clients. Delete the redundant `@Provides` for the clients
  themselves.
- Rewrite `getMultipleTripStatuses` with `coroutineScope { ids.map { async { ... } }.awaitAll() }`,
  optionally bounded with a `Semaphore(4)`. Better: a single GraphQL request with
  aliases for all trip IDs would collapse it to one round trip.
- Split `executeQuery` into a non-inline transport function returning the raw body
  plus a small reified wrapper for decoding, so the client internals can be
  `private`.

## Acceptance criteria

- One `OkHttpClient` instance app-wide (verify via DI graph or logging).
- Realtime refresh time for a multi-leg route no longer scales linearly with the
  number of transit legs.
- Public surface of `GraphQLClient` is just the query methods.
