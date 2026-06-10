# Fixed-layout in Resizable — design (PK muscle memory)

Status: **first cut implemented (B030) from the Resizable-Classic dump; offsets are estimates —
live-tuning via Nudge X/Y + the fixed-mode (group 548) dump pending.**

## Mapped containers (Resizable-Classic, group 161; 919×1000 window, viewport centre ≈ 459,500)

- Inventory/tabs block: children **38..90 + 97** (anchor 97 = panel bg [678,665,241×335]).
- Minimap + orbs: children **19, 22–33, 95** (anchor 95 [708,0,211×207]).
- Chatbox: child **96** [0,835,519×165].

Spec bar (group 593): SP_ATTACKBAR = child 38 (150×26 @ y204), SPECIAL_ATTACK = child 39 (fill,
9 dyn children — must resize the children, not just the container).

## Why PKers use fixed mode (research)

- **Absolute-pixel muscle memory.** Fixed = a locked **765×503** client: the 3D viewport (~512×334,
  top-left) plus the bottom-right UI panel (tabs / inventory / prayer / spellbook / spec) at pixel
  positions that never change on any monitor. 1-tick switches (gear + prayer + attack) are done
  blind on memorised pixels. Resizable breaks this — the panel anchors to the window corner and
  moves/rescales with window size.
- **Minimal mouse travel.** The small area keeps the scene (where you click the opponent to
  attack/freeze) close to the inventory/prayer → faster, more consistent switches. A big window
  puts the opponent far from a corner-anchored inventory.
- **Blind spell/prayer clicking** (freeze = click opponent → click freeze → click opponent) relies
  on fixed coords.
- **Consistency** pixel-identical across setups.

What they give up: a tiny scene (poor wild/area visibility), cramped UI, a small box on big monitors.

## Goal

Let PKers run **Resizable-Classic** (bigger, full-window scene) while every click target lands
exactly where fixed-mode muscle memory expects.

## Decisions (validated)

1. **Target:** full fixed replica — UI blocks keep fixed-mode pixel sizes + offsets.
2. **Anchor:** relative to the **viewport centre** (where the character renders), NOT a window
   corner. So the inventory sits at fixed-mode's distance from the character → character↔inventory
   travel is identical; the 3D scene stays full-window (bigger). The UI block floats centre-right.
3. **Scope:** **per-element** toggles — (a) right panel (tabs + inventory/prayer/spellbook),
   (b) minimap + orbs (HP/prayer/run/spec), (c) chatbox. Lock whichever you want.
4. **Activation:** only when already in **Resizable-Classic**; master toggle; no-op in Fixed/Modern
   (do not force-switch the layout).

## Mechanism (hacky, accepted)

Each frame after the toplevel layout script runs (`onScriptPostFired` for the resize script, plus a
`ClientTick`/`GameTick` safety re-apply): for each enabled block, set its container widget's
`OriginalX/Y` (and width/height if needed) to `viewportCentre + fixedOffset`, then `revalidate()`.
Capture each widget's base geometry on first touch; restore + clear on disable/shutdown. Absolute
targets off the captured base → idempotent re-apply (the spec-bar lesson).

- **viewportCentre** = (`client.getViewportXOffset() + getViewportWidth()/2`,
  `getViewportYOffset() + getViewportHeight()/2`).
- **fixedOffset(block)** = block's fixed-mode top-left minus the fixed viewport centre (≈ 256,167
  within the 512×334 fixed viewport). Hard-coded from the known fixed geometry per block.

This fights the native relayout (same class as the combat-tab spec-bar resize) and is version-
sensitive; expect live tuning.

## OPEN — needed before implementation

- The **Resizable-Classic toplevel widget tree**: the container component ids for the right panel,
  minimap, orbs and chatbox, and their native bounds. Gather via the developer-mode dump, then the
  offsets/ids are pinned here.
- Plugin Hub note: relocating core UI is invasive and may conflict with other layout plugins / be
  scrutinised for the Hub. This is a dev-build feature for now.
