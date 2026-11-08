# Persistent engineering memory — MultiView Browser
Updated: 2026-11-08 (initial Chat session)

## Owner objective
Build owner-controlled Android app in https://github.com/alwansan/MultiView-Browser matching the functionality of the uploaded MultiView Browser v1.2.0: 1–20 simultaneous pages, layouts and navigation; improve UX. Special workload: 10–20 long ChatGPT threads, five visible panes, minimize unwanted page reloads. User does not rely on a PC; GitHub Actions builds APKs. Owner delegates engineering choices and uses "continue" to resume.

## Original APK verified (STATIC only)
- Upload: MultiView Browser_1.2.0.apks (~3.7 MiB), outer ZIP with base.apk + ar, en, es, arm64_v8a, xxxhdpi splits.
- Base APK SHA256: 03183871f124d7fa1855a3f8bdd4f7c7b120ae88ac3401c3a2dbd8d90387f470.
- Manifest: package datyaltech.android.multiviewbrowser; versionName 1.2.0; versionCode 3; minSdk 32; targetSdk 37 (note: raw values parsed are hexadecimal 0x20 and 0x25; validate decode/reality before relying on them).
- MainActivity, SetupActivity, SplashActivity, SettingsActivity, BookmarksActivity, HistoryActivity, AboutActivity; 1 classes.dex; Android WebView references; ads, billing/license related components observed; no native libraries in base.apk.
- Google Play listing corresponding to package describes layouts: Grid, Vertical Stack, Horizontal Strip, Single Focus; modern slate-blue UI, up to 20 views, desktop toggle, history/bookmarks, screenshots, same URL and multi URL modes. Some features may vary by edition/purchase status. Static inspection does NOT prove exact runtime behaviors.
- Never commit or redistribute the original APK or its proprietary resource files.

## Tools
- REA: https://github.com/morluto/rea (analysis bridge, CLI; dependencies can be added to CI if useful)
- Craft Anything: https://github.com/storytold/craft-anything (early alpha, Rust; not required to ship the app)
- Local static APK parsing worked without Android SDK; live Android UI tests pending.
- Both tools are optional; prefer deterministic native build and targeted JADX/apktool analysis if deeper feature verification is necessary.

## Current project
- Native Java/Android WebView, no Compose/Flutter dependency; Gradle 8.10.2, AGP 8.7.3, SDK 35, Java 17.
- Prototype: Activity holds one WebView per pane; retain instances on reflow; four modes; back/forward/reload; web/file chooser/download; simple session persistence; bookmarks/history; desktop per-pane; screenshot; dark theme; keep-awake toggle.
- GitHub Actions builds debug APK and uploads artifact; actual CI result MUST be checked.
- Cookies are shared across normal WebViews; no false claim of per-pane account isolation, no full incognito until separate-process profile and tests exist.

## Next session checklist
1. Read RESTORE_AR.md, AGENTS.md, docs/PLAN_AR.md and current git tree.
2. Inspect latest Actions run and failed steps. Fix compilation without breaking WebView retention.
3. Get first artifact; install on user phone; compare reference app visually (screenshots/video) and test 2, 5, 10, 20 pages.
4. Record actual pass/fail in docs/TEST_LOG.md and refresh this memory.

## CI verification — 2026-11-08
- Successful build: https://github.com/alwansan/MultiView-Browser/actions/runs/42497820917
- Commit 8113cf5ab54bc9282542f5a748406f12c97fc229, artifact ID 13624141929, name multiview-debug-apk.
- app-debug.apk SHA256: 209e33bcbf89aae25c2e38dbdbd7dfd5048bcb6e2caaf7cc29cb8b353c57e2ef.
- Prior failures: setup-android@v3 invoked removed 'tools' SDK package; then sdkmanager was not on PATH. Fixed by using existing Ubuntu 24.04 Android SDK platform 35 and build tools.
- Need actual Android 15 device test. Follow-up commit adds preview release upload and browser security/rotation fixes: verify its Actions outcome separately.

## Verified preview release — 2026-11-08
- Latest BUILD + UPLOAD + RELEASE: https://github.com/alwansan/MultiView-Browser/actions/runs/42498105704 — conclusion SUCCESS.
- Source commit built: 3c8744b7941a08ffae2bce16f4eb8898b51db6ce.
- Artifact ID 13624525836, artifact name multiview-debug-apk.
- Raw APK direct URL: https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk
- GitHub prerelease: https://github.com/alwansan/MultiView-Browser/releases/tag/preview-latest
- APK size 29,385 bytes; SHA256 fa82ab0fd1c111f79ee0b75bc6362a8de55674925bc92690bd537ef7ab156251.
- All CI steps passed, including publishing the rolling prerelease. Runtime on physical Android remains unverified.
- **Next task:** device install/screenshots, layout 5-pane density, genuine ChatGPT login/long chats, performance and memory checks, then feature parity (incognito isolation, auto-refresh opt-in, preview history, adaptive UX).
