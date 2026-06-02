package com.knz.pvpenhancer.model;

import net.runelite.api.HitsplatID;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link HitsplatLabels#label}.
 */
public class HitsplatLabelsTest
{
	@Test
	public void mapsStatusEffects()
	{
		assertEquals("poison", HitsplatLabels.label(HitsplatID.POISON, 4));
		assertEquals("venom", HitsplatLabels.label(HitsplatID.VENOM, 6));
		assertEquals("heal", HitsplatLabels.label(HitsplatID.HEAL, 20));
		assertEquals("disease", HitsplatLabels.label(HitsplatID.DISEASE, 1));
		assertEquals("smite", HitsplatLabels.label(HitsplatID.PRAYER_DRAIN, 0));
	}

	@Test
	public void mapsBlocks()
	{
		assertEquals("block", HitsplatLabels.label(HitsplatID.BLOCK_ME, 0));
		assertEquals("block", HitsplatLabels.label(HitsplatID.BLOCK_OTHER, 0));
	}

	@Test
	public void mapsDamage()
	{
		assertEquals("hit", HitsplatLabels.label(HitsplatID.DAMAGE_ME, 15));
		assertEquals("hit", HitsplatLabels.label(HitsplatID.DAMAGE_OTHER, 7));
	}

	@Test
	public void zeroDamageFallsBackToBlock()
	{
		assertEquals("block", HitsplatLabels.label(HitsplatID.DAMAGE_ME, 0));
	}
}
