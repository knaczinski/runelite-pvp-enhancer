---
scope: com.knz.pvpenhancer.combatant
load_when: adding player/NPC detection, changing what the plugin reads off a fighter, or writing mockable combat tests
---

purpose: interface seam over RuneLite Player/NPC so detection logic is type-agnostic and unit-test mockable.

patterns:
  - Combatant — the interface the plugin programs against. getName / getAnimation / getOverheadPrayer / getTarget / isPlayer / isLocalPlayer.
  - PlayerCombatant, NpcCombatant — adapt RuneLite Player / NPC to Combatant. NPC returns null prayer, false isLocalPlayer.
  - Combatants.of(Actor, localPlayer) — the ONLY place that branches on the concrete RuneLite type (instanceof). Everything downstream uses Combatant.
  - CombatEventFactory.fromAttack(Combatant) — PURE: animation->style + target name/prayer => AttackEvent. unit-tested with mock(Combatant.class).

constraint:
  - info an implementation cannot provide returns blank (null), never fabricated. AttackEvent omits the prayer clause when prayer is null.
  - keep CombatEventFactory free of Client/config — that purity is the testable seam (CombatEventFactoryTest).
  - NPCs are a testing aid (config trackNpcs, default OFF). NPC attack animations are NOT in AnimationStyleMap, so NPCs mainly surface via hitsplats and as attack targets.
