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
 * <p>Two groups of settings:
 * <ul>
 *   <li><b>Shown in the RuneLite config panel</b> — Tracking, Heartbeat, Indicators
 *   (settings for screen-view behaviour that has no sidebar block).</li>
 *   <li><b>Controlled inline in the sidebar panel</b> ({@code hidden = true} here) — the
 *   tick-history filters / depth and the hit-summary toggle / row cap live next to their
 *   block in {@code PvpEnhancerPanel}, not in the config panel.</li>
 * </ul>
 * RuneLite persists all values automatically regardless of where they are edited.
 */
@ConfigGroup("pvpenhancer")
public interface PvpEnhancerConfig extends Config
{
	// ─── Sections ───────────────────────────────────────────────────────────

	@ConfigSection(name = "Tracking", description = "What the plugin records for the sidebar panel.", position = 0)
	String trackingSection = "tracking";

	@ConfigSection(name = "Combat assist", description = "On-screen combat aids and alerts.", position = 1)
	String combatSection = "combat";

	@ConfigSection(name = "Overhead displays",
		description = "Info drawn over players' heads — healing, debuff timers, and resized "
			+ "Vengeance text / PK skull. Each has its own scope (who it applies to).",
		position = 2)
	String overheadSection = "overhead";

	@ConfigSection(name = "Ghostify",
		description = "Reduce characters to just a coloured outline (hidden model + contour). "
			+ "Set, per category, WHEN to ghostify and the outline COLOUR. You stay attackable; "
			+ "to ghost your own model you also need Entity Hider's 'Hide Local Player'.",
		position = 3, closedByDefault = true)
	String ghostifySection = "ghostify";

	@ConfigSection(name = "Fixed-resizable layout",
		description = "EXPERIMENTAL. In Resizable-Classic, pin the inventory/minimap/chat to fixed-mode's "
			+ "distance from your character (viewport centre) so fixed-mode muscle memory carries over. "
			+ "See docs/fixed-resizable-layout.md.",
		position = 4, closedByDefault = true)
	String fixedResizableSection = "fixedresizable";

	@ConfigSection(name = "Developer", description = "Tools for testing overlays without a live fight.", position = 5, closedByDefault = true)
	String developerSection = "developer";

	// ─── Tracking ───────────────────────────────────────────────────────────

	@ConfigItem(keyName = "trackScope", name = "Record events for",
		description = "Which players the tick history and hit summary record events for. "
			+ "Self + opponents = only you and players currently fighting you; Everyone = all visible players.",
		section = trackingSection, position = 0)
	default TrackScope trackScope() { return TrackScope.EVERYONE; }

	@ConfigItem(keyName = "trackNpcs", name = "Track NPCs (testing)",
		description = "Record combat events for NPCs. Useful for testing without a second player.",
		section = trackingSection, position = 1)
	default boolean trackNpcs() { return false; }

	// ─── Combat assist ──────────────────────────────────────────────────────

	@ConfigItem(keyName = "showHeartbeat", name = "Heartbeat vignette",
		description = "Show a red vignette pulsing once per game tick while in combat.",
		section = combatSection, position = 0)
	default boolean showHeartbeat() { return true; }

	@Range(min = 10, max = 250)
	@ConfigItem(keyName = "heartbeatDepth", name = "Vignette size (px)",
		description = "How far the heartbeat vignette reaches inward from the viewport edges. Default 80.",
		section = combatSection, position = 0)
	default int heartbeatDepth() { return 80; }

	@Range(min = 5, max = 100)
	@ConfigItem(keyName = "heartbeatIntensity", name = "Vignette intensity (%)",
		description = "Peak opacity of the heartbeat vignette at the start of each tick. Default 55.",
		section = combatSection, position = 0)
	default int heartbeatIntensity() { return 55; }

	@ConfigItem(keyName = "showNotRetaliating", name = "Not-attacking warning",
		description = "Flash the opponent's outline (red/yellow) when in combat but not attacking "
			+ "them for >= 2 ticks (you walked off, looted, or mis-clicked).",
		section = combatSection, position = 1)
	default boolean showNotRetaliating() { return true; }

	@ConfigItem(keyName = "hitPrediction", name = "Hit prediction (XP)",
		description = "Show predicted outgoing damage near your target, derived from the "
			+ "Hitpoints XP drop — appears before ranged/magic projectiles land.",
		section = combatSection, position = 2)
	default boolean hitPrediction() { return true; }

	@Range(min = 8, max = 48)
	@ConfigItem(keyName = "hitPredictionSize", name = "Hit prediction text size",
		description = "Font size of the predicted-damage number. Default 15.",
		section = combatSection, position = 3)
	default int hitPredictionSize() { return 15; }

