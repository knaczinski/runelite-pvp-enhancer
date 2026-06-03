---
purpose: completed backlog items for runelite-pvp-enhancer. archive only — never edit existing entries.
format: append-only. newest at top. full spec preserved per item.
---

# BACKLOG HISTORY

## S013 (cont.) — scope standardization + config reorg + skull/overhead polish

**Done S013.** Scope enums reworked with `matches(isSelf,isOpponent)`: DebuffScope +
HealDisplayMode = Off/Everyone/Only me/Only opponents/Self+opponents; OverheadScope adds
"Everyone but self + opponents". Heal/debuff/overhead scope checks unified through `matches`;
remote-heal detection is per-player opponent-aware (kept 4 separate enums per user). PK skull
re-centred and raised above the HP bar / overhead-prayer icon when active (over the head
otherwise). Config panel reorganised into 4 themed sections (Tracking, Combat assist, Overhead
displays, Developer) with clearer names. Overhead flicker fixed earlier this session by
re-suppressing native skull/veng every ClientTick. PID indicator spiked
(`docs/pid-indicator-spike.md`) → not feasible via API, deferred (B029). Dev panel gained a
"Clear debuffs & overlays" button.

## S013 — bug fixes, combo redesign, overlay moves, dev panel, resize spike

Design resolved via /grill-me (8 decisions). User batch on top of live-validation feedback.

### B022 — Combat-trigger + ANY_FIGHT focus bugs
**Done S013.** `isInCombat` no longer treats mere interaction as combat (clicking Attack on an
un-attackable player in a safe zone set `getInteracting()` and false-triggered the heartbeat) —
combat now needs real activity (attack/hit/XP) in the window. Combat focus ANY_FIGHT counts only
player↔player interactions, so teleport-in bystanders (PvE/following) no longer stay visible.
Fixes HT-010 partial. HT-020 + HT-010 re-test queued.

### B023 — Not-attacking warning flashes the opponent
**Done S013.** Reworked from a screen panel into an in-scene flash of the opponent's convex hull
(red↔yellow per tick). Tracks the real opponent actor (player OR NPC via `getInteracting()`), so
it fires for NPC fights (guard test) too. HT-015.

### B024 — Combo taxonomy redesign
**Done S013.** Replaced the combo set. Gear: GODLIKE (5+ slots/tick), EXCELLENT (3–4/tick),
HUMBLE (3–4 over two ticks). Eat: TRIPLE_EAT only. Spec: SPEC_COMBO (opponent takes 2+ hits same
tick while you recently threw ranged + used special; via SPECIAL_ATTACK_PERCENT varp drop +
opponent-hit count) and HUMBLE_SPEC_COMBO over two ticks. Kept COMBO_FAILED (potlock); removed
double-eat, clean-switch, swap→attack tiers. Heuristic — HT-016. Tests rewritten.

### B025 — Heal + debuff overlays beside the HP bar; debuff icons + seconds
**Done S013.** Heal popup anchors right of the health bar (clear of the skull). Debuff timers
render right of the HP bar under the heal row as OSRS-wiki spell icons (Ice_Barrage / Entangle /
Tele_Block, bundled) + a seconds countdown. `DebuffTrackerService` now holds multiple
simultaneous debuffs per actor (freeze + TB = two stacked icons). HT-017.

### B026 — Developer panel + OverlayDemoService + overlay catalogue
**Done S013.** `OverlayDemoService` is a central registry of one-click overlay mock scenarios
(heal, hit predict, debuff, combo), fired on the local player on the client thread. `DevPanel`
lists them grouped by overlay. Config `developerMode` toggle gates a 🛠 header button + the dev
nav button (added/removed live via onConfigChanged). Heal/HitPredict/Combo overlays made
`@Singleton` so the service feeds the rendered instances. `docs/overlays.md` catalogues every
overlay. HT-018.

### B027 — Overhead-resize spike + Vengeance text resize
**Done S013.** Spike (`docs/overhead-resize-spike.md`): the API exposes no scale for native
overhead elements. Only Vengeance text is cleanly resizable (`get/setOverheadText`); skull is
feasible-but-hacky (`setSkullIcon(-1)` + sprite, mutates other players — deferred); prayer-icon +
health-bar are API-blocked (read-only). Implemented `VengeanceTextOverlay` (clear native + redraw
scaled) with `OverheadScope` + `vengTextScope`/`vengTextSize` config. HT-019.

### B028 — PK skull resize (after the spike)
**Done S013.** `SkullResizeOverlay` re-renders the regular PK skull at `skullSize`%: the plugin
hides the native skull (`setSkullIcon(-1)`) on in-scope players, redraws a bundled wiki skull
sprite scaled, and restores the original id on scope-exit / feature-off / shutdown. Only
`SkullIcon.SKULL` handled. Config `skullScope` + `skullSize`. Known limit: a skull expiring while
hidden can't be detected (rare — skulls outlast fights). HT-021.

## S012 — Phase 4 PvP overlays + sidebar redesign (autonomous)

