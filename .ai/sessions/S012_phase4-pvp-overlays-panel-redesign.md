---
session: S012
title: Phase 4 PvP overlays + sidebar redesign + UX batch
date: 2026-06-02
mode: autonomous (continue backlog to the end) + user 9-item UX batch
build: green, 69 tests
---

# S012 — Phase 4 PvP overlays + sidebar redesign

## Goal

Finish the Phase 4 backlog (B018–B021) and land the user's 9-item PvP/UX batch on top.

## Shipped

### Phase 4 backlog (all four)
- **B018 Combat focus** — `CombatFocusService` + Hooks `RenderableDrawListener` hide
  non-involved Players/NPCs. Persistence model: engaged actors stamped `tick +
  combatFocusTimeout`, pruned on expiry — survives eats/pauses. Symmetric trigger (you hit OR
  get hit, via HP-xp in onStatChanged). Target never hidden; local always visible. Modes
  OFF/SELF/ANY_FIGHT, `@Range` timeout (16).
- **B019 Hit prediction** — `XpDamage.fromHitpointsXp` + `HitPredictOverlay`; orange predicted
  damage near target before ranged/magic projectiles land. Config `hitPrediction`.
- **B020 Freeze/TB timers** — `DebuffTrackerService` + `DebuffTimerOverlay` + `SpotanimDebuffs`
  seed (ice 361/363/367/369, bind 177/178/179, TB 345). onGraphicChanged matches; unknowns
  debug-logged. Config `DebuffScope` OFF/OPPONENTS/ALL.
- **B021 Prayer highlighter** — `PrayerHighlightOverlay` + `WeaponStyleMap`. Reads target
  weapon via `PlayerComposition.getEquipmentId(KitType.WEAPON)`; finds the protect-prayer
  button by NAME scan over `InterfaceID.Prayerbook.PRAYER1..30`. Config `prayerHighlight`.

### 9-item UX batch (user)
1. Combat focus no longer hides the engaged target.
2. Heal popup raised (+50) clear of the PvP skull.
3. Combat focus triggers on dealing damage too (not only taking it) + configurable timeout.
4. Timeout keeps others visible after they eat (was too short → persistence map).
5. `TrackScope` SELF_AND_OPPONENTS vs EVERYONE for tick history + hit summary.
6. Hit summary = scrollable `JTable`, rows green (you attack) / red (attacked) via `HitDirection`.
7. Tick history in a fixed-height scroll pane.
8. Header ⚙ button opens config via `OverlayMenuClicked(RUNELITE_OVERLAY_CONFIG)` + anchor overlay.
9. Walk-here over Take in combat (`swapPickupInCombat`, onMenuEntryAdded de-prioritise).

## API learned (do not relearn)
- ConfigPlugin resolves the config to open from `overlay.getPlugin()`. To open config from a
  panel button, post `OverlayMenuClicked(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG,...),
  overlay)` where the overlay was built with `new Overlay(this){}` (carries the plugin ref).
  The anchor overlay is never added to the OverlayManager.
- `ComponentID` has NO per-protection-prayer constants. Prayer buttons live in
  `gameval.InterfaceID.Prayerbook.PRAYER1..PRAYER30` (contiguous component ids, generic names).
  Find the right one by scanning + matching `Widget.getName()` ("Protect from Melee", etc.) —
  robust to child reordering.
- `PlayerComposition.getEquipmentId(KitType)` returns the real item id directly (≤0 = empty/kit
  slot); no −512 needed.
- `Actor#getGraphic()` (deprecated) gives the current spot-anim id — fine for debuff detection.

## State
- Build green, 69 tests (+SpotanimDebuffsTest, +WeaponStyleMapTest).
- Commits: panel redesign; B020+B021; (earlier this session) B018, B019, combat-focus fixes,
  tracking scope, walk-here.

## Open / NEXT
- **All dev backlog now closed except B008** (Phase 2, live-data-gated).
- Live validation pending: **HT-010..HT-014** (this session) + HT-001..HT-004 (MVP) +
  earlier heartbeat/hit-summary/combos/healing.
- Seed harvesting via live debug logs: unknown spot-anim ids → B020 map, unknown weapon ids
  → B021 map, unmapped attack anims → B008.
