# Tick History System — Design Document

The tick history system is the initial feature of the PvP Enhancer plugin. It provides a chronological, tick-accurate log of combat events during a PvP fight, displayed as an overlay panel inside the RuneLite client.

---

## 1. What problem it solves

OSRS PvP is fast-paced and tick-dependent. In a live fight it is difficult to track simultaneously: what attack style an opponent threw, what prayer they were on, whether you ate on the right tick, and whether a gear swap landed in time. After a fight, there is no built-in replay or log — you rely on memory.

The tick history overlay provides an always-visible, scrollable record of the last N ticks so you can review what happened in real time or immediately after a fight ends.

---

## 2. Event categories

The overlay groups events into three categories, each togglable independently:

### Combat

Tracks attack actions between players:

```
[COMBAT] knz → opponent  ranged  on MAGIC  hit 24
[COMBAT] opponent → knz  magic   on MELEE  hit 0 (blocked)
```

Fields: attacker name, target name, attack style (melee/ranged/magic), defender's overhead prayer at time of attack, and damage amount when the hitsplat lands.

### Eating

Tracks food and potion consumption:

```
[EAT] knz ate Shark (+20hp)
[EAT] knz drank Super restore
```

Detected via the "Eat"/"Drink" menu click, before the animation plays. This gives one tick of lead time compared to waiting for the animation.

### Gear Swap

Tracks equipment changes tick-by-tick:

```
[GEAR] knz equipped Twisted bow  (weapon slot)
[GEAR] knz equipped Armadyl helmet  (head slot)
```

Detected by diffing the `PlayerComposition.getEquipmentIds()` array between consecutive game ticks.

---

## 3. Architecture

```
PvpEnhancerPlugin          startUp/shutDown, event hub
    │
    ├── TickHistoryService  stateful buffer: Deque<TickEntry>
    │       └── TickEntry   tick number + List<CombatEvent>
    │           └── CombatEvent  (AttackEvent | HitsplatEvent | EatEvent | GearSwapEvent)
    │
    └── TickHistoryOverlay  OverlayPanel, reads service, renders panel
```

**TickHistoryService** is the single source of truth. It:
- Listens to `GameTick` to advance the tick counter and flush pending events.
- Receives events from the plugin's `@Subscribe` methods (not directly — the plugin feeds the service).
- Maintains a bounded deque capped at `config.maxHistoryTicks()` entries.

**TickHistoryOverlay** is purely a read-only view. On each `render()` call it reads the service's deque and paints the entries. It does not mutate state.

### The Combatant abstraction

The plugin never branches on RuneLite's concrete `Player` / `NPC` types in its detection
logic. Instead both are adapted to one small interface, `Combatant`:

```
Combatant  (getName, getAnimation, getOverheadPrayer, getTarget, isPlayer, isLocalPlayer)
   ├── PlayerCombatant   wraps a RuneLite Player   (full info)
   └── NpcCombatant      wraps a RuneLite NPC       (blank where N/A, e.g. prayer = null)

Combatants.of(Actor, localPlayer)            the single point that does `instanceof`
CombatEventFactory.fromAttack(Combatant)     pure Combatant -> AttackEvent, no client
```

Two payoffs:

- **NPC testing.** A `Track NPCs` config toggle (off by default) lets `NpcCombatant` flow
  through the same pipeline, so a developer can exercise combat/hitsplat detection by
  attacking a training dummy — no second player needed. Fields an NPC cannot provide
  (overhead prayer) are returned `null` and rendered blank.
- **Unit testing.** Because the logic consumes `Combatant`, not the heavyweight RuneLite
  interfaces, tests build attacks from `mock(Combatant.class)` with no live client
  (`CombatEventFactoryTest`).

---

## 4. Data flow

```
Game engine
    │  (packets)
    ▼
RuneLite event bus
    │  AnimationChanged / HitsplatApplied / MenuOptionClicked / GameTick
    ▼
PvpEnhancerPlugin (@Subscribe methods)
    │  emits CombatEvent objects
    ▼
TickHistoryService.addEvent(CombatEvent)
    │  buffered until GameTick flushes into TickEntry
    ▼
TickHistoryOverlay.render()
    │  reads Deque<TickEntry>
    ▼
Screen
```

---

## 5. Known limitations

**Attack style inference is probabilistic.** Style is inferred from animation IDs, which must be manually catalogued in `AnimationStyleMap`. Unknown animations show as `UNKNOWN`. The map will grow as test data is collected via human testing.

**Hitsplat delay.** OSRS applies hitsplats 1–3 ticks after the attack animation, depending on weapon and attack type. The overlay logs the hitsplat on the tick it arrives, not the tick the attack was thrown. These will appear as separate entries and will not be auto-correlated in v1.

**Opponent gear swap latency.** Remote player `PlayerComposition` updates arrive asynchronously. A gear swap may appear 1 tick late for opponents.

**Eating detection scope.** `MenuOptionClicked` only fires for the local player. Opponent eating is not detectable reliably in v1 (no menu click event for other players; animation 829 can be ambiguous).

---

## 6. Future extensions (out of scope for v1)

- Auto-correlate attack events with hitsplats (match by actor pair + tick offset).
- Opponent eating detection via animation 829 tracking.
- Prayer flick miss detection (overhead prayer changed too late relative to attack tick).
- Export / copy tick log to clipboard.
- Configurable colour scheme per category.
