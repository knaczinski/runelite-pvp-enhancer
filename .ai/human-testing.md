---
purpose: human-assisted live test / validation queue. The AI cannot run a RuneLite client; these items need a human at the game.
scope: OPEN items only, priority-ordered (top = do first). Completed validations move to .ai/human-testing-history.md.
codes: HT-NNN, never reused. Status: OPEN | IN-PROGRESS | PASSED | FAILED.
mirror_of: this is to live testing what .ai/backlog.md is to development.
---

# HUMAN-ASSISTED TESTING & VALIDATION

Some behaviour (overlay rendering, visual accuracy, in-game feel, performance) cannot be unit-tested — only a human running the plugin in a live OSRS client can confirm it. This file is the queue of those checks.

## How this works (read once)

1. **Pick the top OPEN item** (priority-ordered — top = do first).
2. **Ask the AI to guide you:** say *"run HT-001"* or *"start human testing"*.
   The AI walks through **one step at a time**, waits for your observation, then gives the next step.
3. **Build & install first:** `./gradlew build` (auto-copies the jar to `~/.runelite/sideloaded-plugins`), then start RuneLite with `--developer-mode`.
4. **Follow the SCENARIO SETUP** in the item. Complete all setup steps before starting the test.
5. **Report using the RESULT TEMPLATE** at the bottom. Concrete observations only — "seems ok" is not actionable.
6. **The AI decides the outcome:**
   - **PASS** → AI moves the item to `.ai/human-testing-history.md` and marks the linked B-item validated.
   - **FAIL** → AI creates a new `B###` backlog item, links it here, and leaves the HT item queued for re-test after the fix.

> AI protocol: present ONE numbered step at a time. Never dump all steps at once.
> After the final step, apply the RESULT TEMPLATE and do the bookkeeping.

---

## ▶ PRIORITY 1 — MVP gate (validate before any Phase 2 work)

### HT-001 — Combat events appear with correct attacker/target/style/prayer (B003)
**Linked:** B003.
**SCENARIO SETUP:**
1. Build + install, start RuneLite `--developer-mode`, enable **PvP Enhancer**.
2. Go to a safe PvP area (e.g. an LMS practice match, or PvP world with a friend).
3. Equip a melee weapon you know (e.g. whip). Have a ranged or mage switch ready.
**STEPS / OBSERVE:**
1. Attack your opponent with melee. Watch the overlay top-left.
2. Confirm a line like `you -> <opponent>  melee  on <prayer>` appears.
3. Switch to ranged/mage, attack again. Confirm the style label changes accordingly.
4. Have the opponent turn on Protect from Melee. Attack. Confirm the prayer reads "pro melee".
**PASS CRITERIA:** attacker, target, style, and overhead prayer are all correct. Note any attack that shows `?` style (unmapped animation id — feeds B008).

### HT-002 — Overlay renders ticks newest-first, grouped, colour-coded (B005)
**Linked:** B005.
**SCENARIO SETUP:** plugin enabled, in any combat.
**STEPS / OBSERVE:**
1. Trade a few hits with an opponent.
2. Confirm the panel shows "PvP Tick History" title, then "Tick N" headers, newest at the top.
3. Confirm combat lines are one colour, eating another, gear swaps another.
4. Confirm old ticks drop off once more than `maxHistoryTicks` accumulate.
**PASS CRITERIA:** ordering newest-first, events grouped under the right tick, colours distinct, buffer caps.

### HT-003 — Hitsplats and gear swaps logged on the right tick (B003/B004)
**Linked:** B003, B004.
**SCENARIO SETUP:** plugin enabled, in combat, with an inventory weapon/armour to swap.
**STEPS / OBSERVE:**
1. Take a few hits. Confirm `<you> took N (hit)` lines appear; a blocked/0 hit reads "(block)".
2. Equip a different weapon mid-fight. Confirm `you equipped <item> (weapon)` appears within ~1 tick.
3. Eat a food. Confirm `you ate <food>` appears on the click tick.
**PASS CRITERIA:** hitsplat amounts correct, gear swap names + slots correct, eat item name correct, each on the expected tick.

### HT-004 — Config toggles take effect live (B006)
**Linked:** B006.
**SCENARIO SETUP:** plugin enabled, some history on screen.
**STEPS / OBSERVE:**
1. Open PvP Enhancer config. Toggle **Show eating** off → eating lines disappear.
2. Toggle **Show gear swaps** off → gear lines disappear.
3. Set **Max history ticks** to 5 → panel shrinks to at most 5 ticks.
4. Toggle **Track opponents** off → only your own events record going forward.
**PASS CRITERIA:** every toggle changes the overlay without a client restart.

