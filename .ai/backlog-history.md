---
purpose: completed backlog items for runelite-pvp-enhancer. archive only — never edit existing entries.
format: append-only. newest at top. full spec preserved per item.
---

# BACKLOG HISTORY

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
