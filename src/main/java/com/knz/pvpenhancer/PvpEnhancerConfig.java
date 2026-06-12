package com.knz.pvpenhancer;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

/**
 * User-configurable settings for the PvP Enhancer plugin.
 *
 * <p>Sections are grouped by where the feature draws: screen alerts, targeting aids (on the target),
 * overheads (over heads), PID, click helpers, ghostify, and the resizable layout. A handful of
 * sidebar-panel settings are {@code hidden = true} here and edited inline in {@code PvpEnhancerPanel}.
 * RuneLite persists all values automatically regardless of where they are edited.
 */
@ConfigGroup("pvpenhancer")
public interface PvpEnhancerConfig extends Config
{
	// ─── Sections ───────────────────────────────────────────────────────────

	@ConfigSection(name = "Tracking", description = "What the plugin records for the sidebar panel.", position = 0)
	String trackingSection = "tracking";

	@ConfigSection(name = "Screen alerts", description = "Full-screen / target-agnostic combat alerts.", position = 1)
	String alertsSection = "alerts";

	@ConfigSection(name = "Targeting aids", description = "Aids drawn on or about your target and your switches.", position = 2)
	String targetingSection = "targeting";

	@ConfigSection(name = "Overheads",
		description = "Info drawn over players' heads — healing, freeze/TB timers, and resized "
			+ "Vengeance text / PK skull. Each has its own scope (who it applies to).",
		position = 3)
	String overheadSection = "overhead";

	@ConfigSection(name = "PID guess", description = "Experimental 1v1 PID (processing-order) indicator.", position = 4)
	String pidSection = "pid";

	@ConfigSection(name = "Click helpers", description = "Left/right-click tweaks to avoid mis-clicks in combat.", position = 5)
	String clicksSection = "clicks";

	@ConfigSection(name = "Ghostify",
		description = "Reduce characters to just a coloured outline (hidden model + contour). "
			+ "Set, per category, WHEN to ghostify and the outline COLOUR. You stay attackable; "
			+ "to ghost your own model you also need Entity Hider's 'Hide Local Player'.",
		position = 6, closedByDefault = true)
	String ghostifySection = "ghostify";

	@ConfigSection(name = "Fixed layout in resizable",
		description = "EXPERIMENTAL. In Resizable-Classic, pin the inventory/minimap/chat to fixed-mode's "
			+ "distance from your character so fixed-mode muscle memory carries over. Includes the "
			+ "taller spec bar. See docs/fixed-resizable-layout.md.",
		position = 7, closedByDefault = true)
	String layoutSection = "layout";

	@ConfigSection(name = "Developer", description = "Tools for testing overlays without a live fight.", position = 8, closedByDefault = true)
	String developerSection = "developer";

	// ─── Tracking ───────────────────────────────────────────────────────────

	@ConfigItem(keyName = "trackScope", name = "Record for",
		description = "Which players the tick history and hit summary record events for. "
			+ "Self + opponents = only you and players currently fighting you; Everyone = all visible players.",
		section = trackingSection, position = 0)
	default TrackScope trackScope() { return TrackScope.EVERYONE; }

	@ConfigItem(keyName = "trackNpcs", name = "Track NPCs",
		description = "Record combat events for NPCs. Useful for testing without a second player.",
		section = trackingSection, position = 1)
	default boolean trackNpcs() { return false; }

	// ─── Screen alerts ────────────────────────────────────────────────────────

	@ConfigItem(keyName = "showHeartbeat", name = "Heartbeat",
		description = "Show a red vignette pulsing once per game tick while in combat.",
		section = alertsSection, position = 0)
	default boolean showHeartbeat() { return true; }

	@Range(min = 10, max = 250)
	@ConfigItem(keyName = "heartbeatDepth", name = "Vignette size",
		description = "How far the heartbeat vignette reaches inward from the viewport edges (px). Default 80.",
		section = alertsSection, position = 1)
	default int heartbeatDepth() { return 80; }

	@Range(min = 5, max = 100)
	@ConfigItem(keyName = "heartbeatIntensity", name = "Vignette intensity",
		description = "Peak opacity (%) of the heartbeat vignette at the start of each tick. Default 55.",
		section = alertsSection, position = 2)
	default int heartbeatIntensity() { return 55; }

