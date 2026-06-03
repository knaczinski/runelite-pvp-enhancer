# Overlay catalogue

Standardised reference for every rendering surface in the PvP Enhancer. This is the human-audit
companion to the code: the floating popups also register one-click mocks in
`service/OverlayDemoService`, surfaced through the **Developer** sidebar panel (enable *Config →
Developer → Developer mode*, then the 🛠 button in the main panel header opens it).

Each overlay draws on the client thread. "Scope" lists which actors it can apply to, controlled
by the matching config. Mocks fire on the **local player** so the look can be checked solo.

## Screen / in-scene overlays

| Overlay | Purpose | Trigger source | Scope / config | Demo scenarios |
|---|---|---|---|---|
| `HeartbeatOverlay` | Red edge vignette pulsing once per tick while in combat | `recordTick()` each `GameTick`; only renders when `CombatStateService.isInCombat` | `showHeartbeat` | — (always-on pulse; not mocked) |
| `NotRetaliatingOverlay` | Flashes the opponent's outline red↔yellow when in combat but not attacking for ≥2 ticks | plugin pushes the opponent actor via `setOpponent` when `isNotRetaliating` | `showNotRetaliating` | — (needs a live opponent) |
| `ComboFeedbackOverlay` | Centre-screen popup naming a detected combo, tier-coloured, fades out | `showCombo(ComboResult)` from `ComboDetectorService` | `showCombos` | **Godlike switch**, **Triple eat**, **Spec combo** |
| `HealOverlay` | Floating "+N" / "~N" recovered HP, to the right of the health bar | `addHeal(actor, amount, estimate)` on heal detection each tick | `healDisplayMode` (Off / Self / Opponents / Everyone) | **Heal +12**, **Heal ~30 (est)** |
| `HitPredictOverlay` | Orange predicted outgoing damage near the target, before the projectile lands | `addPrediction(actor, dmg)` from the Hitpoints-XP drop | `hitPrediction` | **Predict 24**, **Predict 40** |
| `DebuffTimerOverlay` | Freeze/snare/teleblock icon + seconds, stacked right of the health bar under the heal row | `DebuffTrackerService.apply` on spot-anim match; counts down each tick | `debuffTimers` (Off / Self+opponents / All) | **Freeze (Barrage 33t)**, **Bind (16t)**, **Teleblock (500t)** |
| `PrayerHighlightOverlay` | Boxes the protection prayer countering the target's weapon style | plugin pushes the target's `AttackStyle` via `setTargetStyle` | `prayerHighlight` | — (needs the prayer tab open + a target) |
| `VengeanceTextOverlay` | Re-renders the "Vengeance!" overhead at a configurable size (native cleared) | plugin captures + clears native overhead text, feeds `add` | `vengTextScope` / `vengTextSize` | — (needs a veng cast) |
| `SkullResizeOverlay` | Re-renders the PK skull at a configurable size (native hidden via `setSkullIcon(-1)`) | plugin feeds in-scope skulled players via `setTargets` | `skullScope` / `skullSize` | — (needs a skulled player) |
| `PidIndicatorOverlay` | Experimental PID guess (YOU/THEM/?) + swap warning for a 1v1 you're in | `PidGuessService` (contested same-tick hits) | `pidIndicator` | PID guess → you / them / swap warning |

## Sidebar panels

| Panel | Purpose | Notes |
|---|---|---|
| `PvpEnhancerPanel` | Tick history (scrollable, colour-coded) + hit-summary table (green = you attack, red = attacked) + inline config + ⚙ open-config + 🛠 dev | Registered via `ClientToolbar`; refreshed on the EDT each tick |
| `DevPanel` | Developer panel: one-click buttons for the mocks above, grouped by overlay | Registered only while `developerMode` is on |

## Adding a new overlay (convention)

1. Implement the overlay; keep its public "feed" method small (`addX` / `showX` / `setX`).
2. If it should be mockable, register scenarios in `OverlayDemoService.build()` under a new
   group, firing the feed method on `local()`. Annotate the overlay `@Singleton` so the demo
   service and the plugin share the one instance that is registered in the `OverlayManager`.
3. Add a row to the table above with its purpose, trigger source, scope config, and demos.
