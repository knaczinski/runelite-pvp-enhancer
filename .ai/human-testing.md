---
purpose: human-assisted live test / validation queue. The AI cannot run a RuneLite client; these items need a human at the game.
scope: OPEN items only, priority-ordered (top = do first). Completed validations move to .ai/human-testing-history.md.
codes: HT-NNN, never reused. Status: OPEN | IN-PROGRESS | PASSED | FAILED.
mirror_of: this is to live testing what .ai/backlog.md is to development.
---

# HUMAN-ASSISTED TESTING & VALIDATION

Some behaviour (overlay rendering, visual accuracy, in-game feel, performance) cannot be unit-tested — only a human running the plugin in a live OSRS client can confirm it. This file is the queue of those checks.

## How this works (read once)

1. **Pick the top OPEN item** (priority-ordered — top = do first).
2. **Ask the AI to guide you:** say *"run HT-001"* or *"start human testing"*.
   The AI walks through **one step at a time**, waits for your observation, then gives the next step.
3. **Build the plugin first:** `./gradlew build` → load via RuneLite developer mode or external plugin loader.
4. **Follow the SCENARIO SETUP** in the item. Complete all setup steps before starting the test.
5. **Report using the RESULT TEMPLATE** at the bottom. Concrete observations only — "seems ok" is not actionable.
6. **The AI decides the outcome:**
   - **PASS** → AI moves the item to `.ai/human-testing-history.md` and marks the linked B-item validated.
   - **FAIL** → AI creates a new `B###` backlog item, links it here, and leaves the HT item queued for re-test after the fix.

> AI protocol: present ONE numbered step at a time. Never dump all steps at once.
> After the final step, apply the RESULT TEMPLATE and do the bookkeeping.

---

No items yet.
