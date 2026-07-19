#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# Prefer JDK 21 for jar packaging (JDK 26 rejects some shaded module-infos)
if [[ -d /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ]]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
else
  export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home}"
fi
export PATH="$JAVA_HOME/bin:${PATH:-}"
echo "Using JAVA_HOME=$JAVA_HOME ($(java -version 2>&1 | head -1))"

ASSIST_SRC="$ROOT/lib/assistant-original.jar"
ONESHOT_SRC="$ROOT/lib/OneShoot-original.jar"
PAPER="$ROOT/lib/paper-api.jar"

for f in "$ASSIST_SRC" "$ONESHOT_SRC" "$PAPER"; do
  [[ -f "$f" ]] || { echo "Missing $f"; exit 1; }
done

CP="$PAPER:$ASSIST_SRC:$ROOT/lib/guava.jar:$ROOT/lib/annotations.jar:$ROOT/lib/adventure-api.jar:$ROOT/lib/adventure-key.jar:$ROOT/lib/examination.jar:$ROOT/lib/commons-lang.jar:$ROOT/lib/bungeecord-chat.jar"

# JOML is needed for Transformation / ItemDisplay
JOML_DIR="$ROOT/lib"
if [[ ! -f "$JOML_DIR/joml.jar" ]]; then
  echo "Fetching JOML..."
  curl -fsSL -o "$JOML_DIR/joml.jar" \
    "https://repo1.maven.org/maven2/org/joml/joml/1.10.8/joml-1.10.8.jar"
fi
CP="$CP:$JOML_DIR/joml.jar"

rm -rf "$ROOT/build/classes" "$ROOT/build/assistant-work" "$ROOT/build/oneshot-work"
mkdir -p "$ROOT/build/classes" "$ROOT/dist"

echo "Compiling display-entity patches..."
javac --release 21 -cp "$CP" -d "$ROOT/build/classes" \
  "$ROOT/patches/com/qualityplus/assistant/hologram/TheHologram.java" \
  "$ROOT/patches/com/qualityplus/assistant/util/armorstand/ArmorStandUtil.java"

echo "Compiling OneShot patches..."
ONESHOT_CP="$CP:$ONESHOT_SRC"
javac --release 21 -cp "$ONESHOT_CP" -d "$ROOT/build/classes" \
  "$ROOT/patches/com/qualityplus/oneshoot/base/config/Config.java" \
  "$ROOT/patches/com/qualityplus/oneshoot/base/config/Commands.java" \
  "$ROOT/patches/com/qualityplus/oneshoot/base/service/OneShootServiceImpl.java" \
  "$ROOT/patches/com/qualityplus/oneshoot/base/commands/provider/OneShootCommandProvider.java" \
  "$ROOT/patches/com/qualityplus/oneshoot/OneShoot.java"

# ---- Patch TheAssistant jar ----
WORKDIR="$ROOT/build/assistant-work"
mkdir -p "$WORKDIR"
( cd "$WORKDIR" && jar xf "$ASSIST_SRC" )
cp -f "$ROOT/build/classes/com/qualityplus/assistant/hologram/TheHologram.class" \
  "$WORKDIR/com/qualityplus/assistant/hologram/"
cp -f "$ROOT/build/classes"/com/qualityplus/assistant/util/armorstand/ArmorStandUtil*.class \
  "$WORKDIR/com/qualityplus/assistant/util/armorstand/"

# Bump api-version for Paper 26.x
cat > "$WORKDIR/plugin.yml" <<'EOF'
name: TheAssistant
description: The Assistant Plugin (display-entity holograms)
main: com.qualityplus.assistant.TheAssistantPlugin
version: 4.0.2-oneshot
api-version: '1.21'
authors: [QualityPlus, Genesi]
load: POSTWORLD
loadbefore: [SkinsRestorer, ajLeaderboards]
softdepend: [MythicMobs, Vault, TokenManager, PlayerPoints, WorldEdit, WorldGuard, UltraRegions, Residence, ProtocolLib, PlaceholderAPI, MVdWPlaceholderAPI, SlimeWorldManager]
EOF

