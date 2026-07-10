# Plan query breaks under non-Gregorian locales and non-Helsinki timezones

Labels: bug, correctness

## Problem

`GraphQLQueries.getCurrentLocalTime` (`app/src/main/java/com/hsl/wear/network/GraphQLQueries.kt:9-15`)
formats the plan request date/time with `SimpleDateFormat(..., Locale.getDefault())`
and the device timezone, then interpolates the strings into the GraphQL query:

1. **Locale digits.** With a device locale using non-ASCII digits (ar, fa, bn, ...)
   `SimpleDateFormat` emits those digits, producing `date: "٢٠٢٦-٠٧-١٠"` and a
   server-side parse error. Route planning fails entirely for those users.
2. **Timezone.** The Digitransit API interprets `date`/`time` in the transit
   feed's timezone (Europe/Helsinki). A watch set to any other timezone sends its
   local wall-clock time, so itineraries are planned for the wrong moment (off by
   the zone difference), silently.

## Fix

- Format with `DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)` (or `Locale.ROOT`)
  over `ZonedDateTime.now(ZoneId.of("Europe/Helsinki"))`, keeping the existing
  5-minute lookback (`NetworkConstants.TIME_ADJUSTMENT_MINUTES`).
- Alternatively omit `date`/`time` from the query; the API defaults to "now",
  which is what the app wants anyway.
- Prefer GraphQL variables over string interpolation for these values, matching
  how `getTripStatus` already does it.

## Acceptance criteria

- Unit test asserting the generated query contains ASCII digits under
  `Locale("ar")` as the default locale.
- Plan results are correct when the device timezone is not Europe/Helsinki.
