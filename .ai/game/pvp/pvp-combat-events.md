---
purpose: OSRS PvP combat event reference — how to detect attacks, prayers, hitsplats, eating, and gear swaps via the RuneLite API.
scope: AI reference for B002–B004 and any future PvP detection work
load_when: implementing combat event detection, animation-to-style mapping, hitsplat handling, or gear change tracking
sources:
  - https://oldschool.runescape.wiki/
  - https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/package-summary.html
  - https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Actor.html
  - https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Player.html
  - https://static.runelite.net/runelite-api/apidocs/net/runelite/api/HeadIcon.html
  - https://static.runelite.net/runelite-api/apidocs/net/runelite/api/PlayerComposition.html
---

# PvP Combat Events — API Reference

## 1. The Tick Clock

`GameTick` fires once per 600ms server tick, after all packets have processed.
Use `client.getTickCount()` to get the current tick number (monotonically increasing).
All PvP events should be attributed to the tick on which they fired.

```java
@Subscribe
public void onGameTick(GameTick event) {
    int tick = client.getTickCount();
    // flush pending events into TickEntry(tick)
}
```

## 2. Detecting Attacks

### AnimationChanged

Fires when any actor's animation ID changes. An attack animation begins the moment the player commits to a hit.

```java
@Subscribe
public void onAnimationChanged(AnimationChanged event) {
    Actor actor = event.getActor();
    if (!(actor instanceof Player)) return;
    int animId = actor.getAnimation();
    AttackStyle style = AnimationStyleMap.lookup(animId); // see §2.1
    if (style == null) return;
    // ...
}
```

### 2.1 AnimationStyleMap — known PvP animation IDs

