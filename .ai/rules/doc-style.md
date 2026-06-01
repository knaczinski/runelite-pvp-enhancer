---
scope: documentation style and tier rules
load_when: writing or editing any markdown file in the project
---

# DOC STYLE

## 3-Tier Classification

**Tier 1 — AI-caveman** (machine-first, compressed, no log noise):
- `CLAUDE.md`, `PROJECT.md`, root `CONTEXT.md`, per-package `CONTEXT.md`
- everything under `.ai/` (rules, sessions, backlog, architecture)
- Rules: caveman lite. No filler. No pleasantries. No `## log` sections. Minimal paths.

**Tier 2 — Human-prose** (readable, full sentences, narrative):
- `README.md`, everything under `docs/`
- Rules: clear, complete sentences. Context for humans unfamiliar with the codebase. Diagrams allowed (ASCII or images). Cite reasoning, not just rules.

**Tier 3 — Code-Javadoc-prose** (in-source, context-rich, complete):
- All `.java` source comments and Javadoc blocks
- Rules: explain why, not what. Javadoc on every public class and public method in plugin/ and overlay/. Constants get inline rationale.

## Per-directory CONTEXT.md rules

Format: frontmatter + `purpose:` + `patterns:` + `constraint:` (if any).
No `## log` sections — session history lives in `.ai/sessions/` only.
Root `CONTEXT.md` tracks `latest_session:` as a single-line pointer, not a full log.
After each session: update root `CONTEXT.md` `latest_session:` and write session file.

## Caveman Lite

Drop: actually, basically, essentially, just, really, very, simply, perhaps, might, could potentially, I think, obviously
Drop pleasantries: Thanks, Great, Excellent, Sure, Of course
Keep: full sentences, articles (the, a), professional tone
Heading depth: ≤ 3 in Tier 1
Sentence length: ≤ 25 words in Tier 1 (warn, not block)
