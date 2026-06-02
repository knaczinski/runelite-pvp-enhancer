---
purpose: isolated session/task file — load alone, not alongside other sessions
format: strict key-value
---

# SESSION S006

session_id: S006
date: 2026-06-01
status: complete
phase: Phase 3 — Phases B + C + D (heartbeat, not-retaliating, hit summary, combos)
goal: implement all four designed combat-awareness features.

## Context Delta

changed_files:
  - model/EventCategory.java (+COMBO)
  - model/CombatEvent.java (+getColor() optional override)
  - model/ComboType.java, ComboTier.java, ComboResult.java, ComboEvent.java, HitSummaryRow.java (all NEW)
  - service/CombatStateService.java (+setEngaged, +isNotRetaliating, +lastEngagedTick)
  - service/TickHistoryService.java (+getLastSequence())
  - service/HitSummaryService.java, ComboDetectorService.java (NEW)
  - overlay/HeartbeatOverlay.java, NotRetaliatingOverlay.java, HitSummaryOverlay.java,
    ComboFeedbackOverlay.java (all NEW)
  - overlay/TickHistoryOverlay.java (use event.getColor() if non-null; COMBO category filter)
  - PvpEnhancerConfig.java (6 sections: Tracking/Overlay/Heartbeat/Indicators/HitSummary/Combos)
  - PvpEnhancerPlugin.java (full rewire: 5 overlays, 5 services, all detection logic)
  - tests: CombatStateServiceTest (+isNotRetaliating), ComboDetectorServiceTest (10),
    HitSummaryServiceTest (4), AttackHitsplatCorrelatorTest (5) — all pass
pending: live validation HT-001..HT-004 + B014..B017 (heartbeat, not-retaliating, hit summary, combos)
blockers: none

## AI Notes

Implemented Phases B, C, D from the S005 grilled design:

PHASE B (heartbeat + not-retaliating):
  - HeartbeatOverlay: red edge vignette, exponential decay over 600ms tick window,
    driven by CombatStateService.isInCombat(). Constant intensity. Plugin calls recordTick().
  - NotRetaliatingOverlay: panel at ABOVE_CHATBOX_RIGHT; shows "NOT ATTACKING" when
    CombatStateService.isNotRetaliating(). CombatStateService extended: setEngaged(bool, tick)
    stamps lastEngagedTick; isNotRetaliating = inCombat && (tick - lastEngagedTick >= 2).

PHASE C (hit summary):
  - HitSummaryService: bounded rows (default 25), newest-first. addAttack() + applyCorrelation().
  - HitSummaryOverlay: OverlayPanel at TOP_RIGHT, table Tick·Player·OffPray·Atk·Target·TgtPray·Hit.
  - Plugin wires AttackHitsplatCorrelator: on attack → correlator.recordAttack(); on hitsplat →
    correlator.recordHitsplat() → applyCorrelation() if matched.
  - Offensive prayer (Piety/Rigour/Augury/Chivalry) snapped local-player only via
    isPrayerActive(@SuppressWarnings deprecation — no API replacement available yet).

PHASE D (combos):
  - ComboDetectorService (pure, @Singleton): eat-combo detection (2/3+ eats same tick),
    potlock detection (same item+qty next tick), clean switch (>=3 gear swaps), offensive swap
    tier by equip→attack gap (0=PERFECT/1=GREAT/2=GOOD). flush() returns List<ComboResult>.
  - ComboFeedbackOverlay: transient text popup at viewport/3 height, fades 1.5s, tier colors.
  - ComboEvent extends CombatEvent with getColor() override (tier color → overrides default).
  - Config: single "Enable combos" toggle in Combos section.

Deprecation cleanup in this session:
  - OverlayPriority removed (use float priorities instead; setPriority calls dropped)
  - getActionParam()/getWidgetId() → getParam0()/getParam1()
  - setInteractingWithPlayer() → setEngaged() (old method kept @Deprecated for test compat)
  - isPrayerActive(Prayer) @SuppressWarnings (no replacement available in API)

Build green, -Xlint:deprecation clean, 46 tests all pass.

## Handoff

next_action: live validation. Delete ~/.runelite/sideloaded-plugins/pvp-enhancer-1.0.0.jar
  (still locked if client open), then ./gradlew run → single entry → validate all features.
  Start with HT items: heartbeat pulses per tick, not-retaliating warning, hit summary rows,
  double/triple eat popup, swap timing tier.
  Then B008 (grow AnimationStyleMap from live data).
load_rules: doc-style.md; docs/combat-features-design.md; .ai/game/pvp/pvp-combat-events.md
