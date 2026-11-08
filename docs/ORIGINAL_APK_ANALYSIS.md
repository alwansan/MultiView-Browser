# Original MultiView Browser 1.2.0 — static reference observations

Source: APK split package uploaded by the owner on 2026-11-08; observed locally, not redistributed.

| Fact | Evidence |
|---|---|
| outer container | 6 APK members: base.apk; split_config.ar.apk; split_config.arm64_v8a.apk; split_config.en.apk; split_config.es.apk; split_config.xxxhdpi.apk |
| base.apk hash | SHA-256 03183871f124d7fa1855a3f8bdd4f7c7b120ae88ac3401c3a2dbd8d90387f470 |
| package | datyaltech.android.multiviewbrowser |
| version | 1.2.0, versionCode=3 |
| min / target SDK | min 32 (0x20); target 37 (0x25) |
| DEX | 1 classes.dex, approximately 6 MB |
| primary activities | Splash, Setup, Main, Settings, Bookmarks, History, About, MoreApps |
| stack signals | Android WebView, AndroidX, Google Ads, Play licensing / protection components |
| base native libs | none observed in base APK |
| manifest permissions | INTERNET, ACCESS_NETWORK_STATE, WIFI, overlay, downloads, vibration and ads related; many are unnecessary for our clean implementation |

Google Play page for this exact package, version 1.2.0:
https://play.google.com/store/apps/details?id=datyaltech.android.multiviewbrowser

Public description: up to 20 screens; Grid, Vertical Stack, Horizontal Strip, Single Focus, desktop mode, saved history/bookmarks, screenshots, dark mode, same/multi URL options.

Important limits:
- APK inspection has NOT proven internal source architecture or each view behavior at runtime.
- Public marketing text may overstate multi-window performance or misdescribe per-view privacy.
- This reference APK is proprietary; do not commit its code, icon, screenshots or resources.
- Next: owner-side screen recordings and functional checklist for pixel/layout comparisons.
