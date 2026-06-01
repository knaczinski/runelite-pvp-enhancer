---
purpose: project specification. read before any architectural or cross-cutting code task.
scope: root
format: caveman lite. complete spec, not a summary.
---

# PROJECT SPEC — runelite-pvp-enhancer

## Mission

RuneLite plugin for Old School RuneScape PvP. Provides overlays, alerts, and convenience helpers for player-vs-player combat. Improves situational awareness and reaction speed without automating any game action.

## Stack

language: Java 11
build: Gradle with RuneLite plugin-template (openosrs/plugin-template or runelite/runelite)
runtime: RuneLite client (latest stable open-source release)
entry_point: PvpEnhancerPlugin extends Plugin
config: PvpEnhancerConfig extends Config (@ConfigGroup("pvpenhancer"))
events: @Subscribe annotated methods on the RuneLite event bus
injection: @Inject via RuneLite's Guice injector

## Plugin Architecture

plugin_class: PvpEnhancerPlugin — registers overlays and services on startUp(), deregisters on shutDown().
config_class: PvpEnhancerConfig — one @ConfigItem per feature toggle, keybind, or threshold. No runtime mutation of config outside the RuneLite config panel.
overlay_pattern: one Overlay subclass per distinct rendering concern. Each overlay is independently togglable via config.
service_pattern: stateless helpers injected via @Inject. No global singletons outside RuneLite's own managers.
event_pattern: all game state consumed via @Subscribe. No polling threads.

## Feature Set

TBD — backlog items define features incrementally. Update this section as B-items are completed.

## RuneLite API Constraints

never: automate game inputs (mouse clicks, keyboard events) — violates Jagex rules and RuneLite policy
never: read or write memory outside RuneLite's sanctioned plugin API
never: retain overlay or listener registrations after shutDown()
always: use @Subscribe for game events (not manual polling loops)
always: deregister overlays via overlayManager.remove() in shutDown()
always: unsubscribe from event bus by deregistering the plugin (RuneLite handles this automatically on shutDown)
always: gate rendering behind config toggles so users can disable individual overlays

## Threat Model (detection by Jagex)

This plugin is passive overlays only — no input injection, no memory manipulation. Detection risk from the plugin itself is negligible. Document any feature that interacts with game state at all and confirm it uses only official RuneLite API methods.

## Out of Scope

automation of any kind (clicks, keys, prayers, eating)
third-party API integration beyond the official OSRS wiki
multi-instance coordination
bot detection or anti-ban logic
