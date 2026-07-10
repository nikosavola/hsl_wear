# Improvement plan

GitHub-style issues from a code and build audit (2026-07-10). Each file is ready to
paste into the GitHub issue tracker (title = H1, labels listed at top).

## Priority order

| # | Issue | Labels |
|---|-------|--------|
| 001 | [Remove `usesCleartextTraffic` from the manifest](001-remove-cleartext-traffic.md) | security |
| 002 | [RouteStore swallows write errors and has read-modify-write races](002-routestore-error-handling-and-races.md) | bug, data-loss |
| 003 | [`parseIsoTime` silently falls back to "now" and corrupts time logic](003-parseisotime-silent-fallback.md) | bug, correctness |
| 004 | [Plan query breaks under non-Gregorian locales and non-Helsinki timezones](004-plan-query-locale-timezone.md) | bug, correctness |
| 005 | [LocationProvider ignores coroutine cancellation and can hang](005-locationprovider-cancellation-timeout.md) | bug, stability |
| 006 | [Tile service leaks its coroutine scope and a dedicated thread](006-tile-service-scope-and-futures.md) | bug, stability, wear-os |
| 007 | [ViewModels take one-shot snapshots of DataStore instead of collecting](007-viewmodels-snapshot-instead-of-collect.md) | architecture, stability |
| 008 | [Clean up leftover refactor artifacts and dead code](008-dead-code-and-leftover-todos.md) | tech-debt |
| 009 | [Add GitHub Actions CI](009-add-ci.md) | devops |
| 010 | [Machine-specific paths are checked into the build config](010-machine-specific-build-config.md) | devops, bug |
| 011 | [Adopt a version catalog and fix dependency drift](011-version-catalog-and-dependency-drift.md) | devops, build |
| 012 | [Share one OkHttpClient/Json via DI and parallelize trip status fetches](012-network-di-and-parallel-fetches.md) | performance |
| 013 | [Over-broad ProGuard keep rules defeat minification](013-proguard-overbroad-keeps.md) | performance, apk-size |
| 014 | [Expand test coverage beyond the single mapper test](014-expand-test-coverage.md) | testing |
