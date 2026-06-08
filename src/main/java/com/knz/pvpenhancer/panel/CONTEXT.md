---
scope: com.knz.pvpenhancer.panel
load_when: changing the RuneLite sidebar panel (tick history / hit summary / status)
---

purpose: RuneLite sidebar PluginPanel(s). PvpEnhancerPanel hosts the DATA (tick history + hit summary)
+ a combat status header + inline config + ⚙/🛠 buttons. DevPanel is the developer mock panel.

patterns:
  - PvpEnhancerPanel extends PluginPanel. Registered via NavigationButton + ClientToolbar in startUp
    (in-code crossed-swords icon). Header: status label, ⚙ (opens this plugin's config via
    OverlayMenuClicked on a plugin-owned anchor overlay), 🛠 (opens DevPanel; shown only when developerMode).
  - DevPanel extends PluginPanel. Lists OverlayDemoService scenarios as one-click buttons grouped by
    overlay, plus a "Clear debuffs & overlays" button. Registered only while developerMode is on (toggled in onConfigChanged).
  - update(entries, rows, inCombat, notRetaliating) rebuilds. Called from onGameTick via SwingUtilities.invokeLater (EDT).
  - thread-safety: plugin snapshots service data on the client thread (copies) and passes it here.
  - tick history: scroll pane of colored JLabels, oldest-first, per-category colours + ComboEvent tier
    colour, honouring show* filters.
  - hit summary: scrollable JTable; rows GREEN when you attack (HitDirection.OUTGOING), RED when attacked
    (INCOMING), default otherwise — via a DefaultTableCellRenderer reading a parallel rowDirections list.
  - INLINE config controls live next to each block: tick-history = maxHistoryTicks spinner + Combat/Eat/Gear/Prayer/Combo checkboxes; hit-summary = Enabled checkbox + Max rows spinner. Created ONCE; read/write config via ConfigManager.setConfiguration("pvpenhancer", key, value). These items are hidden=true in PvpEnhancerConfig (no longer in the RuneLite config panel).
  - control listeners write config + rebuild() from the cached snapshot (instant feedback). syncControls() re-reads config into the widgets (guarded by suppressEvents so programmatic sets don't loop).

constraint:
  - all Swing mutation on the EDT only.
  - controls are persistent widgets — never rebuilt in update()/rebuild() (would drop interaction). only tickList/hitList content is rebuilt.
  - keep colors/filtering in sync with config (showCombat/Eating/GearSwap/Prayer/Combos, showHitSummary).
  - screen-view effects (heartbeat vignette, combo popup, not-attacking alert) stay as overlays — they cannot serve their purpose in a sidebar.
