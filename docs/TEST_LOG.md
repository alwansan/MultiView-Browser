# Test log

## 2026-11-08 — first construction
- [PASS] Original .apks was accessible and statically examined.
- [PASS] GitHub repo alwansan/MultiView-Browser has write permission and initially held only README.md.
- [PASS] Android Gradle build: run 42497820917, commit 8113cf5, artifact multiview-debug-apk (ID 13624141929), SHA256 209e33bcbf89aae25c2e38dbdbd7dfd5048bcb6e2caaf7cc29cb8b353c57e2ef.
- [PASS] Rolling prerelease upload on run 42498105704, source commit 3c8744b, public release https://github.com/alwansan/MultiView-Browser/releases/tag/preview-latest, raw APK 29,385 bytes.
- [PASS] Current build SHA256 fa82ab0fd1c111f79ee0b75bc6362a8de55674925bc92690bd537ef7ab156251.
- [PENDING] Device installation, login, 5-window view, file input, downloads, screenshot and long-chat retention.
- [PENDING] APK install / native Android runtime / WebView behavior / input / downloads.
- [PENDING] 2 / 5 / 10 / 20 pane memory and ChatGPT long-thread tests.

Do not convert PENDING into PASS without run/device evidence.

## 2026-11-08 — on-device startup crash report (owner)
- [FAIL CONFIRMED] v0.1.0 installation launches into system crash report. Screenshot shows:
  `RuntimeException: Unable to start activity ... MainActivity` caused by
  `IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.`
  Call chain: `MainActivity$Pane.styleCard` → `renderLayout` → `addPane` → `onCreate`. Screenshot reported by owner.
- Root cause: `styleCard()` called `header.removeAllViews()` but reused its nested `number` and `address` widgets, still attached to the previous detached `upper` group. This occurs on first pane add/reflow.
- Fix: build header once via `headerBuilt`, keep original edit field and WebView; update colors/index without replacing header children. This additionally avoids discarding in-progress URL input during focus/layout change.
- [PASS] v0.1.1-prototype CI compile / source guards / publish: run 42499479881 (success), commit d9ae965775744a1502644331e7e0918ab40b2160. Artifact 13625070608. Release asset 724484870 (29,633 bytes) at https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk. SHA256 4e779bf623f566086a5c70b95cf38d1ed68c932ceec9631dcd9181478810e8a1.
- [PENDING] Confirm launch + layout reflow on owner phone. Build success alone cannot prove runtime fix.
