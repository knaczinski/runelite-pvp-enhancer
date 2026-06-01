package com.knz.pvpenhancer.model;

import net.runelite.api.HeadIcon;

/**
 * Maps an overhead protection-prayer {@link HeadIcon} to a short readable label, shared
 * by {@link AttackEvent} (the defender's prayer at attack time) and {@link PrayerEvent}
 * (an overhead prayer change).
 */
public final class PrayerNames
{
	private PrayerNames()
	{
	}

	/**
	 * @param icon the overhead icon (may be null)
	 * @return a readable label such as "Protect Magic", or "none" when {@code icon} is null.
	 */
	public static String label(HeadIcon icon)
	{
		if (icon == null)
		{
			return "none";
		}
		switch (icon)
		{
			case MELEE:
				return "Protect Melee";
			case RANGED:
				return "Protect Range";
			case MAGIC:
				return "Protect Magic";
			case SMITE:
				return "Smite";
			case REDEMPTION:
				return "Redemption";
			case RETRIBUTION:
				return "Retribution";
			case SOUL_SPLIT:
				return "Soul Split";
			case DEFLECT_MELEE:
				return "Deflect Melee";
			case DEFLECT_RANGE:
				return "Deflect Range";
			case DEFLECT_MAGE:
				return "Deflect Magic";
			default:
				// RANGE_MAGE, RANGE_MELEE, MAGE_MELEE, RANGE_MAGE_MELEE, WRATH, ...
				return capitalizeWords(icon.name().toLowerCase().replace('_', ' '));
		}
	}

	private static String capitalizeWords(String text)
	{
		String[] parts = text.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String part : parts)
		{
			if (part.isEmpty())
			{
				continue;
			}
			if (sb.length() > 0)
			{
				sb.append(' ');
			}
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return sb.toString();
	}
}
