package com.knz.pvpenhancer.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link CombatFocusService#shouldDraw}.
 */
public class CombatFocusServiceTest
{
	@Test
	public void drawsEverythingWhenInactive()
	{
		CombatFocusService s = new CombatFocusService();
		assertTrue(s.shouldDraw(mock(Player.class), false));
		assertTrue(s.shouldDraw(mock(NPC.class), false));
	}

	@Test
	public void hidesNonInvolvedActorsWhenActive()
	{
		CombatFocusService s = new CombatFocusService();
		Player involvedPlayer = mock(Player.class);
		Player otherPlayer = mock(Player.class);
		NPC otherNpc = mock(NPC.class);

		Set<Renderable> involved = new HashSet<>();
		involved.add(involvedPlayer);
		s.update(true, involved);

		assertTrue("involved player drawn", s.shouldDraw(involvedPlayer, false));
		assertFalse("other player hidden", s.shouldDraw(otherPlayer, false));
		assertFalse("npc hidden", s.shouldDraw(otherNpc, false));
	}

	@Test
	public void neverHidesScenery()
	{
		CombatFocusService s = new CombatFocusService();
		s.update(true, Collections.emptySet());
		// A plain Renderable (not a Player/NPC) is scenery/projectile — always drawn.
		assertTrue(s.shouldDraw(mock(Renderable.class), false));
	}

	@Test
	public void clearDeactivates()
	{
		CombatFocusService s = new CombatFocusService();
		s.update(true, Collections.emptySet());
		s.clear();
		assertTrue(s.shouldDraw(mock(Player.class), false));
	}
}
