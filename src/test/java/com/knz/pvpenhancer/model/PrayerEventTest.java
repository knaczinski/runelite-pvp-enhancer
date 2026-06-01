package com.knz.pvpenhancer.model;

import net.runelite.api.HeadIcon;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link PrayerEvent} formatting and {@link PrayerNames} labels.
 */
public class PrayerEventTest
{
	@Test
	public void formatsActiveOverheadPrayer()
	{
		assertEquals("p prayed Protect Magic", new PrayerEvent("p", HeadIcon.MAGIC).format());
		assertEquals("p prayed Protect Range", new PrayerEvent("p", HeadIcon.RANGED).format());
		assertEquals("p prayed Smite", new PrayerEvent("p", HeadIcon.SMITE).format());
	}

	@Test
	public void formatsPrayerOffWhenIconNull()
	{
		assertEquals("p prayer off", new PrayerEvent("p", null).format());
	}

	@Test
	public void prayerEventIsPrayerCategory()
	{
		assertEquals(EventCategory.PRAYER, new PrayerEvent("p", HeadIcon.MELEE).getCategory());
	}

	@Test
	public void labelFallsBackForComboIcons()
	{
		// Multi-protection / curse icons fall back to a title-cased name, never crash.
		assertEquals("none", PrayerNames.label(null));
		assertEquals("Soul Split", PrayerNames.label(HeadIcon.SOUL_SPLIT));
	}
}
