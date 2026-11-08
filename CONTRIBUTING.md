# Contributing / المساهمة

This is an independently written Android WebView browser. Contributions are welcome through GitHub issues and pull requests, subject to the owner's repository permissions.

## Build prerequisites
- JDK 17; Android SDK Platform 35; Gradle 8.10.2.
- Build: `gradle --no-daemon :app:assembleDebug`
- Java source/manifest: `app/src/main/`
- Source and brand checks: `python3 scripts/source_audit.py && python3 scripts/brand_audit.py`

## Before a PR
1. Read [AGENTS.md](AGENTS.md) and [PROJECT_MEMORY.md](PROJECT_MEMORY.md).
2. Preserve existing live WebViews when switching layouts. Never claim indefinite background liveness.
3. Keep English/Arabic strings synchronized; prefer Android string resources over hard-coded UI copy.
4. Update [CHANGELOG.md](CHANGELOG.md) and [docs/TEST_LOG.md](docs/TEST_LOG.md) with code and actual verification.
5. Do not include proprietary reference APK assets, credentials, cookies, unsent form data or signing keys.

## Bug reports
Supply Android/WebView version, exact steps, page count and layout mode, reproducible error text and a redacted screenshot if useful. Avoid screenshots of private conversations or session cookies.

## Artwork
All repository illustrations under `assets/brand` are original conceptual illustrations, **not** tested-app screenshots. Source: [brand asset guide](assets/brand/README.md).
