---
purpose: OSRS game tick system — cadence, server processing model, action queuing, tick manipulation
scope: AI reference for all timing-sensitive bot decisions. Full technical depth.
load_when: designing tasks that gate on animations, writing sleepUntil conditions, implementing tick-manipulation skilling, reviewing any code that assumes sub-600ms server state changes
source: oldschool.runescape.wiki/w/Game_tick + /w/Tick_manipulation + osrs-docs.com/docs/mechanics/queues
---

# OSRS Engine — Game Tick System

## Core Cadence

tick_duration: 600 ms (nominal). Observed: 606–618 ms depending on server load.
server_rate: ~100 ticks/min nominal.
scope: ALL game events are tick-aligned — damage, movement, experience, spawns, animations.
variation: tick duration is constant within a server per tick; more players → longer tick; the client
  cannot know or predict tick boundaries.

> SOURCE NOTE (S050 audit): the OSRS wiki's `Game_tick` page documents
> only the 600 ms cadence and that all server-processed actions are
> tick-aligned (verified). The per-tick order-of-operations, the
> four-level script queue, and the arrive/normal delay model below are
> NOT on the OSRS wiki — they come from osrs-docs.com + community
> reverse-engineering (secondary source, cited in frontmatter). Treat the
> queue internals as accurate-but-reverse-engineered, not Jagex-official.

## What Happens Each Tick (Order of Operations)

Per tick the server processes in this sequence:
1. Client input processed (clicks, keypresses from previous tick window)
2. For each entity (NPC then player):
   a. Active stalls expire
   b. Timers decrement
   c. Queue scripts execute (see Queue System below)
   d. Object/item interactions
   e. Movement applied
   f. Player/NPC interactions resolved

Key implication: a click submitted during tick N is processed at step 1 of tick N+1.
The player never acts "instantly" — there is always at minimum one tick of latency.

## The Action Queue

Every player has ONE queue with FOUR script priority levels:

| Priority | Name   | Behaviour |
|----------|--------|-----------|
| 0        | Weak   | Removed by strong scripts or any interruption (entity interact, interface change) |
| 1        | Normal | Skipped when a modal interface is open |
| 2        | Strong | Removes all weak scripts AND closes modal interfaces before executing |
| 3        | Soft   | Cannot be interrupted; always executes regardless of delays or open interfaces |

Processing rule: queue runs scripts in a loop until no scripts execute in a complete iteration.
CRITICAL: if ANY script sets a delay, ALL non-soft scripts after it in that loop are skipped.
A script queued BY another script can execute no earlier than the FOLLOWING tick.

### Assigned Priorities (Notable)
- Damage application: Strong
- Fletching/skilling click: Weak
- Dialogue: Normal

NPCs use a single simpler queue without priority levels.

## Delay System

Two types of delay:

### Arrive Delay
- Triggers for one tick when an entity moves.
- Delays the entity for that tick if they moved this or the last tick.
- Purpose: synchronises NPC animations with their tile movement.
- Bot relevance: do not expect an NPC to be "ready" for interaction on the same tick it walks.

### Normal Delay
- Pauses queue execution for N ticks. Blocks most interruptions.
- Used for: teleportation, cutscenes, death, agility obstacles.
- While under normal delay:
  - Timers continue but pause at zero.
  - Entity interactions do not process.
  - Interface clicks may register but are ignored (unequipping gear, etc.).
  - Pre-set movement routes continue.
  - Queue does not process.

## Tick Manipulation

tick_manipulation: intentionally interacting with the tick system to perform actions faster than
their nominal cooldowns would suggest.

### Core Concept
Most skilling actions have a "skill delay" — a cooldown between gatherings.
By queuing a SECOND action during that cooldown in a way that resets or bypasses the delay,
the effective skill rate increases.

### Common Examples

**3-tick gathering (fishing, woodcutting, mining)** — figures from the
OSRS wiki Tick_manipulation page (S050 audit):
- Base skill-delay: barbarian/fly fishing = 5 ticks; woodcutting = 4 ticks;
  mining varies by pickaxe (e.g. adamant ≈ 4 ticks).
