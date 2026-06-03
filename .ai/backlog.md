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

## ▶ PHASE 4 — PvP overlays (focus, prediction, timers, prayer) — CODE COMPLETE

All Phase 4 dev items shipped (S012): **B018** combat focus, **B019** hit prediction,
**B020** freeze/TB timers, **B021** prayer highlighter. Full records in `backlog-history.md`.
Remaining work is **live validation** (HT-010..HT-014) + harvesting seed data (unknown
spot-anim ids → B020 map, unknown weapon ids → B021 map, unmapped attack anims → B008).

---

## ▶ PHASE 5 — Overhead resize + UX (S013)

Done (history, S013): combat-trigger/focus bugfixes, not-attacking opponent flash, combo
rework, heal/debuff side-of-HP-bar + icons, dev panel + mocks, Vengeance + PK skull resize,
overhead flicker fix, scope-enum standardization, config reorg. See `backlog-history.md`.

### B029 — PID indicator (experimental) — DONE S013
Built as an **experimental** "PID guess" + swap warning per user decision. `PidGuessService`
accumulates a noisy vote from contested same-tick hitsplats; `PidIndicatorOverlay` shows
you/them/? (labelled exp) for a clean 1v1 the local player is in; swap warning on lead flip.
Dev-panel mocks included. Honest about being best-effort (PID not API-exposed). Full research +
verdict in `docs/pid-indicator-spike.md`. Live validation: HT-023. **OPEN follow-up:** confirm
whether contested-hitsplat order actually tracks PID (vs index artifact) in live 1v1s — if it's a
pure index artifact, downgrade/remove the live signal and keep only the mock.
