# Over-broad ProGuard keep rules defeat minification

Labels: performance, apk-size

## Problem

`app/proguard-rules.pro` keeps entire framework namespaces:

```
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep class androidx.wear.** { *; }
-keepclassmembers class androidx.wear.** { *; }
-keep class dagger.** { *; }
```

Compose, Wear Compose, Hilt, and OkHttp all ship consumer ProGuard rules in their
AARs; blanket-keeping them disables shrinking and obfuscation for the largest
libraries in the app. `isMinifyEnabled = true` + `isShrinkResources = true` in the
release build then accomplishes little. On Wear OS, where the historical APK
guidance is tight and cold-start matters, this is wasted size and dex.

The kotlinx.serialization block is also broader than the official template
(keeping all `<fields>` of every model), and the `StoptimeWrapper$$serializer`
`-dontwarn` suggests a past symptom-fix; worth rechecking once rules are cleaned.

## Fix

- Delete the Compose, Wear, Hilt, and OkHttp blanket keeps; rely on the libraries'
  consumer rules. Re-add only specific rules that a release-build test proves
  necessary.
- Replace the serialization block with the current official kotlinx.serialization
  ProGuard template.
- Compare release APK size before/after and smoke-test the release build
  (tile, plan, tracking) since serialization issues only appear minified.

## Acceptance criteria

- Release APK measurably smaller (record before/after sizes in the PR).
- Release build passes a manual smoke test: search, plan, track, tile render.
