package com.knz.pvpenhancer.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link HealMath#estimateRemoteHeal}.
 */
public class HealMathTest
{
	@Test
	public void estimatesFromRatioIncrease()
	{
		// 6/30 of 99 HP ≈ 20
		assertEquals(20, HealMath.estimateRemoteHeal(10, 16, 30, 99));
	}

	@Test
	public void fullBarFromHalf()
	{
		// 15/30 of 99 ≈ 50
		assertEquals(50, HealMath.estimateRemoteHeal(15, 30, 30, 99));
	}

	@Test
	public void noIncreaseReturnsZero()
	{
		assertEquals(0, HealMath.estimateRemoteHeal(20, 20, 30, 99));
		assertEquals(0, HealMath.estimateRemoteHeal(20, 10, 30, 99));
	}

	@Test
	public void invalidScaleReturnsZero()
	{
		assertEquals(0, HealMath.estimateRemoteHeal(5, 10, 0, 99));
	}

	@Test
	public void smallestRatioBumpIsAFewHp()
	{
		// a single ratio unit out of 30 on a 99-HP target ≈ 3 HP
		assertEquals(3, HealMath.estimateRemoteHeal(10, 11, 30, 99));
	}
}
