---
purpose: master index for all AI game-mechanics documentation. Load this first; pull only the doc(s) relevant to the current task.
scope: root index for .ai/game/
format: structured reference. each entry: file, one-line purpose, load_when trigger.
---

# Game Mechanics — AI Documentation Index

All docs in this directory are AI-facing references for OSRS game engine mechanics and the RuneLite plugin API.
They are NOT player guides — they are technical references with RuneLite API implications.

Load strategy: read THIS index. Load only the specific doc(s) matching the task.
Do not bulk-load all docs — each is self-contained and context-budget-aware.

## ops/ — Plugin Development & Operational Patterns

| File | Purpose | Load when |
|------|---------|-----------|
| [runelite-plugin-dev.md](ops/runelite-plugin-dev.md) | Full RuneLite plugin reference: annotations, lifecycle, overlays, config, key API classes, build setup | implementing any plugin class, overlay, config, event subscriber, or setting up build/IntelliJ |

## pvp/ — PvP-Specific Mechanics

| File | Purpose | Load when |
|------|---------|-----------|
| [pvp-combat-events.md](pvp/pvp-combat-events.md) | How to detect attacks (animation→style), hitsplats, overhead prayers, eating, gear swaps via RuneLite API; animation ID seed table | implementing B002–B004, any combat detection logic, extending AnimationStyleMap |

## engine/ — Core Engine Mechanics

Ported from knz-multi-tasker. These describe platform-agnostic OSRS engine mechanics —
any DreamBot API references are illustrative; the mechanics apply equally under RuneLite.

| File | Purpose | Load when |
|------|---------|-----------|
| [engine-ticks.md](engine/engine-ticks.md) | 600ms tick cadence, action queue, tick manipulation | reasoning about tick-aligned detection, attack→hitsplat tick offsets, the GameTick flush model, B010 correlation |
| [engine-action-priority.md](engine/engine-action-priority.md) | Eating vs attacking timing, prayer activation, hit delay, combo eating, queue priority | combat event timing, eat-tick reasoning, prayer-flick detection, B009/B010 |
| [engine-pid.md](engine/engine-pid.md) | Player ID assignment, re-randomization cadence, PvP priority impact | same-tick exchange reasoning, who-hit-first attribution, PvP threat ordering |
| [engine-combat-math.md](engine/engine-combat-math.md) | Attack/defence roll formulas, hit chance, max hit, DPS | interpreting hitsplat amounts, max-hit context, damage analytics |

## Source Policy

Primary source: https://oldschool.runescape.wiki/
Secondary source: RuneLite API Javadoc (https://static.runelite.net/runelite-api/apidocs/)
All docs must cite sources in frontmatter.
