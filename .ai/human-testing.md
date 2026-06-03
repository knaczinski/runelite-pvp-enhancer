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

> HT-002, HT-003, HT-004 PASSED (S013) — see human-testing-history.md.

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
6. **Re-test of the S012 bug:** in ANY_FIGHT, have a player TELEPORT IN who is NOT fighting (or is fighting an NPC / following) — confirm they are now HIDDEN (B022 fix: only player↔player counts as a fight).
**PASS CRITERIA:** only non-involved hidden; target never hidden; participants persist through eats up to the timeout; teleport-in bystanders hidden; disabling restores all.

> HT-011 PASSED (S013) — see human-testing-history.md.

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

## ▶ PRIORITY 5 — S013 batch (bug fixes, combos, overlays, dev panel)

### HT-015 — Not-attacking flashes the opponent (B023)
**SCENARIO SETUP:** plugin enabled, `showNotRetaliating` on. A guard or a partner.
**STEPS / OBSERVE:**
1. Attack the opponent, then stop (walk away / click the ground) for 2+ ticks.
2. Confirm the OPPONENT's outline flashes red↔yellow (not a screen panel).
3. Re-click the opponent → confirm the flash stops.
4. Repeat against an NPC (guard) → confirm it works for NPCs too.
**PASS CRITERIA:** the right actor flashes when you disengage; clears on re-target; works for player and NPC.

### HT-016 — Combo taxonomy (B024)
**SCENARIO SETUP:** plugin enabled, `showCombos` on. Spec weapon (AGS) + a bow, brews/pots.
**STEPS / OBSERVE:**
1. Switch 3–4 gear slots in one tick → **EXCELLENT SWITCH**; 5+ in one tick → **GODLIKE SWITCH**; split a 3–4 switch over two ticks → **HUMBLE SWITCH**.
2. Eat/drink 3 things in one tick → **TRIPLE EAT** (double eat shows nothing).
3. Land MSB spec + AGS spec on the opponent the same tick → **SPEC COMBO** (across two ticks → **HUMBLE SPEC COMBO**).
4. Potlock an eat → **COMBO FAILED** still shows.
**PASS CRITERIA:** each combo fires with the right label/colour; removed combos (double-eat, clean-switch, swap→attack) no longer appear.

### HT-017 — Heal + debuff beside the HP bar, debuff icons (B025)
**SCENARIO SETUP:** `healDisplayMode` = Everyone, `debuffTimers` = Self+opponents.
**STEPS / OBSERVE:**
1. Heal → confirm the +N appears to the RIGHT of the health bar (not above the head / behind the skull).
2. Get frozen → confirm a freeze ICON (not text) + a seconds countdown appears right of the HP bar, under the heal row.
3. Get frozen AND teleblocked → confirm TWO stacked icons, each counting down.
**PASS CRITERIA:** heal + debuffs sit beside the HP bar; debuffs show wiki icons + seconds; multiple debuffs stack.

### HT-018 — Developer panel mocks (B026)
**SCENARIO SETUP:** Config → Developer → **Developer mode** ON. Logged in.
**STEPS / OBSERVE:**
1. Confirm a 🛠 button appears in the main panel header and a Developer nav icon in the sidebar.
2. Open the dev panel; click each button (Heal, Hit predict, Debuff, Combo) → confirm the matching overlay fires ON YOU.
3. Turn Developer mode OFF → confirm the dev panel + 🛠 button disappear.
**PASS CRITERIA:** every mock fires its overlay on the local player; the panel is gated by the toggle.

### HT-019 — Vengeance text resize (B027)
**SCENARIO SETUP:** Config → Combat (PvP) → **Resize Vengeance text** = Self; **size** e.g. 250%.
**STEPS / OBSERVE:**
1. Cast Vengeance → confirm the "Vengeance!" overhead appears at the enlarged size (native size replaced, no doubled text).
2. Lower the size to ~50% → confirm it shrinks.
3. Set scope to Everyone and have a partner veng → confirm theirs scales too.
**PASS CRITERIA:** the veng text renders at the configured size for the configured scope; no double/native text behind it.

### HT-021 — PK skull resize (B028)
**SCENARIO SETUP:** Config → Combat (PvP) → **Resize PK skull** = Self+opponents; **size** e.g. 250%.
**STEPS / OBSERVE:**
1. Get skulled (attack a player in the wild) → confirm YOUR skull renders enlarged, with no native small skull behind it.
2. Have a skulled opponent nearby → confirm theirs scales too.
3. Lower size to ~50% → confirm it shrinks. Turn the feature OFF → confirm the native skull returns to normal.
4. Confirm a non-skulled / high-risk-skull player is unaffected (only the regular skull is handled).
**PASS CRITERIA:** regular skull renders at the configured size for the configured scope; native restored when off; no doubled skull.

### HT-020 — Heartbeat no longer false-fires on invalid attack (B022)
**SCENARIO SETUP:** plugin enabled, in a NON-PvP / safe area, another player present.
**STEPS / OBSERVE:**
1. Click **Attack** on a player you cannot attack ("You can't attack this player").
2. Confirm the heartbeat vignette does NOT appear and the panel does not say "In combat".
3. Now actually trade hits in a PvP area → confirm the heartbeat DOES appear.
**PASS CRITERIA:** combat (heartbeat) triggers only on real activity, not on a rejected attack click.

### HT-022 — Scope modes + config reorg + skull repositioning (S013)
**SCENARIO SETUP:** plugin enabled; second player + a third (uninvolved) player if possible.
**STEPS / OBSERVE:**
1. Open config → confirm 4 sections: **Tracking, Combat assist, Overhead displays, Developer**, names readable.
2. **Freeze/TB timers** + **Healing**: cycle Only me / Only opponents / Self+opponents / Everyone → confirm the timer/heal shows only for the right players.
3. **Vengeance / PK skull resize**: confirm the extra **Everyone but self + opponents** option shows them only on third parties (not you, not your opponent).
4. **Skull position**: in combat (HP bar showing) the resized skull sits ABOVE the HP/prayer, centred; out of combat it sits over the head. No left misalignment, no overlap by the HP bar.
**PASS CRITERIA:** each scope filters correctly; skull centred + correctly layered above HP/prayer when active.

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
