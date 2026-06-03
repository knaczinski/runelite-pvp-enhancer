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

---

# PID guess — extended research (S013, experimental build)

Decision (user): build the **experimental "PID guess" + swap warning**, 1v1 only, only for fights
the local player is in, with a dev-panel mock. This section catalogues every moment where PID
*could* be inferred and how observable each is.

## Catalogue of PID-revealing moments

| # | Moment | Reveals PID? | API-observable? | Verdict |
|---|--------|-------------|-----------------|---------|
| 1 | **Kill race** — both deal lethal damage the same tick; the first-processed player's hit kills the other before theirs resolves, so the survivor had PID | **Yes, definitively** | Yes (you see who died / `Actor.isDead`, HP→0) | **Strong** but rare (fight end only) |
| 2 | **Same-tick hitsplat application order** — game logic applies the two players' hits in PID order | Partially | `HitsplatApplied` event order — **only reflects PID if the server writes per-player updates in PID order**; may instead be scene/index order | **Weak / uncertain** — primary live signal, treat as a noisy vote |
| 3 | **Freeze vs step** — A freezes B the same tick B steps; if B moved one tile, B was processed first | Yes | Partially (compare B's tile before/after) | Situational, noisy |
| 4 | **Player array index** (`players()` order) | No — index is the spawn slot, fixed; ≠ PID | Yes | **Reject** (confidently wrong, never changes) |
| 5 | **PID reshuffle timer** (~100–150 ticks) | Timing only | No (phase unknown) | Can't time blindly |

## Chosen approach (experimental)

- **Activation:** only when it is a clean **1v1 involving the local player** — exactly one entry in
  the opponent set and you are engaged with that player. Otherwise the indicator is inactive.
- **Evidence:** moment #2 — on a tick where **both** the local player and the single opponent take a
  hitsplat, record which of the two was applied first (a "contest"). Tally contests; the side that
  leads is the current guess. This is explicitly a **noisy vote** (see verdict above), surfaced as
  low/again-rising confidence, never as certainty.
- **Swap warning:** when the leading guess **flips** (the other side overtakes), flash a "PID swap?"
  warning — the server likely reshuffled. The tally decays/clears when the opponent changes or the
  fight ends.
- **Honesty:** the indicator is labelled experimental; the dev-panel mock drives the UI directly so
  it can be reviewed without relying on live signal quality.

## Not attempted

Reading PID directly (impossible), index-based guessing (#4, rejected), and blind reshuffle timing
(#5). Kill-race (#1) is definitive but terminal, so it is not used for a *live* indicator.
