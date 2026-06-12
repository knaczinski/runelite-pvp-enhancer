---
purpose: live project state. read before editing anything.
scope: root
format: caveman lite. re-baseline the full narrative at session end, not just latest_session.
---

# PROJECT STATE

latest_session: S016
phase: Phases 1-5 code-complete + S016 fix/feature batch + B030 fixed-layout-in-resizable landed (camera dropped). Only B008 open (live-data-gated). Live validation pending.
status: active

## Current Focus (S016)

Long live-tested turn. Big fix/feature batch + B030 breakthrough.
- **Combos**: eat + gear over a 2-tick sliding window (consecutive-tick clusters register). New
  `comboPopups` (Never/In combat/Always). Combo detection moved early in onGameTick + relayout
  try/catch so a cosmetic NPE can't kill combos.
- **Duel/PvP Arena**: hit predict + combat-state work where XP is blocked (fallback to own hitsplat,
  `Hitsplat.isMine()`, de-duped vs XP).
- **Attack timer**: decoupled from AnimationStyleMap (unmapped weapons like Eclipse atlatl work).
- **PID**: small over-head star + "pid" label over the likely holder, 1v1-only, shows from fight start.
- **Hit predict**: `hitPredictAnchor` (over opponent / over me). **Heal**: `healNumberStyle` breakdown
  "65 + 25 = 90". **Ghostify**: Others "Can't attack here + idle". **Heartbeat**: size + intensity.
- **Spec bar**: native resize abandoned (fill layers auto-sized) → new movable `SpecBarOverlay` HUD
  (reads VarPlayerID.SA_ENERGY). Removed specBarExtraHeight + applyCombatTabLayout.
- **Config reorg**: Screen alerts / Targeting aids / PID / Click helpers / etc.; short names; keyNames
  unchanged.
- **B030 fixed-layout-in-resizable**: drive RuneLite's native WidgetOverlays (set preferredLocation)
  instead of moving widgets directly — they reposition each frame during overlay render and the user's
  Alt-drag saves there. Movable DETACHED guide (Alt-drag, yellow outline) positions the layout;
  blocks pin to guideTopLeft+fixedOffset; per-block restore. A free-camera "keep character in scene"
  prototype was built then **removed** (tremor + framing-assumption fragility).
Build green, 82 tests. NEXT: live-validate spec-bar HUD, duel-arena predict, PID star, heal breakdown,
ghostify idle. User already confirmed pinning + chat + combos + atlatl timer live.

--- S015 detail (prior) ---

## Current Focus (S015)

S015 = 6-feature batch (design via /grill-me on uncertain points). Defensive prayer highlighter now
also boxes the prayer-tab button; new offensive prayer highlighter (PRAYER_FROM_WEAPON → Piety/Rigour/
Augury; WEAPON_FROM_PRAYER → WeaponSuggestOverlay boxes an inventory weapon). Hit-predict spec-combo HP
cue (bigger+red when post-hit HP ≤ threshold%) + font size. Attack-again countdown above the skull
(AttackCooldownService + AttackTimerOverlay + WeaponSpeeds seed; eat extends; per-scope). Taller spec
bar (specBarExtraHeight resizes SP_ATTACKBAR + style boxes, restorable). Skull higher with overhead
prayer. Earlier this turn: combat right-click filter, hit-predict size, UNDER_WIDGETS head overlays,
ghostify per-group show-chat. Build green, 79 tests. NEXT: live validation (HT-025 + earlier) + tune
weapon speeds / spec-bar px / skull offsets.

--- S014 detail (prior) ---

## Current Focus (S014)

S014 = documentation audit/refresh + the post-S013 fix batch. Docs brought current: README
features + code-org, .ai/architecture.md (real component map), package CONTEXT.md (overlay,
service, model, panel, root), docs/overlays.md scopes, PROJECT.md mission/scope, .ai/INDEX.md.

