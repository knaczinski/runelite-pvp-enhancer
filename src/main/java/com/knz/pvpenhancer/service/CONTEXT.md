---
scope: com.knz.pvpenhancer.service
load_when: changing any stateful/pure service (buffer, combat state, correlation, combos, debuffs, ghostify, PID, hit summary, demos)
---

purpose: @Singleton services the plugin feeds. Pure / no game-API where possible (unit-testable);
the plugin (the only Client-coupled class) translates raw events into calls here.

services:
  - TickHistoryService — capped newest-first tick buffer. pending list accumulates during a tick;
    flushTick(n) runs ComboEatMerger then seals a TickEntry at the front. ++tickSequence ("Tick 0001");
    clear() resets to 0. capped at maxHistory; getEntries() returns a copy.
  - CombatStateService — isInCombat(tick) = REAL combat activity (attack thrown / hit / XP) within the
    window. Mere interaction is intentionally NOT combat (fixed the un-attackable-target false trigger).
    isNotRetaliating(tick) = in combat but not targeting the opponent for ≥2 ticks.
  - AttackHitsplatCorrelator — pure. queue attacks; a hitsplat on the same target within the style window
    (melee 1, ranged/magic 3) claims the earliest; stale expire. Feeds Hit Summary + spec combo.
  - HitSummaryService — bounded rows, one per attack; applyCorrelation fills the Hit column. Each row
    carries a HitDirection (OUTGOING/INCOMING/OTHER) for the green/red panel colouring.
  - ComboDetectorService — pure. switch tiers (Godlike 5+/Excellent 3-4 same tick, Humble 3-4 over 2t),
    triple-eat, spec-combo (ranged hit + special same tick on opponent), potlock fail. Plugin emits results
    ONLY while in combat (banking changes many slots but is out of combat).
  - DebuffTrackerService — Map<Actor, EnumMap<Debuff,ticks>>. apply (merge-max), tick() decrements + drops
    expired, remove(actor) on despawn. Fed from GraphicChanged spot-anims (SpotanimDebuffs seed).
  - GhostifyService — volatile player→colour snapshot (+ name set). shouldDraw() = the hider, matches by
    NAME (robust to the talking re-draw using a different Player instance). getGhosted() for the outline overlay.
  - PidGuessService — experimental. noisy vote from contested same-tick hitsplats → guess LOCAL/OPPONENT/UNKNOWN
    + swap warning on lead flip (tracked across ties via lastDefinite). 1v1 only; dev-panel mock hooks.
  - AttackCooldownService — per-actor wall-clock epoch-ms of when they can attack again (recordAttack from
    weapon speed, recordConsume extends by the 3-tick eat delay, prune/remove). Drives AttackTimerOverlay.
  - OverlayDemoService — central registry of dev-panel mock scenarios (group/label/Runnable) firing on the
    local player; clearAll() wipes transient debuff timers + floating overlays. Backs DevPanel.

constraint:
  - no game-API dependency inside the services — keeps them unit-testable (see *ServiceTest).
  - all access is on the client thread; volatile snapshots are published whole, never mutated after publish.
  - getEntries()/getRows() return copies; never expose the live collections.
