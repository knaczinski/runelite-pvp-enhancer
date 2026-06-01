---
scope: com.knz.pvpenhancer.overlay
load_when: changing overlay rendering, layout, colours, or adding a new overlay
---

purpose: render the TickHistoryService buffer. read-only view.

patterns:
  - TickHistoryOverlay extends OverlayPanel. position TOP_LEFT. registered in plugin startUp, removed in shutDown.
  - render(): clear panelComponent children, add title, then per tick a "Tick 0001" header (String.format %04d on TickEntry.sequence) + one LineComponent per visible event.
  - OLDEST-FIRST: getEntries() is newest-first, so render iterates it in reverse → ticks read chronologically top-to-bottom (matches the user's spec).
  - per-category colour + visibility honoured from config (showCombat/showEating/showGearSwap/showPrayer). colours: combat red, eating green, gear blue, prayer yellow.
  - show* are DISPLAY-ONLY filters here. the plugin records everything tracked regardless; toggling a category reveals/hides already-recorded events.

constraint:
  - never mutate the service. read getEntries() only.
  - clear panelComponent.getChildren() at the top of every render() to avoid stale lines.
  - do NOT call setPriority(OverlayPriority...) — the enum is removed on current API. position only.
  - one Overlay subclass per rendering concern; add new overlays here and register in the plugin.
