---
session: S013
title: Bug fixes, combo redesign, overlay moves, dev panel, resize spike
date: 2026-06-03
mode: /grill-me design (8 decisions) + autonomous execution
build: green, 69 tests
---

# S013 — bug fixes + combos + dev panel + overhead spike

## Design (resolved via /grill-me)
Dev panel: central `OverlayDemoService` registry (overlays unchanged), mocks fire on the **local
player**, separate `DevPanel` + 🛠 header button, gated by a **Developer mode** config toggle,
preset buttons per overlay, v1 = floating popups (heal/hitpredict/debuff/combo), catalogue in
`docs/overlays.md`. Combos: full taxonomy replace, "way" = worn slots, spec combo by hitsplat.
Debuff icons: per-type, seconds. Not-attacking: flash opponent hull. Resize: spike first.

## Shipped (execution order: bugs → combos → overlays → dev panel → spike)
- **B022** combat-trigger fix (no combat on mere interaction → kills the false heartbeat) +
  ANY_FIGHT focus = player↔player only (fixes HT-010 teleport-in bug).
- **B023** not-attacking flashes the opponent's hull red↔yellow; tracks real opponent (incl. NPC).
- **B024** combo taxonomy redesign: Godlike/Excellent/Humble switch, Triple eat, Spec combo
  (+ Humble) by hitsplat heuristic (spec varp drop + opponent-hit count), keep COMBO_FAILED.
- **B025** heal + debuff overlays moved beside the HP bar; debuff = wiki icons + seconds;
  multiple debuffs per actor (EnumMap).
- **B026** developer panel + `OverlayDemoService` + `developerMode` toggle + `docs/overlays.md`;
  heal/hitpredict/combo overlays made `@Singleton`.
- **B027** overhead-resize spike → only Vengeance text feasible; `VengeanceTextOverlay` +
  `OverheadScope` + size/scope config. Skull deferred (hacky); prayer-icon + health-bar dropped
  (API-blocked, read-only).

## User HT results folded in
HT-002/003/004/011 PASSED → history. HT-010 PARTIAL → B022 fix, re-test queued. New HT-015..020
added for the S013 features.

## API learned
- Combat must be activity-based; `getInteracting()` is set even on a rejected attack.
- `Actor.getOverheadText()/setOverheadText()` lets us clear + redraw overhead text (veng resize).
- `Player.setSkullIcon(-1)` can hide the skull; overhead prayer icon + health bar are read-only,
  no scale/hide → not resizable.
- `VarPlayer.SPECIAL_ATTACK_PERCENT` + `client.getVarpValue` to detect special use.
- Overlays fed by a shared service must be `@Singleton` or Guice hands out a second instance.

## State
Build green, 69 tests. 6 commits (B022–B027) + bookkeeping. README features rewritten.

## Open / NEXT
- Live validation: HT-010 (re-test), HT-012/013/014 (Phase 4 seeds), HT-015..020 (S013).
- Seed harvest in live fights: spot-anim ids (B020), weapon ids (B021), attack anims (B008).
- Deferred: skull resize (feasible-but-hacky); per-group independent overhead sizes.
- Still open dev item: **B008** (live-data-gated).
