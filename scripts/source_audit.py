#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=(root/'app/src/main/java/com/alwansan/multiview/MainActivity.java').read_text()
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
for token in ('WebView','removeView','addView','renderLayout','saveSession','onShowFileChooser','DownloadManager'):
    assert token in source, f'missing source feature marker: {token}'
for token in ('INTERNET','MainActivity','android:exported="true"'):
    assert token in manifest, f'missing manifest marker: {token}'
assert 'SYSTEM_ALERT_WINDOW' not in manifest, 'unnecessary overlay permission'
print('Static source guard passed. This is NOT a runtime verification.')

# Regression guard for the owner's launch crash (IllegalStateException: child has parent).
# The pane address + number controls have stable parents for the pane's lifetime.
pane_style=source.split('void styleCard() {',1)[1].split('void navigate(',1)[0]
assert 'if (!headerBuilt)' in pane_style, 'Header must be created once'
assert 'header.removeAllViews()' not in pane_style, 'Rebuilding header reparents child views and crashes on startup'
assert 'upper.addView(number' in pane_style and 'upper.addView(address' in pane_style
assert 'retintHeaderButtons(header)' in pane_style
print('Pane header lifecycle regression checks passed.')

# UX pass regression checks
assert 'GridSizing.columns' in source and 'GridSizing.shouldFit' in source
assert 'updateGridRowHeights' in source
assert 'if (!headerBuilt)' in source
assert 'header.removeAllViews()' not in source
assert 'onDestroy()' in source and 'p.web.destroy()' in source
assert 'onProgressChanged' in source
assert 'fitGrid' in source
print('Adaptive-layout and lifecycle source checks passed.')

assert 'onCreateWindow(WebView source' in source and 'WebView.WebViewTransport' in source
assert 'if(!userGesture||panes.size()>=MAX_PANES)' in source
assert 'desktopFlags' in source
assert 'private void resizePanes' in source
assert 'pane.card.removeView(pane.web)' in source
print('Popup/session/lifecycle guards passed.')

assert 'url.split' not in source # URLs should be distributed by lines, not word tokenization.
assert 'String[] raw=value.trim().split("\\\\r?\\\\n");' in source, 'expected line-oriented URL distribution'
assert 'R.string.duplicate_view' in source and 'R.string.external_browser' in source
assert 'Duplicate view' in (root/'app/src/main/res/values/strings.xml').read_text()
assert 'zoomPct=WebAppearance.clampZoom(zoomPct+10)' in source
assert 'zoomPct=WebAppearance.clampZoom(zoomPct-10)' in source
print('Multi-URL distribution and pane utility checks passed.')

# Localization and content-appearance regression gates.
assert 'showZoomDialog' in source and 'confirmReloadAll' in source
assert 'zoomPercents' in source and 'defaultZoom' in source
assert 'siteDarks' in source and 'websiteDark' in source
assert 'onPageFinished' in source and 'applyAppearance();' in source
assert 'setApplicationLocales' in source and 'attachBaseContext' in source
assert 'header.removeAllViews()' not in source
from xml.etree import ElementTree as ET
en=ET.parse(root/'app/src/main/res/values/strings.xml').getroot()
ar=ET.parse(root/'app/src/main/res/values-ar/strings.xml').getroot()
en_names={x.attrib.get('name') for x in en}
ar_names={x.attrib.get('name') for x in ar}
assert en_names==ar_names and len(en_names)>=60
assert (root/'app/src/main/res/xml/locale_config.xml').exists()
print('Localization, zoom and appearance checks passed.')

# Document search control must work without reloading WebView.
assert 'web.findAllAsync(findQuery)' in source
assert 'web.findNext(true)' in source and 'web.clearMatches()' in source
assert 'web.scrollTo(0,0)' in source
print('In-page find/navigation guards passed.')

# Session retention: do not finish Activity on Back; never pause all WebViews.
assert 'moveTaskToBack(true)' in source
assert 'WebView.RENDERER_PRIORITY_IMPORTANT,false' in source
assert 'onSaveInstanceState(Bundle out)' in source and 'p.dataSize()<=160*1024' in source
assert 'pendingWebHistory' in source and 'web.restoreState(pendingWebHistory)' in source
assert 'void showRecovery()' in source and 'new Pane(current,desktop,false)' in source
assert 'SessionKeepAliveService.class' in source
assert 'android:foregroundServiceType="specialUse"' in manifest
assert 'android.permission.FOREGROUND_SERVICE_SPECIAL_USE' in manifest
assert 'web.pauseTimers();' not in source and 'WebView.pauseTimers();' not in source
print('Background session retention guards passed.')

# Link chooser is a top-level action and is backed by local history / bookmarks.
assert 'navButton("⇄ "+t(R.string.links_short),this::showUrlDistribution' in source
assert 'drawLinkPicker(sections,input,dialog)' in source
assert 'linkOption(into,input,dialog' in source
assert 'savedBatches' in source and 'saveLinkSet(String text)' in source
assert 'openUrlBatch(String value)' in source
print('Link shortcut and history/bookmark picker guards passed.')
