---
purpose: isolated session/task file — load alone, not alongside other sessions
format: strict key-value
---

# SESSION S008

session_id: S008
date: 2026-06-01
status: complete
phase: UI — sidebar panel
goal: move on-screen data overlays into a RuneLite sidebar PluginPanel (Loot-Tracker style).

## Context Delta

changed_files:
  - panel/PvpEnhancerPanel.java (NEW) — PluginPanel: status header + tick history + hit summary
  - panel/CONTEXT.md (NEW)
  - PvpEnhancerPlugin.java — ClientToolbar + NavigationButton (in-code crossed-swords icon);
    removed TickHistoryOverlay + HitSummaryOverlay from overlayManager; refreshPanel() per tick (EDT)
  - DELETED overlay/TickHistoryOverlay.java, overlay/HitSummaryOverlay.java
  - overlay/CONTEXT.md (rewritten — effects only)
  - INDEX, CLAUDE, CONTEXT bumps
pending: live validation (panel renders, effects still on screen). Then B008.
blockers: none

## AI Notes

User wanted a RuneLite sidebar panel (like Loot Tracker) and the on-screen overlays moved
into it.

Design call: moved the DATA overlays (tick history + hit summary) into PvpEnhancerPanel.
Kept the three SCREEN effects/alerts as overlays — heartbeat vignette (peripheral tick
metronome), combo popup (transient center feedback), not-attacking alert (must be seen
mid-fight). A sidebar can't serve those. Told the user; offered to mirror them too if wanted.

PvpEnhancerPanel: status header (Idle / In combat / NOT ATTACKING), tick history as
oldest-first colored JLabels (mirrors old overlay: category colors + ComboEvent tier color,
show* filters), compact one-line hit summary rows (sidebar ~225px). NavigationButton icon
built in code (no image resource). Thread-safe: plugin snapshots getEntries()/getRows() on
the client thread, hands them to panel.update() via SwingUtilities.invokeLater (EDT).

Build green (46 tests). Two overlay classes deleted.

## Handoff

next_action: live-validate the sidebar panel + the 3 remaining screen effects. Then B008
  (grow AnimationStyleMap from live data).
load_rules: doc-style.md; docs/combat-features-design.md
