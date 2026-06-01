---
purpose: product backlog for runelite-pvp-enhancer — OPEN items only
auto-update: AI updates item status each session. session logs → .ai/sessions/SXXX_<slug>.md (never here).
completed: full records in .ai/backlog-history.md. When an item is accepted, remove its spec here and prepend the record there.
format: priority-ordered. effort: XS (<15min) S (15-45min) M (1-2h) L (2-4h) XL (4h+).
rule: read at session start. "continue from where backlog stopped" → execute NEXT UP.
lang: English only. caveman lite.
---

# BACKLOG

Execution is phase-ordered. Within each phase, items run in numeric order unless explicitly noted.
Completed phases/items are archived in `.ai/backlog-history.md` — only OPEN work lives here.

---

## ▶ PHASE 0 — Project bootstrap

### B001 — Gradle project scaffold
**Effort:** S
**Status:** OPEN
**Scope:** initialise the Gradle project from the RuneLite external plugin template.
- Set up `build.gradle`, `settings.gradle`, `gradle.properties` targeting RuneLite's API.
- Create `PvpEnhancerPlugin.java` (stub `startUp`/`shutDown`), `PvpEnhancerConfig.java` (empty @ConfigGroup).
- Confirm `./gradlew build` compiles clean with zero warnings.
- Add `.gitignore` for Gradle build outputs.
**Acceptance:** `./gradlew build` succeeds. Plugin loads in RuneLite developer mode without errors.
