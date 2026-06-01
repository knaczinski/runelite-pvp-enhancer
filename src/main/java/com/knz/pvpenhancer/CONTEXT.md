---
scope: root source package com.knz.pvpenhancer
load_when: editing the plugin entry point, config, or wiring event subscriptions
---

purpose: plugin root. holds the entry point and config; wires RuneLite events to the history service.

patterns:
  - PvpEnhancerPlugin — @PluginDescriptor entry point. Owns ALL @Subscribe handlers. Only class coupled to live Client/ItemManager.
  - PvpEnhancerConfig — @ConfigGroup("pvpenhancer"), two @ConfigSection: Tracking (functionality) + Overlay (display). RuneLite persists automatically.
  - subpackages: combatant/ (Player/NPC interface seam), model/ (data), service/ (buffer), overlay/ (render).

constraint:
  - event handlers wrap actors via Combatants.of, gate on isTracked (track* config), translate to model.CombatEvent, call history.addEvent(). No buffering logic here.
  - show* config are DISPLAY-ONLY (overlay filters). Handlers do NOT gate on show* — everything tracked is recorded.
  - onGameTick order: detectGearSwaps() → detectPrayerChanges() → setMaxHistory() → flushTick(). Detected events must land before the flush that seals the tick.
  - gear: diff the local player's WORN item container (gameval InventoryID.WORN) for REAL item ids → correct names. NOT PlayerComposition appearance-id decode (wrong names).
  - prayer: diff Player.getOverheadIcon() per tracked player via client.getTopLevelWorldView().players() (NOT deprecated getPlayers()).
  - item names via injected ItemManager.getItemComposition(int), NOT Client.
  - build uses -Xlint:deprecation; keep the tree warning-clean (prefer gameval + WorldView APIs over deprecated ones).
  - never automate input. read-only observation only.
