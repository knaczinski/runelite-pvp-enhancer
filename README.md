# runelite-pvp-enhancer

A RuneLite plugin for Old School RuneScape that adds PvP-focused overlays and combat helpers.

**Initial scope:** tick-accurate combat history overlay — shows a chronological log of combat events (attacks, eating, gear swaps) during a fight, separated by category and tick.

---

## 1. Build & install

| Thing | Value |
|---|---|
| Language | Java 11 |
| Build | Gradle |
| Runtime | RuneLite client (open-source) |
| Entry point | `com.knz.pvpenhancer.PvpEnhancerPlugin` |

### Build the plugin
```
./gradlew build
```
The JAR is written to `build/libs/`. Load via RuneLite developer mode or the external plugin loader.

### Run tests
```
./gradlew test
```

### IntelliJ IDEA setup

1. Clone this repo alongside or inside the RuneLite project.
2. File → Project Structure → SDK → Download JDK → version 11, Eclipse Temurin.
3. Run `RuneLite.main()` from `runelite-client/src/main/java/net/runelite/client/RuneLite.java`.

See `.ai/game/ops/runelite-plugin-dev.md` for the full setup reference.

---

## 2. Features

All overlays are catalogued in **`docs/overlays.md`** (purpose, trigger, scope, demos).

### Sidebar panel

- **Tick history** — scrollable, colour-coded last-N ticks (combat / eating / gear / prayer /
  combo), each with a per-category toggle and a "Copy log" button.
- **Hit summary** — scrollable table, rows **green** when you attack, **red** when you are
  attacked.
- **⚙** opens the plugin config; **🛠** (with *Developer mode* on) opens the developer panel.

Config is grouped into **Tracking**, **Combat assist**, **Overhead displays**, **Ghostify**, and
**Developer** sections.

### Combat assist

- **Heartbeat** — red edge vignette pulsing per tick while in combat (combat triggers only on
  real activity, not on clicking an un-attackable target).
- **Not-attacking warning** — flashes the opponent's outline red↔yellow when you stop attacking.
- **Hit prediction** — orange predicted damage from the Hitpoints-XP drop, before the projectile
  lands; configurable text size. **Spec-combo HP cue**: when the opponent's estimated HP *after*
  the hit drops to a configured % or below, the number turns bigger and red — start the switch + spec.
- **Defensive prayer highlighter** — boxes the protection prayer countering the target's weapon
  style, and flags the prayer-tab button (so you notice with the inventory open).
- **Offensive prayer highlighter** — *Prayer from weapon* highlights Piety/Rigour/Augury for your
  weapon, or *Weapon from prayer* highlights a matching weapon in your inventory.
- **Taller spec bar** — grows the special-attack bar in the Combat Options tab by a configurable
  px (shrinking the style boxes); restores natively when off.
- **Right-click filter** — in combat, a player right-click shows only Walk here + Attack.
- **Walk-here over Take** — de-prioritises ground-item "Take" in combat so a left-click walks.
- **PID guess** *(experimental)* — best-effort guess of who has PID in a 1v1 you're in, plus a
  swap warning. PID isn't exposed by the API, so this is a noisy estimate (see
  `docs/pid-indicator-spike.md`).

### Overhead displays (per-player scope: Off / Everyone / Me / Opponents / Self+opp)

- **Healing** — recovered HP beside the health bar. Local is exact; remote is an estimate using
  **real max HP** (NPC table + OSRS Hiscores, like Opponent Information), not a flat 99.
- **Debuff timers** — freeze / bind / teleblock as OSRS-wiki icons + seconds; teleblock tracked
  as half (~2.5 min) when the target prayed Magic as it landed.
