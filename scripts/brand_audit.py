#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
svgs=[
  "assets/brand/icon.svg", "assets/brand/hero-en.svg",
  "assets/brand/hero-ar.svg", "assets/brand/features.svg",
  "assets/brand/layouts.svg"
]
for path in svgs:
    target=root/path
    assert target.is_file(), f"Missing image: {path}"
    r=ET.parse(target).getroot()
    assert r.tag.endswith("svg") and r.get("viewBox"), f"Invalid SVG: {path}"
    assert not any(x in target.read_text().lower() for x in ('<script','javascript:')), path
for readme in ("README.md","README.ar.md"):
    content=(root/readme).read_text(encoding="utf-8")
    assert "(README.md)" in content and "(README.ar.md)" in content, readme
    for path in svgs:
        name=path.rsplit('/',1)[-1]
        if name.startswith("icon") or name.startswith("features") or name.startswith("layouts"):
            assert path in content, f"Asset {path} missing from {readme}"
for path in (
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
    "app/src/main/res/drawable/ic_launcher_background.xml",
    "app/src/main/res/drawable/ic_launcher_foreground.xml",
    "app/src/main/res/drawable/ic_launcher_monochrome.xml",
    "app/src/main/res/drawable/ic_notification.xml",
    "app/src/main/res/values/styles.xml",
):
    ET.parse(root/path)
print("Bilingual README, all illustrated assets and Android launcher icons passed.")
