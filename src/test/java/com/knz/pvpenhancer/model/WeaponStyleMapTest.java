package com.knz.pvpenhancer.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WeaponStyleMapTest
{
	@Test
	public void mapsKnownWeapons()
	{
		assertEquals(AttackStyle.MELEE, WeaponStyleMap.styleOf(4151));   // Abyssal whip
		assertEquals(AttackStyle.RANGED, WeaponStyleMap.styleOf(12926)); // Toxic blowpipe
		assertEquals(AttackStyle.MAGIC, WeaponStyleMap.styleOf(21006));  // Kodai wand
	}

	@Test
	public void unknownWeaponIsUnknownStyle()
	{
		assertEquals(AttackStyle.UNKNOWN, WeaponStyleMap.styleOf(-1));
		assertEquals(AttackStyle.UNKNOWN, WeaponStyleMap.styleOf(0));
	}
}
