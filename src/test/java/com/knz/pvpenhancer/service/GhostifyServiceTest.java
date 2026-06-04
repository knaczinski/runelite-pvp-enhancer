package com.knz.pvpenhancer.service;

import java.awt.Color;
import java.util.Collections;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		when(ghosted.getName()).thenReturn("Ghost");
		Player other = mock(Player.class);
		when(other.getName()).thenReturn("Other");
		NPC npc = mock(NPC.class);

		s.update(Collections.singletonMap(ghosted, Color.RED));

		assertTrue(s.isActive());
		assertFalse(s.shouldDraw(ghosted, false)); // hidden by name
		assertTrue(s.shouldDraw(other, false));    // different name
		assertTrue(s.shouldDraw(npc, false));       // NPCs never ghosted
	}

	@Test
	public void hidesAcrossPlayerInstancesWithSameName()
	{
		GhostifyService s = new GhostifyService();
		Player ghosted = mock(Player.class);
		when(ghosted.getName()).thenReturn("Talker");
		s.update(Collections.singletonMap(ghosted, Color.RED));

		// A different Player instance with the same name (e.g. the talking re-draw) is still hidden.
		Player sameNameDifferentInstance = mock(Player.class);
		when(sameNameDifferentInstance.getName()).thenReturn("Talker");
		assertFalse(s.shouldDraw(sameNameDifferentInstance, false));
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