Post-S013 fixes (already shipped + pushed):
- Combat focus → **Ghostify** redesign (per-category when + colour; opponents>group>friends>others).
- Ghostify "in combat" smoothed by a per-player window (eating no longer un-ghosts; stamps target too).
- Ghostify hider matches by NAME (talking re-draw used a different Player instance → model popped back).
- Ghostify "Can't attack here" now works on PvP worlds + outside the wild (Varbits.IN_WILDERNESS /
  WorldType.isPvpWorld; PVP_WORLD_RANGE=15, HT-flagged).
- Combos emitted only while in combat (banking's bulk equipment change no longer counts).
- Accurate remote heal: real max HP via NPCManager + Hiscores (toggle accurateRemoteHp), not flat 99.
- Ghostify default outline colours set (self #ECE12C, opp #FF4040, group #4D96FF, friends #25E725, others #BDBDBD).
Build green, 78 tests. NEXT: live validation (HT queue) + seed harvest. Only B008 open.

--- S013 detail (prior) ---

S013 (design via /grill-me, then autonomous) shipped a bug+UX+feature batch:
- B022 combat trigger needs real activity (kills false heartbeat on rejected attack); ANY_FIGHT
  focus = player↔player only (fixes HT-010 teleport-in).
- B023 not-attacking flashes the opponent's hull (red↔yellow), works for NPCs.
- B024 combo redesign: Godlike/Excellent/Humble switch, Triple eat, Spec combo (hitsplat
  heuristic via SPECIAL_ATTACK_PERCENT drop + opponent-hit count), keep COMBO_FAILED.
- B025 heal + debuff overlays beside the HP bar; debuff = wiki icons + seconds; multi-debuff/actor.
- B026 developer panel + OverlayDemoService (mock any floating overlay on yourself) +
  developerMode toggle + docs/overlays.md.
- B027 overhead-resize spike → only Vengeance text + skull feasible (VengeanceTextOverlay +
  OverheadScope); prayer-icon + health-bar API-blocked (read-only).
- B028 PK skull resize (SkullResizeOverlay): hide native via setSkullIcon(-1), redraw scaled,
  restore on scope-exit; regular skull only. skullScope + skullSize config. Layer ABOVE_WIDGETS
  so it paints over native HP bar / overhead-prayer; centred; raised when HP/prayer active.
- B029 PID guess (EXPERIMENTAL): PidGuessService (contested-hitsplat vote) + PidIndicatorOverlay
  (you/them/?, swap warning), 1v1-only, config pidIndicator, dev mocks. PID not API-exposed —
  honest best-effort; HT-023 to check the live signal isn't an index artifact. Spike doc updated.
  Scope enums standardized with matches(isSelf,isOpponent); config reorganized into 4 sections.
- Ghostify (replaced combat focus): own config section, per-category WHEN + outline COLOUR for
  Self/Opponents/Group(CC+FC)/Friends/Others (Others adds "can't attack here" = combat level
  outside Wilderness range). Priority opponents>group>friends>others. GhostifyService publishes
  player→colour each tick; RenderableDrawListener skips models + GhostifyOutlineOverlay draws
  coloured contours via ModelOutlineRenderer. Players only. Self-ghost needs Entity Hider's
  "Hide Local Player". GhostifyWhen/GhostifyOthersWhen enums have shouldGhost(...).
HT results folded: HT-002/003/004/011 PASSED; HT-010 partial→B022 fix re-queued; HT-015..020 added.
Build green, 69 tests.
NEXT: live validation (HT-010 re-test, HT-012/013/014, HT-015..020). Harvest seeds in fights
(spot-anim/weapon/attack-anim ids). Deferred: skull resize, per-group overhead sizes. B008 open.

## Prior Focus

S012 shipped all of Phase 4 + a 9-item PvP/UX batch. Phase 4: B018 combat focus (hide
non-involved via Hooks RenderableDrawListener, persistence-timeout model, symmetric trigger),
B019 XP-drop hit prediction (orange number before projectile), B020 freeze/snare/TB timers
(SpotanimDebuffs seed + DebuffTimerOverlay, scope OFF/OPPONENTS/ALL), B021 prayer highlighter
(WeaponStyleMap seed → box the counter-prayer, name-scanned over Prayerbook.PRAYER1..30).
UX batch: combat focus no longer hides the target; heal popup raised above the skull;
TrackScope (self+opponents vs everyone); hit summary as a scrollable colour-coded JTable
(green=you attack, red=attacked) via HitDirection; tick history scroll pane; header ⚙ button
opens config via OverlayMenuClicked + anchor overlay; walk-here over Take in combat. Build
green, 69 tests.
NEXT: live validation — HT-010..HT-014 (Phase 4 + UX) plus the still-pending HT-001..HT-004.
Harvest debug ids in live fights: unknown spot-anims → B020 map, unknown weapons → B021 map,
unmapped attack anims → B008. Then B008 (grow AnimationStyleMap).

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

S008: tick history + hit summary moved from on-screen overlays into a RuneLite sidebar
PluginPanel (panel.PvpEnhancerPanel, registered via ClientToolbar + NavigationButton with
an in-code crossed-swords icon). The plugin snapshots service data on the client thread and
refreshes the panel on the EDT each tick. Screen-view effects (heartbeat vignette, combo
popup, not-attacking alert) stay as overlays — they can't serve their purpose in a sidebar.

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
- equipment id decode: raw >= 512 → item id = raw - 512 (raw getEquipmentIds()). BUT
  PlayerComposition.getEquipmentId(KitType) already returns the item id directly (≤0 = empty).
- Open this plugin's config from a panel button: post OverlayMenuClicked(new OverlayMenuEntry(
  RUNELITE_OVERLAY_CONFIG, "Configure", name), anchorOverlay). ConfigPlugin reads
  anchorOverlay.getPlugin(), so the anchor must be `new Overlay(this){}` (never added to OM).
