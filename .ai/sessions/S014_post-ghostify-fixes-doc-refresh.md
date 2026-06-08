---
session: S014
title: Post-Ghostify fix batch + full documentation refresh
date: 2026-06-08
mode: incremental fixes (user-driven) + documentation audit
build: green, 78 tests
---

# S014 — post-Ghostify fixes + doc refresh

## Fixes shipped (each its own commit, pushed)

- **Ghostify = hide + outline** (`ModelOutlineRenderer`): non-involved/categorised players show as a
  coloured contour, not invisible. Then redesigned into a dedicated **Ghostify** config section:
  per category (Self / Opponents / Group(CC+FC) / Friends / Others) a WHEN rule + outline COLOUR;
  Others adds "Can't attack here". Priority opponents > group > friends > others. Replaced
  CombatFocusMode/Service + FocusOutlineOverlay.
- **In-combat smoothing** — per-player `GHOST_COMBAT_WINDOW` (10t) stamping attacker AND target, so
  an eating opponent (interaction clears ~3t) doesn't briefly un-ghost.
- **Name-based hider** — `GhostifyService.shouldDraw` matches by player NAME, not object identity
  (a talking re-draw uses a different `Player` instance → identity miss → model popped back in).
- **"Can't attack here" on PvP worlds** — `attackableRange` = Wilderness level (`Varbits.IN_WILDERNESS`)
  or, off-wild on a PvP world (`WorldType.isPvpWorld`), `PVP_WORLD_RANGE=15` (HT-flagged); else -1.
- **Combos only in combat** — banking/depositing all worn items changed many slots → false Godlike
  switch. Detector still advances; combos emit only when `isInCombat`.
- **Accurate remote heal** — real max HP: `NPCManager.getHealth` for NPCs, OSRS Hiscores
  (`HiscoreManager.lookupAsync`, cached per name) for players, like Opponent Information. Toggle
  `accurateRemoteHp` (default on); falls back to 99 while pending/unranked. Still a `~estimate`
  (health-bar resolution + HP boosts). +explained to the user why it can't be perfectly exact.
- **Ghostify default colours** set to the user's values.

## Documentation audit (this session's main ask)

Brought stale docs current to the Ghostify/PID/accurate-heal state:
- `README.md` — Features rewritten (Combat assist / Overhead / Ghostify / Combos / PID), code-org block.
- `.ai/architecture.md` — replaced the generic "target" skeleton with the real component map
  (services, overlays, ghostify hider, external data, threading).
- Package `CONTEXT.md` — overlay/ (all 11 overlays), service/ (all 9 services), root (5 config
  sections + ClientTick re-suppression rule), panel/ (JTable + DevPanel), model/ (combo/hit-summary/
  debuff/heal models + seed maps).
- `docs/overlays.md` — Heal/Debuff scope columns, accurateRemoteHp, DevPanel Clear + PID mocks.
- `PROJECT.md` — Mission/scope note (suite, not just tick history), healing data source, config sections.
- `.ai/INDEX.md` — package map + design-docs list. `CLAUDE.md` + `CONTEXT.md` — latest_session S014.

## State / NEXT
- Build green, 78 tests. All requested features in. Only **B008** open (live-data-gated).
- NEXT: live validation (HT-010 re-test, HT-012/013/014, HT-021..024) + seed harvest
  (spot-anim / weapon / attack-anim ids). Validate `PVP_WORLD_RANGE=15` and skull offsets live.
