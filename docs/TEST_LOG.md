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

## 2026-11-08 — v0.2.0 UX and responsive grid
- [PASS owner report] v0.1.1 opens and previous startup crash is no longer observed on test Android phone. This is owner-confirmed runtime feedback.
- Proposed upgrades implemented: Fit grid for <=6 panes with viewport-driven row heights (responsive keyboard/orientation), Scroll grid alternative, refined global top/bottom toolbar, richer pane chooser, one-click focus from tile badge, compact headers, page loading progress, safe manual reload confirmation, live 1–20 pane retention and cleanup on Activity destroy.
- GridSizing pure-Java test added to CI with 393dp/5-pane and various tablet/phone/memory/layout cases.
- [PENDING] v0.2.0 Actions compile and release verification; then owner visual/UX test on Android 15.
- [PENDING] Long ChatGPT thread/keyboard/landscape/renderer-restart performance measurements on phone.

## 2026-11-08 — v0.2.1 additional fixes
- [IMPLEMENTED, runtime unverified] Popup tab transport for user gestures, per-pane desktop UA restore, batched pane counts, explicit WebView removal, restricted external URI dispatch, Focus Back returns to Grid.
- [PENDING] Build + publication / APK SHA.
- [PENDING] User Android 15 five-pane, ChatGPT login, target-blank, desktop and restoration tests.

## 2026-11-08 — v0.2.2 URL/toolbar refinements
- [IMPLEMENTED, runtime unverified] Multi-link distributor splits on newlines; preserves pane selection and avoids per-pane layout reflow.
- [IMPLEMENTED, runtime unverified] Per-pane duplicate, external browser hand-off, zoom in/out.
- [PENDING] New build and updated release.
