---
session: S015
title: Prayer split, hit-predict cue, attack timer, taller spec bar (+ earlier batch)
date: 2026-06-09
mode: /grill-me on the uncertain points, then chunked implementation
build: green, 79 tests
---

# S015 — 6-feature combat batch

Design forks resolved via /grill-me: offensive "weapon from prayer" → highlight the inventory
weapon; attack-timer speed → seed table + reset on consume; spec bar → resize native widgets.

## Shipped (chunked, one commit each)

- **Chunk A** — hit-predict spec-combo HP cue (bigger+red when post-hit HP ≤ `hitPredictThreshold`%)
  + `hitPredictionSize`; PK skull sits higher when an overhead-prayer icon is active.
- **Chunk B** — prayer highlighter split: defensive now also boxes the prayer-tab button;
  `offensivePrayerMode` (PRAYER_FROM_WEAPON boxes Piety/Rigour/Augury; WEAPON_FROM_PRAYER boxes an
  inventory weapon via new `WeaponSuggestOverlay`). `PrayerHighlightOverlay` reworked to `setPrayers`.
- **Chunk C** — attack-again countdown (`AttackCooldownService` + `AttackTimerOverlay` + `WeaponSpeeds`
  seed; eat/drink extends; per-scope self/opponents/others) and taller spec bar (`specBarExtraHeight`
  resizes SP_ATTACKBAR + style boxes off a captured base, restorable).
- **Earlier this turn** — combat right-click filter (Walk here + Attack only), hit-predict font size,
  UNDER_WIDGETS layer for head overlays (over native overheads, under the bank UI), ghostify per-group
  show-chat (2D pass kept while 3D hidden).

## Decisions / honesty
- Heal precision: already accurate (real max HP); kept the `~`. OpponentInfo doesn't show exact
  player HP/prayer either — corrected the premise.
- Ghostify "clickable while ghosted": dropped — hiding the model removes it from click/hover (the
  game picks off the rendered model; same limit as Entity Hider).

## API learned
- `InterfaceID.CombatInterface` — SP_ATTACKBAR / SPECIAL_ATTACK / _0.._3 (style boxes); Widget
  setOriginalY/Height + revalidate to resize (re-apply each tick off a captured base = idempotent).
- ComponentID *_PRAYER_TAB (fixed / resizable / bottom-line) for the prayer-tab button; INVENTORY_CONTAINER
  + Widget.getDynamicChildren()/getItemId() for inventory item slots.
- Player.isFriend/isClanMember/isFriendsChatMember NPE on a null name — guard (also fixed the
  ghostify-NPE-kills-onGameTick regression earlier).

## State / NEXT
- Build green, 79 tests. Only B008 open. NEXT: live validation (HT-025) + tune the seed/px guesses
  (WeaponSpeeds durations, spec-bar layout, skull offsets).
