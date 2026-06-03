package com.knz.pvpenhancer;

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

	@ConfigSection(name = "Developer", description = "Tools for testing overlays without a live fight.", position = 3, closedByDefault = true)
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

	@ConfigItem(keyName = "prayerHighlight", name = "Prayer highlighter",
		description = "Highlight the protection prayer matching your current target's equipped "
			+ "weapon style (predictive). Switches as they switch weapons.",
		section = combatSection, position = 3)
	default boolean prayerHighlight() { return false; }

	@ConfigItem(keyName = "combatFocusMode", name = "Combat focus (hide others)",
		description = "Hide players + NPCs NOT involved in the fight, to focus on the "
			+ "participants. RuneLite can't dim entities, only hide them. Only you fight = while you "
			+ "fight; Anyone fights = while anyone nearby fights. You stay visible. (Hides "
			+ "non-involved players too — a third party can be invisible until they engage.)",
		section = combatSection, position = 4)
	default CombatFocusMode combatFocusMode() { return CombatFocusMode.OFF; }

	@Range(min = 1, max = 100)
	@ConfigItem(keyName = "combatFocusTimeout", name = "Combat focus timeout (ticks)",
		description = "How long a participant stays 'in the fight' (and visible) after their "
			+ "last attack/interaction. Higher keeps them visible through eats and pauses. "
			+ "Also how long focus stays on after you stop hitting. ~16 ticks ≈ 10s.",
		section = combatSection, position = 5)
	default int combatFocusTimeout() { return 16; }

	@ConfigItem(keyName = "swapPickupInCombat", name = "Walk-here over Take (in combat)",
		description = "While in combat, de-prioritise ground-item \"Take\" so a left-click "
			+ "walks instead of picking up (avoids breaking your attack). Take stays on right-click.",
		section = combatSection, position = 6)
	default boolean swapPickupInCombat() { return false; }

	// ─── Overhead displays ──────────────────────────────────────────────────

	@ConfigItem(keyName = "healDisplayMode", name = "Healing numbers",
		description = "Show recovered HP near the health bar of whoever healed. Opponent "
			+ "amounts are estimates (~) — the API only exposes their health ratio, not real HP.",
		section = overheadSection, position = 0)
	default HealDisplayMode healDisplayMode() { return HealDisplayMode.EVERYONE; }

	@ConfigItem(keyName = "debuffTimers", name = "Freeze / TB timers",
		description = "Show an icon + seconds countdown over players hit by freeze/snare/teleblock. "
			+ "Teleblock is tracked as half (~2.5 min) when the target prayed Magic as it landed.",
		section = overheadSection, position = 1)
	default DebuffScope debuffTimers() { return DebuffScope.OFF; }

	@ConfigItem(keyName = "vengTextScope", name = "Vengeance text resize",
		description = "Re-render the 'Vengeance!' overhead text at a custom size (the native size "
			+ "is not resizable via the API, so the original is replaced).",
		section = overheadSection, position = 2)
	default OverheadScope vengTextScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "vengTextSize", name = "Vengeance text size (%)",
		description = "Size of the re-rendered Vengeance text. 100% ≈ the native size.",
		section = overheadSection, position = 3)
	default int vengTextSize() { return 100; }

	@ConfigItem(keyName = "skullScope", name = "PK skull resize",
		description = "Re-render the PK skull at a custom size (the native size is not resizable, "
			+ "so the native skull is hidden and replaced). Only the regular skull is handled.",
		section = overheadSection, position = 4)
	default OverheadScope skullScope() { return OverheadScope.OFF; }

	@Range(min = 20, max = 400)
	@ConfigItem(keyName = "skullSize", name = "PK skull size (%)",
		description = "Size of the re-rendered PK skull. 100% ≈ the native size.",
		section = overheadSection, position = 5)
	default int skullSize() { return 100; }

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
