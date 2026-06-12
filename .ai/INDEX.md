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

active_session: S016
load_rule: read only rule files relevant to current task. priority order in CLAUDE.md rule_04.

## Package CONTEXT.md map

per-directory CONTEXT.md under src/main/java/com/knz/pvpenhancer/:

- pvpenhancer/CONTEXT.md — plugin root; entry point + config (5 sections) + event wiring + ghostify hider
- pvpenhancer/combatant/CONTEXT.md — Combatant interface seam over Player/NPC + pure CombatEventFactory
- pvpenhancer/model/CONTEXT.md — immutable event/data classes + seed maps (Animation/Spotanim/Weapon) + combo/hit-summary/debuff models
- pvpenhancer/service/CONTEXT.md — TickHistory, CombatState, Correlator, HitSummary, ComboDetector, DebuffTracker, Ghostify, PidGuess, OverlayDemo
- pvpenhancer/panel/CONTEXT.md — sidebar panel (tick history + hit-summary table + inline config + ⚙/🛠) + DevPanel
- pvpenhancer/overlay/CONTEXT.md — all overlays (heartbeat, not-attacking, combo, heal, hit-predict, debuff, prayer, veng, skull, PID, ghostify outline)

## Design docs (docs/)

tick-history-design · combat-features-design · overlays (catalogue) · overhead-resize-spike ·
pid-indicator-spike · building-and-testing · dev-faq
