# Agent handoff — OneShot

Read this first in any new Cursor session before changing this plugin or connecting it to the Genesi website / custom API.

## What this repo is

Patched QualityPlus OneShoot — one-arrow duels; depends on TheAssistant.

| Item | Value |
|------|--------|
| Bukkit `name` | `OneShot` |
| Main class | `com.qualityplus.oneshoot.OneShoot` |
| Paper | `26.1.2+` |
| Data folder | `plugins/GenesiCore/games/OneShot/ (config, arenas, storage, …)` |
| Player / admin commands | `/oneshot` (`os`) |

## Non-negotiables

- Keep runtime data under **`plugins/GenesiCore/games/OneShot/`** (via GenesiGamesApi).
- Do not relocate jars out of `plugins/`; only data folders live under GenesiCore/games.
- Preserve existing arena/config YAML — never wipe Club data to "test defaults".

## Integration hooks (today)

Jar-patch pipeline in `scripts/build-patched-jars.sh`; data-folder redirect in patched `OneShoot.setup()`.

Future API: arena join, match outcomes. Permissions remain `oneshoot.*`. Always rebuild via patch script; do not redistribute vendor sources.

## Genesi API / website (future)

When wiring this minigame to the Genesi website or custom API:

1. Prefer a **companion bridge** (HTTP ↔ Paper) or signed console/WebSender commands — do not invent ad-hoc file writes while players are online.
2. Use **idempotent** grant/purchase IDs for any economy, points, or prize grants.
3. Keep OpenAPI / event schemas as the source of truth once they exist under GenesiCore or the web repo.
4. Soft-depend / respect **GenesiCore** inventory/limbo sync — avoid putting persistent items in player inventories that GenesiCore will overwrite.
5. Never commit SFTP, RCON, WebSender, or panel secrets.

## Shared stack

- Depends on **GenesiGamesApi** (`dev.genesi.games.*`) for data-folder redirect and shared helpers where applicable.
- Club SFTP: FileZilla site **Genesi / Club**.

## Build / deploy

```bash
./scripts/build-patched-jars.sh
# Deploy dist/OneShot.jar + dist/the-assistant-4.0.2.jar
```

## Related

- Shared library + convention: `/Users/admin/GenesiCore` (`games-api`, `AGENTS.md`)
- Sibling minigames follow the same `GenesiCore/games/<name>/` layout
