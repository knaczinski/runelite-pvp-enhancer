---
scope: com.knz.pvpenhancer.model
load_when: adding a new event type, changing event display format, or extending the animation map
---

purpose: immutable data classes for the tick history. no game-API calls except the HeadIcon/enum types carried as fields.

patterns:
  - CombatEvent — abstract base. getCategory() + format() (one display line).
  - subclasses: AttackEvent, HitsplatEvent, EatEvent, GearSwapEvent. each maps to one EventCategory.
  - EventCategory — COMBAT | EATING | GEAR_SWAP. drives overlay filter + colour.
  - AttackStyle — MELEE | RANGED | MAGIC | UNKNOWN.
  - TickEntry — int tick + unmodifiable List<CombatEvent>.
  - AnimationStyleMap — static animation-id → AttackStyle. seed table; grow from debug logs of unmapped ids.

constraint:
  - keep classes immutable (final fields, defensive copies). overlay reads them on the client thread.
  - new event type → new CombatEvent subclass + EventCategory (if new) + overlay colour + config toggle.
  - animation ids cited to OSRS Wiki. see .ai/game/pvp/pvp-combat-events.md.
