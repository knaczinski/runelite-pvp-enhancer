# PID indicator — feasibility spike

**Question:** can the plugin show whether the local player currently has PID advantage over an
opponent (i.e. is processed first in the game tick)?

## What PID is

PID (player processing order / "player identity") is the per-tick order in which the server
processes players. The player processed first "wins" simultaneous contests in that tick — who
eats first at low HP, whose freeze/spec lands first, who gets the last hit. The order is a
permutation that the server **reshuffles roughly every 100–150 ticks** (and on some events).

## What the RuneLite API exposes

- **No direct PID.** There is no `getPid()` or processing-order accessor.
- `client.getTopLevelWorldView().players()` is indexed by the **player index** (server slot,
  0–2047), which is assigned on spawn and is **not** PID. Comparing indices tells you nothing
  about who acts first this tick.
- Events (`AnimationChanged`, `GraphicChanged`, `HitsplatApplied`, `ProjectileMoved`) are
  dispatched by RuneLite in **player-index order**, not PID order — so "who fired the event
  first this tick" is an artifact of indexing, not real PID. Using it would be misleading.

## What is actually detectable

Only a **probabilistic estimate** from *contested* same-tick outcomes:
- When you and an opponent perform an order-sensitive action on the same tick (both eat at low
  HP, both freeze, trade the killing blow) and the *result* reveals who resolved first, that is
  one observation of the current PID ordering.
- These observations are sparse, only appear in specific contested situations, and the estimate
  is invalidated every time the server reshuffles (~every 100–150 ticks). Confidence stays low.

## Verdict

A **reliable** PID indicator is **not feasible** with the public API. The honest options are:

1. **Drop it** — recommended unless there's appetite for an explicitly-fuzzy tool.
2. **Experimental "PID guess"** — accumulate evidence from contested events, show a low-confidence
   guess (e.g. "PID: you? / them? / unknown") that resets on suspected reshuffle. Clearly labelled
   as an estimate. Meaningful only in sustained 1v1s with frequent contested ticks.

A naive "compare player indices" indicator is worse than nothing (confidently wrong), so it is
explicitly rejected.

## Recommendation

Hold PID as **OPEN / deferred** pending a product decision between option 1 and 2 above. No code
shipped from this spike.