	@ConfigItem(keyName = "showNotRetaliating", name = "Not attacking",
		description = "Flash the opponent's outline (red/yellow) when in combat but not attacking "
			+ "them for >= 2 ticks (you walked off, looted, or mis-clicked).",
		section = alertsSection, position = 3)
	default boolean showNotRetaliating() { return true; }

	@ConfigItem(keyName = "comboPopups", name = "Combo popups",
		description = "When the floating combo popup (e.g. GODLIKE SWITCH, TRIPLE EAT) is shown. "
			+ "'In combat' = only while fighting (avoids banking false-triggers); 'Always' = also out "
			+ "of combat (useful for testing); 'Never' = detect but don't pop. Needs combos enabled.",
		section = alertsSection, position = 4)
	default ComboPopups comboPopups() { return ComboPopups.IN_COMBAT; }

	@ConfigItem(keyName = "specBar", name = "Spec bar HUD",
		description = "Draw a large, always-readable special-attack bar (green = ready %) as a movable "
			+ "overlay — drag it where you want. Independent of the cramped Combat Options tab bar.",
		section = alertsSection, position = 5)
	default boolean specBar() { return false; }

	@Range(min = 10, max = 60)
	@ConfigItem(keyName = "specBarHeight", name = "Spec bar height",
		description = "Height (px) of the spec bar HUD. Default 22.",
		section = alertsSection, position = 6)
	default int specBarHeight() { return 22; }

	@Range(min = 60, max = 400)
	@ConfigItem(keyName = "specBarWidth", name = "Spec bar width",
		description = "Width (px) of the spec bar HUD. Default 140.",
		section = alertsSection, position = 7)
	default int specBarWidth() { return 140; }

	// ─── Targeting aids ─────────────────────────────────────────────────────────

	@ConfigItem(keyName = "hitPrediction", name = "Hit predict",
		description = "Show your outgoing damage near your target. Derived from the Hitpoints XP drop "
			+ "(appears before ranged/magic projectiles land). Where XP is blocked (Duel/PvP Arena) it "
			+ "falls back to your hitsplat on the target, so it still works there (not predictive).",
		section = targetingSection, position = 0)
	default boolean hitPrediction() { return true; }

	@Range(min = 8, max = 48)
	@ConfigItem(keyName = "hitPredictionSize", name = "Hit predict size",
		description = "Font size of the predicted-damage number. Default 15.",
		section = targetingSection, position = 1)
	default int hitPredictionSize() { return 15; }

	@ConfigItem(keyName = "hitPredictAnchor", name = "Hit predict spot",
		description = "Where the predicted-damage number appears: over the opponent, or over your own "
			+ "character next to the health bar (the same spot as heal numbers).",
		section = targetingSection, position = 2)
	default HitPredictAnchor hitPredictAnchor() { return HitPredictAnchor.OPPONENT; }

	@Range(max = 100)
	@ConfigItem(keyName = "hitPredictThreshold", name = "Spec-combo HP cue %",
		description = "When the opponent's estimated HP AFTER this predicted hit drops to this % of "
			+ "their max HP or below, the predicted number shows bigger and redder — your cue to start "
			+ "the switch + special before the hitsplat lands. 0 = off. HP is an estimate (health bar).",
		section = targetingSection, position = 3)
	default int hitPredictThreshold() { return 0; }

	@ConfigItem(keyName = "prayerHighlight", name = "Def prayer hint",
		description = "Highlight the PROTECTION prayer matching your current target's equipped "
			+ "weapon style (predictive). Also flags the prayer tab button so you notice with the "
			+ "inventory open.",
		section = targetingSection, position = 4)
	default boolean prayerHighlight() { return false; }

	@ConfigItem(keyName = "offensivePrayerMode", name = "Off prayer hint",
		description = "Highlight your offensive prayer vs weapon. 'Prayer from weapon' = your equipped "
			+ "weapon highlights Piety/Rigour/Augury in the prayer tab. 'Weapon from prayer' = your "
			+ "active offensive prayer highlights a matching weapon in your inventory.",
		section = targetingSection, position = 5)
	default OffensivePrayerMode offensivePrayerMode() { return OffensivePrayerMode.OFF; }

