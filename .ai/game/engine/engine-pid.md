---
purpose: Player ID (PID) system — assignment, rotation cadence, PvP and PvM impact
scope: AI reference for combat priority reasoning. Bot-decision depth (no formula detail needed).
load_when: implementing PvP tasks, reasoning about who wins a same-tick exchange, wildy task threat assessment
source: oldschool.runescape.wiki/w/Player_ID + community reverse-engineering (Jagex undocumented)
---

# OSRS Engine — Player ID (PID)

## What is PID

Every player in a game world is assigned a numeric ID — the Player ID (PID).
When two players act on the same game tick in ways that conflict (both attacking each other,
both trying to occupy the same tile outcome), the server resolves priority by PID.

lower_pid_wins: the player with the LOWER PID is processed first within that tick.
  "First" means their action executes before the opponent's in the same tick's resolution phase.

## PID Assignment and Rotation

initial_assignment: when a player logs in, they are assigned a PID from the available pool.
  The pool is not globally sequential — gaps exist from logged-out players.

rotation_cadence:
  main_worlds:   re-randomizes every 100–150 ticks (~60–90 seconds).
  pvp_worlds:    re-randomizes every 40–60 ticks (~24–36 seconds).

re_randomization: on each rotation event, all active players in the world receive new PIDs
  drawn from the available pool. The draw is random — a player may get a lower or higher
  PID than before.

## PvP Impact

same_tick_exchanges: in PvP, both players typically attack on the same tick cycle.
  The player with lower PID has their hit calculated first.

what_this_means_in_practice:
  - Lower PID player's attack hits the opponent BEFORE the opponent's attack lands on them.
  - If the lower PID hit kills the opponent, the opponent's queued attack is cancelled.
  - If neither hit is lethal, both hits land — PID order only matters at the kill threshold.

freezing: if lower PID player freezes the opponent (Ice Barrage etc.) on tick N,
  the opponent cannot move on tick N. Upper PID opponent's movement is blocked.
  Upper PID opponent's attack, however, still resolves (movement and attacks are
  different steps in the tick order).

bridding: advanced PvP players track the PID rotation timer and adapt strategy
  when they know they have disadvantaged PID. Relevant for wildy task threat models.

## PvM Impact

most_pvm: PID is irrelevant for standard PvM. NPCs have their own processing order
  independent of player PID. The PID system specifically governs PLAYER-vs-PLAYER priority.

exceptions:
  - Multi-combat wildy: two players attacking the same NPC — PID determines whose
    attack resolves first if they attack on the same tick. Cosmetic for most purposes
    (both hits still land unless the first kill the NPC).
  - Player-controlled content (Pest Control, Clan Wars): PID matters for player-vs-player
    actions within those minigames.

## Bot Decision Relevance

The bot cannot read or influence PID. It is invisible to the DreamBot API.

wildy_threat_model:
  - Do not assume PID advantage. Treat every same-tick exchange as potentially lower PID
    for the opponent.
  - Flee logic should not rely on "winning" a same-tick hit trade.
  - Conservative threshold: flee when HP drops below (max_hit_of_attacker + combo_food_heal)
    to absorb a worst-case PID-disadvantaged exchange.

rotation_awareness:
  - PID rotates every 100-150 ticks on main worlds. A bot that has been in danger for
    >150 ticks may have swapped from advantaged to disadvantaged PID silently.
  - Implication: if implementing a wildy safespot that relies on tile-blocking (player flag
    blocks NPC pathing), PID does NOT affect this — only movement flags matter there.

### Practical Guidance
- Never design combat logic that assumes PID advantage.
- Flee decisions: use HP absolute threshold, not outcome prediction.
- If a task enters multi-combat wildy: flag it in config; enable aggressive HP monitoring
  and shorter flee threshold to account for potential same-tick trades from multiple opponents.
- PID rotation is not trackable from wall-clock. Treat PID as uniformly random for all
  planning purposes.
