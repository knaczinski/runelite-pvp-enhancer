---
purpose: project specification. read before any architectural or cross-cutting code task.
scope: root
format: caveman lite. complete spec, not a summary.
---

# PROJECT SPEC — runelite-pvp-enhancer

## Mission

RuneLite plugin for Old School RuneScape PvP. Improves in-fight awareness and post-fight review
without automating any game action (read-only observation + rendering only).

Phase 1 (below) was the tick-accurate combat-history overlay. The plugin has since grown into a
PvP overlay suite — see the README "Features" and `docs/overlays.md` for the current surface:
sidebar tick history + hit-summary table; Combat-assist overlays (heartbeat, not-attacking flash,
hit prediction, prayer highlighter, walk-here, experimental PID guess); Overhead displays (healing
with real max HP, debuff timers, Vengeance/skull resize); **Ghostify** (per-category model
hide + coloured outline); a combo system; and a developer mock panel. The sections below remain the
canonical spec for the tick-history core; newer features are specified in `docs/` + `.ai/backlog-history.md`.

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
| prayer | player X prayed Protect Magic / prayer off (overhead protection prayer change) |

Each tick is shown under a 1-based code ("Tick 0001"), holds N events from any number of
characters, and reads chronologically (oldest tick at the top).

### Data sources

attack_style: infer from animation ID (Actor.getAnimation()). requires animation→style mapping table (see .ai/game/pvp/pvp-combat-events.md).
overhead_prayer: Player.getOverheadIcon() → HeadIcon enum (MELEE/RANGED/MAGIC/SMITE/etc.).
hitsplat: HitsplatApplied event — hitsplat.getAmount(), hitsplat.getHitsplatType().
gear: local player's worn item container (gameval InventoryID.WORN) diff between ticks → changed slot → REAL item id → name via ItemManager. (Not PlayerComposition appearance ids — those decode to wrong names.)
prayer_change: Player.getOverheadIcon() diff per tick per tracked player → PrayerEvent (overhead protection prayer only).
healing: local = exact Hitpoints-skill delta; remote = estimate from getHealthRatio() delta × REAL max
HP (NPCManager + OSRS Hiscores, like Opponent Information; falls back to 99 / toggle accurateRemoteHp),
shown "~". Near the healer's health bar (HealOverlay). Scope (HealDisplayMode): Off/Everyone/Me/Opponents/Self+opp.
eating: MenuOptionClicked with option "Eat"/"Drink"; same-player same-tick consumes merge into a combo eat.
tick_clock: GameTick event — one event = one 600ms server tick. Each tick gets a 1-based display code ("Tick 0001").

### Rendering

overlay_type: OverlayPanel — left sidebar panel.
tick_code: each TickEntry carries a 1-based sequence rendered as "Tick %04d"; raw client tick retained internally.
order: oldest-first (chronological top-to-bottom).
max_history: configurable (default 20 ticks).
filter: toggle per category via PvpEnhancerConfig.
scope: TrackScope — SELF_AND_OPPONENTS (you + players currently fighting you) or EVERYONE (all visible); NPCs when trackNpcs (testing).

## Plugin Architecture

plugin_class: PvpEnhancerPlugin — registers overlays, subscribes to events in startUp(); deregisters in shutDown().
config_class: PvpEnhancerConfig — one @ConfigItem per feature toggle, category filter, and numeric threshold. RuneLite persists automatically.
overlay_pattern: one Overlay subclass per distinct rendering concern. Each independently togglable.
service_pattern: TickHistoryService — stateful, injected. Owns the tick event buffer; updated on GameTick; queried by overlay.
combatant_pattern: detection programs against the Combatant interface, not RuneLite Player/NPC. Combatants.of(Actor) is the sole instanceof site; PlayerCombatant/NpcCombatant adapt the two types; CombatEventFactory is pure (mockable). NPCs report blank for fields they lack (e.g. prayer).
event_pattern: all game state consumed via @Subscribe. No polling threads.

config_note: RuneLite config panel sections — Tracking (trackScope, trackNpcs), Combat assist
(heartbeat, not-attacking, hit prediction, prayer highlighter, walk-here, PID guess), Overhead displays
(healing, debuff timers, veng/skull resize, accurateRemoteHp), Ghostify (per-category when + colour),
Developer (developerMode). The tick-history filters/depth and hit-summary caps are hidden=true and edited
INLINE in the sidebar panel (via ConfigManager). show* are display-only filters; they do not gate
recording (except trackScope/trackNpcs which do). maxHistoryTicks has no minimum.

## RuneLite API Constraints

never: automate game inputs (mouse clicks, keyboard events) — violates Jagex rules and RuneLite policy
never: read or write memory outside RuneLite's sanctioned plugin API
never: retain overlay or listener registrations after shutDown()
always: use @Subscribe for game events, not polling loops
always: deregister overlays via overlayManager.remove() in shutDown()
always: gate rendering behind config toggles

## Out of Scope (permanent)

automation of any kind (input/clicks/keys)
gear suggestion / BIS calculator
multi-instance coordination

Note: the predictive **prayer highlighter** (from the opponent's weapon style) shipped — it is a
read-only display, not a prayer-flick alert/automation. Opponent HP/heal estimates use only
on-screen health ratio + public Hiscores (no hidden state), matching the core Opponent Information.
