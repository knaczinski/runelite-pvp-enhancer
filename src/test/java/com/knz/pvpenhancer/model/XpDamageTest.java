package com.knz.pvpenhancer.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link XpDamage#fromHitpointsXp}.
 */
public class XpDamageTest
{
	@Test
	public void convertsXpToDamage()
	{
		// 20 damage -> 26.667 HP xp; the integer delta (26 or 27) rounds back to ~20
		assertEquals(20, XpDamage.fromHitpointsXp(27));
		assertEquals(20, XpDamage.fromHitpointsXp(26));
		assertEquals(3, XpDamage.fromHitpointsXp(4));
		assertEquals(1, XpDamage.fromHitpointsXp(1));
	}

	@Test
	public void nonPositiveIsZero()
	{
		assertEquals(0, XpDamage.fromHitpointsXp(0));
		assertEquals(0, XpDamage.fromHitpointsXp(-5));
	}
}
