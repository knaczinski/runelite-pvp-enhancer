package com.knz.pvpenhancer.service;

import java.awt.Color;
import java.util.Collections;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class GhostifyServiceTest
{
	@Test
	public void drawsEverythingWhenEmpty()
	{
		GhostifyService s = new GhostifyService();
		assertTrue(s.shouldDraw(mock(Player.class), false));
		assertFalse(s.isActive());
	}

	@Test
	public void hidesOnlyGhostedPlayers()
	{
		GhostifyService s = new GhostifyService();
		Player ghosted = mock(Player.class);
		Player other = mock(Player.class);
		NPC npc = mock(NPC.class);

		s.update(Collections.singletonMap(ghosted, Color.RED));

		assertTrue(s.isActive());
		assertFalse(s.shouldDraw(ghosted, false)); // hidden
		assertTrue(s.shouldDraw(other, false));    // not in set
		assertTrue(s.shouldDraw(npc, false));       // NPCs never ghosted
	}

	@Test
	public void clearRestoresDrawing()
	{
		GhostifyService s = new GhostifyService();
		Player p = mock(Player.class);
		s.update(Collections.singletonMap(p, Color.RED));
		s.clear();
		assertTrue(s.shouldDraw(p, false));
		assertFalse(s.isActive());
	}
}
