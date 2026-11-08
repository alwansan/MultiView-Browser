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
- [PASS] v0.2.0 CI Actions run 42501281095 success (compile, GridSizing test, source audit, release). Device visual/UX still pending.
- [PENDING] Long ChatGPT thread/keyboard/landscape/renderer-restart performance measurements on phone.

## 2026-11-08 — v0.2.1 additional fixes
- [IMPLEMENTED, runtime unverified] Popup tab transport for user gestures, per-pane desktop UA restore, batched pane counts, explicit WebView removal, restricted external URI dispatch, Focus Back returns to Grid.
- [PENDING] Build + publication / APK SHA.
- [PENDING] User Android 15 five-pane, ChatGPT login, target-blank, desktop and restoration tests.

## 2026-11-08 — v0.2.2 URL/toolbar refinements
- [IMPLEMENTED, runtime unverified] Multi-link distributor splits on newlines; preserves pane selection and avoids per-pane layout reflow.
- [IMPLEMENTED, runtime unverified] Per-pane duplicate, external browser hand-off, zoom in/out.
- [PASS] v0.2.2 Actions https://github.com/alwansan/MultiView-Browser/actions/runs/42501943269 SUCCESS. Commit cb3a6997550aa600f15440c5bc83930246c38a47, Artifact 13625991380, APK 34341 bytes, SHA256 878d14ba0d101aadfab70e9949c248db95d0b2348865acc4d3134cc57cbafe87. Live on-device test pending.

## 2026-11-08 — v0.3.0 request / implementation
- [PASS owner report] v0.2.2 works on the Android phone; no crash/other issue observed by owner.
- [IMPLEMENTED, CI awaiting verification] Zoom 10%-200%, 10% choices, per pane, save default + apply to all, persistence after navigations.
- [IMPLEMENTED, CI awaiting verification] Dedicated Reload All toolbar control with unsent-data warning.
- [IMPLEMENTED, CI awaiting verification] Arabic/English resources + language selection; Android 13+ per-app language setting, older fallback restart.
- [IMPLEMENTED, CI awaiting verification] Experimental opt-in dark websites per pane + globally; saved/restore on next launch, can toggle off.
- [IMPLEMENTED, CI awaiting verification] Copy all page URLs to clipboard.
- [PENDING] Device UI, Chrome/ChatGPT CSS zoom behavior and dark-site accuracy, language-switch persistence and multi-pane reload safety.

## 2026-11-08 — v0.3.1 localization polish and page search
- [PASS] v0.3.0 Actions 42503641714 succeeded; CSS zoom/dark unit tests, string consistency, Android compile, APK + Release published. Physical Android UI not yet checked.
- [IMPLEMENTED] Additional Arabic localization: bookmarks, page history, downloads, screenshots and error notices.
- [IMPLEMENTED] Per-pane Find in page, Next match, Clear highlights, Scroll to top via WebView native APIs.
- [PENDING] New v0.3.1 build+Release verification and owner testing.

## 2026-11-08 — v0.3.1 CI certification
- [PASS] CI https://github.com/alwansan/MultiView-Browser/actions/runs/42503968802 status SUCCESS, source 8f3c95ff24bc83cd28a4d9792b0e363957043baa.
- [PASS] Adaptive grid JVM unit tests, WebAppearance zoom/dark unit tests, source checks, Gradle assembleDebug.
- [PASS] Artifact 13626239434 and public release APK 54,568 bytes (asset 724629194); SHA256 3f3d55ec7682b2074a6ed168e957e66bbad7d4c21df9fa734d0e3bdb982e496d.
- [PENDING] Android v0.3.1 on-device UI: Zoom 10/100/200 + re-navigation, apply all save, reload all, Arabic/English switching, experimental dark per-site, Find text in long ChatGPT thread.

## 2026-11-08 — v0.4.0 retention development
- [IMPLEMENTED / CI PENDING] Back minimizes instead of finishing Activity; explicit renderer priority; active WebView bounded back-stack saveState/restoreState + per-pane scroll metadata.
- [IMPLEMENTED / CI PENDING] Opt-in same-process foreground service with ongoing notification, stop action, service type specialUse, Android 14+ permissions; Android 15 still allowed to kill process.
- [IMPLEMENTED / CI PENDING] Renderer death cleanup with tap-to-restore instead of auto-reloading content in background.
- [PENDING] Verify GitHub Actions, Android 15 permission behavior, long chat after 10+ minutes background, battery impact and process death.

- [IMPLEMENTED, CI PENDING] Main-toolbar Links shortcut, multiline editor with bookmark/history rows below, tap-to-open/paste and '+' append, saved multi-URL batches.

- [IMPLEMENTED/CI PENDING] On cold process restart, eager-load visible/active panes only; defer other saved pages to explicit tap-to-restore to avoid mass reloading. Added SessionRestorePlan JVM tests.

- [IMPLEMENTED, CI PENDING] Handle locale/layout direction and screen configuration changes in same Activity and refresh URL hints without recreating WebViews. Needs Android 13+/15 actual locale-switch test.

- [IMPLEMENTED / CI PENDING] Battery settings shortcut opens native Android app details; notification permission request when user enables session guard.

- [IMPLEMENTED, CI PENDING] Optional owner-secret stable signing pathway; absent secrets should preserve normal successful debug-build behavior. Actual stable signer is NOT configured yet.

## 2026-11-08 — v0.4.0 final CI verification
- [PASS] Workflow https://github.com/alwansan/MultiView-Browser/actions/runs/42506003292, commit 28d0d322c1b173b951e0864c59641a221e110ab8 — conclusion SUCCESS.
- [PASS] Gradle Java compile and Android resource link, grid/appearance/session restore JVM unit checks, source audit, APK artifact upload, release publishing.
- [PASS] Artifact 13627228232; GitHub Release asset 724693964; APK size 68,436 bytes; SHA256 6788d8aec8e91a23ceee72a44fbd0d541f2cdb3e48150ff252170b74f011ef24.
- [PASS] No signing secrets supplied: fallback debug signing step successfully skipped stable signer.
- [PENDING] Real POCO Android 15: FGS specialUse start/stop notification; Home then 10/30-minute absence; WebView renderer process recovery; 20-tab cold start; Links picker, saved sets, Arabic/English relayout.
- [LIMIT] No process or foreground service can guarantee indefinite continuous WebView execution, and WebView.saveState doesn't save DOM/unsent messages.
