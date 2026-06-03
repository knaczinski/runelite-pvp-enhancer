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

### Combat overlays

- **Heartbeat** — red edge vignette pulsing per tick while in combat (combat now triggers only
  on real activity, not on clicking an un-attackable target).
- **Not-attacking warning** — flashes the opponent's outline red↔yellow when you stop attacking.
- **Combat focus** — hides players/NPCs not involved in the fight (Self / Any-fight; Any-fight
  is player-vs-player only).
- **Hit prediction** — orange predicted damage from the Hitpoints-XP drop, before the projectile
  lands.
- **Healing** — recovered HP to the right of the health bar (scope: self / opponents / everyone).
- **Debuff timers** — freeze / bind / teleblock as OSRS-wiki icons + seconds, stacked beside the
  health bar (scope: self+opponents / all).
- **Prayer highlighter** — boxes the protection prayer countering the target's weapon style.
- **Vengeance text resize** — re-renders the "Vengeance!" overhead at a configurable size.

### Combos

Godlike / Excellent / Humble **switch** tiers, **Triple eat**, and the **Spec combo** (ranged +
special on the opponent the same tick; *Humble* across two ticks), plus a potlock **fail**
indicator. Shown as a centre-screen popup and in the tick history.

### Developer panel

With *Config → Developer → Developer mode* on, the **🛠** panel fires one-click **mocks** of the
floating overlays (heal, hit predict, debuff, combo) on yourself — no live fight needed. Backed
by `OverlayDemoService` (see `docs/overlays.md`).

See `docs/tick-history-design.md`, `docs/combat-features-design.md`, `docs/overlays.md`, and
`docs/overhead-resize-spike.md` for the designs.

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
  PvpEnhancerPlugin.java     plugin entry point (startUp / shutDown)
  PvpEnhancerConfig.java     user-configurable settings (@ConfigGroup)
  panel/
    PvpEnhancerPanel.java    RuneLite sidebar panel (tick history + hit summary + status)
  overlay/
    HeartbeatOverlay.java    tick-synced red combat vignette
    NotRetaliatingOverlay.java  "NOT ATTACKING" alert
    ComboFeedbackOverlay.java   transient combo popup
  service/
    TickHistoryService.java  stateful tick event buffer
  combatant/
    Combatant.java           interface over Player/NPC (mockable)
    PlayerCombatant.java     adapts a RuneLite Player
    NpcCombatant.java        adapts a RuneLite NPC (testing aid)
    Combatants.java          factory: wraps an Actor
    CombatEventFactory.java  pure Combatant → AttackEvent
  model/
    TickEntry.java           one entry per game tick
    CombatEvent.java         event hierarchy (Attack/Hitsplat/Eat/GearSwap)
    AnimationStyleMap.java   animation ID → attack style mapping
  util/                      shared stateless helpers
```

See [`docs/building-and-testing.md`](docs/building-and-testing.md) (build, run, Jagex-account
login) and [`docs/dev-faq.md`](docs/dev-faq.md) (common gotchas) before developing.
