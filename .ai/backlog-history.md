---
purpose: completed backlog items for runelite-pvp-enhancer. archive only — never edit existing entries.
format: append-only. newest at top. full spec preserved per item.
---

# BACKLOG HISTORY

## S006 — Phase 3 complete (B014-B017): heartbeat, not-retaliating, hit summary, combos

All four combat-awareness features fully shipped. Build green, 46 tests, deprecation-clean.

- **B014 Heartbeat**: `HeartbeatOverlay` — red edge vignette, exponential decay over 600ms.
  `CombatStateService.setEngaged(bool, tick)` added (+`isNotRetaliating`).
- **B015 Not-retaliating**: `NotRetaliatingOverlay` — panel warning when in combat but
  `getInteracting()` off opponent for >= 2 ticks.
- **B016 Hit Summary**: `HitSummaryService` + `HitSummaryOverlay` (table). Wired with
  `AttackHitsplatCorrelator`. Offen.Pray local-only via `isPrayerActive` (@SuppressWarnings
  deprecation — no API replacement available).
- **B017 Combos**: `ComboDetectorService` (pure, tested) — double/triple eat, potlock
  (same item+qty next tick), clean switch (>=3 swaps), offensive swap tier (gap 0/1/2).
  `ComboFeedbackOverlay` transient popup, `ComboEvent` + `ComboTier/Type/Result` model.
  Config: 6 sections (Tracking/Overlay/Heartbeat/Indicators/HitSummary/Combos).

Deprecation cleanup: OverlayPriority removed; `getActionParam/getWidgetId` → `getParam0/1`;
`getPlayers()` was already migrated in S004; `isPrayerActive` suppressed with rationale.

## S005 — Phase 3 design interview + foundation (B013)

Grilled the 4 combat-awareness features (10 decisions) → `docs/combat-features-design.md`;
added Phase 3 backlog (B013–B017). Shipped **B013** foundation:

- `CombatStateService` (@Singleton): isInCombat = local combat activity within 8 ticks OR
  interacting with a player; fed by the plugin (interaction per tick, activity on local
  hit/attack); no game state of its own (unit tested).
- `AttackHitsplatCorrelator` (pure): best-effort match of an attack to its hitsplat by target
  + style delay window (melee 1, ranged/magic 3 ticks); stale attacks expire. Unit tested.

Build green, 30 tests. B014–B017 remain open (heartbeat, not-retaliating, hit summary, combos).

## S004 — Gear name fix + prayer events + sectioned config + deprecation cleanup

User-requested. Delivered S004 (2026-06-01). Build green (-Xlint:deprecation clean), 21 tests.

- **Wrong gear item names fixed.** Root cause: decoding `PlayerComposition` appearance ids
  (raw - 512) produced wrong names for many items (AGS, infernal cape). Now reads the local
  player's worn item container (`gameval.InventoryID.WORN`) → real item ids → exact names
  via `ItemManager`. Slots use `EquipmentInventorySlot`. Remote-player gear stays out of v1.
- **Prayers in the tick history.** New `EventCategory.PRAYER` + `PrayerEvent` +
  `PrayerNames` (shared HeadIcon→label). `detectPrayerChanges()` diffs each tracked player's
  `getOverheadIcon()` per tick → "prayed Protect Magic" / "prayer off". Overhead protection
  prayer only (the sole prayer observable on remote players).
- **Config split into sections.** `@ConfigSection` "Tracking" (functionality: trackOpponents,
  trackNpcs — gate recording) vs "Overlay" (display: maxHistoryTicks, show combat/eating/
  gearSwap/prayer). The `show*` toggles are now display-only filters; the plugin records
  everything tracked (removed the early-return gating in handlers).
- **Max ticks: no minimum.** `@Range(min=5,max=100)` → `@Range(max=5000)` (min defaults 0).
- **Deprecation cleanup.** Migrated `InventoryID.EQUIPMENT` → `gameval.InventoryID.WORN`
  and `getPlayers()` → `getTopLevelWorldView().players()`; added `-Xlint:deprecation` to keep
  the tree warning-clean.
- Tests: CombatEventFactoryTest label update; Combo... unchanged; new PrayerEventTest (4).

## S003 — Per-tick code + combo-eat merge + chronological display

User-clarified display model. Delivered S003 (2026-06-01). Build green, 17 tests.

- **Tick code identifier.** `TickEntry` gained a 1-based `sequence` assigned by the service
  at flush (reset on clear); the overlay renders it as `Tick 0001`. Raw client tick is
  retained for future timing analysis.
- **N events per tick / multiple characters.** Already provided by the flush model + each
  event carrying its actor name — confirmed, no change needed.
- **Combo-eat merge.** `EatEvent` now holds a list of items; `ComboEatMerger` (pure)
  collapses same-player eats within a tick into one line — "ate Shark + Karambwan
  (double eat)" / "triple eat" / "Nx eat". Different players and non-eat events stay
  separate and ordered.
