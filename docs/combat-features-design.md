# Combat Awareness & Combos — Design Document

Four PvP features layered on the existing tick-history plugin, delivered in four phases.
Design decisions below were resolved in a design interview (session S005). Items reference
backlog B013–B017.

---

## Shared foundation (Phase A — B013)

### "In combat" (CombatStateService)

A single source of truth for combat state, used by the heartbeat and the not-retaliating
indicator. **In combat** is true when *either*:

- the local player dealt or took a hitsplat within the last **8 ticks** (configurable window), or
- the local player is currently interacting with another player (or one with them).

Tracked by feeding the service: a hitsplat involving the local player stamps the
last-activity tick; each `GameTick` reports whether the local player is interacting with a
player. Cleared on logout / plugin stop.

### Attack → hitsplat correlation (AttackHitsplatCorrelator)

In OSRS a hitsplat lands **1–3 ticks after** the attack animation (melee ~0–1, ranged/
magic ~1–3 by projectile travel). The correlator matches them **best-effort**: each attack
is queued (attacker, target, style, tick); the next hitsplat on that target within the
style's delay window claims the earliest matching attack. Pending attacks expire after the
max window. Works cleanly in 1v1, degrades gracefully in chaotic multi-combat. Reused by
the Hit Summary (the "Hit" column) and the combo system (spec damage).

---

## Heartbeat indicator (Phase B — B014)

A red "blood" vignette around the screen edges that **pulses once per game tick while in
combat**, perfectly synced to the server tick (peak on the `GameTick`, decaying over the
600 ms). Purpose: a combat tick metronome to help time tick-perfect actions.

- **Intensity: constant** (not HP-scaled) — a pure metronome.
- Only renders while in combat (CombatStateService).
- Config: enable/disable (opacity tunable later).

## Not-retaliating indicator (Phase B — B015)

Warns when you are in combat but have stopped attacking. **Trigger:** while in combat, if
the local player's `getInteracting()` is **not** an opponent (null / a non-combat target)
for **≥ 2 ticks**, show a prominent "NOT ATTACKING — re-click target" warning; clear it the
moment you re-engage.

Rationale: actively attacking keeps `getInteracting()` on the opponent (even between hits,
during weapon cooldown), and eating does not break engagement — so this does not false-fire
between hits or on eats. Walking, looting, or clicking elsewhere *does* drop engagement and
requires a re-click, which is exactly what we flag.

- Config: enable/disable.

## Hit Summary overlay (Phase C — B016)

A tabular overlay complementing the chronological tick history:

| Tick | Player | Offen. Pray | Attack | Target | Target Prayer | Hit |
|------|--------|-------------|--------|--------|---------------|-----|

- **One row per attack**, for all tracked players (you + opponents per the tracking config).
- **Offen. Pray** (attacker's offensive prayer, e.g. Piety/Rigour/Augury): readable **only
  for the local player** (`client.isPrayerActive`); RuneLite cannot see opponents' offensive
  prayers, so it is **blank** for others.
- **Attack** = inferred style/weapon; **Target Prayer** = defender's overhead at attack time.
- **Hit** = filled by the best-effort correlator (the hitsplat landing on the target within
  the style's window). Blank until/if it correlates.
- Config: enable/disable, row cap.

## Combo system (Phase D — B017)

Self-improvement feedback for **your own** combos. A combo is a named pattern matched over
the recent tick-history events; each detector yields **success + optional quality tier +
optional failure reason**. Recipes are **fixed** in v1 (future: configurable); config is a
single **enable/disable**.

### Fixed recipes (v1)

| Combo | Detection | Result |
|-------|-----------|--------|
| Double eat | 2 consumes on the same tick | success |
| Triple eat | 3 consumes on the same tick | success |
| Combo failed | an "Eat"/"Drink" click with **no** consumption (potlock / fumble) | failure |
| Offensive swap→attack | weapon equip followed by an attack; graded by the equip→attack tick gap | tier: gap 0 = **perfect**, 1 = **great**, 2 = **good**, ≥3 / none = no combo |
| Clean switch | **≥ 3** worn-equipment slots changed on the **same** tick | success |

**Why the gap = quality (offensive):** the attack can only fire when the weapon cooldown
expires; equipping earlier telegraphs the swap for that many ticks, giving the opponent time
to flick the protection prayer. Equipping on the same tick the attack fires (gap 0) is the
"last-tick swap" / 1-tick switch — zero reaction window. So the equip→attack gap *is* the
telegraph window. Applies to spec or normal attacks after a swap.

**Combo-failed detection (heuristic, drag-aware):** fire only on an "Eat"/"Drink" click that
produces no consumption (no food-item decrement in the inventory on the click tick). Inventory
**reorganization** must never be flagged: a drag to a *different* slot is intentional and
ignored; only a no-op drag back to the *same* slot is a fumble candidate. (A full per-category
eat-delay model — hard 3t / potion 3t / karambwan 2t — is a future upgrade.)

### Visual feedback

A **transient floating popup** (center / above the player) that fades over ~1.5 s, colour-
coded: perfect = gold, great = green, good = blue, eat-combos = green, failed = red. Texts:
"DOUBLE EAT", "TRIPLE EAT", "PERFECT!", "GREAT", "GOOD", "CLEAN SWITCH", "COMBO FAILED".
Every combo is also recorded as a `ComboEvent` in the tick history.

---

## OSRS mechanics reference (user-provided, S005)

- Tick = 600 ms; the server resolves all queued inputs at the end of the tick.
- Attack cooldowns (ticks): Magic shortbow 3, Abyssal whip 4, Armadyl godsword 6.
- Last-tick swap: equip new-style weapon + attack queued in the same 600 ms window → server
  processes both same tick → opponent gets ~0 reaction time (must pray predictively).
- Combo eating: hard food (+3t attack delay, blocks hard food 3t), potion (+3t to next
  potion), karambwan (+2t, independent flag) — different flags → all three can land on one
  tick; the attack delay then **sums** (e.g. manta 3t + karambwan 2t = 5t before next attack).
