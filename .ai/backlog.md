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
**Status:** OPEN — NEXT UP after HT validation.
**Scope:** the v1 AnimationStyleMap is a small seed. Collect real animation ids from live PvP
(the plugin debug-logs unmapped attack-like animations) and expand the map.
- Run a fight, harvest the "Unmapped animation N by X -> Y" debug lines.
- Add verified ids to `model/AnimationStyleMap.java`, citing the OSRS Wiki weapon page.
- Add spec-attack ids; consider an `isSpecial` flag on AttackEvent if specs should be marked.
**Acceptance:** common weapons in the user's PvP loadout all resolve to the correct style, no UNKNOWN.

### B009 — Refine hitsplat typing (poison / venom / heal)
**Effort:** S
**Status:** OPEN
**Scope:** v1 collapses hitsplats to "block" (0 dmg) or "hit". Use the real hitsplat type to
distinguish poison, venom, heal, and prayer-drain (smite).
- Investigate the current API: `Hitsplat.getHitsplatType()` return type + `HitsplatID` constants.
- Map to a richer label in `HitsplatEvent` / the plugin's `hitsplatLabel`.
**Acceptance:** poison/venom/heal hitsplats render with distinct labels in the overlay.

### B010 — Correlate attacks with their hitsplats
**Effort:** M
**Status:** OPEN
**Scope:** hitsplats land 1-3 ticks after the attack animation. Match an AttackEvent to the
resulting HitsplatEvent (by attacker→target pair + expected tick offset per attack type) so the
overlay can show "X hit Y for N" as one correlated line.
- Needs per-style projectile/hit delay table (melee 0, ranged/magic vary by distance).
**Acceptance:** in a live fight, most attacks display their resulting damage on one line.

### B011 — Opponent eating detection
**Effort:** M
**Status:** OPEN
**Scope:** MenuOptionClicked only fires for the local player. Detect opponent eating via the
eat animation (829) + a heuristic to avoid false positives.
**Acceptance:** opponent eats are logged with acceptable precision (document the false-positive rate).

### B012 — Copy / export tick log
**Effort:** S
**Status:** OPEN
**Scope:** add a right-click overlay menu option (or config-bound hotkey) to copy the current
tick history to the clipboard as text, for post-fight review/sharing.
**Acceptance:** clicking the option puts a readable text dump on the clipboard.
