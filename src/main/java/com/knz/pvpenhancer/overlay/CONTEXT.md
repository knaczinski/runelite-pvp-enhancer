---
scope: com.knz.pvpenhancer.overlay
load_when: changing any screen/scene/overhead overlay
---

purpose: all Overlay subclasses (one per rendering concern). DATA views (tick history, hit summary)
live in the sidebar panel (com.knz.pvpenhancer.panel), NOT here. Full catalogue with scopes + dev
mocks: docs/overlays.md.

overlays:
  - HeartbeatOverlay — ABOVE_SCENE. Red edge vignette, pulses once per game tick in combat
    (CombatStateService); recordTick() each GameTick, ~600ms decay.
  - NotRetaliatingOverlay — flashes the OPPONENT's model outline red↔yellow when in combat but not
    attacking ≥2 ticks. Plugin pushes the opponent actor via setOpponent. (Was a screen text; now an outline.)
  - ComboFeedbackOverlay — ABOVE_SCENE. Centre popup naming a combo, tier-coloured, ~1.5s fade. showCombo(result). clear().
  - HealOverlay — UNDER_WIDGETS (over native HP bar/overheads, under the game UI). Floating "+N"/"~N" beside the
    health bar. addHeal(actor, amount, estimate). Local exact; remote estimate ("~") via HealMath using
    REAL max HP (NPCManager + Hiscores, resolved in the plugin), not assumed 99. Scope = healDisplayMode.
  - HitPredictOverlay — UNDER_WIDGETS. Orange predicted outgoing damage near the target from the
    Hitpoints-XP drop; bigger + RED when the spec-combo HP cue fires. addPrediction(actor, dmg, critical).
    Config hitPrediction / hitPredictionSize / hitPredictThreshold.
  - AttackTimerOverlay — UNDER_WIDGETS. Countdown (seconds, 2dp) above the head/skull until the actor
    can attack again. Reads AttackCooldownService; plugin feeds the in-scope set via setTimers.
    Config attackTimerSelf/Opponents/Others.
  - WeaponSuggestOverlay — ABOVE_WIDGETS. Boxes inventory weapons (WeaponStyleMap) of the style of your
    active offensive prayer. setStyle. Config offensivePrayerMode = WEAPON_FROM_PRAYER.
  - DebuffTimerOverlay — freeze/snare/TB wiki icon + seconds, stacked beside the health bar. Reads
    DebuffTrackerService.getActive (per-actor EnumMap<Debuff,ticks>). Scope = debuffTimers.
  - PrayerHighlightOverlay — ABOVE_WIDGETS. Boxes prayers by NAME over InterfaceID.Prayerbook.PRAYER1..30
    (defensive protect prayer + offensive Piety/Rigour/Augury) + the prayer-tab button (ComponentID
    *_PRAYER_TAB) so the cue shows with the inventory open. setPrayers(names). Config prayerHighlight +
    offensivePrayerMode (PRAYER_FROM_WEAPON).
  - VengeanceTextOverlay — ABOVE_SCENE. Redraws cleared native "Vengeance!" text scaled. add(actor, text). vengTextScope/Size.
  - SkullResizeOverlay — UNDER_WIDGETS (over native overheads, under the game UI). Redraws the hidden native skull scaled + centred, raised above
    HP/prayer when active. setTargets(players). skullScope/Size. Regular skull only.
  - PidIndicatorOverlay — TOP_CENTER OverlayPanel. Experimental PID guess (you/them/?) + swap flash. pidIndicator.
  - GhostifyOutlineOverlay — ABOVE_SCENE. Draws each ghosted player's contour in its category colour
    via ModelOutlineRenderer. Reads GhostifyService.getGhosted (player→colour).

patterns:
  - registered/removed in plugin startUp/shutDown via overlayManager; gate on their own config.
  - floating popups (Heal/HitPredict/Debuff/Combo/Veng/PID) register one-click mocks in
    service/OverlayDemoService → DevPanel. Annotate @Singleton so demo + plugin share the instance.

constraint:
  - do NOT call setPriority(OverlayPriority...) — enum removed on current API.
  - layer choice for head overlays: ABOVE_SCENE draws UNDER native overheads; ABOVE_WIDGETS draws over
    everything incl. the bank UI; UNDER_WIDGETS = over native overheads but UNDER the game UI (use this
    for skull/heal/debuff/veng/ghost-outline). PrayerHighlight stays ABOVE_WIDGETS (it boxes a UI widget).
  - screen effects stay overlays; do not move them into the sidebar panel.
