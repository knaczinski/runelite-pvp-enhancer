---
purpose: live project state. read before editing anything.
scope: root
format: caveman lite. re-baseline the full narrative at session end, not just latest_session.
---

# PROJECT STATE

latest_session: S005
phase: Phase 1 complete. Phase 3 (combat awareness & combos) IN PROGRESS — foundation (B013) shipped.
status: active

## Current Focus

S005: grilled 4 new combat-awareness features (docs/combat-features-design.md) and shipped
Phase 3 foundation B013 — CombatStateService (in-combat) + AttackHitsplatCorrelator. Build
green, 30 tests. NEXT phases: B014 heartbeat + B015 not-retaliating (Phase B), B016 hit
summary (Phase C), B017 combos (Phase D).

## (Phase 1) Focus history

MVP S001 (B001-B007). S002 NPC tracking (Combatant interface) + dev-env fixes. S003 per-tick
code + chronological order + combo-eat. S004 fixed gear item names (worn container, real ids,
not appearance decode), added PRAYER events (overhead diff), split config into Tracking vs
Overlay sections (show* are display-only), removed max-ticks minimum, and cleaned all
deprecations (gameval.InventoryID.WORN, getTopLevelWorldView().players()). Build green
(-Xlint:deprecation clean), 21 unit tests.
NEXT: user deletes the leftover sideloaded jar (one-time), then `./gradlew run` = single entry.
Validate HT-001..HT-004 (+ gear names, prayers, combo-eat, tick codes, config). Then B008 (grow AnimationStyleMap).

## Open Threads

- Live validation pending (HT-001..HT-004). MVP behaviour unconfirmed in a real fight.
- AnimationStyleMap is a small seed; unmapped attack anims log at debug for B008.

## Known Issues

- Dev run path confirmed working: `./gradlew run` loads the plugin (verified in client.log
  10:39 session — "Side-loading plugin pvp-enhancer-1.0.0.jar" + "Plugin PvpEnhancerPlugin
  is now running"). Production clients (Jagex Launcher, Desktop RuneLite.jar) do NOT
  reliably side-load it — Plugin Hub is the real channel for the everyday client.
- Jagex-account login in the dev client: the gradle-run client shows the legacy login.
  Fix = `--insecure-write-credentials` on the Jagex-launched client → writes
  ~/.runelite/credentials.properties → dev client reuses it. See docs/building-and-testing.md.
- RESOLVED (S001 follow-up): plugin appeared TWICE under `gradle run`. Cause: `build`
  auto-installed the jar to sideloaded-plugins AND `run` loads via loadBuiltin, so
  --developer-mode side-loaded it a second time. Fix: removed `build.finalizedBy
  installPlugin` (installPlugin is now opt-in only); `run` uses loadBuiltin from classpath
  only. Added `--add-opens=java.base/java.lang.reflect,java.lang` to the run task for
  loadBuiltin reflection on JDK 17+. Rule: never have the jar in sideloaded-plugins while
  using `gradle run`.
- EventBus `LambdaConversionException: Invalid caller` only occurred on the side-loaded
  copy (child classloader). With loadBuiltin (app classloader) the lambda binds normally;
  the warning should disappear.

## Architecture Notes

Code exists now. Layering:
- PvpEnhancerPlugin (root pkg) — @PluginDescriptor entry point, owns all @Subscribe handlers,
  only class coupled to live Client/ItemManager. Wraps actors via Combatants.of and feeds events → service.
- combatant.* — Combatant interface over Player/NPC; Combatants.of is the sole instanceof site;
  CombatEventFactory is pure (Combatant → AttackEvent), mocked in tests. NPC = blank prayer.
- service.TickHistoryService — @Singleton, pure buffer (pending list + capped newest-first Deque),
  unit-tested without a client.
- model.* — immutable event/data classes + AnimationStyleMap seed.
- overlay.TickHistoryOverlay — OverlayPanel, read-only view of the service.

Build: Gradle 8.10 wrapper committed. `compileOnly net.runelite:client:latest.release`,
release 11. `./gradlew run` = dev loop (launches RuneLite). `./gradlew build` = compile +
test + auto-install jar to sideloaded-plugins.

API gotchas (do not relearn):
- Client has NO getItemComposition(int). Use injected ItemManager.getItemComposition(int).
- OverlayPriority enum removed on current API — set position only, never setPriority.
- equipment id decode: raw >= 512 → item id = raw - 512.

Design doc: docs/tick-history-design.md. Build/test: docs/building-and-testing.md.
API reference: .ai/game/ops/runelite-plugin-dev.md, .ai/game/pvp/pvp-combat-events.md.
