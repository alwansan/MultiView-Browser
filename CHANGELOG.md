# Changelog / سجل التغييرات

The [rolling preview APK](https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk) is updated when a new `main` source build succeeds. Physical-device results are distinct from CI build results.

## 0.4.2-preview — Visual identity + cleanup (2026-11-08)
- Completely refreshed GitHub landing page in English and Arabic, linked language navigation, original SVG concept art.
- Native Android adaptive app icon and monochrome themed icon, branded foreground notification icon.
- Branded app header, cyan active-pane outline, ripple feedback and consistent color/theme resources.
- New selective **Privacy & cleanup** controls with confirmation before deleting cache, history, saved link lists, cookies or closing only blank pages. No automatic deletion of open session content.
- Gradle build + JVM tests + SVG/localization/source audits all passed in [Actions 42507742254](https://github.com/alwansan/MultiView-Browser/actions/runs/42507742254). Source commit `6d75c27f8068b019e756ef6c925a3d59126073de`.
- Preview APK SHA-256: `f7d9485eb99b8d9985e28decd6f20ffd9d658b03a0c6438219a3cf4322b114b0`; 78,469 bytes. Physical device validation of this build pending.

## 0.4.0-preview — Background resilience (2026-11-08)
- Opt-in foreground session protection with visible notification, Back minimizes instead of finishing.
- Bounded active navigation-state saving, scroll metadata, manual WebView recovery, and selective cold-start loading.
- Direct Links shortcut, saved URL sets, bookmarks and visit history in picker.

## 0.3.x-preview — Navigation and localization
- Arabic / English strings and per-app language controls.
- Per-pane and apply-all website zoom 10–200%, experimental site darkening and dedicated reload-all.
- In-page find, bookmark, history, file picker, downloads and screenshots.

## 0.2.x-preview — Layout and browser usability
- Fit Grid / Scroll Grid / Vertical / Horizontal / Focus; visible progress, popup panes and per-pane desktop flags.
- Fixed original launch crash in v0.1.1; owner confirmed later builds ran without reported startup crashes.

## 0.1.x-preview — Initial foundation
- Java Android WebView multi-pane foundation and GitHub Actions debug APK pipeline.

**Limitations:** Android can terminate background processes and WebView renderers. A successful build never proves that pages remain alive indefinitely. The dark-websites CSS mode is experimental and normal panes share the default cookie profile.
