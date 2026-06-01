---
scope: com.knz.pvpenhancer.service
load_when: changing buffering, capping, or tick-flush logic
---

purpose: stateful tick-bucketed event buffer. single source of truth for the overlay.

patterns:
  - TickHistoryService — @Singleton (Guice). plugin + overlay share one instance.
  - pending List accumulates events during a tick; flushTick(n) runs ComboEatMerger.merge then seals into a TickEntry at the front (newest first).
  - each flush assigns the next sequence code (++tickSequence); clear() resets it to 0 so a fresh session starts at "Tick 0001".
  - capped at maxHistory; trim() drops oldest. setMaxHistory clamps to >= 1 and trims now.

constraint:
  - no game-API dependency here — keeps it unit-testable (see TickHistoryServiceTest).
  - all access is on the client thread; no synchronisation.
  - getEntries() returns a copy; never expose the live deque.
