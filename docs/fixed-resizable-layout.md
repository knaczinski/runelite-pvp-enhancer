# Fixed-layout in Resizable — design (PK muscle memory)

Status: **second cut implemented (B030); root-only move + on-screen clamp; offsets pinned from the
fixed-mode (548) dump. Live Nudge X/Y tuning pending.**

## Mapped containers (Resizable-Classic, group 161)

Each block is moved by relocating **only its ROOT container** — children are laid out relative to
the root, so moving the root moves the whole block. (The first cut moved each child to an absolute
screen coord too; nested children then got `parentOffset + absoluteTarget` and flew off-screen —
the inventory "disappeared". Fixed by moving the root alone.)

- Inventory/tabs root: child **97** [524,168,241×335].
- Minimap + orbs root: child **95** [554,0,211×207].
- Chatbox root: child **96** [0,338,519×165].

## Offsets (measured from the fixed-mode 548 dump; fixed scene centre ≈ 260,171)

Block root top-left in fixed → offset from fixed scene centre:

- Inventory panel-bg ≈ (516,167) → **(256, -4)**.
- Minimap+orbs panel ≈ (516, 4) → **(256, -167)**.
- Chatbox = (0,338) → **(-260, 167)**.

In Resizable-Classic the target = `resizableSceneCentre + offset`, clamped fully on-screen. The
constants live in `util/FixedLayoutGeometry`, shared by the plugin (live move) and the guide overlay.

## Per-block restore

Each tick a block that is **not** pinned (its toggle off, master still on) is restored to its native
position individually — not only when the master switch is off. So unchecking one block returns it
home; the captured base is forgotten on restore and re-captured on re-enable.

## Fixed-size guide overlay (visual only)

`frShowGuide` draws reference outlines at the pinned positions without moving any widget: the fixed
client footprint (765×503, dashed), the fixed scene (512×334), and the inventory / minimap / chat
boxes for whichever blocks are enabled — clamped the same way as the live pin, so the guide matches
the real result. Use it to gauge the layout before/while tuning Nudge X/Y.

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
