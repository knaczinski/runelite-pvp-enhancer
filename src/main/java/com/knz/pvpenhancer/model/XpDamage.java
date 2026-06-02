package com.knz.pvpenhancer.model;

/**
 * Converts a Hitpoints XP gain into the damage that produced it.
 *
 * <p>In OSRS, dealing N damage grants the Hitpoints skill {@code 1.333 × N} XP (4/3). Since
 * the XP is awarded at attack time — before a ranged/magic projectile visually lands — the
 * derived damage is a <em>prediction</em> of the incoming hitsplat.
 */
public final class XpDamage
{
	private static final double HP_XP_PER_DAMAGE = 4.0 / 3.0; // 1.333…

	private XpDamage()
	{
	}

	/**
	 * @param deltaXp the Hitpoints XP just gained
	 * @return the damage that produced it (rounded), or 0 for a non-positive delta
	 */
	public static int fromHitpointsXp(int deltaXp)
	{
		if (deltaXp <= 0)
		{
			return 0;
		}
		return (int) Math.round(deltaXp / HP_XP_PER_DAMAGE);
	}
}
