---
scope: root source package com.knz.pvpenhancer
load_when: editing the plugin entry point, config, or wiring event subscriptions
---

purpose: plugin root. entry point + config + all event wiring; the ONLY class on the live Client.

patterns:
  - PvpEnhancerPlugin — @PluginDescriptor entry point. Owns ALL @Subscribe handlers (GameTick,
    AnimationChanged, HitsplatApplied, GraphicChanged, StatChanged, Menu*, Player/NpcDespawned,
    ClientTick, ConfigChanged). Feeds the pure services. Holds a Hooks.RenderableDrawListener that
    delegates to GhostifyService (the entity "hider").
  - PvpEnhancerConfig — @ConfigGroup("pvpenhancer"). Sections: Tracking · Combat assist · Overhead
    displays · Ghostify · Developer. Tick-history filters + hit-summary caps are hidden=true, edited
    INLINE in the sidebar panel. Scope enums carry matches()/shouldGhost() so the rule lives on the enum.
  - subpackages: combatant/ (Player/NPC seam), model/ (data + seed maps), service/ (stateful/pure),
    overlay/ (render), panel/ (sidebar + dev panel).

constraint:
  - event handlers wrap actors via Combatants.of, gate on isTracked (track* config), translate to
    model.CombatEvent, call history.addEvent(). No buffering logic here.
  - show* config are DISPLAY-ONLY filters; handlers do NOT gate on show*. Combos are emitted only while
    in combat (so banking's bulk slot change doesn't register as a switch).
  - onGameTick order matters — see .ai/architecture.md. Detected events land before flushTick seals the tick.
  - gear: diff the local player's WORN container (gameval InventoryID.WORN) for REAL item ids. item names
    via injected ItemManager.getItemComposition(int), NOT Client. prayer: diff getOverheadIcon() via
    getTopLevelWorldView().players().
  - mutating other players' client state (setSkullIcon, setOverheadText) must be re-applied each ClientTick
    (the client re-renders natives between game ticks → flicker) and RESTORED on scope-exit/shutdown.
  - build uses -Xlint:deprecation; keep warning-clean (gameval + WorldView over deprecated APIs).
  - never automate input. read-only observation only (RenderableDrawListener hide is EntityHider's pattern).
