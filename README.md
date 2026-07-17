# OneShot (Genesiverse)

Patched [QualityPlus OneShoot](https://qualityplus.gitbook.io/) plugin for the Genesiverse Club server, renamed to **OneShot** and updated for **Paper 26.1.2+**.

## Changes

1. **Rename** – Plugin id `OneShot`, command `/oneshot` (aliases: `oneshoot`, `os`). Data folder `plugins/OneShot/`.
2. **Paper 26.1.2+** – `api-version: 1.21`; works with Club’s Paper `26.1.2` (TheAssistant already maps `V26_R1`).
3. **Display entities** – TheAssistant’s `TheHologram` now spawns `TextDisplay` entities instead of ArmorStands. `ArmorStandUtil` creates `ItemDisplay` entities.

## Requirements

- Paper **26.1.2+** (or 1.21.4+)
- **TheAssistant** 4.0.2 (use the patched jar from `dist/`)

## Build

```bash
# Place originals in lib/ (already done if you cloned with libs)
#   lib/OneShoot-original.jar
#   lib/assistant-original.jar

./scripts/build-patched-jars.sh
```

Output:

- `dist/OneShot.jar`
- `dist/the-assistant-4.0.2.jar`

## Install (Club)

1. Stop the server or unload OneShoot / TheAssistant.
2. Replace jars:
   - `plugins/OneShot.jar`
   - `plugins/the-assistant-4.0.2.jar`
3. Use folder `plugins/OneShot/` (migrate from `plugins/OneShoot/` if needed).
4. Start / reload.

## Config templates

See `config-templates/` for the Club configs with OneShot branding.

## Notes

- Java package remains `com.qualityplus.oneshoot` (binary compatible with existing arenas/DB).
- Permission nodes remain `oneshoot.*` so existing LuckPerms grants keep working.
