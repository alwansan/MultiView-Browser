# AGENTS — MultiView Browser

Scope: this repository only. Follow `PROJECT_MEMORY.md` and `docs/PLAN_AR.md` before changing code.

1. Keep source original. Treat externally supplied APKs as observation/analysis references, not source to copy, redistribute, re-sign or bypass licensing.
2. Primary product requirement: 1–20 simultaneous live WebViews inside a single Android Activity, with Grid, Vertical Stack, Horizontal Strip, Single Focus. Reparent existing View objects during layout changes; NEVER rebuild WebView instances just to rearrange panes.
3. Do not claim an OS-level guarantee that 10–20 ChatGPT pages will never reload: Android/WebView may kill renderers under memory pressure.
4. Never store sign-in cookies, accounts, original APK files or authentication tokens in Git or Actions artifacts.
5. Keep permissions minimal. File picking via system picker; downloaded files via DownloadManager; captures via MediaStore.
6. Every meaningful change: run Gradle compile, inspect Actions outcome, record commit SHA, actual APK artifact link and device-test results or explicitly mark unverified.
7. Use one initial branch/main for this standalone owner repository; do not modify any other user repositories. No force pushes. Do not cancel another workflow run.
8. Prioritize compatibility with Android 15, Snapdragon 8+ Gen 1, 120 Hz and no root. Include portrait/landscape/tablet tests.
9. On every new session, read `RESTORE_AR.md`, `PROJECT_MEMORY.md`, `docs/PLAN_AR.md`, workflows and last run before modifying code. Update memory with evidence, not guesses.
10. Security: allow only http(s) downloads; do not silently grant camera/microphone/geolocation; limit file chooser to user selection.

## Gate

A commit passing source checks is not a verified Android binary. An Actions green build is not a verified login, page retention, input, scroll, or multitasking result. Those need on-device checks.

11. When backgrounding, never call Activity.finish() or WebView.pauseTimers() merely to leave the app; Back minimizes. Retain the original WebViews while process exists. Renderer deaths require destroying the dead view, never automatic 20-tab reloads.
12. Opt-in foreground service specialUse with visible notification must remain user controlled, not forced on startup for unconsenting users. No "never reload" guarantees. Android and OEM memory managers remain authoritative.
13. Keep browser signing private; secrets and private keystore must not be committed. CI may use optional signing secrets. Do not claim stable signing is configured until verified.

14. Keep README.md and README.ar.md both navigable using relative language links; preserve clearly labeled conceptual artwork and never substitute mockups for real screenshots.
15. Keep Android adaptive icon/background/monochrome resources and app theme consistent with assets/brand. Run scripts/brand_audit.py and Actions assemble after visual asset changes.
16. Privacy cleanup must remain user-initiated with confirmation; do not erase saved session data, web cookies or app storage implicitly.