- Trick: every 3 ticks click an item producing an animation interrupt
  (wiki examples: a herb on swamp tar, pestle/mortar, eating karambwan),
  then immediately re-click the resource — this resets the skill timer to
  3 ticks.
- Result (wiki): barbarian/fly fishing 5→3 ticks ≈ **+67% xp/hr**; mining
  adamant 4→3 ≈ **+33%**. The gain scales with how far the base delay
  exceeds 3 (not a flat "~25-40%").

**1.5-tick / 2-tick fletching**
- Fletching with a knife on logs is normally ~3 ticks per cut.
- Trick: click log+knife, then on the next tick an action with a 1-tick
  animation (e.g. drop), then re-click — effectively ~2 ticks per cut.
- Result: roughly +50% xp/hr. NOTE: the S050 wiki audit did not find the
  exact fletching cadence on the Tick_manipulation page section fetched —
  treat this figure as approximate (the woodcutting 1.5-tick variant the
  wiki does describe yields "at most two rolls every three ticks").

**Tick-eating**
- Melee damage is calculated and applied on the SAME tick.
- Ranged/magic damage is calculated on tick N but applied on tick N+1 (or later, depending on
  Chebyshev distance). This "hit delay" creates a window.
- By eating food on the tick AFTER the attack was calculated but BEFORE damage applies,
  the player heals first, surviving a hit that would otherwise kill them.
- Only works for ranged/magic (hit delay > 0). Melee cannot be tick-eaten.
- Bot relevance: flee/eat decisions must account for hit delay when assessing lethality.

**Combo eating**
- Certain foods ("combo foods": karambwan, dark crabs vs. regular food) do NOT share eat delay.
- Click regular food + combo food in the SAME tick: both consumed, both healing values apply.
- The normal 3-tick eat delay applies AFTER, but only one delay is incurred for two heals.
- Bot relevance: combat tasks requiring survival under heavy damage should chain food clicks.

**1-tick prayer flicking**
- Prayers active at the START of a tick protect attacks in that tick.
- Prayers do not drain on the tick they are activated.
- By deactivating and reactivating a protection prayer every tick precisely, the prayer is active
  to block attacks but the game treats each re-activation as "just turned on" → zero drain.
- Extremely click-intensive; requires frame-perfect input.
- Bot relevance: implementing this correctly would require tick-aligned input hooks not available
  in DreamBot's wall-clock API — do not attempt unless using tick-counting infrastructure.

## DreamBot Implications

CRITICAL — DreamBot operates on WALL-CLOCK TIME, not tick boundaries.

| Engine behaviour | DreamBot reality |
|---|---|
| Server processes input at start of next tick | `sleepUntil(condition, timeout)` polls condition every ~50ms — fires as soon as the condition becomes true, NOT at a tick boundary |
| Action delay is measured in ticks (600ms units) | `sleep(N)` uses milliseconds; N ticks = N * 600ms. But actual tick length varies 600–618ms, so `sleep(600)` may occasionally be one tick short on loaded servers |
| Combo-eating requires two clicks in same tick | Two sequential `Inventory.interact()` calls are NOT guaranteed same tick — network+server latency makes this non-deterministic from the bot side |
| Tick manipulation requires precise tick timing | The bot cannot know which tick boundary it's on. Tick-manipulation skilling (3-tick fishing etc.) is unreliable via standard DreamBot API without an explicit tick-counting listener |

### Practical Guidance
- Use `sleepUntil(() -> animationFinished, 2400)` (4 tick max) for animation-gated actions,
  not `sleep(1200)`. Conditions are more robust than fixed sleeps.
- When emitting EventLog on action completion, note the wall-clock timestamp and expected tick
  duration so drift can be detected over many iterations.
- Do not attempt tick-manipulation techniques in the bot; play at nominal rates. The antiban
  value of consistent nominal timing outweighs the small XP loss.
