---
purpose: OSRS action queue priority, same-tick action ordering, eating vs attacking, prayer timing, hit delay, combo eating
scope: AI reference for combat tasks, food/potion timing, prayer decisions. Full depth.
load_when: implementing combat tasks, designing eat-on-low-hp logic, reasoning about same-tick action ordering, debugging "why did player die despite eating"
source: oldschool.runescape.wiki/w/Game_tick + /w/Prayer + /w/Food + osrs-docs.com/docs/mechanics/queues
---

# OSRS Engine — Action Queue Priority

> SOURCE NOTE (S050 audit): the queue priority levels, the tick
> order-of-operations and the ranged/magic hit-delay formula here are
> reverse-engineered (osrs-docs.com + community), NOT on the OSRS wiki —
> the wiki `Game_tick` page only confirms the 600 ms cadence. The
> wiki-verifiable numbers WERE checked: standard-food eat delay = 3 ticks
> and cooked karambwan = 2 ticks (wiki Tick_manipulation), matching the
> Combo Eating section below. Treat the queue/hit-delay internals as
> accurate-but-reverse-engineered, not Jagex-official.

## The Queue — Recap

Each player has ONE queue. Scripts run in priority order:

| Priority | Name   | Interrupt behaviour |
|----------|--------|---------------------|
| 0        | Weak   | Removed by Strong scripts, entity interactions, interface changes |
| 1        | Normal | Skipped when a modal interface is open |
| 2        | Strong | Clears weak scripts AND closes modal interfaces |
| 3        | Soft   | Cannot be interrupted; always executes |

Processing: queue runs until a full iteration passes with no scripts executing.
CRITICAL: if any script sets a delay, ALL non-soft scripts later in that pass are skipped.

## Tick Order of Operations (Relevant Subset)

Per tick, the server processes actions in this fixed order:

```
1. Client input ingested (clicks from previous tick)
2. Active stalls expire
3. Timers decrement
4. Queue scripts execute (in priority order, per-entity)
5. Object and item interactions resolve
6. Movement applied
7. Player/NPC interactions resolved (attacks calculated here)
```

Key implication: **eating and prayer activation (queued actions) happen BEFORE attacks are
calculated at step 7.** Clicking food or a prayer on tick N means the effect lands at step
4 of tick N+1 — before that tick's attacks resolve at step 7.

## Eating Food

eat_priority: Weak (priority 0).
eat_delay: after eating, a 3-tick cooldown before the player can eat again.
  The delay does NOT prevent other actions (movement, attacking, alching).
  It only gates the next food consumption.

combo_eating:
  Some foods bypass the standard eat delay when paired with a "combo food":
    standard_foods: sharks, monkfish, swordfish, dark crabs (start the 3-tick delay)
    combo_foods: karambwan, pineapple pizza (half)
  Rule: one standard food + one combo food can be consumed in the SAME tick.
  Both heals apply. Only one 3-tick delay is incurred (from the standard food).
  The combo food consumes its own 2-tick delay independently.
  Net effect: up to two full heal values in a single tick, then normal delay resumes.

## Drinking Potions

potion_priority: Weak (priority 0), same as eating.
drink_delay: 3-tick delay identical to eating. Shared delay — eating a fish blocks potion
  consumption for 3 ticks and vice versa.
same_tick_rule: player CANNOT eat food AND drink a potion in the same tick if both are
  weak-queue scripts (they share the single queue and block each other).
  Exception: if the potion use is triggered by a different mechanism (e.g. auto-cast), the
  delay behaviour may differ — but this is edge-case PvM content, not standard.

## Attacking (Melee)

attack_priority: Normal (priority 1).
timing: attack calculation runs at step 7 of the tick (entity interaction phase).
melee_hit_delay: 0 ticks. Melee damage is calculated AND applied on the SAME tick.
  There is no window to heal between the calculation and application of melee damage.

## Attacking (Ranged / Magic)

ranged_magic_hit_delay: variable. Damage is CALCULATED at step 7 of tick N but APPLIED
  at step 7 of tick N + delay.
  delay formula: ceil(1 + Chebyshev_distance / 3) ticks for ranged.
  magic: typically 1-5 ticks depending on spell and distance.