---

## ▶ PRIORITY 4 — Phase 4 PvP overlays (S012)

### HT-010 — Combat focus hides only non-involved entities (B018)
**Linked:** B018.
**SCENARIO SETUP:** PvP area with a partner + a third uninvolved player/NPC nearby. Config →
Combat (PvP) → set **Combat focus** to SELF; note **Combat focus timeout** (default 16).
**STEPS / OBSERVE:**
1. Before fighting: confirm everyone is visible.
2. Start attacking your partner. Confirm uninvolved players/NPCs vanish; YOU and your TARGET stay visible.
3. Let your partner eat / pause attacking for a few ticks (< timeout). Confirm they STAY visible (no flicker out).
4. Stop attacking entirely. Confirm focus releases (~timeout ticks later) and everyone returns.
5. Switch mode to ANY_FIGHT: confirm focus also triggers when other players fight near you.
**PASS CRITERIA:** only non-involved hidden; target never hidden; participants persist through eats up to the timeout; disabling restores all.

### HT-011 — XP-drop hit prediction precedes the hitsplat (B019)
**Linked:** B019.
**SCENARIO SETUP:** plugin enabled, `hitPrediction` on, ranged or magic weapon.
**STEPS / OBSERVE:**
1. Attack a target with ranged/magic from a distance.
2. Confirm an orange predicted-damage number appears near the target a tick or two BEFORE the hitsplat lands.
3. Compare the predicted number to the actual hitsplat — confirm they match.
**PASS CRITERIA:** number appears before the projectile lands and equals the real damage.

### HT-012 — Freeze / snare / teleblock timers + id harvest (B020)
**Linked:** B020.
**SCENARIO SETUP:** plugin enabled, Config → **Freeze / TB timers** = Self + opponents.
Bring (or have a partner bring) ice spells / bind / teleblock.
**STEPS / OBSERVE:**
1. Get frozen (e.g. Ice Barrage). Confirm a "Freeze N" countdown appears over you and counts down to 0.
2. Compare the starting number to the real freeze duration (Barrage ≈ 33 ticks / 20s). Note any mismatch.
3. Repeat for snare/bind and teleblock if available; note durations.
4. **Harvest:** with client logs at debug, note any "Unknown spot-anim N on X" lines for freezes that showed NO timer — report the ids.
**PASS CRITERIA:** known freezes/TB show a countdown with roughly correct duration. Report wrong durations + unknown spot-anim ids (feeds the seed map).

### HT-013 — Prayer highlighter matches target weapon + id harvest (B021)
**Linked:** B021.
**SCENARIO SETUP:** plugin enabled, `prayerHighlight` on, prayer tab open. Partner with melee/ranged/mage weapons.
**STEPS / OBSERVE:**
1. Target a partner holding a melee weapon (e.g. whip). Confirm **Protect from Melee** is boxed/highlighted.
2. Have them switch to ranged → confirm highlight moves to **Protect from Missiles**.
3. Switch to mage → confirm **Protect from Magic**.
4. **Harvest:** note "Unknown weapon id N on X" debug lines for weapons that highlighted nothing — report the ids.
**PASS CRITERIA:** highlight tracks the target's weapon style and lands on the correct prayer. Report unknown weapon ids.

### HT-014 — Sidebar redesign + config button + walk-here (S012 UX batch)
**Linked:** panel redesign, swapPickupInCombat.
**SCENARIO SETUP:** plugin enabled, sidebar panel open.
**STEPS / OBSERVE:**
1. Trade hits. Confirm the **Hit Summary** is a table that scrolls and that rows you attack are GREEN, rows where you're attacked are RED.
2. Confirm the **Tick History** scrolls within its box.
3. Click the header **⚙** button → confirm the RuneLite config for **PvP Enhancer** opens.
4. Enable **Walk-here over Take**, enter combat, left-click a ground item → confirm you WALK (don't pick up); right-click still shows **Take**.
**PASS CRITERIA:** table colours + both scrollbars work; gear button opens config; in combat left-click no longer grabs loot while right-click Take remains.

---

## RESULT TEMPLATE

```
HT-NNN result
build: <jar version / commit>
scenario: <what you set up>
observed: <concrete, per-step — names, numbers, tick counts, colours>
unmapped animation ids seen: <list, or none>
verdict: PASS | FAIL
notes: <anything off>
```
