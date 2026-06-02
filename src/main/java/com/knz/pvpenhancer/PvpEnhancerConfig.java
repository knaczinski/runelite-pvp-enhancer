package com.knz.pvpenhancer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

/**
 * User-configurable settings for the PvP Enhancer plugin.
 *
 * <p>Sections:
 * <ul>
 *   <li><b>Tracking</b> — what the plugin records (opponents, NPCs).</li>
 *   <li><b>Overlay</b> — tick-history display filters (show/hide categories).</li>
 *   <li><b>Heartbeat</b> — tick-synced combat vignette.</li>
 *   <li><b>Indicators</b> — in-fight status warnings (not attacking, etc.).</li>
 *   <li><b>Hit Summary</b> — tabular attack/hitsplat log.</li>
 *   <li><b>Combos</b> — combo detection and popup feedback.</li>
 * </ul>
 *
 * RuneLite persists all values automatically.
 */
@ConfigGroup("pvpenhancer")
public interface PvpEnhancerConfig extends Config
{
	// ─── Section declarations ───────────────────────────────────────────────

	@ConfigSection(name = "Tracking",    description = "What the plugin records.",              position = 0)
	String trackingSection  = "tracking";

	@ConfigSection(name = "Overlay",     description = "Tick-history display filters.",         position = 1)
	String overlaySection   = "overlay";

	@ConfigSection(name = "Heartbeat",   description = "Tick-synced combat vignette.",          position = 2)
	String heartbeatSection = "heartbeat";

	@ConfigSection(name = "Indicators",  description = "In-fight status warnings.",             position = 3)
	String indicatorsSection = "indicators";

	@ConfigSection(name = "Hit Summary", description = "Tabular attack / hitsplat log.",        position = 4)
	String hitSummarySection = "hitSummary";

	@ConfigSection(name = "Combos",      description = "Combo detection and popup feedback.",   position = 5)
	String combosSection    = "combos";

	// ─── Tracking ───────────────────────────────────────────────────────────

	@ConfigItem(keyName = "trackOpponents", name = "Track opponents",
		description = "Record combat events for all visible players, not just yourself.",
		section = trackingSection, position = 0)
	default boolean trackOpponents() { return true; }

	@ConfigItem(keyName = "trackNpcs", name = "Track NPCs (testing)",
		description = "Record combat events for NPCs. Useful for testing without a second player.",
		section = trackingSection, position = 1)
	default boolean trackNpcs() { return false; }

	// ─── Overlay (tick-history display) ─────────────────────────────────────

	@Range(max = 5000)
	@ConfigItem(keyName = "maxHistoryTicks", name = "Max history ticks",
		description = "How many ticks of history to keep on screen. No minimum.",
		section = overlaySection, position = 0)
	default int maxHistoryTicks() { return 20; }

	@ConfigItem(keyName = "showCombat", name = "Show combat",
		description = "Show attack and hitsplat events.",
		section = overlaySection, position = 1)
	default boolean showCombat() { return true; }

	@ConfigItem(keyName = "showEating", name = "Show eating",
		description = "Show eat and drink events.",
		section = overlaySection, position = 2)
	default boolean showEating() { return true; }

	@ConfigItem(keyName = "showGearSwap", name = "Show gear swaps",
		description = "Show equipment change events.",
		section = overlaySection, position = 3)
	default boolean showGearSwap() { return true; }

	@ConfigItem(keyName = "showPrayer", name = "Show prayers",
		description = "Show overhead protection prayer changes.",
		section = overlaySection, position = 4)
	default boolean showPrayer() { return true; }

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

	// ─── Hit Summary ────────────────────────────────────────────────────────

	@ConfigItem(keyName = "showHitSummary", name = "Enable hit summary",
		description = "Show the tabular attack/hitsplat log.",
		section = hitSummarySection, position = 0)
	default boolean showHitSummary() { return true; }

	@Range(max = 100)
	@ConfigItem(keyName = "hitSummaryRows", name = "Max rows",
		description = "Maximum number of rows in the hit summary.",
		section = hitSummarySection, position = 1)
	default int hitSummaryRows() { return 25; }

	// ─── Combos ─────────────────────────────────────────────────────────────

	@ConfigItem(keyName = "showCombos", name = "Enable combos",
		description = "Detect and display combo feedback (double/triple eat, offensive swap timing, etc.).",
		section = combosSection, position = 0)
	default boolean showCombos() { return true; }
}