hit_delay_window: because the damage has not yet applied, the player CAN eat food on the
  ticks between calculation and application. If they heal above the pending hit's value,
  they survive a hit that would have killed them at the moment of calculation.

tick_eating:
  procedure:
    1. Ranged/magic attack is calculated on tick N (at step 7).
    2. Player eats food on tick N+1 (or any tick before delay expires) to raise HP.
    3. Damage applies on tick N + delay — player now has higher HP and survives.
  limitations:
    - Only works for ranged/magic (melee has 0 delay, no window).
    - Requires the player to KNOW a hit is incoming and eat on the correct tick.
    - Human reaction: skilled PvP players watch projectile launch and eat precisely.
    - Bot relevance: bots cannot reliably know when a delayed hit was calculated.
      A pragmatic approximation: eat when HP drops below threshold; accept that this
      may be one tick late for fast-hitting content.

## Prayer

prayer_priority: Strong (priority 2) — activating or deactivating a prayer closes
  modal interfaces and clears weak scripts.
timing: prayers are active at the START of each tick they are enabled.
drain: prayers drain per tick they are ACTIVE at the start of that tick.
activation_rule: a prayer activated mid-tick (queued) takes effect at the START of the
  NEXT tick. No drain is charged on the tick it is activated.

protection_prayers:
  active_at_start_of_tick → blocks attacks resolved in step 7 of that tick.
  click_to_activate_on_tick_N → protection_active = tick N+1.
  implication: to block an attack on tick N, prayer must already be active BEFORE tick N.
  
prayer_flicking:
  concept: deactivate prayer at end of tick, reactivate at start of next. If timed so
    the prayer is active at step 7 each tick but counted as "just activated" (no drain
    tick), zero drain is incurred while maintaining full protection.
  human_feasibility: requires click within ~20ms window of tick boundary.
  bot_note: not implementable via wall-clock DreamBot API. Do not attempt.

## Same-Tick Priority Summary

When multiple actions are queued for the same tick, the queue runs them in order until
a delay is encountered. After any delay, non-soft scripts are skipped for that iteration.

| Action        | Priority | Notes |
|---------------|----------|-------|
| Eat food      | Weak (0) | Blocked by Strong scripts, entity interactions |
| Drink potion  | Weak (0) | Shares delay with eating |
| Dialogue      | Normal(1)| Skipped if modal open |
| Attack        | Normal(1)| Entity interaction; resolves at step 7 |
| Prayer toggle | Strong(2)| Clears weak queue; closes interfaces |
| Death/TP/obstacle | Soft(3) | Uninterruptible |

Rule of thumb: **eating → attack** is the typical tick ordering for players because
eating is queued and resolves before the attack interaction at step 7.

## DreamBot Implications

| Engine behaviour | DreamBot reality |
|---|---|
| Eating resolves before attacks on same tick | `Inventory.interact("Eat")` is wall-clock; submit it on the tick BEFORE the expected hit. DreamBot cannot know tick boundaries. |
| Combo eating requires two clicks same tick | Two `Inventory.interact()` calls in sequence are NOT guaranteed same tick. Non-deterministic from wall-clock API. |
| Prayer must be active before hit tick | Click prayer activation one tick early; sleepUntil prayer active, then proceed. |
| Hit delay window for tick-eating | Bot cannot observe hit calculation timestamp. Use HP threshold instead: eat when HP < safe threshold regardless of tick. |
| Eat delay blocks next food for 3 ticks | `sleepUntil(() -> !localPlayer.isAnimating(), 2400)` is NOT sufficient — animation end ≠ eat delay end. Use `sleep(1800)` (3 tick nominal) after eating before attempting second food click. |

### Practical Guidance
- Combat eat threshold: `localPlayer.getHp() < safeHpThreshold` → `Inventory.interact("Eat")`.
  Do not try to tick-eat. Eat early, accept the conservatism.
- Prayer: activate protection prayer before entering combat area; do not toggle per-attack.
- Combo eating: bot strategy = eat shark first, then `sleep(100)` then click karambwan.
  Same-tick is not guaranteed but ~300ms window means they often land within 1 tick.
  Acceptable approximation; do not spend engineering effort on perfect tick alignment.
- Potion: drink at start of combat or on a scheduled interval, not reactively.
  Drink delay overlaps eat delay — don't queue both.
