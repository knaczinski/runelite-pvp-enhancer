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

	@ConfigSection(name = "Developer", description = "Tools for testing overlays without a live fight.", position = 4, closedByDefault = true)
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

	@ConfigItem(keyName = "prayerHighlight", name = "Prayer highlighter",
		description = "Highlight the protection prayer matching your current target's equipped "
			+ "weapon style (predictive). Switches as they switch weapons.",
		section = combatSection, position = 4)
	default boolean prayerHighlight() { return false; }

	@ConfigItem(keyName = "combatPlayerMenuFilter", name = "Only Walk-here + Attack on players (in combat)",
		description = "While in combat, right-clicking a player shows only \"Walk here\" and "
			+ "\"Attack\" — hides Follow/Trade/Report/etc. to avoid mis-clicks.",
		section = combatSection, position = 5)
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

	// ─── Overhead displays ──────────────────────────────────────────────────

	@ConfigItem(keyName = "healDisplayMode", name = "Healing",
		description = "Show recovered HP near the health bar of whoever healed. Opponent "
			+ "amounts are estimates (~) — the API only exposes their health ratio, not real HP. "
			+ "Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 0)
	default HealDisplayMode healDisplayMode() { return HealDisplayMode.EVERYONE; }

	@ConfigItem(keyName = "debuffTimers", name = "Freeze/TB",
		description = "Freeze / snare / teleblock timers: an icon + seconds countdown over affected "
			+ "players. Teleblock is tracked as half (~2.5 min) when the target prayed Magic as it landed.",
		section = overheadSection, position = 1)
	default DebuffScope debuffTimers() { return DebuffScope.OFF; }

	@ConfigItem(keyName = "vengTextScope", name = "Veng resize",
		description = "Re-render the 'Vengeance!' overhead text at a custom size (the native size "
			+ "is not resizable via the API, so the original is replaced). Scope 'Others' = everyone "
			+ "except you and your opponents.",
		section = overheadSection, position = 2)
	default OverheadScope vengTextScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "vengTextSize", name = "Vengeance text size (%)",
		description = "Size of the re-rendered Vengeance text. 100% ≈ the native size.",
		section = overheadSection, position = 3)
	default int vengTextSize() { return 100; }

	@ConfigItem(keyName = "skullScope", name = "Skull resize",
		description = "Re-render the PK skull at a custom size (the native size is not resizable, "
			+ "so the native skull is hidden and replaced). Only the regular skull is handled. "
			+ "Scope 'Others' = everyone except you and your opponents.",
		section = overheadSection, position = 4)
	default OverheadScope skullScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "skullSize", name = "PK skull size (%)",
		description = "Size of the re-rendered PK skull. 100% ≈ the native size.",
		section = overheadSection, position = 5)
	default int skullSize() { return 100; }

	@ConfigItem(keyName = "accurateRemoteHp", name = "Accurate heal amounts",
		description = "Use real max HP for the opponent heal estimate — NPC HP from RuneLite's "
			+ "table, player Hitpoints level from the OSRS Hiscores (like Opponent Information) — "
			+ "instead of assuming 99. Still a ~estimate (health-bar resolution / HP boosts). "
			+ "Off = no Hiscores lookups.",
		section = overheadSection, position = 6)
	default boolean accurateRemoteHp() { return true; }

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
