---
scope: com.knz.pvpenhancer.panel
load_when: changing the RuneLite sidebar panel (tick history / hit summary / status)
---

purpose: RuneLite sidebar PluginPanel. Hosts the DATA that used to be on-screen overlays (tick history + hit summary) + a combat status header.

patterns:
  - PvpEnhancerPanel extends PluginPanel. Registered via a NavigationButton + ClientToolbar in the plugin startUp (icon built in code, crossed swords). Removed in shutDown.
  - update(entries, rows, inCombat, notRetaliating) rebuilds the panel. Called from the plugin's onGameTick via SwingUtilities.invokeLater (EDT).
  - thread-safety: the plugin snapshots service data on the client thread (getEntries/getRows return copies) and passes it here; the panel never reads the live services concurrently.
  - tick history rendered oldest-first as colored JLabels (mirrors the old overlay): per-category colors + ComboEvent tier color via event.getColor(). honours show* config filters.
  - hit summary rendered compactly (sidebar is ~225px) one line per row.
  - INLINE config controls live next to each block: tick-history = maxHistoryTicks spinner + Combat/Eat/Gear/Prayer/Combo checkboxes; hit-summary = Enabled checkbox + Max rows spinner. Created ONCE; read/write config via ConfigManager.setConfiguration("pvpenhancer", key, value). These items are hidden=true in PvpEnhancerConfig (no longer in the RuneLite config panel).
  - control listeners write config + rebuild() from the cached snapshot (instant feedback). syncControls() re-reads config into the widgets (guarded by suppressEvents so programmatic sets don't loop).

constraint:
  - all Swing mutation on the EDT only.
  - controls are persistent widgets — never rebuilt in update()/rebuild() (would drop interaction). only tickList/hitList content is rebuilt.
  - keep colors/filtering in sync with config (showCombat/Eating/GearSwap/Prayer/Combos, showHitSummary).
  - screen-view effects (heartbeat vignette, combo popup, not-attacking alert) stay as overlays — they cannot serve their purpose in a sidebar.
