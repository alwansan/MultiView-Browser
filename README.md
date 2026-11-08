# MultiView Browser

A native Android multi-pane browser rebuilt from independently written source for the MultiView-Browser project.

## Goals

- Display 1–20 independently navigable websites inside one Activity.
- Switch between Grid, Vertical Stack, Horizontal Strip, and Single Focus without destroying or recreating WebViews.
- Save page URLs, layouts and favorites; support per-pane back, forward, reload, desktop mode and screenshots.
- Keep browser chrome small and usable on phones and tablets, including Android 15 system bars.
- Ship a GitHub Actions-built debug APK.

**Status:** Prototype v0.1.0. Compilation, device compatibility and behavioral parity are tested in stages; do not confuse repository commit with a proven working APK. Android can still reclaim WebView renderer memory.

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
