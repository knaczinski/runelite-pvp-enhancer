package com.knz.pvpenhancer.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SpotanimDebuffsTest
{
	@Test
	public void resolvesSeededFreeze()
	{
		SpotanimDebuffs.Entry barrage = SpotanimDebuffs.lookup(369);
		assertEquals(Debuff.FREEZE, barrage.debuff);
		assertEquals(33, barrage.durationTicks);
	}

	@Test
	public void resolvesTeleblock()
	{
		assertEquals(Debuff.TELEBLOCK, SpotanimDebuffs.lookup(345).debuff);
	}

	@Test
	public void unknownIdReturnsNull()
	{
		assertNull(SpotanimDebuffs.lookup(-1));
		assertFalse(SpotanimDebuffs.isKnown(999999));
		assertTrue(SpotanimDebuffs.isKnown(369));
	}
}
