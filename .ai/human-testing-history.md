---
purpose: completed human-assisted validations. archive of outcomes + the human reports that produced them.
format: append-only. newest at top.
rule: AI moves an HT item here when it PASSES (or is cancelled). On FAIL the item stays in .ai/human-testing.md and a B### is opened in .ai/backlog.md; record the FAIL→B### linkage here once the item is eventually retired.
mirror_of: this is to live testing what .ai/backlog-history.md is to development.
---

# HUMAN-TESTING HISTORY

## S013 — user validation pass

- **HT-002 — Tick history ordering / grouping / colours (B005): PASSED.** User confirmed working.
- **HT-003 — Hitsplats + gear swaps on the right tick (B003/B004): PASSED.** User confirmed working.
- **HT-004 — Config toggles take effect live (B006): PASSED.** User confirmed working.
- **HT-011 — XP-drop hit prediction precedes the hitsplat (B019): PASSED.** User confirmed working.
- **HT-010 — Combat focus hides only non-involved (B018): PARTIAL → fix shipped, re-test queued.**
  ANY_FIGHT left players who teleported in visible. Root cause: involvement counted any
  interaction (PvE/following). Fixed in B022 (ANY_FIGHT = player↔player only). HT-010 stays
  queued in human-testing.md for re-validation of the fix.