### B018 — Combat focus (hide non-involved entities)
**Effort:** M — **Done S012.**
`CombatFocusService` + a Hooks `RenderableDrawListener` hide Players/NPCs not involved in the
fight; scenery untouched. Config `CombatFocusMode` OFF | SELF | ANY_FIGHT. Involvement is
persistence-based: each engaged actor is stamped `tick + combatFocusTimeout` and stays visible
(through eats/pauses) until it expires — fixes the "others vanish after eating" and "focus only
on taking a hit" reports. Trigger is symmetric (you hitting OR being hit, via HP-xp gain in
onStatChanged). Local player always stays visible; the engaged target is never hidden.
`@Range` config `combatFocusTimeout` (default 16 ticks ≈ 10s). LIVE-VALIDATE: HT-010.

### B019 — XP-drop hit prediction
**Effort:** S–M — **Done S012.**
`XpDamage.fromHitpointsXp(deltaXp) = round(deltaXp / 1.333)` (pure, tested). onStatChanged
HITPOINTS delta → predicted outgoing damage → `HitPredictOverlay` floats an orange number near
the current target, before ranged/magic projectiles land. Config `hitPrediction`. HT-011.

### B020 — Freeze / snare / teleblock timers
**Effort:** M–L — **Done S012 (seed data — LIVE-VALIDATE).**
`DebuffTrackerService` (per-actor timers, count down each GameTick, re-apply only extends) +
`DebuffTimerOverlay` (countdown over the actor's head, coloured per debuff). `SpotanimDebuffs`
seeds spot-anim id → (Debuff, duration ticks) from memory (ice spells 361/363/367/369,
bind/snare/entangle 177/178/179, teleblock 345). onGraphicChanged matches; unknown spot-anims
on tracked players debug-log for harvest. Config `DebuffScope` OFF | OPPONENTS | ALL. HT-012
validates ids/durations.

### B021 — Prayer defensive highlighter
**Effort:** M — **Done S012 (seed data).**
`PrayerHighlightOverlay` boxes the protection prayer countering the current target's
equipped-weapon style. `WeaponStyleMap` seeds common PvP weapon item ids → style; unknown
weapons debug-log. Target weapon read via `PlayerComposition.getEquipmentId(KitType.WEAPON)`
(returns the item id directly). The prayer button is found by NAME scan over
`InterfaceID.Prayerbook.PRAYER1..30` (robust to child-index reordering — ComponentID has no
per-protect-prayer constants). Config `prayerHighlight`. HT-013.

### Sidebar panel redesign + UX batch (user request, S012)
**Effort:** M — **Done S012.**
Nine-item UX batch on top of Phase 4:
- Combat focus no longer hides the engaged target; only truly non-involved entities.
- Heal popup raised to `logicalHeight + 50` so the PvP skull no longer covers it.
- `TrackScope` (SELF_AND_OPPONENTS vs EVERYONE) replaces the trackOpponents boolean; tick
  history + hit summary honour it via `isTracked`/`currentOpponents`.
- Hit summary is a scrollable `JTable` sized for the thin panel; rows green when you attack
  (`HitDirection.OUTGOING`), red when attacked (INCOMING) — new HitDirection threaded through
  `HitSummaryService.addAttack`.
- Tick history moved into a fixed-height scroll pane.
- Header gear button opens this plugin's RuneLite config by posting
  `OverlayMenuClicked(RUNELITE_OVERLAY_CONFIG)` against a plugin-owned anchor overlay
  (`overlay.getPlugin()` is how ConfigPlugin resolves the target).
- Walk-here over Take in combat: onMenuEntryAdded de-prioritises ground-item "Take" while
  `isInCombat`, so left-click walks (Take stays on right-click). Config `swapPickupInCombat`.

## S011 — Phase 2 batch (autonomous)

### B011 — Opponent eating detection
**Effort:** M — **Done S011.**
Opponent eating detected via the eat animation (829) in onAnimationChanged: when a tracked
non-local player plays 829, emit EatEvent(name, "food"). Local eating stays on the menu-click
path (exact item, no double-count). Caveat: 829 cannot distinguish food vs potion and the
item is unknown for remote players; rare overlap with other consume animations is possible.

### B010 — Correlate attacks with their hitsplats
**Effort:** M — **Done S011 (satisfied by Hit Summary, S006).**
Already delivered: AttackHitsplatCorrelator matches each attack to the hitsplat landing on the
target within the style's window (melee 1, ranged/magic 3 ticks); HitSummaryService fills the
Hit column so each attack shows its damage on one row. Closed as satisfied — no new code.

### B009 — Refine hitsplat typing (poison / venom / heal)
**Effort:** S — **Done S011.**
`HitsplatLabels.label(type, amount)` (pure, tested ×4) maps the real hitsplat type id
(HitsplatID) to poison / venom / heal / disease / smite / block / hit. The plugin's
hitsplatLabel delegates to it. Build green, 58 tests.

### B012 — Copy / export tick log
**Effort:** S — **Done S011.**
`TickLogFormatter` (pure, tested ×3) renders the full tick history oldest-first as plain
text. A "Copy log" button in the sidebar panel copies it to the system clipboard
(StringSelection) and flashes "Copied!" for 1.2s. Build green, 54 tests.

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