	@ConfigItem(keyName = "attackTimerSelf", name = "Atk timer: self",
		description = "Show a countdown (seconds, above the head/skull) until YOU can attack again.",
		section = targetingSection, position = 6)
	default boolean attackTimerSelf() { return false; }

	@ConfigItem(keyName = "attackTimerOpponents", name = "Atk timer: opponents",
		description = "Show the attack-again countdown over players fighting you. Speed from a "
			+ "weapon table (best-effort); resets on eat/drink.",
		section = targetingSection, position = 7)
	default boolean attackTimerOpponents() { return false; }

	@ConfigItem(keyName = "attackTimerOthers", name = "Atk timer: others",
		description = "Show the attack-again countdown over other players.",
		section = targetingSection, position = 8)
	default boolean attackTimerOthers() { return false; }

	// ─── Overheads ──────────────────────────────────────────────────────────

	@ConfigItem(keyName = "healDisplayMode", name = "Healing",
		description = "Show recovered HP near the health bar of whoever healed. Opponent amounts are "
			+ "estimates (~) — using real max HP (NPC table + OSRS Hiscores) but bounded by the health "
			+ "bar resolution. Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 0)
	default HealDisplayMode healDisplayMode() { return HealDisplayMode.EVERYONE; }

	@Range(min = 8, max = 48)
	@ConfigItem(keyName = "healSize", name = "Heal size",
		description = "Font size of the healing number. Default 14.",
		section = overheadSection, position = 1)
	default int healSize() { return 14; }

	@ConfigItem(keyName = "healNumberStyle", name = "Heal style",
		description = "'Heal only' shows just the recovered HP (+25). 'Before + heal = total' shows "
			+ "the full breakdown (65 + 25 = 90); remote players are estimates (~).",
		section = overheadSection, position = 2)
	default HealNumberStyle healNumberStyle() { return HealNumberStyle.AMOUNT; }

	@ConfigItem(keyName = "debuffTimers", name = "Freeze/TB",
		description = "Freeze / snare / teleblock timers: an icon + seconds countdown over affected "
			+ "players. Teleblock is tracked as half (~2.5 min) when the target prayed Magic as it landed.",
		section = overheadSection, position = 3)
	default DebuffScope debuffTimers() { return DebuffScope.OFF; }

	@ConfigItem(keyName = "vengTextScope", name = "Veng resize",
		description = "Re-render the 'Vengeance!' overhead text at a custom size (the native size "
			+ "is not resizable via the API, so the original is replaced). Scope 'Others' = everyone "
			+ "except you and your opponents.",
		section = overheadSection, position = 4)
	default OverheadScope vengTextScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "vengTextSize", name = "Veng size",
		description = "Size (%) of the re-rendered Vengeance text. 100% ≈ the native size.",
		section = overheadSection, position = 5)
	default int vengTextSize() { return 100; }

	@ConfigItem(keyName = "skullScope", name = "Skull resize",
		description = "Re-render the PK skull at a custom size (the native size is not resizable, "
			+ "so the native skull is hidden and replaced). Only the regular skull is handled. "
			+ "Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 6)
	default OverheadScope skullScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "skullSize", name = "Skull size",
		description = "Size (%) of the re-rendered PK skull. 100% ≈ the native size.",
		section = overheadSection, position = 7)
	default int skullSize() { return 100; }

	// ─── PID guess ──────────────────────────────────────────────────────────

	@ConfigItem(keyName = "pidIndicator", name = "PID guess (exp)",
		description = "EXPERIMENTAL. Only works in a 1v1 you're part of. Draws a small star (with a "
			+ "tiny 'pid' label) over the head of whoever likely has PID (processing order) — green = "
			+ "you, red = them, grey = still computing — and flashes on a likely PID swap. PID isn't "
			+ "exposed by the API; this is a noisy best-effort vote from contested same-tick hits, not "
			+ "certainty. See docs/pid-indicator-spike.md.",
		section = pidSection, position = 0)
	default boolean pidIndicator() { return false; }

	// ─── Click helpers ──────────────────────────────────────────────────────

	@ConfigItem(keyName = "combatPlayerMenuFilter", name = "Combat right-click filter",
		description = "While in combat, right-clicking a player shows only \"Walk here\" and "
			+ "\"Attack\" — hides Follow/Trade/Report/etc. to avoid mis-clicks.",
		section = clicksSection, position = 0)
	default boolean combatPlayerMenuFilter() { return false; }

