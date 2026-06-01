package com.knz.pvpenhancer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/**
 * User-configurable settings for the PvP Enhancer plugin. RuneLite renders one widget
 * per method and persists changes automatically.
 */
@ConfigGroup("pvpenhancer")
public interface PvpEnhancerConfig extends Config
{
	@Range(min = 5, max = 100)
	@ConfigItem(
		keyName = "maxHistoryTicks",
		name = "Max history ticks",
		description = "How many ticks of combat history to keep on screen.",
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
		position = 3
	)
	default boolean showGearSwap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackOpponents",
		name = "Track opponents",
		description = "Track combat events for all visible players, not just yourself.",
		position = 4
	)
	default boolean trackOpponents()
	{
		return true;
	}
}