- ComponentID has NO per-protection-prayer constants. Prayer buttons =
  gameval.InterfaceID.Prayerbook.PRAYER1..PRAYER30 (contiguous ids). Match by Widget.getName()
  ("Protect from Melee/Missiles/Magic") — robust to child reordering.
- No per-entity transparency in the API — combat focus can only HIDE (Hooks
  RenderableDrawListener.draw returns false), not dim.
- Actor#getGraphic() (deprecated) = current spot-anim id; adequate for debuff detection.
- Native overhead elements have NO scale API. Resizable: Vengeance text only
  (Actor.get/setOverheadText — clear native + redraw). setSkullIcon(-1) hides the skull
  (hacky). Overhead prayer icon (getOverheadIcon) + health bar (getHealthRatio/Scale) are
  read-only → not resizable. See docs/overhead-resize-spike.md.
- isInCombat must be activity-based: getInteracting() is set even on a REJECTED attack
  (clicking Attack on an un-attackable player), so interaction != combat.
- Special-attack use = SPECIAL_ATTACK_PERCENT varp (client.getVarpValue) dropping between ticks.
- An overlay fed by a shared service must be @Singleton, else Guice creates a 2nd instance
  that is never registered in the OverlayManager (demo would feed a dead overlay).
- Resizable-Classic minimap/inventory/chat are repositioned every frame by RuneLite's native
  draggable WidgetOverlays (RESIZABLE_MINIMAP_STONES_WIDGET / RESIZABLE_VIEWPORT_INVENTORY_PARENT /
  RESIZABLE_VIEWPORT_CHATBOX_PARENT). Moving the widgets directly always loses; set the overlay's
  preferredLocation instead. OverlayManager.getOverlays() is package-private — enumerate via the
  public anyMatch(Predicate) as a side-effect.
- Chatbox = its own interface InterfaceID.CHATBOX (162) nested in toplevel slot 96; moving the slot
  doesn't move it (the WidgetOverlay owns its position).
- Hitsplat.isMine() = own damage (use for hit-predict where XP is blocked, e.g. Duel/PvP Arena).
- VarPlayerID.SA_ENERGY = spec % (0..1000). InterfaceID.BuffBar = group 651.
- Camera: setCameraFocalPointX/Y/Z only act in setCameraMode(1) (free); that + camera easing make
  per-frame focal control tremor-prone — a "keep player in a screen box" feature was tried + removed.

Design doc: docs/tick-history-design.md. Build/test: docs/building-and-testing.md.
API reference: .ai/game/ops/runelite-plugin-dev.md, .ai/game/pvp/pvp-combat-events.md.
