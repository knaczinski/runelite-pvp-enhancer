---
purpose: AI sub-index for internal workspace tooling
---

# AI INTERNAL INDEX

sessions_dir: .ai/sessions/
rules_dir: .ai/rules/
architecture_map: .ai/architecture.md
backlog: .ai/backlog.md
backlog_history: .ai/backlog-history.md
game_docs: .ai/game/INDEX.md

active_session: S009
load_rule: read only rule files relevant to current task. priority order in CLAUDE.md rule_04.

## Package CONTEXT.md map

per-directory CONTEXT.md under src/main/java/com/knz/pvpenhancer/:

- pvpenhancer/CONTEXT.md — plugin root; entry point + config + event wiring
- pvpenhancer/combatant/CONTEXT.md — Combatant interface seam over Player/NPC + pure CombatEventFactory
- pvpenhancer/model/CONTEXT.md — immutable event/data classes + AnimationStyleMap + combo/hit-summary models
- pvpenhancer/service/CONTEXT.md — TickHistory + CombatState + AttackHitsplatCorrelator + HitSummary + ComboDetector
- pvpenhancer/panel/CONTEXT.md — RuneLite sidebar PluginPanel (tick history + hit summary + status)
- pvpenhancer/overlay/CONTEXT.md — screen-view effects only (heartbeat, combo popup, not-attacking alert)
