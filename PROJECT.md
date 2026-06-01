---
purpose: project specification. read before any architectural or cross-cutting code task.
scope: root
format: caveman lite. complete spec, not a summary.
---

# PROJECT SPEC — runelite-pvp-enhancer

## Mission

RuneLite plugin for Old School RuneScape PvP. Initial scope: tick-accurate combat history overlay. Shows a chronological log of events during a fight, separated by category (combat, eating, gear swap). Improves post-fight review and in-fight awareness without automating any game action.

## Stack

language: Java 11
build: Gradle with RuneLite plugin-template (openosrs/plugin-template or runelite/runelite)
runtime: RuneLite client (latest stable open-source release)
entry_point: PvpEnhancerPlugin extends Plugin
config: PvpEnhancerConfig extends Config (@ConfigGroup("pvpenhancer"))
events: @Subscribe annotated methods on the RuneLite event bus
injection: @Inject via RuneLite's Guice injector

## Feature Set — Phase 1: Tick History

### What it does

Records game events tick-by-tick during combat. Displays a scrollable history panel in the overlay, categorised per tick:

| Category | Events tracked |
|---|---|
| combat | player X attacked player Y with Z (melee/range/mage), on prayer P, hit N |
| eating | player X ate item Y; same-player same-tick consumes merge → "ate A + B (double eat)" |
| gear swap | player X equipped item Y (slot Z) |

Each tick is shown under a 1-based code ("Tick 0001"), holds N events from any number of
characters, and reads chronologically (oldest tick at the top).

### Data sources

attack_style: infer from animation ID (Actor.getAnimation()). requires animation→style mapping table (see .ai/game/pvp/pvp-combat-events.md).
overhead_prayer: Player.getOverheadIcon() → HeadIcon enum (MELEE/RANGED/MAGIC/SMITE/etc.).
hitsplat: HitsplatApplied event — hitsplat.getAmount(), hitsplat.getHitsplatType().
gear: PlayerComposition.getEquipmentIds() diff between ticks → changed slot → item.
eating: MenuOptionClicked with option "Eat" or animation 829 (eating animation).
tick_clock: GameTick event — one event = one 600ms server tick.

### Rendering

overlay_type: OverlayPanel — left sidebar panel.
tick_code: each TickEntry carries a 1-based sequence rendered as "Tick %04d"; raw client tick retained internally.
order: oldest-first (chronological top-to-bottom).
max_history: configurable (default 20 ticks).
filter: toggle per category via PvpEnhancerConfig.
scope: local player always; all visible players when trackOpponents; NPCs when trackNpcs (testing).

## Plugin Architecture

plugin_class: PvpEnhancerPlugin — registers overlays, subscribes to events in startUp(); deregisters in shutDown().
config_class: PvpEnhancerConfig — one @ConfigItem per feature toggle, category filter, and numeric threshold. RuneLite persists automatically.
overlay_pattern: one Overlay subclass per distinct rendering concern. Each independently togglable.
service_pattern: TickHistoryService — stateful, injected. Owns the tick event buffer; updated on GameTick; queried by overlay.
combatant_pattern: detection programs against the Combatant interface, not RuneLite Player/NPC. Combatants.of(Actor) is the sole instanceof site; PlayerCombatant/NpcCombatant adapt the two types; CombatEventFactory is pure (mockable). NPCs report blank for fields they lack (e.g. prayer).
event_pattern: all game state consumed via @Subscribe. No polling threads.

config_note: trackNpcs (default off) records NPC combat events — a testing aid so detection can be exercised without a second player.

## RuneLite API Constraints

never: automate game inputs (mouse clicks, keyboard events) — violates Jagex rules and RuneLite policy
never: read or write memory outside RuneLite's sanctioned plugin API
never: retain overlay or listener registrations after shutDown()
always: use @Subscribe for game events, not polling loops
always: deregister overlays via overlayManager.remove() in shutDown()
always: gate rendering behind config toggles

## Out of Scope (v1)

automation of any kind
prayer flick helper / alerts
gear suggestion / BIS calculator
opponent stat tracking beyond what is visible on-screen
multi-instance coordination
