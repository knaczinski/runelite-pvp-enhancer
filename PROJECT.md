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
| eating | player X ate item Y |
| gear swap | player X equipped item Y (slot Z) |

### Data sources

attack_style: infer from animation ID (Actor.getAnimation()). requires animation→style mapping table (see .ai/game/pvp/pvp-combat-events.md).
overhead_prayer: Player.getOverheadIcon() → HeadIcon enum (MELEE/RANGED/MAGIC/SMITE/etc.).
hitsplat: HitsplatApplied event — hitsplat.getAmount(), hitsplat.getHitsplatType().
gear: PlayerComposition.getEquipmentIds() diff between ticks → changed slot → item.
eating: MenuOptionClicked with option "Eat" or animation 829 (eating animation).
tick_clock: GameTick event — one event = one 600ms server tick.

### Rendering

overlay_type: OverlayPanel — left sidebar panel.
max_history: configurable (default 20 ticks).
filter: toggle per category via PvpEnhancerConfig.
scope: track local player + all visible players in combat (or local player only — TBD in B005).

## Plugin Architecture

plugin_class: PvpEnhancerPlugin — registers overlays, subscribes to events in startUp(); deregisters in shutDown().
config_class: PvpEnhancerConfig — one @ConfigItem per feature toggle, category filter, and numeric threshold. RuneLite persists automatically.
overlay_pattern: one Overlay subclass per distinct rendering concern. Each independently togglable.
service_pattern: TickHistoryService — stateful, injected. Owns the tick event buffer; updated on GameTick; queried by overlay.
event_pattern: all game state consumed via @Subscribe. No polling threads.

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