	@ConfigItem(keyName = "swapPickupInCombat", name = "Walk over Take (combat)",
		description = "While in combat, de-prioritise ground-item \"Take\" so a left-click "
			+ "walks instead of picking up (avoids breaking your attack). Take stays on right-click.",
		section = clicksSection, position = 1)
	default boolean swapPickupInCombat() { return false; }

	// ─── Ghostify ──────────────────────────────────────────────────────────────
	// When (per category) + outline colour (per category). Priority when a player fits several:
	// opponents > group (CC/FC) > friends > others. "In combat" = that character is fighting.

	@ConfigItem(keyName = "ghostifySelf", name = "Self — when",
		description = "When to ghostify your own character. Needs Entity Hider's 'Hide Local "
			+ "Player' to also hide your model; this controls the outline.",
		section = ghostifySection, position = 0)
	default GhostifyWhen ghostifySelf() { return GhostifyWhen.NEVER; }

	@Alpha
	@ConfigItem(keyName = "ghostColorSelf", name = "Self — colour",
		description = "Outline colour for your own ghostified character.",
		section = ghostifySection, position = 1)
	default Color ghostColorSelf() { return new Color(0xEC, 0xE1, 0x2C); }

	@ConfigItem(keyName = "ghostifyOpponents", name = "Opponents — when",
		description = "When to ghostify players currently fighting you.",
		section = ghostifySection, position = 2)
	default GhostifyWhen ghostifyOpponents() { return GhostifyWhen.NEVER; }

	@Alpha
	@ConfigItem(keyName = "ghostColorOpponents", name = "Opponents — colour",
		description = "Outline colour for ghostified opponents.",
		section = ghostifySection, position = 3)
	default Color ghostColorOpponents() { return new Color(0xFF, 0x40, 0x40); }

	@ConfigItem(keyName = "ghostifyGroup", name = "Group — when",
		description = "When to ghostify your group (clan + friends-chat members).",
		section = ghostifySection, position = 4)
	default GhostifyWhen ghostifyGroup() { return GhostifyWhen.NEVER; }

	@Alpha
	@ConfigItem(keyName = "ghostColorGroup", name = "Group — colour",
		description = "Outline colour for ghostified clan / friends-chat members.",
		section = ghostifySection, position = 5)
	default Color ghostColorGroup() { return new Color(0x4D, 0x96, 0xFF); }

	@ConfigItem(keyName = "ghostifyFriends", name = "Friends — when",
		description = "When to ghostify players on your friends list.",
		section = ghostifySection, position = 6)
	default GhostifyWhen ghostifyFriends() { return GhostifyWhen.NEVER; }

	@Alpha
	@ConfigItem(keyName = "ghostColorFriends", name = "Friends — colour",
		description = "Outline colour for ghostified friends.",
		section = ghostifySection, position = 7)
	default Color ghostColorFriends() { return new Color(0x25, 0xE7, 0x25); }

	@ConfigItem(keyName = "ghostifyOthers", name = "Others — when",
		description = "When to ghostify everyone else. 'Can't attack here' = their combat level "
			+ "is outside your attackable range at the current Wilderness level; '+ idle' also requires "
			+ "them to not be in combat.",
		section = ghostifySection, position = 8)
	default GhostifyOthersWhen ghostifyOthers() { return GhostifyOthersWhen.NEVER; }

	@Alpha
	@ConfigItem(keyName = "ghostColorOthers", name = "Others — colour",
		description = "Outline colour for ghostified other players.",
		section = ghostifySection, position = 9)
	default Color ghostColorOthers() { return new Color(0xBD, 0xBD, 0xBD); }

	// Show the ghostified player's 2D overheads (name + chat message) even though the model is
	// hidden. Off = fully hidden. Best-effort: the 2D overhead is shown/hidden as a whole.

	@ConfigItem(keyName = "ghostChatSelf", name = "Self — show chat",
		description = "Show your own name/chat overhead while ghostified.",
		section = ghostifySection, position = 10)
	default boolean ghostChatSelf() { return false; }

	@ConfigItem(keyName = "ghostChatOpponents", name = "Opponents — show chat",
		description = "Show ghostified opponents' name/chat overhead.",
		section = ghostifySection, position = 11)
	default boolean ghostChatOpponents() { return false; }