- **Chronological order.** Overlay flipped to oldest-first (Tick 0001 at top) to match the
  requested example (was newest-first in S001).
- Tests: TickHistoryServiceTest rewritten (7); ComboEatMergerTest added (5).

## S002 — NPC tracking + Combatant abstraction + dev-env fixes

User-requested (not a pre-listed B-item). Delivered S002 (2026-06-01). Build green, 10 tests.

- **NPC test toggle.** New `Combatant` interface abstracts RuneLite `Player`/`NPC`.
  `PlayerCombatant`/`NpcCombatant` adapt them; `Combatants.of(Actor, localPlayer)` is the
  sole `instanceof` site; `CombatEventFactory.fromAttack(Combatant)` is pure and
  Mockito-tested. Config `trackNpcs` (default off) records NPC combat events for testing
  without a second player. NPCs return blank for fields they lack (prayer); `AttackEvent`
  omits the "on <prayer>" clause when null.
- **Dev-env fixes (carried from the session's debugging):**
  - Jagex-account login in the dev client → documented `--insecure-write-credentials` →
    `credentials.properties` flow (the dev client showed the legacy login).
  - Double "PvP Enhancer" entry → removed `build.finalizedBy installPlugin` (installPlugin
    is opt-in, mutually exclusive with `run`); added `--add-opens` to the run task for
    loadBuiltin on JDK 17+.
- **Docs:** new `docs/dev-faq.md`; updated building-and-testing, tick-history-design,
  PROJECT, README, combatant/CONTEXT.md.

## S001 — Phase 0 + Phase 1 (tick history MVP) — B001-B007

All shipped in S001 (2026-06-01). Build green, 5 unit tests pass, jar auto-installs to
~/.runelite/sideloaded-plugins. Live validation queued as HT-001..HT-004.

### B007 — Build auto-install + build/test documentation
**Effort:** S — **Done S001.**
Gradle `installPlugin` Copy task drops the built jar into `~/.runelite/sideloaded-plugins`;
`build` finalizes with it (disable via `-x installPlugin`). `run` task launches RuneLite
in developer mode with the plugin loaded (primary dev loop). Wrote docs/building-and-testing.md
covering both loops, prerequisites, tests, and troubleshooting.

### B006 — Config panel
**Effort:** S — **Done S001.**
PvpEnhancerConfig: maxHistoryTicks (@Range 5-100, default 20), showCombat, showEating,
showGearSwap, trackOpponents. RuneLite persists automatically. Live-rendered toggle check is HT-004.

### B005 — Tick history overlay (OverlayPanel)
**Effort:** M — **Done S001.**
TickHistoryOverlay extends OverlayPanel, TOP_LEFT. Title + per-tick "Tick N" header +
one colour-coded LineComponent per visible event. Newest tick first. Category colour +
visibility from config. setPriority omitted (enum removed on current API). Live render is HT-002.

### B004 — Eating and gear swap detector
**Effort:** S — **Done S001.**
Eating: MenuOptionClicked "Eat"/"Drink" → EatEvent (local player, Text.removeTags item).
Gear swap: PlayerComposition.getEquipmentIds() diff per GameTick → GearSwapEvent per changed
slot (KitType name, item id = raw - 512, name via ItemManager). Local player only in v1.

### B003 — Combat event detector
**Effort:** M — **Done S001.**
AnimationChanged → AnimationStyleMap.lookup(anim) → AttackEvent(attacker, target, style,
target overhead HeadIcon). HitsplatApplied → HitsplatEvent(target, amount, block/hit).
Unmapped attack-like animations logged at debug for cataloguing. trackOpponents gates
whether remote players are recorded. Live accuracy is HT-001/HT-003.

### B002 — Tick event collector (TickHistoryService)
**Effort:** M — **Done S001.**
@Singleton service: pending list + capped Deque<TickEntry> (newest first). addEvent
during a tick; flushTick(tickCount) seals into a TickEntry; trim to maxHistory. Pure (no
client), 5 unit tests (cap, order, group, shrink-trim, clear) all green. Model classes:
CombatEvent base + AttackEvent/HitsplatEvent/EatEvent/GearSwapEvent, EventCategory,
AttackStyle, TickEntry, AnimationStyleMap.

### B001 — Gradle project scaffold
**Effort:** S — **Done S001.**
Adapted RuneLite example-plugin template: compileOnly net.runelite:client:latest.release
from repo.runelite.net, options.release=11. Committed Gradle 8.10 wrapper. PvpEnhancerPlugin
(@PluginDescriptor) + PvpEnhancerConfig stubs + PvpEnhancerTest dev harness. `./gradlew build`
succeeds clean. Build gotcha: Client has no getItemComposition(int) — use ItemManager.
