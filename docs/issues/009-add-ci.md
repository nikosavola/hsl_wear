# Add GitHub Actions CI

Labels: devops

## Problem

There is no `.github/workflows/` directory. Nothing builds the app, runs the unit
tests, or runs lint on push/PR, so regressions land silently. The single existing
test (`GraphQLResponseMapperTest`) has likely never run outside a developer machine.

## Fix

Add `.github/workflows/ci.yml` roughly:

```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew assembleDebug testDebugUnitTest lint
      - uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: reports
          path: app/build/reports
```

Notes:

- Requires issue 010 first (`org.gradle.java.home` in `gradle.properties` points
  at a homebrew path and will fail on the runner).
- `HSL_API_KEY` is only needed at runtime, not for compilation; the build config
  falls back to an empty string, so no secret is required for CI.
- Follow-up candidates: dependabot/renovate for dependency PRs, a release job
  building a signed bundle from repository secrets.

## Acceptance criteria

- PRs show a green/red check from build + unit tests + lint.
- CI passes on a clean clone with no `local.properties`.
