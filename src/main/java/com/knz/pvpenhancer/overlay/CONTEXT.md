---
scope: com.knz.pvpenhancer.overlay
load_when: changing screen-view overlays (heartbeat, not-attacking alert, combo popup)
---

purpose: screen-view effects and alerts. DATA overlays (tick history, hit summary) moved to the sidebar panel (com.knz.pvpenhancer.panel) — only effects/alerts live here.

patterns:
  - HeartbeatOverlay extends Overlay (ABOVE_SCENE, DYNAMIC). Red edge vignette, pulses once per game tick while in combat (CombatStateService). Plugin calls recordTick() each GameTick; exponential decay over 600ms. Constant intensity.
  - ComboFeedbackOverlay extends Overlay (ABOVE_SCENE). Transient centered popup (~1.5s fade), tier-colored. Plugin calls showCombo(result) when a combo fires.
  - NotRetaliatingOverlay extends OverlayPanel (ABOVE_CHATBOX_RIGHT). "NOT ATTACKING" warning when CombatStateService.isNotRetaliating(). Stays on-screen (must be seen mid-fight).
  - registered/removed in the plugin startUp/shutDown via overlayManager.

constraint:
  - do NOT call setPriority(OverlayPriority...) — enum removed on current API.
  - these are screen effects by design; do not move them into the sidebar panel (they lose their purpose). Tick history / hit summary belong in the panel, not here.
