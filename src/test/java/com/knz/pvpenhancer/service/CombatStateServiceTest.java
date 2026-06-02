package com.knz.pvpenhancer.service;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CombatStateService}: recent-activity window, interaction, clear.
 */
public class CombatStateServiceTest
{
	@Test
	public void notInCombatInitially()
	{
		assertFalse(new CombatStateService().isInCombat(0));
	}

	@Test
	public void recentActivityKeepsInCombatWithinWindow()
	{
		CombatStateService service = new CombatStateService();
		service.setCombatWindow(8);
		service.recordCombatActivity(100);

		assertTrue(service.isInCombat(100));
		assertTrue(service.isInCombat(108)); // edge of window
		assertFalse(service.isInCombat(109)); // past window
	}

	@Test
	public void interactionKeepsInCombatWithoutAnyHit()
	{
		CombatStateService service = new CombatStateService();
		service.setEngaged(true, 100);
		assertTrue(service.isInCombat(999999));

		service.setEngaged(false, 101);
		assertFalse(service.isInCombat(999999));
	}

	@Test
	public void clearResetsState()
	{
		CombatStateService service = new CombatStateService();
		service.recordCombatActivity(50);
		service.setEngaged(true, 50);

		service.clear();

		assertFalse(service.isInCombat(50));
		assertFalse(service.isInteractingWithPlayer());
	}

	@Test
	public void isNotRetaliatingAfterTwoTicksDisengaged()
	{
		CombatStateService service = new CombatStateService();
		service.setCombatWindow(8);
		service.recordCombatActivity(100);
		service.setEngaged(true, 100);

		// Still engaged tick 101
		service.setEngaged(false, 101);
		assertFalse(service.isNotRetaliating(101)); // only 1 tick disengaged

		// 2 ticks disengaged
		service.setEngaged(false, 102);
		assertTrue(service.isNotRetaliating(102));
	}

	@Test
	public void notRetaliatingClearsOnReengage()
	{
		CombatStateService service = new CombatStateService();
		service.recordCombatActivity(100);
		service.setEngaged(true, 100);
		service.setEngaged(false, 103); // disengaged for 3 ticks

		assertTrue(service.isNotRetaliating(103));

		service.setEngaged(true, 104); // re-engaged
		assertFalse(service.isNotRetaliating(104));
	}
}