	@Range(max = 100)
	@ConfigItem(keyName = "hitPredictThreshold", name = "Spec-combo HP cue (%)",
		description = "When the opponent's estimated HP AFTER this predicted hit drops to this % of "
			+ "their max HP or below, the predicted number shows bigger and redder — your cue to start "
			+ "the switch + special before the hitsplat lands. 0 = off. HP is an estimate (health bar).",
		section = combatSection, position = 3)
	default int hitPredictThreshold() { return 0; }

	@ConfigItem(keyName = "prayerHighlight", name = "Defensive prayer highlighter",
		description = "Highlight the PROTECTION prayer matching your current target's equipped "
			+ "weapon style (predictive). Also flags the prayer tab button so you notice with the "
			+ "inventory open.",
		section = combatSection, position = 4)
	default boolean prayerHighlight() { return false; }

	@Range(max = 80)
	@ConfigItem(keyName = "specBarExtraHeight", name = "Taller spec bar (px)",
		description = "Grow the special-attack bar in the Combat Options tab taller by this many "
			+ "pixels (upward) and shrink the attack-style boxes to make room. 0 = off. Native layout "
			+ "is restored when set back to 0 / on tab rebuild.",
		section = combatSection, position = 6)
	default int specBarExtraHeight() { return 0; }

	@ConfigItem(keyName = "offensivePrayerMode", name = "Offensive prayer highlighter",
		description = "Highlight your offensive prayer vs weapon. 'Prayer from weapon' = your equipped "
			+ "weapon highlights Piety/Rigour/Augury in the prayer tab. 'Weapon from prayer' = your "
			+ "active offensive prayer highlights a matching weapon in your inventory.",
		section = combatSection, position = 5)
	default OffensivePrayerMode offensivePrayerMode() { return OffensivePrayerMode.OFF; }

	@ConfigItem(keyName = "combatPlayerMenuFilter", name = "Walk/Att Restrict. Menu in combat",
		description = "While in combat, right-clicking a player shows only \"Walk here\" and "
			+ "\"Attack\" — hides Follow/Trade/Report/etc. to avoid mis-clicks.",
		section = combatSection, position = 8)
	default boolean combatPlayerMenuFilter() { return false; }

	@ConfigItem(keyName = "swapPickupInCombat", name = "Walk-here over Take (in combat)",
		description = "While in combat, de-prioritise ground-item \"Take\" so a left-click "
			+ "walks instead of picking up (avoids breaking your attack). Take stays on right-click.",
		section = combatSection, position = 6)
	default boolean swapPickupInCombat() { return false; }

	@ConfigItem(keyName = "pidIndicator", name = "PID guess (experimental)",
		description = "EXPERIMENTAL. In a 1v1 you're part of, guess who has PID (processing order) "
			+ "from contested same-tick hits, and warn on a likely PID swap. PID isn't exposed by the "
			+ "API — this is a noisy best-effort vote, not certainty. See docs/pid-indicator-spike.md.",
		section = combatSection, position = 7)
	default boolean pidIndicator() { return false; }

	@ConfigItem(keyName = "comboPopups", name = "Combo popups",
		description = "When the floating combo popup (e.g. GODLIKE SWITCH, TRIPLE EAT) is shown. "
			+ "'In combat' = only while fighting (avoids banking false-triggers); 'Always' = also out "
			+ "of combat (useful for testing); 'Never' = detect but don't pop. Needs combos enabled.",
		section = combatSection, position = 9)
	default ComboPopups comboPopups() { return ComboPopups.IN_COMBAT; }

	// ─── Overhead displays ──────────────────────────────────────────────────

	@ConfigItem(keyName = "healDisplayMode", name = "Healing",
		description = "Show recovered HP near the health bar of whoever healed. Opponent amounts are "
			+ "estimates (~) — using real max HP (NPC table + OSRS Hiscores) but bounded by the health "
			+ "bar resolution. Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 0)
	default HealDisplayMode healDisplayMode() { return HealDisplayMode.EVERYONE; }

	@Range(min = 8, max = 48)
	@ConfigItem(keyName = "healSize", name = "Healing text size",
		description = "Font size of the healing number. Default 14.",
		section = overheadSection, position = 1)
	default int healSize() { return 14; }

	@ConfigItem(keyName = "debuffTimers", name = "Freeze/TB",
		description = "Freeze / snare / teleblock timers: an icon + seconds countdown over affected "
			+ "players. Teleblock is tracked as half (~2.5 min) when the target prayed Magic as it landed.",
		section = overheadSection, position = 2)
	default DebuffScope debuffTimers() { return DebuffScope.OFF; }

	@ConfigItem(keyName = "vengTextScope", name = "Veng resize",
		description = "Re-render the 'Vengeance!' overhead text at a custom size (the native size "
			+ "is not resizable via the API, so the original is replaced). Scope 'Others' = everyone "
			+ "except you and your opponents.",
		section = overheadSection, position = 3)
	default OverheadScope vengTextScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "vengTextSize", name = "Veng text size (%)",
		description = "Size of the re-rendered Vengeance text. 100% ≈ the native size.",
		section = overheadSection, position = 4)
	default int vengTextSize() { return 100; }

