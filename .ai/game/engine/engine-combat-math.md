---
purpose: OSRS accuracy (hit/miss) and max hit formulas — attack roll, defence roll, hit chance, damage RNG
scope: AI reference for combat task gear selection, DPS estimation, gear recommendations. Bot-decision depth.
load_when: implementing combat tasks, selecting gear for a job, estimating kill times, comparing weapon options
source: oldschool.runescape.wiki/w/Damage_per_second + /w/Attack_roll + /w/Defence_roll + /w/Maximum_hit
---

# OSRS Engine — Combat Accuracy and Max Hit

## Overview

Two independent rolls per attack:
1. **Accuracy roll** — did the attack hit or miss?
2. **Damage roll** — if it hit, how much damage?

Both use the same "effective level × equipment bonus" pattern but with different stats.

## Accuracy — Attack Roll

```
attack_roll = effective_attack_level × (equipment_attack_bonus + 64)
```

effective_attack_level:
```
effective_attack_level = floor(attack_level × prayer_multiplier) + stance_bonus + 8
```

| Stance           | Stance bonus |
|------------------|-------------|
| Accurate         | +3          |
| Aggressive       | +0 (strength bonus instead) |
| Defensive        | +0          |
| Controlled       | +1          |

prayer_multipliers (attack):
  Clarity of Thought / Improved Reflexes / Incredible Reflexes: ×1.05 / ×1.10 / ×1.15
  Chivalry: ×1.15 | Piety: ×1.20 | Rigour (ranged): ×1.20 | Augury (magic): ×1.25

equipment_attack_bonus: the relevant attack style bonus from worn gear summed across all
  slots. Check wiki's equipment table for each item's bonus in the attack style used.
  (e.g. for slash: use the "Slash" column; for ranged: use "Ranged attack" column.)

### Effective level — exact form (attack / strength / defence / ranged)

The `floor(level × prayer) + stance + 8` shorthand used throughout this
doc is the NO-boost, NO-void case. Full OSRS form (wiki:
Damage_per_second/Melee):

```
effective = floor( floor((base_level + visible_boost) × prayer_multiplier)
                    + stance_bonus + 8 )
            × void_multiplier   // ×1.1 ONLY with the matching full Void
                                 // set for that style; re-floored after
```

- visible_boost: level shift from potions / other boosts, added BEFORE
  the prayer multiply.
- void_multiplier: 1.1 with the matching Void Knight set for the combat
  style, else 1.0; applied after +8 then re-floored.

For bot decisions with no potions/void these reduce to the simple form.

## Accuracy — Defence Roll

```
defence_roll = effective_defence_level × (equipment_defence_bonus + 64)
```

effective_defence_level:
```
effective_defence_level = floor(defence_level × prayer_multiplier) + defence_stance_bonus + 8
```

| Stance                          | Defence stance bonus |
|---------------------------------|----------------------|
| Defensive                       | +3                   |
| Longrange (ranged)              | +3                   |
| Controlled                      | +1                   |
| Accurate / Aggressive / Rapid   | +0                   |

