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

## Crash RCA and v0.1.1 repair — 2026-11-08
- Owner's first launch v0.1.0 on Xiaomi Android 15 failed: MainActivity$Pane.styleCard reused number/address TextView/EditText while those controls remained attached to previous detached LinearLayout parent, causing IllegalStateException "specified child already has a parent"; report indicated styleCard line 562, renderLayout line 297 and addPane line 225.
- Do NOT reintroduce `header.removeAllViews()` inside `styleCard()`; never reparent the same number/address controls to newly allocated header rows.
- Replacement approach: create pane header once with `headerBuilt`, re-style without reparenting; pane card can move between layouts and WebView stays live. Regression script source_audit.py enforces.
- Version bump to 0.1.1-prototype / versionCode 2. New build/release and on-device retest pending.
- ACTION FOR OWNER: uninstall previous DEBUG APK before installing another Actions-built debug APK if signing certificates are incompatible; debug keystore is not stable across clean CI runners.

## v0.1.1 verified CI and user retest procedure — 2026-11-08
- Verified Actions SUCCESS: https://github.com/alwansan/MultiView-Browser/actions/runs/42499479881
- Source commit d9ae965775744a1502644331e7e0918ab40b2160; artifact multiview-debug-apk id 13625070608.
- Rolling release asset app-debug.apk id 724484870, size 29,633 bytes; direct URL https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk.
- SHA256 4e779bf623f566086a5c70b95cf38d1ed68c932ceec9631dcd9181478810e8a1.
- Passed Gradle assembleDebug, source_audit regression, artifact upload and release upload. **Runtime on owner device still pending**.
- Owner next: uninstall older debug build only if signature mismatch / installation fails, then install preview APK; test app starts, 2->5 panes, Grid/Vertical/Horizontal/Focus cycling and URL typing. Report screenshot/log if another crash.

## v0.2.0 UX pass (2026-11-08)
- Owner personally confirmed v0.1.1 no longer crashes at startup.
- Adaptive grid: GridSizing.java computes phone/tablet column count; at 393dp and 5 panes => 2 columns / 3 rows; fit mode sizes row based on viewport height (provided each >=148dp), else scroll fallback. 1–6 eligible for Fit Grid; 7–20 remain scrollable grid to avoid unusably small tiles.
- Pane header uses existing mounted controls; fit mode hides redundant toolbar row while keeping URL field, focus badge and overflow menu. Never reload pages on layout switch; use onDestroy explicit cleanup.
- Main chrome: clearer site/pane labels, access to layouts, pane chooser, link distributor, saved library and tools; quick ChatGPT action; confirmed source-of-truth website remains URL.
- Added progress bars, titles and layout planning test runnable via plain javac in Actions. Do not mark runtime behavior verified until phone testing.
- Research: Android docs on webview memory / renderer lifecycle and SDK 35 system bars:
  https://developer.android.com/develop/ui/views/layout/webapps/manage-webview-memory
  https://developer.android.com/develop/ui/views/layout/webapps/handle-termination
  https://developer.android.com/develop/ui/views/layout/edge-to-edge

## v0.2.1 follow-on: browser navigation and tab workflow (2026-11-08)
- User-gesture-only WebChromeClient.onCreateWindow opens the requested window in a new live pane through WebViewTransport. Non-gesture popups rejected, max 20.
- Batching 1–20 resize avoids re-rendering for each intermediate size, significantly reducing layout churn.
- Persist Desktop UA per pane in session JSON and re-apply before first WebView.loadUrl after restart.
- On explicit pane close / Activity.onDestroy / renderer-gone, detach WebView from card before destroy to avoid leaks.
- Android Back in single Focus mode returns to grid when no website history is left and multiple pages exist.
- Only mailto/tel/sms URI schemes dispatched to external apps; other non-http(s) external schemes blocked.
- Source remains original. New versionCode 4 and v0.2.1-preview. Actions and device validation pending.

## v0.2.2 workflow improvement — 2026-11-08
- Multi-URL distributor now splits by line (not whitespace) and loads URLs directly, retaining active selection, and performs one session persistence update. This avoids doing a full layout reflow per URL. Supports a single URL in all panes, or per-pane lines.
- Pane overflow now includes Duplicate view, open in external browser, zoom +/- alongside bookmark, Desktop mode, screenshot, copy, share, move, close.
- Version code 5, version 0.2.2-preview. Verify new Actions result + release SHA; phone screenshot and runtime checks still needed.

## Verified v0.2.2 preview delivery — 2026-11-08
- CI run 42501943269 SUCCESS including GridSizing JVM test, source guards, Gradle assembleDebug, artifact upload, rolling prerelease upload.
- Source commit: cb3a6997550aa600f15440c5bc83930246c38a47; artifact id 13625991380, release asset id 724567454, bytes 34341.
- Direct APK: https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk
- SHA256: 878d14ba0d101aadfab70e9949c248db95d0b2348865acc4d3134cc57cbafe87
- Owner already confirmed v0.1.1 fixed startup crash. New 0.2.2 layout, ChatGPT, popup, screenshot, long-chat performance have NOT been physically tested yet.
- Next iteration: owner-side screenshots on Android 15, 5-pane Fit Grid and Focus, orientation/keyboard, popup and sign-in tests, identify on-device defects; continue UI polish, memory measurements, stable signing via secure GitHub Secrets when feasible.
