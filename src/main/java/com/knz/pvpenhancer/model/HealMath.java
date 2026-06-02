package com.knz.pvpenhancer.model;

/**
 * Pure helpers for estimating healing amounts.
 *
 * <p>The local player's healing is read exactly from the Hitpoints skill, but remote
 * players only expose a health <em>ratio</em> (current/max scaled to a small integer), not
 * their real HP. This converts a ratio increase into an approximate HP amount using an
 * assumed max HP — hence remote heals are shown as estimates.
 */
public final class HealMath
{
	private HealMath()
	{
	}

	/**
	 * Estimates the HP healed from a health-ratio increase.
	 *
	 * @param prevRatio    the actor's health ratio last tick
	 * @param curRatio     the actor's health ratio this tick
	 * @param scale        the health scale (max value the ratio can take)
	 * @param assumedMaxHp the assumed real max HP of the actor (e.g. 99)
	 * @return the estimated HP healed, or 0 if there was no increase or the scale is invalid
	 */
	public static int estimateRemoteHeal(int prevRatio, int curRatio, int scale, int assumedMaxHp)
	{
		if (scale <= 0 || curRatio <= prevRatio)
		{
			return 0;
		}
		double fraction = (double) (curRatio - prevRatio) / scale;
		return (int) Math.round(fraction * assumedMaxHp);
	}
}
