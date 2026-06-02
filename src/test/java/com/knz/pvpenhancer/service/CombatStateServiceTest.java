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
		service.setInteractingWithPlayer(true);
		assertTrue(service.isInCombat(999999));

		service.setInteractingWithPlayer(false);
		assertFalse(service.isInCombat(999999));
	}

	@Test
	public void clearResetsState()
	{
		CombatStateService service = new CombatStateService();
		service.recordCombatActivity(50);
		service.setInteractingWithPlayer(true);

		service.clear();

		assertFalse(service.isInCombat(50));
		assertFalse(service.isInteractingWithPlayer());
	}
}
