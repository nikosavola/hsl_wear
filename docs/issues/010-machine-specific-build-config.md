# Machine-specific paths are checked into the build config

Labels: devops, bug

## Problem

The build only works on one particular macOS machine:

- `gradle.properties:13` sets `org.gradle.java.home=/opt/homebrew/opt/openjdk@17`.
  This is a homebrew path; the build fails immediately on Linux, Windows, CI, or
  any Mac without that formula. Checked-in `gradle.properties` is shared config
  and must not contain machine paths.
- `build.sh` exports `JAVA_HOME="/Applications/Android Studio.app/..."`, same
  problem in script form.
- `app/build.gradle.kts:53`: the release keystore falls back to
  `file(properties.getProperty("KEYSTORE_FILE") ?: "123")`, a nonsense path that
  produces a confusing error at signing time instead of a clear "signing not
  configured" message.

The project already declares `jvmToolchain(17)` (`app/build.gradle.kts:81`), which
is the portable mechanism and makes both overrides unnecessary.

## Fix

- Remove `org.gradle.java.home` from the checked-in `gradle.properties`
  (developers who need it can set it in `~/.gradle/gradle.properties`).
- Delete `build.sh`, or reduce it to a plain `./gradlew "$@"` passthrough.
- Enable `org.gradle.java.installations.auto-download=true` (foojay resolver
  plugin in `settings.gradle.kts`) so the toolchain provisions itself.
- Guard the release signing config: only create it when `KEYSTORE_FILE` is set,
  and fail with a clear message when `assembleRelease` runs without it.
- While in `gradle.properties`, add `org.gradle.caching=true` and
  `org.gradle.configuration-cache=true` (verify plugins are compatible) and raise
  `-Xmx` to 4g; Kotlin+KSP+Hilt on 2 GB causes slow GC-bound builds.

## Acceptance criteria

- `./gradlew assembleDebug` succeeds on a clean Linux checkout with only a JDK
  or auto-provisioned toolchain.
- No absolute paths in any checked-in file.
