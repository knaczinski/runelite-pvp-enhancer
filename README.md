# runelite-pvp-enhancer

A RuneLite plugin for Old School RuneScape that adds PvP-focused overlays and combat helpers. Improves situational awareness without automating any game action.

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
The JAR is written to `build/libs/`. Load it via RuneLite developer mode or the external plugin loader.

### Run tests
```
./gradlew test
```

---

## 2. How the AI navigates (the self-feeding loop)

The repo is structured so an AI agent can work it with minimal context. Read order:

| File | Role |
|---|---|
| `CLAUDE.md` | Workspace index + AI protocol rules. The starting point. |
| `CONTEXT.md` | Live project state. **Read before editing anything.** |
| `PROJECT.md` | Full project spec. Read before any architectural change. |
| `.ai/INDEX.md` | Master index of the `.ai/` knowledge base. |
| `.ai/backlog.md` | OPEN work only, phase- and priority-ordered. |
| `.ai/backlog-history.md` | Completed items (full records, newest first). |
| `.ai/human-testing.md` | Live-validation queue the AI cannot run itself. |
| `.ai/human-testing-history.md` | Passed/closed validations. |
| `.ai/rules/` | Standing rules loaded by `load_when` frontmatter. |
| `.ai/sessions/SXXX_*.md` | One isolated file per work session. Load only the active one. |
| `.ai/game/` | OSRS game mechanics reference docs — `INDEX.md` first. |
| `docs/` | Tier-2 human-prose design docs. |

**Working rhythm:** read `CONTEXT.md` → pick the next `.ai/backlog.md` item → implement → build green → write `.ai/sessions/SXXX_*.md` → archive item to `backlog-history.md` → update `CONTEXT.md` → commit.

**Doc tiers** (`.ai/rules/doc-style.md`): Tier-1 AI-caveman (`.ai/`, `CLAUDE.md`, `CONTEXT.md`), Tier-2 human-prose (`README.md`, `docs/`), Tier-3 Javadoc in source.

---

## 3. How a human uses it

1. **Build & load** (`./gradlew build` → RuneLite external plugin loader).
2. **Toggle features** in the RuneLite config panel under "PvP Enhancer".
3. **Validate in-game** — see `.ai/human-testing.md` for the validation queue.
4. **Report bugs** — describe the scenario; the AI will open a `B###` backlog item and an `HT-NNN` validation item.

---

## 4. Human-assisted testing

`.ai/human-testing.md` (+ `-history.md`) contains things the AI **cannot** verify — overlay rendering, visual accuracy, in-game feel. Items are coded `HT-NNN`, priority-ordered, each with: setup steps, what to observe, a RESULT TEMPLATE, and pass criteria. Ask the AI to guide you one step at a time.

---

## 5. Code organisation

```
src/main/java/com/knz/pvpenhancer/
  PvpEnhancerPlugin.java     plugin entry point (startUp / shutDown)
  PvpEnhancerConfig.java     user-configurable settings (@ConfigGroup)
  overlay/                   one Overlay subclass per rendering concern
  util/                      shared stateless helpers
```
