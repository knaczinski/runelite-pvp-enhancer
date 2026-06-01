package com.knz.pvpenhancer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

/**
 * User-configurable settings for the PvP Enhancer plugin.
 *
 * <p>Split into two sections so the panel scales as features are added:
 * <ul>
 *     <li><b>Tracking</b> — functionality: what the plugin records (opponents, NPCs, …).</li>
 *     <li><b>Overlay</b> — display: what the tick-history panel shows. The {@code show*}
 *     toggles only hide/show already-recorded events; they do not stop recording.</li>
 * </ul>
 * RuneLite persists all values automatically.
 */
@ConfigGroup("pvpenhancer")
public interface PvpEnhancerConfig extends Config
{
	@ConfigSection(
		name = "Tracking",
		description = "Functionality: what the plugin records.",
		position = 0
	)
	String trackingSection = "tracking";

	@ConfigSection(
		name = "Overlay",
		description = "Display: what the tick-history panel shows.",
		position = 1
	)
	String overlaySection = "overlay";

	// ----- Tracking (functionality) -----

	@ConfigItem(
		keyName = "trackOpponents",
		name = "Track opponents",
		description = "Record combat events for all visible players, not just yourself.",
		section = trackingSection,
		position = 0
	)
	default boolean trackOpponents()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackNpcs",
		name = "Track NPCs (testing)",
		description = "Also record combat events for NPCs. Handy for testing without a "
			+ "second player. NPCs cannot report some fields (e.g. overhead prayer) — "
			+ "those are left blank.",
		section = trackingSection,
		position = 1
	)
	default boolean trackNpcs()
	{
		return false;
	}

	// ----- Overlay (display) -----

	@Range(max = 5000)
	@ConfigItem(
		keyName = "maxHistoryTicks",
		name = "Max history ticks",
		description = "How many ticks of history to keep on screen. No minimum.",
		section = overlaySection,
		position = 0
	)
	default int maxHistoryTicks()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "showCombat",
		name = "Show combat",
		description = "Show attack and hitsplat events.",
		section = overlaySection,
		position = 1
	)
	default boolean showCombat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showEating",
		name = "Show eating",
		description = "Show eat and drink events.",
		section = overlaySection,
		position = 2
	)
	default boolean showEating()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGearSwap",
		name = "Show gear swaps",
		description = "Show equipment change events.",
		section = overlaySection,
		position = 3
	)
	default boolean showGearSwap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPrayer",
		name = "Show prayers",
		description = "Show overhead protection prayer changes.",
		section = overlaySection,
		position = 4
	)
	default boolean showPrayer()
	{
		return true;
	}
}
