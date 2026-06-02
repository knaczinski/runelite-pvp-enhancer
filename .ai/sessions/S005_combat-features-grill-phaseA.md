---
purpose: isolated session/task file — load alone, not alongside other sessions
format: strict key-value
---

# SESSION S005

session_id: S005
date: 2026-06-01
status: complete
phase: Phase 3 kickoff — design interview + foundation (Phase A)
goal: grill the 4 combat-awareness features; build shared foundation services.

## Context Delta

changed_files:
  - docs/combat-features-design.md (NEW) — all 10 grilled decisions + OSRS mechanics ref
  - .ai/backlog.md — Phase 3 added (B013 foundation, B014 heartbeat, B015 not-retaliating, B016 hit summary, B017 combos)
  - service/CombatStateService.java (NEW) + service/AttackHitsplatCorrelator.java (NEW)
  - PvpEnhancerPlugin.java — inject + feed CombatStateService (interaction per tick; combat activity on local hit/attack)
  - tests: service/CombatStateServiceTest (4), service/AttackHitsplatCorrelatorTest (5)
pending: Phase B (B014/B015), C (B016), D (B017)
blockers: none

## AI Notes

Ran the grill-me interview (10 decisions) before coding. Decisions recorded in
docs/combat-features-design.md. Key ones:
  - Combos: local-player self-feedback; unified "pattern over recent ticks" model; fixed
    recipes (double/triple eat, combo failed via inventory-consumption heuristic [drag-aware],
    offensive swap->attack tiered by equip->attack gap 0/1/2 = perfect/great/good, clean
    switch >=3 worn slots same tick); transient floating popup; single enable/disable.
  - In combat = local hit <=8 ticks OR interacting with a player.
  - Not-retaliating = in combat + getInteracting() off the opponent >=2 ticks.
  - Hit Summary = row per attack, Offen.Pray local-only, Hit via best-effort correlation.
  - Heartbeat = red edge vignette, pulse per tick in combat, constant intensity.
  - Delivery order: A (foundation) -> B (heartbeat+not-retaliating) -> C (hit summary) -> D (combos).

Phase A shipped: CombatStateService (@Singleton, fed by plugin, isInCombat) +
AttackHitsplatCorrelator (pure, best-effort match by target + style window). Build green,
30 tests. Correlator not wired yet (used in Phase C).

B013 done -> backlog-history. B014-B017 remain open.

Pending user action (from S002): delete leftover ~/.runelite/sideloaded-plugins jar before ./gradlew run.

## Handoff

next_action: Phase B — HeartbeatOverlay + NotRetaliatingOverlay using CombatStateService.
load_rules: doc-style.md; docs/combat-features-design.md; .ai/game/ops/runelite-plugin-dev.md
