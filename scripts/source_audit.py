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
