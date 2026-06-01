---
purpose: system architecture map. read before cross-cutting changes.
scope: plugin-wide
format: caveman lite
---

# ARCHITECTURE MAP

## Entry point

PvpEnhancerPlugin (extends Plugin)
  startUp()   — register overlays, subscribe to event bus (RuneLite handles @Subscribe auto-registration)
  shutDown()  — deregister overlays via overlayManager.remove(), clean up any cached state

## Config

PvpEnhancerConfig (extends Config)
  @ConfigGroup("pvpenhancer")
  one @ConfigItem per feature toggle, keybind, or numeric threshold

## Overlays

one Overlay subclass per distinct rendering concern.
registered in startUp() → overlayManager.add(overlay)
deregistered in shutDown() → overlayManager.remove(overlay)
each overlay checks its own config toggle before rendering.

## Events

all game state consumed via @Subscribe on PvpEnhancerPlugin (or injected service classes).
no polling threads or manual timers.
typical events: GameTick, InteractingChanged, AnimationChanged, HitsplatApplied, PlayerDespawned.

## Utilities

stateless helpers in util/. injected via @Inject where needed.
no shared mutable state outside RuneLite-provided managers.

## Dependency graph (target)

PvpEnhancerPlugin
  └─ overlayManager        (RuneLite provided)
  └─ client                (RuneLite provided)
  └─ config                (PvpEnhancerConfig)
  └─ [overlay classes]     (one per concern)
  └─ [util helpers]        (stateless)
