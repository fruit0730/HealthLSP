#!/bin/bash
set -e
set -o pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-E:/adnroid}}"
BT="${BUILD_TOOLS_DIR:-$SDK/build-tools/36.0.0}"
AJ="${ANDROID_JAR:-}"
if [ -z "$AJ" ]; then
    AJ="$(ls "$SDK"/platforms/android-*/android.jar 2>/dev/null | sort -V | tail -1)"
fi
if [ -z "$AJ" ] || [ ! -f "$AJ" ]; then
    echo "android.jar not found under $SDK/platforms" >&2
    exit 1
fi

JAVAC="${JAVAC:-javac}"
KEYTOOL="${KEYTOOL:-keytool}"
if ! command -v "$KEYTOOL" >/dev/null 2>&1; then
    KEYTOOL="D:/Program Files/Android/Android Studio/jbr/bin/keytool.exe"
fi

AAPT2="${AAPT2:-$BT/aapt2}"
[ -x "$AAPT2" ] || AAPT2="$BT/aapt2.exe"
D8="${D8:-$BT/d8}"
[ -x "$D8" ] || D8="$BT/d8.bat"
ZIPALIGN="${ZIPALIGN:-$BT/zipalign}"
[ -x "$ZIPALIGN" ] || ZIPALIGN="$BT/zipalign.exe"
APKSIGNER="${APKSIGNER:-$BT/apksigner}"
[ -x "$APKSIGNER" ] || APKSIGNER="$BT/apksigner.bat"

OUT="build"
rm -rf "$OUT"
mkdir -p "$OUT/stubclasses" "$OUT/classes" "$OUT/dex"

"$JAVAC" -source 1.8 -target 1.8 -bootclasspath "$AJ" -nowarn \
    -d "$OUT/stubclasses" $(find stubs -name '*.java')

"$JAVAC" -source 1.8 -target 1.8 -bootclasspath "$AJ" -classpath "$OUT/stubclasses" -nowarn \
    -d "$OUT/classes" $(find src -name '*.java')

"$D8" --min-api 29 --lib "$AJ" --output "$OUT/dex" \
    $(find "$OUT/classes" -name '*.class')

"$AAPT2" link -o "$OUT/unsigned.apk" -I "$AJ" \
    --manifest AndroidManifest.xml -A assets

python pack.py "$OUT/unsigned.apk" "$OUT/dex/classes.dex" "$OUT/withdex.apk"

"$ZIPALIGN" -f -p 4 "$OUT/withdex.apk" "$OUT/aligned.apk"

if [ ! -f keystore.jks ]; then
    "$KEYTOOL" -genkeypair -keystore keystore.jks -storetype JKS \
        -storepass android -keypass android -alias healthlsp \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=HealthLSP, OU=classfruit, O=classfruit, L=CN, ST=CN, C=CN"
fi

"$APKSIGNER" sign --ks keystore.jks --ks-key-alias healthlsp \
    --ks-pass pass:android --key-pass pass:android \
    --out HealthLSP.apk "$OUT/aligned.apk"

"$APKSIGNER" verify --verbose HealthLSP.apk | head -6
echo "built: $(pwd)/HealthLSP.apk"
