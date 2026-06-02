---
scope: com.knz.pvpenhancer.service
load_when: changing buffering, capping, or tick-flush logic
---

purpose: stateful services. tick-bucketed event buffer + combat-state + attack/hitsplat correlation.

patterns:
  - TickHistoryService — @Singleton (Guice). plugin + overlay share one instance.
  - CombatStateService — @Singleton. isInCombat(tick) = recent combat activity (<=8 ticks) OR interacting with a player. Fed by the plugin; no game state of its own (testable). Used by heartbeat + not-retaliating overlays.
  - AttackHitsplatCorrelator — pure. queue attacks; a hitsplat on the same target within the style window (melee 1, ranged/magic 3) claims the earliest; stale expire. Used by Hit Summary + combos.
  - pending List accumulates events during a tick; flushTick(n) runs ComboEatMerger.merge then seals into a TickEntry at the front (newest first).
  - each flush assigns the next sequence code (++tickSequence); clear() resets it to 0 so a fresh session starts at "Tick 0001".
  - capped at maxHistory; trim() drops oldest. setMaxHistory clamps to >= 1 and trims now.

constraint:
  - no game-API dependency here — keeps it unit-testable (see TickHistoryServiceTest).
  - all access is on the client thread; no synchronisation.
  - getEntries() returns a copy; never expose the live deque.