# Use zip to avoid JDK module-info validation on shaded deps
rm -f "$ROOT/dist/the-assistant-4.0.2.jar"
( cd "$WORKDIR" && zip -qr "$ROOT/dist/the-assistant-4.0.2.jar" . )
echo "Built $ROOT/dist/the-assistant-4.0.2.jar ($(wc -c < "$ROOT/dist/the-assistant-4.0.2.jar") bytes)"

# ---- Patch + rename OneShoot -> OneShot jar ----
WORKDIR="$ROOT/build/oneshot-work"
mkdir -p "$WORKDIR"
( cd "$WORKDIR" && jar xf "$ONESHOT_SRC" )

# Overlay shaded assistant hologram classes if present in the fat jar
if [[ -d "$WORKDIR/com/qualityplus/assistant/hologram" ]]; then
  cp -f "$ROOT/build/classes/com/qualityplus/assistant/hologram/TheHologram.class" \
    "$WORKDIR/com/qualityplus/assistant/hologram/"
fi
if [[ -d "$WORKDIR/com/qualityplus/assistant/util/armorstand" ]]; then
  cp -f "$ROOT/build/classes"/com/qualityplus/assistant/util/armorstand/ArmorStandUtil*.class \
    "$WORKDIR/com/qualityplus/assistant/util/armorstand/"
fi

# Overlay OneShot service/config patches
cp -f "$ROOT/build/classes"/com/qualityplus/oneshoot/base/config/Config*.class \
  "$WORKDIR/com/qualityplus/oneshoot/base/config/"
cp -f "$ROOT/build/classes"/com/qualityplus/oneshoot/base/config/Commands*.class \
  "$WORKDIR/com/qualityplus/oneshoot/base/config/"
cp -f "$ROOT/build/classes"/com/qualityplus/oneshoot/base/service/OneShootServiceImpl*.class \
  "$WORKDIR/com/qualityplus/oneshoot/base/service/"
cp -f "$ROOT/build/classes/com/qualityplus/oneshoot/OneShoot.class" \
  "$WORKDIR/com/qualityplus/oneshoot/"
mkdir -p "$WORKDIR/com/qualityplus/oneshoot/base/commands/provider"
cp -f "$ROOT/build/classes"/com/qualityplus/oneshoot/base/commands/provider/OneShootCommandProvider*.class \
  "$WORKDIR/com/qualityplus/oneshoot/base/commands/provider/"

cat > "$WORKDIR/plugin.yml" <<'EOF'
name: OneShot
description: OneShot — one-arrow duel minigame (Genesiverse)
main: com.qualityplus.oneshoot.OneShoot
version: 1.0.3
api-version: '1.21'
authors: [QualityPlus, Genesi]
load: POSTWORLD
depend: [TheAssistant, GenesiGamesApi]
softdepend: [GenesiCore]
commands:
  oneshot:
    aliases: [os]
    description: OneShot's main command
    usage: /oneshot
EOF

rm -f "$ROOT/dist/OneShot.jar"
( cd "$WORKDIR" && zip -qr "$ROOT/dist/OneShot.jar" . )
echo "Built $ROOT/dist/OneShot.jar ($(wc -c < "$ROOT/dist/OneShot.jar") bytes)"

# Verify bytecode markers
javap -c -classpath "$ROOT/dist/the-assistant-4.0.2.jar" com.qualityplus.assistant.hologram.TheHologram \
  | grep -q TextDisplay && echo "OK: TheHologram uses TextDisplay"
javap -classpath "$ROOT/dist/OneShot.jar" -verbose 2>/dev/null | head -1
unzip -p "$ROOT/dist/OneShot.jar" plugin.yml | head -8
javap -c -classpath "$ROOT/dist/OneShot.jar" \
  com.qualityplus.oneshoot.base.commands.provider.OneShootCommandProvider \
  | grep -q 'oneshot' && echo "OK: CommandProvider label is oneshot"
echo "Done."
