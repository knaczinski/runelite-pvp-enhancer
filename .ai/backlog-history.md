---
purpose: completed backlog items for runelite-pvp-enhancer. archive only — never edit existing entries.
format: append-only. newest at top. full spec preserved per item.
---

# BACKLOG HISTORY

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
