# Spike: resizing native overhead elements (skull, prayer icon, health bar, Vengeance text)

**Question.** The user asked to make four head-mounted elements size-configurable (per scope:
self / opponents / everyone): the PK **skull**, the **overhead protection-prayer icon**, the
**overhead health bar**, and the **Vengeance overhead text**.

**Finding.** The RuneLite API exposes **no scale control** for any natively-rendered overhead
element, and only allows hiding/replacing some of them. Verified against `runelite-api`
(`Actor`, `Player`) in the build classpath:

| Element | Relevant API | Can hide native? | Can resize? | Verdict |
|---|---|---|---|---|
| Vengeance text | `Actor.getOverheadText()` / `setOverheadText(String)` (+ `getOverheadCycle`) | Yes — clear the text | Only by drawing our own | ✅ **Feasible** (clear native + draw scaled copy) |
| PK skull | `Player.getSkullIcon()` / `setSkullIcon(int)` | Yes — `setSkullIcon(-1)` | Only by drawing our own | ⚠️ **Feasible but hacky** — needs a bundled skull sprite + mutating *other players'* client-side state and restoring it; side-effect risk |
| Overhead prayer icon | `Player.getOverheadIcon()` → `HeadIcon` (**read-only**) | No setter | No | ❌ **Not feasible** |
| Overhead health bar | `Actor.getHealthRatio()` / `getHealthScale()` (**read-only**) | No render hook (`RenderableDrawListener` is for models, not bars) | No | ❌ **Not feasible** |

There is no per-element draw callback: `Hooks` only offers `RenderableDrawListener` (whole
Renderable = model, used by Entity Hider). It cannot target a single overhead widget.

## Decision

- **Implemented:** Vengeance text resize (`VengeanceTextOverlay`). The plugin reads the native
  overhead text, clears it, and re-draws a scaled copy with its own fade. Config:
  `vengTextScope` (Off / Self / Self+opponents / Everyone) + `vengTextSize` (20–400 %, 100 % ≈
  native). Scope uses the shared `OverheadScope` enum.
- **Deferred (feasible but hacky):** skull resize. Would require a skull sprite asset and
  `setSkullIcon(-1)` on other players with careful restore. Revisit only if the user wants it
  despite the side-effect risk.
- **Dropped (API-blocked):** overhead prayer-icon and health-bar resize. No API to hide or
  scale them; would require client-internal access we should not take.

## Note on the per-scope "separate size" request

The original ask was for an independent size per group (self / opponents / everyone). Shipped
v1 uses one size + a scope selector (matching the heal/debuff pattern), which is simpler and
covers the common case. Independent per-group sizes can be a follow-up if needed.
