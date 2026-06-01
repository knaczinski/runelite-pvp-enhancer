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

Docs created on-demand as features require them.

| File | Purpose | Load when |
|------|---------|-----------|
| (none yet) | | |

## Source Policy

Primary source: https://oldschool.runescape.wiki/
Secondary source: RuneLite API Javadoc (https://static.runelite.net/runelite-api/apidocs/)
All docs must cite sources in frontmatter.
