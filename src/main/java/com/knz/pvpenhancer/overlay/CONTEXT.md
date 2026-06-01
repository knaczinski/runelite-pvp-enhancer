---
scope: com.knz.pvpenhancer.overlay
load_when: changing overlay rendering, layout, colours, or adding a new overlay
---

purpose: render the TickHistoryService buffer. read-only view.

patterns:
  - TickHistoryOverlay extends OverlayPanel. position TOP_LEFT. registered in plugin startUp, removed in shutDown.
  - render(): clear panelComponent children, add title, then per tick a "Tick N" header + one LineComponent per visible event.
  - per-category colour + visibility honoured from config (showCombat/showEating/showGearSwap).

constraint:
  - never mutate the service. read getEntries() only.
  - clear panelComponent.getChildren() at the top of every render() to avoid stale lines.
  - do NOT call setPriority(OverlayPriority...) — the enum is removed on current API. position only.
  - one Overlay subclass per rendering concern; add new overlays here and register in the plugin.
