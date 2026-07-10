# Adopt a version catalog and fix dependency drift

Labels: devops, build

## Problem

`app/build.gradle.kts` declares ~35 dependencies with inline version strings, and
they have already drifted apart:

- Compose UI is split across versions: main `androidx.compose.ui:ui:1.7.6`
  (lines 132-134) vs `ui-tooling`/`ui-test-junit4`/`ui-test-manifest` at `1.5.4`
  (lines 167-169), and `androidx.wear.compose:compose-ui-tooling:1.2.1` (line 135)
  against the `1.5.0` wear-compose family. Mixed Compose artifact versions cause
  runtime `NoSuchMethodError`s in exactly the tooling/test paths that are least
  exercised.
- `guava:31.1-android` (line 121) is years old; protolayout pulls its own newer
  listenablefuture handling.
- `kotlinx-datetime:0.4.1` (line 161) is old, and the project uses both
  kotlinx-datetime and `java.time` for the same jobs (`HslRepository` vs
  `TimeFormatter`); one should go (minSdk 30 has full `java.time`).
- Both `kapt` and KSP plugins are applied (`app/build.gradle.kts:8-15`), but no
  dependency uses `kapt(...)`. The kapt plugin alone adds stub-generation cost to
  every build.
- Duplicate wearable libs: `compileOnly com.google.android.wearable:wearable` and
  `implementation com.google.android.support:wearable` (lines 125-126); the
  support artifact is legacy and likely unused with Compose.

## Fix

- Create `gradle/libs.versions.toml` and move all coordinates there; use a single
  `compose` version (or the Compose BOM) and one wear-compose version.
- Remove `kotlin("kapt")` and the `kapt { }` block.
- Drop `guava` if protolayout's transitive version suffices, or bump to a current
  `-android` release.
- Standardize on `java.time`, remove `kotlinx-datetime` (issue 008 removes its
  only caller).
- Audit the two `wearable` artifacts and remove what nothing references.
- Optional follow-up: renovate/dependabot config so drift cannot recur.

## Acceptance criteria

- All versions come from the catalog; no inline version strings in build scripts.
- Exactly one Compose and one wear-compose version in the dependency tree
  (`./gradlew :app:dependencies` verified).
- Debug and release builds pass.
