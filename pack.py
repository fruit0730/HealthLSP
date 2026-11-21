import os
import sys
import zipfile

src, dex, dst = sys.argv[1], sys.argv[2], sys.argv[3]
tmp = dst + ".tmp"

with zipfile.ZipFile(src, "r") as zin:
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            zout.writestr(item, zin.read(item.filename))
        zout.write(dex, "classes.dex")

os.replace(tmp, dst)

with zipfile.ZipFile(dst, "r") as z:
    names = set(z.namelist())
    for required in ("AndroidManifest.xml", "classes.dex", "assets/xposed_init", "assets/xposed_scope"):
        if required not in names:
            raise SystemExit("missing in apk: " + required)

print("packed", dst, os.path.getsize(dst), "bytes")
