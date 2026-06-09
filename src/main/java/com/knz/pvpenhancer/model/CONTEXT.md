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
  - EventCategory — COMBAT | EATING | GEAR_SWAP | PRAYER | COMBO. drives overlay filter + colour.
  - AttackStyle — MELEE | RANGED | MAGIC | UNKNOWN.
  - TickEntry — int sequence (1-based recording code, shown "Tick 0001") + int tick (raw client tick) + unmodifiable List<CombatEvent>.
  - ComboEvent / ComboResult / ComboType / ComboTier — combo taxonomy: switch tiers (Godlike/Excellent/Humble),
    triple-eat, spec-combo, potlock fail. ComboResult carries label + tier colour.
  - HitSummaryRow / HitDirection — one row per attack for the panel table; HitDirection (OUTGOING/INCOMING/OTHER)
    drives the green/red colouring.
  - HitsplatEvent / HitsplatLabels — hitsplat typing (poison/venom/heal/block/hit...).
  - HealMath — pure remote-heal estimate from health-ratio delta × max HP (max HP resolved in the plugin).
  - XpDamage — Hitpoints-XP drop → predicted outgoing damage (round(xp / 1.333)).
  - Debuff (FREEZE/SNARE/TELEBLOCK: label, colour, wiki icon file) — used by the debuff timers.
  - TickLogFormatter — renders the tick history as plain text for the "Copy log" button.

seed maps (memory-cited, grow from live debug logs):
  - AnimationStyleMap — animation-id → AttackStyle (attack detection).
  - SpotanimDebuffs — spot-anim id → (Debuff, base duration ticks).
  - WeaponStyleMap — equipped weapon item id → AttackStyle (prayer highlighter).
  - WeaponSpeeds — weapon item id → base attack speed (ticks), default 4 (attack-again timer).

constraint:
  - keep classes immutable (final fields, defensive copies). overlay reads them on the client thread.
  - new event type → new CombatEvent subclass + EventCategory (if new) + overlay colour + config toggle.
  - blank, never fabricate: an event leaves a field out when the source can't provide it.
  - animation ids cited to OSRS Wiki. see .ai/game/pvp/pvp-combat-events.md.
