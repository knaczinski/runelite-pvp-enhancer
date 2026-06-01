---
scope: com.knz.pvpenhancer.model
load_when: adding a new event type, changing event display format, or extending the animation map
---

purpose: immutable data classes for the tick history. no game-API calls except the HeadIcon/enum types carried as fields.

patterns:
  - CombatEvent — abstract base. getCategory() + format() (one display line).
  - subclasses: AttackEvent, HitsplatEvent, EatEvent, GearSwapEvent, PrayerEvent. each maps to one EventCategory.
  - AttackEvent.format() omits the "on <prayer>" clause when prayer is null (NPC target / not praying). uses PrayerNames.label.
  - EatEvent — holds List<String> items. 1 = "p ate X"; 2+ = "p ate X + Y (double eat / triple eat / Nx eat)".
  - ComboEatMerger — pure. merges same-player EatEvents within a tick; preserves other events + order. called by the service at flush.
  - PrayerEvent — overhead protection prayer change. "p prayed Protect Magic" / "p prayer off" (icon null). PrayerNames maps HeadIcon → label (shared with AttackEvent).
  - EventCategory — COMBAT | EATING | GEAR_SWAP | PRAYER. drives overlay filter + colour.
  - AttackStyle — MELEE | RANGED | MAGIC | UNKNOWN.
  - TickEntry — int sequence (1-based recording code, shown "Tick 0001") + int tick (raw client tick) + unmodifiable List<CombatEvent>.
  - AnimationStyleMap — static animation-id → AttackStyle. seed table; grow from debug logs of unmapped ids.

constraint:
  - keep classes immutable (final fields, defensive copies). overlay reads them on the client thread.
  - new event type → new CombatEvent subclass + EventCategory (if new) + overlay colour + config toggle.
  - blank, never fabricate: an event leaves a field out when the source can't provide it.
  - animation ids cited to OSRS Wiki. see .ai/game/pvp/pvp-combat-events.md.
