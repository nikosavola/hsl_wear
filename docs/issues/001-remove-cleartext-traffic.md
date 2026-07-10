# Remove `usesCleartextTraffic` from the manifest

Labels: security

## Problem

`AndroidManifest.xml:31` sets `android:usesCleartextTraffic="true"`, allowing any
HTTP connection app-wide. Every endpoint the app actually talks to is HTTPS
(`NetworkConstants.kt`: `api.digitransit.fi` routing, geocoding, reverse geocoding),
so the flag only widens the attack surface. On a watch on public Wi-Fi this permits
downgrade/MITM for any future or transitive request.

## Fix

- Delete the `android:usesCleartextTraffic="true"` attribute.
- If a debug-only plaintext endpoint is ever needed (local emulator proxy), add a
  `network_security_config.xml` with a debug-overlay `<domain-config>` instead of
  the global flag.

## Acceptance criteria

- Attribute removed; app builds and all network features (search, plan, realtime
  refresh, tile refresh) still work against the live API.
- Optional: `networkSecurityConfig` present with cleartext blocked in release.
