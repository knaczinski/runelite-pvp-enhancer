package com.knz.pvpenhancer.model;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ComboEatMerger}: same-player combo eats merge; different players
 * and non-eat events are preserved in order.
 */
public class ComboEatMergerTest
{
	@Test
	public void mergesSamePlayerEatsIntoDoubleEat()
	{
		List<CombatEvent> in = Arrays.asList(
			new EatEvent("p", "Shark"),
			new EatEvent("p", "Karambwan"));

		List<CombatEvent> out = ComboEatMerger.merge(in);

		assertEquals(1, out.size());
		assertEquals("p ate Shark + Karambwan (double eat)", out.get(0).format());
	}

	@Test
	public void threeSamePlayerEatsAreTripleEat()
	{
		List<CombatEvent> in = Arrays.asList(
			new EatEvent("p", "Shark"),
			new EatEvent("p", "Karambwan"),
			new EatEvent("p", "Saradomin brew"));

		List<CombatEvent> out = ComboEatMerger.merge(in);

		assertEquals(1, out.size());
		assertTrue(out.get(0).format().contains("(triple eat)"));
	}

	@Test
	public void differentPlayersAreNotMerged()
	{
		List<CombatEvent> in = Arrays.asList(
			new EatEvent("p1", "Shark"),
			new EatEvent("p2", "Shark"));

		List<CombatEvent> out = ComboEatMerger.merge(in);

		assertEquals(2, out.size());
	}

	@Test
	public void nonEatEventsArePreservedInOrder()
	{
		CombatEvent gear = new GearSwapEvent("p", "WEAPON", 4151, "Abyssal whip");
		List<CombatEvent> in = Arrays.asList(
			gear,
			new EatEvent("p", "Shark"),
			new EatEvent("p", "Karambwan"));

		List<CombatEvent> out = ComboEatMerger.merge(in);

		assertEquals(2, out.size());
		assertEquals(gear, out.get(0)); // gear stays first
		assertEquals("p ate Shark + Karambwan (double eat)", out.get(1).format());
	}

	@Test
	public void singleEatIsUnchanged()
	{
		List<CombatEvent> in = Arrays.asList(new EatEvent("p", "Shark"));

		List<CombatEvent> out = ComboEatMerger.merge(in);

		assertEquals(1, out.size());
		assertEquals("p ate Shark", out.get(0).format());
	}
}
