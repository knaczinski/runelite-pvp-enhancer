---
scope: root source package com.knz.pvpenhancer
load_when: editing the plugin entry point, config, or wiring event subscriptions
---

purpose: plugin root. holds the entry point and config; wires RuneLite events to the history service.

patterns:
  - PvpEnhancerPlugin — @PluginDescriptor entry point. Owns ALL @Subscribe handlers. Only class coupled to live Client/ItemManager.
  - PvpEnhancerConfig — @ConfigGroup("pvpenhancer"). One @ConfigItem per setting. RuneLite persists automatically.
  - subpackages: model/ (data), service/ (buffer), overlay/ (render).

constraint:
  - event handlers translate raw events into model.CombatEvent and call history.addEvent(). No buffering logic here.
  - onGameTick order: detectGearSwaps() → setMaxHistory() → flushTick(). Gear events must land before the flush that seals the tick.
  - item names via injected ItemManager.getItemComposition(int), NOT Client (Client has no such method on this API version).
  - never automate input. read-only observation only.