	@ConfigItem(keyName = "skullScope", name = "Skull resize",
		description = "Re-render the PK skull at a custom size (the native size is not resizable, "
			+ "so the native skull is hidden and replaced). Only the regular skull is handled. "
			+ "Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 5)
	default OverheadScope skullScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "skullSize", name = "PK skull size (%)",
		description = "Size of the re-rendered PK skull. 100% ≈ the native size.",
		section = overheadSection, position = 6)
	default int skullSize() { return 100; }

	@ConfigItem(keyName = "attackTimerSelf", name = "Attack timer — self",
		description = "Show a countdown (seconds, above the head/skull) until YOU can attack again.",
		section = overheadSection, position = 7)
	default boolean attackTimerSelf() { return false; }

	@ConfigItem(keyName = "attackTimerOpponents", name = "Attack timer — opponents",
		description = "Show the attack-again countdown over players fighting you. Speed from a "
			+ "weapon table (best-effort); resets on eat/drink.",
		section = overheadSection, position = 8)
	default boolean attackTimerOpponents() { return false; }

	@ConfigItem(keyName = "attackTimerOthers", name = "Attack timer — others",
		description = "Show the attack-again countdown over other players.",
		section = overheadSection, position = 9)
	default boolean attackTimerOthers() { return false; }

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
			+ "is outside your attackable range at the current Wilderness level.",
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

	// ─── Fixed-resizable layout ─────────────────────────────────────────────────

	@ConfigItem(keyName = "fixedResizableLayout", name = "Enable",
		description = "Master switch. Only acts in Resizable-Classic. Pins enabled UI blocks to "
			+ "fixed-mode positions relative to your character. EXPERIMENTAL — fights the native "
			+ "relayout; expect tuning.",
		section = fixedResizableSection, position = 0)
	default boolean fixedResizableLayout() { return false; }

	@ConfigItem(keyName = "frInventory", name = "Pin inventory / tabs",
		description = "Move the tabs + inventory/prayer/spellbook panel to its fixed-mode distance "
			+ "from your character.",
		section = fixedResizableSection, position = 1)
	default boolean frInventory() { return true; }

	@ConfigItem(keyName = "frMinimap", name = "Pin minimap + orbs",
		description = "Move the minimap and orbs to their fixed-mode position.",
		section = fixedResizableSection, position = 2)
	default boolean frMinimap() { return false; }

	@ConfigItem(keyName = "frChat", name = "Pin chatbox",
		description = "Move the chatbox to its fixed-mode position.",
		section = fixedResizableSection, position = 3)
	default boolean frChat() { return false; }

	@Range(min = -400, max = 400)
	@ConfigItem(keyName = "frNudgeX", name = "Nudge X (px)",
		description = "Fine-tune: shift all pinned blocks horizontally. Tune until clicks land where "
			+ "fixed-mode muscle memory expects.",
		section = fixedResizableSection, position = 4)
	default int frNudgeX() { return 0; }

	@Range(min = -400, max = 400)
	@ConfigItem(keyName = "frNudgeY", name = "Nudge Y (px)",
		description = "Fine-tune: shift all pinned blocks vertically.",
		section = fixedResizableSection, position = 5)
	default int frNudgeY() { return 0; }

	@ConfigItem(keyName = "frShowGuide", name = "Show fixed-size guide",
		description = "Draw reference outlines of the fixed-mode client (scene + inventory, minimap "
			+ "and chat boxes) at their pinned positions, so you can gauge the layout. Visual only.",
		section = fixedResizableSection, position = 6)
	default boolean frShowGuide() { return false; }

	@ConfigItem(keyName = "frClampOnScreen", name = "Keep blocks on-screen",
		description = "Pull a pinned block back inside the window if it would extend past an edge. "
			+ "ON prevents blocks vanishing on a narrow window, but on a window as narrow as fixed it "
			+ "drags the inventory closer to your character than fixed distance. Turn OFF for exact "
			+ "fixed distance (use a window wide enough that the block still fits).",
		section = fixedResizableSection, position = 7)
	default boolean frClampOnScreen() { return true; }

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
