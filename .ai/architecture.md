---
purpose: system architecture map. read before cross-cutting changes.
scope: plugin-wide
format: caveman lite
---

# ARCHITECTURE MAP

## Entry point

PvpEnhancerPlugin (extends Plugin) — the ONLY class coupled to the live Client/ItemManager.
- startUp(): add all overlays to overlayManager, register the ghostify RenderableDrawListener,
  add the sidebar nav button, wire panel callbacks, applyDevMode.
- shutDown(): remove overlays + listener + nav buttons, restore mutated client state
  (skulls, veng text), resetState().
- Owns ALL @Subscribe handlers: GameTick, AnimationChanged, HitsplatApplied, GraphicChanged,
  StatChanged, MenuOptionClicked, MenuEntryAdded, PlayerDespawned, NpcDespawned, ClientTick,
  ConfigChanged. Translates raw events → model objects → pure services. No buffering logic here.

onGameTick orchestration (order matters): updateCombatOpponent → updateCurrentOpponents →
gear/prayer/heal/veng/skull detection → updateGhostify → debuffTracker.tick + prayer highlight →
PID guess → combo flush (emit only in combat) → history.flushTick → heartbeat + panel refresh.

## Config

PvpEnhancerConfig (@ConfigGroup "pvpenhancer"), sections:
Tracking · Combat assist · Overhead displays · Ghostify · Developer.
Tick-history filters + hit-summary caps are hidden=true and edited INLINE in the sidebar panel
(via ConfigManager). Scope enums each carry a matches()/shouldGhost() so the rule lives on the enum.

## Services (com.knz.pvpenhancer.service, all @Singleton; pure / no game-API where possible)

- TickHistoryService — capped newest-first tick buffer (pending list → flushTick seals a TickEntry).
- CombatStateService — isInCombat(tick) = real combat activity within a window (NOT mere interaction).
- AttackHitsplatCorrelator — matches attacks to the hitsplat that lands in the style window.
- HitSummaryService — one row per attack; correlator fills the Hit column. HitDirection per row.
- ComboDetectorService — switch tiers / triple-eat / spec-combo from per-tick gear/eat/attack/hit input.
- DebuffTrackerService — per-actor freeze/snare/TB countdowns (EnumMap), tick() decrements.
- GhostifyService — publishes a player→colour snapshot each tick; shouldDraw() (the hider) matches
  by NAME (robust to the talking re-draw using a different Player instance).
- PidGuessService — experimental contested-hitsplat vote → PID guess + swap warning (1v1 only).
- OverlayDemoService — central registry of dev-panel mock scenarios + clearAll().

## Overlays (com.knz.pvpenhancer.overlay, one per concern; gate on their own config)

Screen/scene: Heartbeat, NotRetaliating (flashes opponent outline), ComboFeedback.
Floating over heads: Heal, HitPredict, DebuffTimer, VengeanceText (ABOVE_WIDGETS), SkullResize
(ABOVE_WIDGETS), GhostifyOutline (ModelOutlineRenderer). Widget-space: PrayerHighlight. Panel-anchored:
PidIndicator (TOP_CENTER). Full catalogue + scopes + mocks: docs/overlays.md.

## Hiding entities (Ghostify)

EntityHider pattern: a Hooks.RenderableDrawListener (deprecated but the API EntityHider uses)
returns false to skip the native model; GhostifyOutlineOverlay draws the contour with
ModelOutlineRenderer (reads geometry from memory regardless of the skipped draw). Local player is
never hidden by us (GPU/117HD conflict) — that needs Entity Hider's "Hide Local Player".

## External data

NPCManager (NPC max HP) + HiscoreManager (player Hitpoints level, async+cached) → accurate remote
heal estimates. WorldType.isPvpWorld + Varbits.IN_WILDERNESS → the "can't attack here" range.

## Events / threading

All game state via @Subscribe; no polling threads. All plugin/overlay/service code runs on the
client thread; the only EDT hop is panel.update via SwingUtilities.invokeLater. Combatant interface
(Combatants.of is the sole instanceof site) keeps event translation unit-testable.
