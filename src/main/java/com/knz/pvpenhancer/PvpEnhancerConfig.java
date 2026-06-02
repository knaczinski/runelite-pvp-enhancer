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
	// ─── Sections shown in the RuneLite config panel ────────────────────────

	@ConfigSection(name = "Tracking", description = "What the plugin records.", position = 0)
	String trackingSection = "tracking";

	@ConfigSection(name = "Heartbeat", description = "Tick-synced combat vignette.", position = 1)
	String heartbeatSection = "heartbeat";

	@ConfigSection(name = "Indicators", description = "In-fight status warnings.", position = 2)
	String indicatorsSection = "indicators";

	@ConfigSection(name = "Healing", description = "Show recovered HP near health bars.", position = 3)
	String healingSection = "healing";

	@ConfigSection(name = "Combat (PvP)", description = "PvP overlays: focus, hit prediction, debuff timers, prayer.", position = 4)
	String pvpSection = "pvp";

	// ─── Tracking ───────────────────────────────────────────────────────────

	@ConfigItem(keyName = "trackOpponents", name = "Track opponents",
		description = "Record combat events for all visible players, not just yourself.",
		section = trackingSection, position = 0)
	default boolean trackOpponents() { return true; }

	@ConfigItem(keyName = "trackNpcs", name = "Track NPCs (testing)",
		description = "Record combat events for NPCs. Useful for testing without a second player.",
		section = trackingSection, position = 1)
	default boolean trackNpcs() { return false; }

	// ─── Heartbeat ──────────────────────────────────────────────────────────

	@ConfigItem(keyName = "showHeartbeat", name = "Enable heartbeat",
		description = "Show a red vignette pulsing once per game tick while in combat.",
		section = heartbeatSection, position = 0)
	default boolean showHeartbeat() { return true; }

	// ─── Indicators ─────────────────────────────────────────────────────────

	@ConfigItem(keyName = "showNotRetaliating", name = "Not-attacking warning",
		description = "Warn when in combat but not attacking the opponent (disengaged for >= 2 ticks).",
		section = indicatorsSection, position = 0)
	default boolean showNotRetaliating() { return true; }

	// ─── Healing ────────────────────────────────────────────────────────────

	@ConfigItem(keyName = "healDisplayMode", name = "Show healing",
		description = "Show recovered HP near the health bar of whoever healed. Opponent "
			+ "amounts are estimates (~) — the API only exposes their health ratio, not real HP.",
		section = healingSection, position = 0)
	default HealDisplayMode healDisplayMode() { return HealDisplayMode.EVERYONE; }

	// ─── Combat (PvP) ─────────────────────────────────────────────────────────

	@ConfigItem(keyName = "combatFocusMode", name = "Combat focus (hide others)",
		description = "Hide players + NPCs NOT involved in the fight, to focus on the "
			+ "participants. RuneLite can't dim entities, only hide them. SELF = while you "
			+ "fight; ANY_FIGHT = while anyone nearby fights. You stay visible. (Hides "
			+ "non-involved players too — a third party can be invisible until they engage.)",
		section = pvpSection, position = 0)
	default CombatFocusMode combatFocusMode() { return CombatFocusMode.OFF; }

	@Range(min = 1, max = 100)
	@ConfigItem(keyName = "combatFocusTimeout", name = "Combat focus timeout (ticks)",
		description = "How long a participant stays 'in the fight' (and visible) after their "
			+ "last attack/interaction. Higher keeps them visible through eats and pauses. "
			+ "Also how long focus stays on after you stop hitting. ~16 ticks ≈ 10s.",
		section = pvpSection, position = 1)
	default int combatFocusTimeout() { return 16; }

	@ConfigItem(keyName = "hitPrediction", name = "Hit prediction (XP)",
		description = "Show predicted outgoing damage near your target, derived from the "
			+ "Hitpoints XP drop — appears before ranged/magic projectiles land.",
		section = pvpSection, position = 1)
	default boolean hitPrediction() { return true; }

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