- **Vengeance text resize** / **PK skull resize** — re-render those overheads at a configurable
  size (the native size isn't resizable via the API, so the original is hidden/replaced; regular
  skull only — prayer-icon and health-bar resize aren't possible, see `docs/overhead-resize-spike.md`).
- **Attack timer** — countdown (seconds, 2 dp) above the head/skull until the player can attack
  again (weapon-speed seed table; resets on eat/drink). Per-scope toggles: self / opponents / others.

### Ghostify

Reduce characters to a coloured **outline** only (native model hidden via a `RenderableDrawListener`
+ `ModelOutlineRenderer` contour). Per category — **Self / Opponents / Group (CC+FC) / Friends /
Others** — set **when** (Never / Always / In combat / Not in combat; Others adds *Can't attack
here* using the Wilderness / PvP-world combat-level range) and the outline **colour**. Priority on
overlap: opponents > group > friends > others. Hiding your own model still needs Entity Hider's
"Hide Local Player".

### Combos

Godlike / Excellent / Humble **switch** tiers, **Triple eat**, and the **Spec combo** (ranged +
special on the opponent the same tick; *Humble* across two ticks), plus a potlock **fail**
indicator. Only emitted while in combat (so banking doesn't count). Shown as a centre-screen popup
and in the tick history.

### Developer panel

With *Config → Developer → Developer mode* on, the **🛠** panel fires one-click **mocks** of the
floating overlays (heal, hit predict, debuff, combo, PID guess/swap) on yourself — no live fight
needed — plus a **Clear debuffs & overlays** button. Backed by `OverlayDemoService` (see
`docs/overlays.md`).

See `docs/tick-history-design.md`, `docs/combat-features-design.md`, `docs/overlays.md`,
`docs/overhead-resize-spike.md`, and `docs/pid-indicator-spike.md` for the designs.

---

## 3. How the AI navigates

| File | Role |
|---|---|
| `CLAUDE.md` | Workspace index + AI protocol rules. The starting point. |
| `CONTEXT.md` | Live project state. **Read before editing anything.** |
| `PROJECT.md` | Full project spec. Read before any architectural change. |
| `.ai/INDEX.md` | Master index of the `.ai/` knowledge base. |
| `.ai/backlog.md` | OPEN work only, phase- and priority-ordered. |
| `.ai/backlog-history.md` | Completed items (full records, newest first). |
| `.ai/human-testing.md` | Live-validation queue the AI cannot run itself. |
| `.ai/rules/` | Standing rules loaded by `load_when` frontmatter. |
| `.ai/sessions/SXXX_*.md` | One isolated file per work session. Load only the active one. |
| `.ai/game/INDEX.md` | OSRS + RuneLite API reference docs — read index first. |
| `docs/` | Tier-2 human-prose design docs. |

**Working rhythm:** read `CONTEXT.md` → pick next `.ai/backlog.md` item → implement → build green → write session file → archive item → update `CONTEXT.md` → commit.

---

## 4. Human-assisted testing

`.ai/human-testing.md` contains validations only a human at the game can do — overlay rendering, visual accuracy, in-game feel. Ask the AI to guide you one step at a time.

---

## 5. Code organisation

```
src/main/java/com/knz/pvpenhancer/
  PvpEnhancerPlugin.java      plugin entry point + all @Subscribe handlers (only class on the live Client)
  PvpEnhancerConfig.java      settings (@ConfigGroup) — Tracking / Combat assist / Overhead / Ghostify / Developer
  *Scope.java, Ghostify*.java, etc.   config enums (TrackScope, HealDisplayMode, DebuffScope,
                              OverheadScope, GhostifyWhen, GhostifyOthersWhen)
  panel/
    PvpEnhancerPanel.java     sidebar panel (tick history + hit-summary table + inline config + ⚙/🛠)
    DevPanel.java             developer panel (overlay mocks + clear), shown when developerMode
  overlay/                    one Overlay per concern (see docs/overlays.md for the full catalogue)
    Heartbeat / NotRetaliating / ComboFeedback / Heal / HitPredict / DebuffTimer /
    PrayerHighlight / VengeanceText / SkullResize / PidIndicator / GhostifyOutline
  service/                    @Singleton stateful/pure services
    TickHistoryService, CombatStateService, AttackHitsplatCorrelator, HitSummaryService,
    ComboDetectorService, DebuffTrackerService, GhostifyService, PidGuessService, OverlayDemoService
  combatant/                  Combatant interface over Player/NPC (mockable) + Combatants factory + pure CombatEventFactory
  model/                      immutable event/data classes + seed maps (AnimationStyleMap,
                              SpotanimDebuffs, WeaponStyleMap) + combo / hit-summary / debuff models
```

See [`docs/building-and-testing.md`](docs/building-and-testing.md) (build, run, Jagex-account
login) and [`docs/dev-faq.md`](docs/dev-faq.md) (common gotchas) before developing.
