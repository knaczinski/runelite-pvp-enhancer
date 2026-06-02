---
purpose: isolated session/task file — load alone, not alongside other sessions
format: strict key-value
---

# SESSION S010

session_id: S010
date: 2026-06-01
status: complete
phase: feature — in-game healing display
goal: show recovered HP near the healer's health bar, with a self/opponents/everyone scope.

## Context Delta

changed_files:
  - HealDisplayMode.java (NEW, root) — OFF / SELF / OPPONENTS / EVERYONE
  - model/HealMath.java (NEW, pure) — estimateRemoteHeal(prevRatio,curRatio,scale,assumedMaxHp)
  - overlay/HealOverlay.java (NEW) — floating green "+N"/"~N" near actor health bar, rises+fades
  - PvpEnhancerConfig.java — Healing section + healDisplayMode (default EVERYONE)
  - PvpEnhancerPlugin.java — detectHeals() per tick: local exact HP delta; remote ratio estimate;
    register/clear HealOverlay; prevLocalHp + prevHealthRatio state
  - tests: model/HealMathTest (5)
  - docs: overlay/CONTEXT.md, PROJECT.md
pending: live validation (heal numbers appear correctly; opponent estimates reasonable). Then B008.
blockers: none

## AI Notes

User: configurable in-game healing display near the health bar, scope self/opponents/everyone.

API reality handled explicitly:
  - Local player healing is EXACT — client.getBoostedSkillLevel(HITPOINTS) delta (skip +1 regen).
  - Remote players only expose getHealthRatio()/getHealthScale() — no real HP. So remote heals
    are ESTIMATES: HealMath.estimateRemoteHeal((curRatio-prevRatio)/scale * 99). Shown with "~".
    Documented in the config tooltip + design.

HealOverlay: ABOVE_SCENE, renders at actor.getCanvasTextLocation(g, text, logicalHeight),
green, rises 28px + fades over 1500ms. addHeal(actor, amount, estimate) from the plugin tick.
Both detection (GameTick) and render run on the client thread → no sync needed.

Gotcha fixed: getCanvasTextLocation returns net.runelite.api.Point (int getX/getY), not
java.awt.Point.

Config: new "Healing" section, healDisplayMode enum default EVERYONE. (Screen overlay setting,
so it lives in the RuneLite config panel — not the sidebar, which is for data-block filters.)

Build green, 51 tests.

## Handoff

next_action: live-validate healing numbers (self exact, opponents ~estimate) + scope switching.
  Then B008 (grow AnimationStyleMap from live data).
load_rules: doc-style.md; .ai/game/pvp/pvp-combat-events.md
