# MultiView Browser

A native Android multi-pane browser rebuilt from independently written source for the MultiView-Browser project.

## Goals

- Display 1–20 independently navigable websites inside one Activity.
- Switch between Grid, Vertical Stack, Horizontal Strip, and Single Focus without destroying or recreating WebViews.
- Save page URLs, layouts and favorites; support per-pane back, forward, reload, desktop mode and screenshots.
- Keep browser chrome small and usable on phones and tablets, including Android 15 system bars.
- Ship a GitHub Actions-built debug APK.

**Experimental APK (direct download):** https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk

**GitHub Release:** https://github.com/alwansan/MultiView-Browser/releases/tag/preview-latest

**Status:** v0.2.2 experimental (CI build tested; physical Android runtime still requires confirmation): Fit Grid, Scroll Grid, Vertical, Horizontal and Focus; loading indicators, browser popups, per-pane Desktop flags, quick ChatGPT link, saved library, pane duplication and zoom controls. Compilation, device compatibility and behavioral parity are tested in stages; do not confuse repository commit with a proven working APK. Android can still reclaim WebView renderer memory.

### Building

Requires JDK 17, Android SDK Platform 35 and Gradle 8.10.2:

```bash
gradle :app:assembleDebug
```

The push-triggered workflow uploads `app-debug.apk` as a build artifact.

### Documents

- [Master plan (Arabic)](docs/PLAN_AR.md)
- [Original APK static inspection](docs/ORIGINAL_APK_ANALYSIS.md)
- [Working memory](PROJECT_MEMORY.md)
- [Agent instructions](AGENTS.md)
- [Copyable restoration prompt (Arabic)](RESTORE_AR.md)

The original commercial APK and licensed assets are intentionally **not** stored in this repository. This app does not affiliate with third-party websites.

## On-device testing

See [Arabic device checklist](docs/DEVICE_TEST_AR.md). Start with five pages in Fit Grid and verify focus/navigation, real ChatGPT typing, file chooser and rotation. **No account credentials, auth cookies or personal chat text should be uploaded to GitHub.**

Current verified preview: https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk — SHA256 `878d14ba0d101aadfab70e9949c248db95d0b2348865acc4d3134cc57cbafe87`.
