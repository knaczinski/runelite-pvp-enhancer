package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator.Correlation;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link AttackHitsplatCorrelator}: matching within the style window,
 * target mismatch, and expiry.
 */
public class AttackHitsplatCorrelatorTest
{
	@Test
	public void matchesMeleeHitOneTickLater()
	{
		AttackHitsplatCorrelator c = new AttackHitsplatCorrelator();
		c.recordAttack("me", "opp", AttackStyle.MELEE, 10);

		Correlation result = c.recordHitsplat("opp", 24, 11);

		assertNotNull(result);
		assertEquals("me", result.getAttacker());
		assertEquals("opp", result.getTarget());
		assertEquals(24, result.getAmount());
		assertEquals(10, result.getAttackTick());
	}

	@Test
	public void matchesRangedHitWithinThreeTicks()
	{
		AttackHitsplatCorrelator c = new AttackHitsplatCorrelator();
		c.recordAttack("me", "opp", AttackStyle.RANGED, 10);
		assertNotNull(c.recordHitsplat("opp", 12, 13)); // 3-tick travel
	}

	@Test
	public void meleeHitTooLateDoesNotMatch()
	{
		AttackHitsplatCorrelator c = new AttackHitsplatCorrelator();
		c.recordAttack("me", "opp", AttackStyle.MELEE, 10);
		// melee window is 1 tick; a hit 3 ticks later is not this attack's
		assertNull(c.recordHitsplat("opp", 5, 13));
	}

	@Test
	public void hitOnDifferentTargetDoesNotMatch()
	{
		AttackHitsplatCorrelator c = new AttackHitsplatCorrelator();
		c.recordAttack("me", "opp", AttackStyle.MAGIC, 10);
		assertNull(c.recordHitsplat("someoneElse", 9, 11));
	}

	@Test
	public void eachAttackMatchesAtMostOneHit()
	{
		AttackHitsplatCorrelator c = new AttackHitsplatCorrelator();
		c.recordAttack("me", "opp", AttackStyle.MELEE, 10);

		assertNotNull(c.recordHitsplat("opp", 10, 10)); // claims the attack
		assertNull(c.recordHitsplat("opp", 10, 10));    // nothing left to claim
	}
}