Animation IDs must be verified against the OSRS wiki (https://oldschool.runescape.wiki/w/Animation). Common ones are listed here as a starting seed; expand as needed.

| Animation ID | Style | Weapon / Source |
|---|---|---|
| 390 | MELEE | Punch (unarmed) |
| 422 | MELEE | Kick (unarmed) |
| 423 | MELEE | Block (unarmed) |
| 1658 | MELEE | Whip |
| 1711 | MELEE | Abyssal whip |
| 1979 | MELEE | Dragon dagger spec (x2) |
| 7045 | MELEE | Dharok's greataxe |
| 7054 | MELEE | Bandos godsword |
| 7055 | MELEE | Saradomin godsword |
| 7056 | MELEE | Zamorak godsword |
| 7638 | MELEE | Armadyl godsword spec |
| 1062 | RANGED | Shortbow |
| 426 | RANGED | Crossbow |
| 4230 | RANGED | Karils crossbow |
| 9168 | RANGED | Twisted bow |
| 711 | MAGIC | Wind/Water/Earth/Fire spells |
| 1979 | MAGIC | (check — some overlap with melee specs) |
| 1978 | MAGIC | God spells (Claws of Guthix / Sara Strike / Flames of Zamorak) |
| 811 | MAGIC | Ice Barrage / Blood Barrage / etc. |

**Important:** animation IDs are not exhaustive. When an animation is observed in a live fight that is not in the map, log the ID and add it here. Use UNKNOWN as a fallback.

### InteractingChanged

Fires when an actor changes their interaction target. Use this to associate attacker→target pairs before the animation fires.

```java
@Subscribe
public void onInteractingChanged(InteractingChanged event) {
    Actor source = event.getSource();
    Actor target = event.getTarget();
    if (!(source instanceof Player)) return;
    if (target == null) return; // deselected
    // record pending interaction: source is targeting target
}
```

Note: `getTarget()` can be null when the actor stops interacting (e.g. target ran away).

## 3. Detecting Hitsplats

`HitsplatApplied` fires when a hitsplat is applied to any actor (player or NPC).

```java
@Subscribe
public void onHitsplatApplied(HitsplatApplied event) {
    Actor actor = event.getActor();
    Hitsplat hitsplat = event.getHitsplat();
    int amount = hitsplat.getAmount();
    Hitsplat.HitsplatType type = hitsplat.getHitsplatType(); // HIT, BLOCK, POISON, VENOM, etc.
    // emit HitsplatEvent
}
```

Hitsplat types relevant to PvP:

| HitsplatType | Meaning |
|---|---|
| `HIT` | Standard hit (red) |
| `BLOCK_ME` / `BLOCK` | Blocked (blue 0) |
| `POISON` | Poison damage (green) |
| `VENOM` | Venom damage (dark green) |
| `HEAL` | HP recovery (green positive) |
| `DISEASE` | Disease |
| `PRAYER_DRAIN` | Smite drain |

Note: hitsplats arrive on the tick the hit is applied, not the tick the attack was thrown. In OSRS there is a 1–2 tick delay between attack animation and hitsplat, depending on attack speed and attack type (magic/range travel time). Do not assume same-tick correlation.

## 4. Reading Overhead Prayer

```java
HeadIcon icon = player.getOverheadIcon(); // null if no overhead prayer
if (icon == HeadIcon.MAGIC) { /* protecting magic */ }
```

Snapshot `getOverheadIcon()` at the time of `AnimationChanged` to record what prayer the defender was on when the attack was thrown.

| HeadIcon | PvP protection |
|---|---|
| `MELEE` | Protect from Melee |
| `RANGED` | Protect from Ranged |
| `MAGIC` | Protect from Magic |
| `SMITE` | Smite |
| `RETRIBUTION` | Retribution |
| `REDEMPTION` | Redemption |
| `SOUL_SPLIT` | Soul Split |
| `DEFLECT_MELEE/RANGE/MAGE` | Ancient curse deflects |
| `null` | No overhead prayer |

## 5. Detecting Eating

### Option A — MenuOptionClicked (preferred, zero-lag)

```java
@Subscribe
public void onMenuOptionClicked(MenuOptionClicked event) {
    if (event.getMenuOption().equals("Eat") || event.getMenuOption().equals("Drink")) {
        int itemId = event.getItemId();
        String itemName = client.getItemDefinition(itemId).getName();
        // emit EatEvent(localPlayer, itemName)
    }
}
```

`MenuOptionClicked` fires on the tick the player clicks, before the animation. This gives the earliest possible signal.

### Option B — AnimationChanged (fallback)

Animation 829 is the generic eat animation. Use as a fallback if `MenuOptionClicked` misses edge cases.

## 6. Detecting Gear Swaps

No dedicated event. Use a `PlayerComposition` snapshot diff on each `GameTick`.

```java
int[] previousEquipment = null;

@Subscribe
public void onGameTick(GameTick event) {
    Player localPlayer = client.getLocalPlayer();
    if (localPlayer == null) return;
    PlayerComposition comp = localPlayer.getPlayerComposition();
    if (comp == null) return;
    int[] current = comp.getEquipmentIds().clone();

    if (previousEquipment != null) {
        for (int i = 0; i < current.length; i++) {
            if (current[i] != previousEquipment[i]) {
                KitType slot = KitType.values()[i];
                int rawId = current[i];
                int itemId = rawId >= PlayerComposition.ITEM_OFFSET
                    ? rawId - PlayerComposition.ITEM_OFFSET : -1;
                // emit GearSwapEvent(localPlayer, slot, itemId)
            }
        }
    }
    previousEquipment = current;
}
```

`KitType` constants: `HEAD`, `CAPE`, `AMULET`, `WEAPON`, `TORSO`, `SHIELD`, `LEGS`, `GLOVES`, `BOOTS`, `RING`, `AMMO`.
Item ID = raw equipment ID minus `PlayerComposition.ITEM_OFFSET`. If the result is negative, the slot is empty or shows a cosmetic kit.

## 7. Tracking Opponent Equipment

`client.getPlayers()` returns all visible players. Iterate to find targets and apply the same `PlayerComposition` diff. Be aware:
- Equipment updates for remote players arrive in the same `GameTick` processing.
- Remote player `PlayerComposition` may be stale for 1 tick after a gear swap.

## 8. Attack Style Inference Limitations

- Animation IDs are a strong signal but not infallible. Some weapons share animation IDs across styles.
- Magic attacks that use autocasting show the same animation as manual cast. Distinguish by checking `getSpotAnims()` for the spell's graphic ID if needed.
- Spec attacks typically have unique animation IDs — register them separately with `style = MELEE/RANGED/MAGIC` + `isSpec = true` flag.
- When animation ID is unknown, fall back to `UNKNOWN` style; log the ID for later addition to `AnimationStyleMap`.
