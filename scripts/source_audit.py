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
