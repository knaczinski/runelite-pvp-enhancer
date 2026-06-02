---
purpose: product backlog for runelite-pvp-enhancer — OPEN items only
auto-update: AI updates item status each session. session logs → .ai/sessions/SXXX_<slug>.md (never here).
completed: full records in .ai/backlog-history.md. When an item is accepted, remove its spec here and prepend the record there.
format: priority-ordered. effort: XS (<15min) S (15-45min) M (1-2h) L (2-4h) XL (4h+).
rule: read at session start. "continue from where backlog stopped" → execute NEXT UP.
lang: English only. caveman lite.
---

# BACKLOG

Execution is phase-ordered. Within each phase, items run in numeric order unless explicitly noted.
Completed phases/items are archived in `.ai/backlog-history.md` — only OPEN work lives here.

Phases fully complete (see backlog-history.md): **Phase 0** (B001), **Phase 1** (B002-B007, tick history MVP).

---

## ▶ PHASE 2 — Tick history refinement

Refinements deferred from the v1 MVP. All gated on live validation (HT-001..HT-004) first —
do not start Phase 2 until the MVP behaviour is confirmed in a real fight, since the seed
data (animation ids, offsets) needs live correction.

### B008 — Grow AnimationStyleMap from live data
**Effort:** S–M
**Status:** OPEN — LIVE-DATA-GATED. Cannot be completed autonomously: adding animation ids
from memory risks the wrong-mapping bug the user already flagged. The plugin debug-logs
"Unmapped animation N by X -> Y" in a live fight; the user harvests those verified ids and
they get added here. Correctness requires live data, not guesses.
**Scope:** the v1 AnimationStyleMap is a small seed. Collect real animation ids from live PvP
(the plugin debug-logs unmapped attack-like animations) and expand the map.
- Run a fight, harvest the "Unmapped animation N by X -> Y" debug lines.
- Add verified ids to `model/AnimationStyleMap.java`, citing the OSRS Wiki weapon page.
- Add spec-attack ids; consider an `isSpecial` flag on AttackEvent if specs should be marked.
**Acceptance:** common weapons in the user's PvP loadout all resolve to the correct style, no UNKNOWN.

Done (history): B009, B010 (satisfied by Hit Summary), B011, B012 — see backlog-history.md.

---

## ▶ PHASE 3 — Combat awareness & combos

Design resolved in S005 (interview). Full spec: `docs/combat-features-design.md`.
Done (history): B013-B017 (all shipped S005-S006). See backlog-history.md.

---

## ▶ PHASE 4 — PvP overlays (focus, prediction, timers, prayer)

Decisions (user, S012): combat focus = HIDE non-involved (transparency not exposed by the
API); prayer highlighter = predictive from the opponent's equipped weapon.

### B018 — Combat focus (hide non-involved entities)
**Effort:** M
**Status:** OPEN.
**Scope:** during combat, hide Players + NPCs not involved in the relevant fight (scenery
untouched) via a Hooks RenderableDrawListener, to focus on the participants.
- Config `CombatFocusMode`: OFF | SELF (only when you fight) | ANY_FIGHT (when anyone nearby fights).
- "Involved" set recomputed each tick: SELF = you + your target + anyone targeting you;
  ANY_FIGHT = every player currently interacting + their targets.
- Listener returns false (hide) for non-involved Player/NPC; true for everything else.
**Acceptance:** non-involved players/NPCs vanish during combat per the mode; scenery intact;
disabling restores everything. (Live HT — hiding non-involved players is intentional.)

### B019 — XP-drop hit prediction
**Effort:** S–M
**Status:** OPEN.
**Scope:** predict the local player's outgoing damage from the Hitpoints XP drop (HP xp =
1.333 × damage, so damage = round(hpXp / 1.333)). XP is granted at attack time, before the
ranged/magic projectile lands — so the number precedes the hitsplat (lets you react / stack spec).
- StatChanged HITPOINTS → delta → predicted damage; floating number near the current target.
- Pure XpDamage helper + test. Config toggle.
**Acceptance:** predicted damage appears on the target a tick or two before the hitsplat for ranged.

### B020 — Freeze / snare / teleblock timers
**Effort:** M–L
**Status:** OPEN — seed data, LIVE-VALIDATE.
**Scope:** detect freeze/snare/teleblock via GraphicChanged spotanims; show a countdown timer
(+ debuff icon) on the affected actor. Config scope: opponent-only | all players.
- Seed table graphic-id → (debuff, duration ticks). Memory-cited → log unknown spotanims for
  live collection + an HT to verify ids/durations (like the animation map).
- Decrement timers each GameTick; remove at 0.
**Acceptance:** a freeze/TB on a tracked player shows a correct countdown. (HT validates ids/durations.)

### B021 — Prayer defensive highlighter
**Effort:** M
**Status:** OPEN — seed data.
**Scope:** highlight the protection prayer (Melee/Ranged/Magic) matching the current opponent's
EQUIPPED WEAPON (predictive).
- `WeaponStyleMap` seed (item id → style); read opponent weapon via PlayerComposition
  (appearance id − 512). Log unknown weapon ids for collection.
- Highlight the matching prayer widget in the prayer tab (overlay box).
- Config toggle.
**Acceptance:** facing a known melee/range/mage weapon, the right protection prayer is highlighted.
