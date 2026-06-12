---
session: S016
title: B030 WidgetOverlay pinning + config reorg + big fix/feature batch (combos, duel arena, PID, spec-bar HUD)
date: 2026-06-12
mode: iterative live-tested with user; /grill-me for the camera design
build: green, 82 tests
---

# S016 — fixes/features batch + B030 fixed-layout-in-resizable landed (camera dropped)

Long live-tested turn. Many small wins + the B030 breakthrough (drive RuneLite's own widget overlays
instead of fighting the relayout). A free-camera "keep character in the guide scene" prototype was
built and then **removed** — too fragile to make exact + tremor-free.

## Shipped (one commit each)

- **Combos** — eat + gear now count over a **2-tick sliding window** (combo eat / switch spread across
  consecutive ticks register, not only same-tick). Fire-once-per-cluster + clear. Tight single-tick
  switch keeps its tier; spread drops one. New `comboPopups` config (Never/In combat/Always) — the
  "combos broken" report was just the in-combat gate while testing out of combat. (+2 tests)
- **Resilience** — combo detection moved EARLY in onGameTick (ahead of all cosmetic/overlay/layout
  steps) + experimental relayout wrapped in try/catch, so a downstream cosmetic NPE can't kill combos
  again (2nd time that bit).
- **Duel/PvP Arena** — hit prediction + combat-state now work where **XP is blocked**: fallback to the
  player's own hitsplat (`Hitsplat.isMine()`) on the opponent, de-duped vs the XP path.
- **Attack timer** — decoupled from `AnimationStyleMap`: any non-eat animation while interacting starts
  the weapon-speed countdown, so weapons with unmapped attack anims (Eclipse atlatl) work.
- **PID** — over-head star + tiny "pid" label over the likely-PID holder (green you / red them / grey
  computing), 1v1-only, shows from fight start; replaces the TOP_CENTER text panel.
- **Hit predict** — `hitPredictAnchor` (over opponent vs over me / heal spot).
- **Healing** — `healNumberStyle` breakdown "65 + 25 = 90" (remote estimated).
- **Ghostify** — Others gains "Can't attack here + idle".
- **Heartbeat** — configurable vignette size + intensity.
- **Spec bar** — the native-resize was abandoned (dump showed the fill layers are auto-sized/
  positioned). New **`SpecBarOverlay`**: a movable HUD bar reading `VarPlayerID.SA_ENERGY`. Removed
  `specBarExtraHeight` + applyCombatTabLayout machinery.
- **Config reorg** — split overloaded "Combat assist" into Screen alerts / Targeting aids / PID /
  Click helpers; renamed everything short; spec bar → "Fixed layout in resizable". keyNames unchanged
  (settings persist).

## B030 — Fixed-layout-in-resizable (the real win)

Direct widget moves (group 161 childs 95/97/96) always lost. **Root cause:** RuneLite's native
draggable `WidgetOverlay`s (`RESIZABLE_MINIMAP_STONES_WIDGET`, `RESIZABLE_VIEWPORT_INVENTORY_PARENT`,
`RESIZABLE_VIEWPORT_CHATBOX_PARENT`) reposition those widgets every frame during overlay render —
after BeforeRender — and the user's Alt-drag saves a position there. Fix: **set each WidgetOverlay's
`preferredLocation`** to the guide-derived target and let RuneLite place the widget. The guide is a
movable DETACHED overlay (Alt-drag, yellow outline); blocks pin to `guideTopLeft + fixedOffset`.
Per-block restore to the user's prior position on unpin/disable.

## Camera — built then removed
Prototyped (grilled design): free-camera mode + focal-point solved via `Perspective.localToCanvas`
Jacobian so the player projects to the guide scene centre. Hit a feedback tremor (eased camera fed
back into the solve) and framing-assumption errors; a damped-feedback version still wasn't good. User
called it: **remove entirely**, keep the rest. Done — no camera manipulation remains.

## API learned (don't relearn)
- Resizable-Classic minimap/inv/chat are wrapped by `WidgetOverlay`s; reposition by setting their
  `preferredLocation` (drive RuneLite, don't fight it). `OverlayManager.getOverlays()` is
  package-private — enumerate via the public `anyMatch(Predicate)` side-effect.
- The chatbox is its own interface (`InterfaceID.CHATBOX` = 162) nested in toplevel slot 96.
- `BeforeRender` is the last hook before draw; still loses to the WidgetOverlay reposition (overlay
  render is later) — hence the preferredLocation approach.
- `Hitsplat.isMine()` for own-damage; `VarPlayerID.SA_ENERGY` = spec %; `InterfaceID.BuffBar` = 651.
- Camera: `setCameraFocalPointX/Y/Z` only take effect in `setCameraMode(1)` (free); that mode + the
  easing make per-frame focal control tremor-prone. `Perspective.localToCanvas` for projection.

## State / NEXT
- Build green, 82 tests. B030 delivered (pinning + draggable guide); camera dropped.
- User live-confirmed: pinning works, chat fix works, combos (in combat), atlatl timer.
- NEXT (live-validate): spec-bar HUD, duel-arena hit predict, PID star, heal breakdown, ghostify idle.