	@ConfigItem(keyName = "ghostChatGroup", name = "Group — show chat",
		description = "Show ghostified clan / friends-chat members' name/chat overhead.",
		section = ghostifySection, position = 12)
	default boolean ghostChatGroup() { return false; }

	@ConfigItem(keyName = "ghostChatFriends", name = "Friends — show chat",
		description = "Show ghostified friends' name/chat overhead.",
		section = ghostifySection, position = 13)
	default boolean ghostChatFriends() { return false; }

	@ConfigItem(keyName = "ghostChatOthers", name = "Others — show chat",
		description = "Show ghostified other players' name/chat overhead.",
		section = ghostifySection, position = 14)
	default boolean ghostChatOthers() { return false; }

	// ─── Fixed layout in resizable ───────────────────────────────────────────────

	@ConfigItem(keyName = "fixedResizableLayout", name = "Enable",
		description = "Master switch. Only acts in Resizable-Classic. Pins enabled UI blocks to "
			+ "fixed-mode positions relative to your character. EXPERIMENTAL — fights the native "
			+ "relayout; expect tuning.",
		section = layoutSection, position = 0)
	default boolean fixedResizableLayout() { return false; }

	@ConfigItem(keyName = "frInventory", name = "Pin inventory",
		description = "Move the tabs + inventory/prayer/spellbook panel to its fixed-mode distance "
			+ "from your character.",
		section = layoutSection, position = 1)
	default boolean frInventory() { return true; }

	@ConfigItem(keyName = "frMinimap", name = "Pin minimap",
		description = "Move the minimap and orbs to their fixed-mode position.",
		section = layoutSection, position = 2)
	default boolean frMinimap() { return false; }

	@ConfigItem(keyName = "frChat", name = "Pin chat",
		description = "Move the chatbox to its fixed-mode position.",
		section = layoutSection, position = 3)
	default boolean frChat() { return false; }

	@ConfigItem(keyName = "frShowGuide", name = "Show guide",
		description = "Show the fixed-mode outline (scene + inventory/minimap/chat boxes). Hold Alt and "
			+ "drag it (yellow outline) to position the whole fixed layout — the pinned blocks follow.",
		section = layoutSection, position = 4)
	default boolean frShowGuide() { return false; }

	// ─── Developer ────────────────────────────────────────────────────────────

	@ConfigItem(keyName = "developerMode", name = "Developer mode",
		description = "Show a Developer panel in the sidebar for triggering mock overlays "
			+ "(heal, hit predict, debuff, combo) on yourself without a live fight.",
		section = developerSection, position = 0)
	default boolean developerMode() { return false; }

	// ─── Controlled inline in the sidebar panel (hidden from the config panel) ──
	// These are edited next to their block in PvpEnhancerPanel.

	@ConfigItem(keyName = "maxHistoryTicks", name = "Max history ticks",
		description = "How many ticks of history to keep.", hidden = true)
	default int maxHistoryTicks() { return 20; }

	@ConfigItem(keyName = "showCombat", name = "Show combat",
		description = "Show attack and hitsplat events.", hidden = true)
	default boolean showCombat() { return true; }

	@ConfigItem(keyName = "showEating", name = "Show eating",
		description = "Show eat and drink events.", hidden = true)
	default boolean showEating() { return true; }

	@ConfigItem(keyName = "showGearSwap", name = "Show gear swaps",
		description = "Show equipment change events.", hidden = true)
	default boolean showGearSwap() { return true; }

	@ConfigItem(keyName = "showPrayer", name = "Show prayers",
		description = "Show overhead protection prayer changes.", hidden = true)
	default boolean showPrayer() { return true; }

	@ConfigItem(keyName = "showCombos", name = "Enable combos",
		description = "Detect and display combo feedback.", hidden = true)
	default boolean showCombos() { return true; }

	@ConfigItem(keyName = "showHitSummary", name = "Enable hit summary",
		description = "Show the hit summary table.", hidden = true)
	default boolean showHitSummary() { return true; }

	@ConfigItem(keyName = "hitSummaryRows", name = "Hit summary rows",
		description = "Maximum number of rows in the hit summary.", hidden = true)
	default int hitSummaryRows() { return 25; }
}