Note: the defence roll DOES add the defender's combat-style stance bonus
  (source: OSRS wiki Damage_per_second/Melee — "add 3 if defensive stance
  or +1 if controlled"; longrange gives +3 Defence). A prior revision of
  this doc wrongly stated stance was NOT added — corrected in the S050
  wiki audit. This was the single confirmed transcription error.

vs_monster: when the DEFENDER is an NPC the wiki uses a different defence
  roll — `(npc_defence_level + 9) × (npc_style_defence_bonus + 64)` — with
  NO effective-level / prayer / stance term. The effective-level form
  above is the PLAYER-defender (PvP) case.

prayer_multipliers (defence):
  Thick Skin / Rock Skin / Steel Skin: ×1.05 / ×1.10 / ×1.15
  Chivalry: ×1.20 | Piety: ×1.25 | Rigour: ×1.25 | Augury: ×1.25

equipment_defence_bonus: the relevant defence bonus for the attack type used by the attacker.
  (e.g. if attacker uses slash, use target's "Slash defence" bonus.)

## Hit Chance Formula

Two-branch formula:

```
if attack_roll > defence_roll:
    hit_chance = 1 − (defence_roll + 2) / (2 × (attack_roll + 1))

if attack_roll ≤ defence_roll:
    hit_chance = attack_roll / (2 × (defence_roll + 1))
```

result: a value in [0, 1]. Multiply by 100 for percentage.

examples:
  attack_roll=10000, defence_roll=5000 → hit_chance = 1 − 5002/20002 ≈ 75.0%
  attack_roll=5000, defence_roll=10000 → hit_chance = 5000/20002 ≈ 25.0%

## Max Hit Formula

### Melee

```
base_max_hit = 0.5 + effective_strength_level × (strength_bonus + 64) / 640
```

where:
```
effective_strength_level = floor(strength_level × prayer_multiplier) + stance_bonus + 8
```

| Stance     | Stance bonus |
|------------|-------------|
| Aggressive | +3          |
| Controlled | +1          |
| Accurate   | +0          |
| Defensive  | +0          |

prayer_multipliers (strength):
  Burst of Strength / Superhuman Strength / Ultimate Strength: ×1.05 / ×1.10 / ×1.15
  Chivalry: ×1.18 | Piety: ×1.23

strength_bonus: "Melee strength" bonus from worn gear (all slots summed).

final: `max_hit = floor(base_max_hit)`. Damage rolls uniformly in [0, max_hit].

equivalent_form: the wiki writes melee base max hit as
  `floor((effective_strength_level × (strength_bonus + 64) + 320) / 640)` —
  algebraically identical to `floor(0.5 + …/640)` since 320/640 = 0.5.
  Both are correct; this doc keeps the `0.5 +` form for readability.

### Ranged

```
base_max_hit = 0.5 + effective_ranged_level × (ranged_strength_bonus + 64) / 640
```

effective_ranged_level:
```
effective_ranged_level = floor(ranged_level × prayer_multiplier) + stance_bonus + 8
```

ranged stance bonus: Accurate = +3, Rapid = +0, Longrange = +0.
prayer_multipliers: Sharp Eye / Hawk Eye / Eagle Eye: ×1.05/1.10/1.15; Rigour: ×1.23.
ranged_strength_bonus: from ammo + weapon combined (check each item's "Ranged strength" column).

### Magic

Magic max hit is spell-defined (base damage written on each spell).
  Boosted by: magic damage % bonus from gear (e.g. Occult Necklace +10%, Ancestral +6%).
  formula: `max_hit = floor(spell_base_damage × (1 + magic_damage_bonus / 100))`.

## Damage Roll

Once a hit lands, damage is uniformly distributed:
```
damage = random_int(0, max_hit)   // inclusive on both ends
```

Note: a roll of 0 still counts as a "hit" for accuracy — the attack connected but did 0 damage.
  This matters for poison/venom application and for counting attacks in some content.

## Special Attacks

Special attacks use the base formulas with MULTIPLIERS applied to either the attack roll,
the max hit, or both. Multipliers are weapon-specific. Examples:
  Dragon claws: 4-hit mechanic, complex formula. See wiki.
  Armadyl godsword: ×1.25 accuracy, ×1.375 max hit.
  Dragon warhammer: ×1.50 max hit, reduces target defence on hit.

Do not hard-code special attack logic. Reference the wiki for each weapon before implementing.

## DPS Calculation

```
DPS = hit_chance × expected_damage / attack_speed_seconds
```

where:
  expected_damage = max_hit / 2   (uniform distribution mean)
  attack_speed_seconds = attack_speed_ticks × 0.6

example: scimitar (speed 4 ticks), max_hit 30, hit_chance 60%:
  DPS = 0.60 × 15 / 2.4 = 3.75 damage/second

## DreamBot / Bot Implications

| Engine behaviour | Bot relevance |
|---|---|
| Hit chance depends on both attack AND defence rolls | Cannot compute hit chance without knowing target's defence level and gear. Use observed hit rate over ~100 attacks if needed. |
| Max hit determines flee threshold safety margin | Include opponent max hit in HP flee threshold: `fleeHp = maxEnemyHit + comboFoodHeal + 5`. |
| Damage = uniform [0, max_hit] | Don't assume average. Plan for max hit in safety calculations. |
| Prayer multipliers stack multiplicatively with base | Enabling Piety on a high-str account gives disproportionate gains. Factor into job gear config. |
| Ranged strength comes from ammo + weapon | Swapping ammo changes max hit. Track ammo type in job config. |

### Practical Guidance
- Gear selection for a task: maximize attack roll for the target's dominant defence style
  (check wiki for the NPC's defence bonuses — use the style they are weakest to).
- DPS comparison: compute DPS for each candidate weapon setup before choosing.
  A slower weapon with higher max hit may have lower DPS than a faster, lower-max-hit weapon.
- Combat task safety: always store `maxHit` of the target in TaskConfig or fetch it at
  task start. Use it as the floor for HP flee threshold.
- Magic tasks: track magic damage % bonus from each equipped piece; sum at task init to
  compute actual max hit for safety calculations.
