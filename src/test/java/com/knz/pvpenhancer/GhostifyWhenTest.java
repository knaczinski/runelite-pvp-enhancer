package com.knz.pvpenhancer;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GhostifyWhenTest
{
	@Test
	public void whenRules()
	{
		assertFalse(GhostifyWhen.NEVER.shouldGhost(true));
		assertFalse(GhostifyWhen.NEVER.shouldGhost(false));
		assertTrue(GhostifyWhen.ALWAYS.shouldGhost(false));
		assertTrue(GhostifyWhen.IN_COMBAT.shouldGhost(true));
		assertFalse(GhostifyWhen.IN_COMBAT.shouldGhost(false));
		assertTrue(GhostifyWhen.NOT_IN_COMBAT.shouldGhost(false));
		assertFalse(GhostifyWhen.NOT_IN_COMBAT.shouldGhost(true));
	}

	@Test
	public void othersCannotAttackRule()
	{
		assertTrue(GhostifyOthersWhen.CANNOT_ATTACK.shouldGhost(false, true));
		assertFalse(GhostifyOthersWhen.CANNOT_ATTACK.shouldGhost(true, false));
		assertTrue(GhostifyOthersWhen.ALWAYS.shouldGhost(false, false));
		assertFalse(GhostifyOthersWhen.NEVER.shouldGhost(true, true));
	}
}
