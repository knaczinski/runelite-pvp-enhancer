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

---

## ▶ PHASE 0 — Project bootstrap

### B001 — Gradle project scaffold
**Effort:** S
**Status:** OPEN
**Scope:** initialise the Gradle project from the RuneLite external plugin template.
- Set up `build.gradle`, `settings.gradle`, `gradle.properties` targeting RuneLite API.
- Create `PvpEnhancerPlugin.java` (stub `startUp`/`shutDown` + `@PluginDescriptor`).
- Create `PvpEnhancerConfig.java` (empty `@ConfigGroup("pvpenhancer")`).
- Confirm `./gradlew build` compiles clean with zero errors.
- Add `.gitignore` for Gradle build outputs.
**Acceptance:** `./gradlew build` succeeds. Plugin loads in RuneLite developer mode without errors in the log.

---

## ▶ PHASE 1 — Tick history core

### B002 — Tick event collector (TickHistoryService)
**Effort:** M
**Status:** OPEN — depends on B001
**Scope:** stateful service that builds a tick-by-tick event buffer.
- `TickHistoryService` — injectable, maintains a `Deque<TickEntry>` capped at `config.maxHistoryTicks()`.
- `TickEntry` — data class: `tick` (int, from `client.getTickCount()`), `List<CombatEvent>` events.
- `CombatEvent` — sealed hierarchy: `AttackEvent`, `HitsplatEvent`, `EatEvent`, `GearSwapEvent`.
- On `GameTick`: advance the current tick, flush pending events into a new `TickEntry`, drop entries beyond cap.
- Unit test: feed synthetic events over N ticks, assert buffer size and order.
**Acceptance:** unit tests green. Buffer correctly caps and orders entries.

### B003 — Combat event detector
**Effort:** M
**Status:** OPEN — depends on B002
**Scope:** subscribe to game events and emit `AttackEvent` and `HitsplatEvent` into `TickHistoryService`.
- `@Subscribe InteractingChanged` — record when a player targets another player. Log actor + target names.
- `@Subscribe AnimationChanged` — when a player actor's animation changes, map animation ID to attack style (MELEE/RANGED/MAGIC/UNKNOWN) using `AnimationStyleMap` lookup table. Emit `AttackEvent(attacker, target, style, overheadIcon)`.
- `@Subscribe HitsplatApplied` — emit `HitsplatEvent(target, amount, type)`. Use `Hitsplat.HitsplatType` to distinguish hit/block/poison/etc.
- `AnimationStyleMap` — static map of known PvP animation IDs to `AttackStyle` enum. Seed with common weapon animations (see .ai/game/pvp/pvp-combat-events.md for animation ID reference).
- `Player.getOverheadIcon()` returns `HeadIcon` — snapshot at time of `AnimationChanged` event.
**Acceptance:** in a live PvP scenario (HT-001), combat events appear in the service buffer with correct attacker/target/style/prayer.

### B004 — Eating and gear swap detector
**Effort:** S
**Status:** OPEN — depends on B002
**Scope:** emit `EatEvent` and `GearSwapEvent` into `TickHistoryService`.
- **Eating:** `@Subscribe MenuOptionClicked` — if `menuOption` equals "Eat" or "Drink", emit `EatEvent(player, itemName)`. Alternative: animation 829 is the eat animation — emit on `AnimationChanged` if animation == 829 and cross-reference inventory diff.
- **Gear swap:** snapshot `PlayerComposition.getEquipmentIds()` at each `GameTick`. Diff against previous snapshot. Any changed slot emits `GearSwapEvent(player, slot, oldItemId, newItemId)`. Use `KitType` enum for slot names.
- Cover local player only in v1. Opponent gear swap detection requires observing their `PlayerComposition` changes — include if feasible without additional complexity.
**Acceptance:** eating and gear swap events appear in service buffer on next `GameTick` after the action.

### B005 — Tick history overlay (OverlayPanel)
**Effort:** M
**Status:** OPEN — depends on B002, B003, B004
**Scope:** render the tick history as a left-sidebar OverlayPanel.
- `TickHistoryOverlay extends OverlayPanel`.
- Renders each `TickEntry` as a block: heading = "Tick N", lines = one per `CombatEvent`.
- Format per event type:
  - `AttackEvent`: `[COMBAT] <attacker> → <target> (<style>) on <prayer>, hit <amount>`
  - `HitsplatEvent`: `[HIT] <target> took <amount> (<type>)`
  - `EatEvent`: `[EAT] <player> ate <item>`
  - `GearSwapEvent`: `[GEAR] <player> equipped <item> (<slot>)`
- Colour-code by category (configurable palette).
- Most recent tick at the top. Scroll if panel height exceeded.
- Toggle categories via `config.showCombat()`, `config.showEating()`, `config.showGearSwap()`.
**Acceptance:** HT-002 — overlay renders correct events in the correct tick order in a live fight.

### B006 — Config panel
**Effort:** S
**Status:** OPEN — depends on B005
**Scope:** expose user-configurable settings via `PvpEnhancerConfig`.
- `maxHistoryTicks` — int slider, default 20, range 5–100.
- `showCombat` — boolean, default true.
- `showEating` — boolean, default true.
- `showGearSwap` — boolean, default true.
- `trackOpponents` — boolean: if true, track all visible players in combat; if false, local player only. Default true.
- Colour items for each category (optional — defer if complex).
**Acceptance:** settings panel renders in RuneLite config. Changes take effect without restart.
