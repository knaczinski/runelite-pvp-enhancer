package com.knz.pvpenhancer.model;

import net.runelite.api.HitsplatID;

/**
 * Maps a RuneLite hitsplat type id (+ amount) to a short label for the tick history.
 * Pure and stateless. Distinguishes status effects (poison/venom/heal/disease/smite) and
 * blocks from regular damage.
 */
public final class HitsplatLabels
{
	private HitsplatLabels()
	{
	}

	/**
	 * @param type   the hitsplat type id ({@code Hitsplat.getHitsplatType()}, a {@link HitsplatID})
	 * @param amount the hitsplat amount
	 * @return a short label: poison / venom / heal / disease / smite / block / hit
	 */
	public static String label(int type, int amount)
	{
		switch (type)
		{
			case HitsplatID.POISON:
				return "poison";
			case HitsplatID.VENOM:
				return "venom";
			case HitsplatID.HEAL:
				return "heal";
			case HitsplatID.DISEASE:
				return "disease";
			case HitsplatID.PRAYER_DRAIN:
				return "smite";
			case HitsplatID.BLOCK_ME:
			case HitsplatID.BLOCK_OTHER:
				return "block";
			default:
				return amount == 0 ? "block" : "hit";
		}
	}
}
