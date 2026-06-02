package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.HitDirection;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator.Correlation;
import com.knz.pvpenhancer.model.HitSummaryRow;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link HitSummaryService}: row insertion, cap, Hit filling, and clear.
 */
public class HitSummaryServiceTest
{
	@Test
	public void addsRowAndFillsHitViaCorrelation()
	{
		HitSummaryService service = new HitSummaryService();
		AttackHitsplatCorrelator correlator = new AttackHitsplatCorrelator();

		correlator.recordAttack("me", "opp", AttackStyle.MELEE, 10);
		service.addAttack(10, "me", AttackStyle.MELEE, "opp", "pro mage", "Piety", HitDirection.OUTGOING);

		Correlation corr = correlator.recordHitsplat("opp", 25, 10);
		assertNotNull(corr);
		service.applyCorrelation(corr);

		List<HitSummaryRow> rows = service.getRows();
		assertEquals(1, rows.size());
		assertEquals((Integer) 25, rows.get(0).hit);
		assertEquals("Piety", rows.get(0).offensivePrayer);
	}

	@Test
	public void hitIsNullBeforeCorrelation()
	{
		HitSummaryService service = new HitSummaryService();
		service.addAttack(5, "me", AttackStyle.RANGED, "opp", null, null, HitDirection.OUTGOING);

		assertNull(service.getRows().get(0).hit);
	}

	@Test
	public void capsAtMaxRows()
	{
		HitSummaryService service = new HitSummaryService();
		service.setMaxRows(3);
		for (int i = 0; i < 5; i++)
		{
			service.addAttack(i, "me", AttackStyle.MAGIC, "opp", null, null, HitDirection.OUTGOING);
		}
		assertEquals(3, service.getRows().size());
	}

	@Test
	public void clearEmptiesRows()
	{
		HitSummaryService service = new HitSummaryService();
		service.addAttack(1, "me", AttackStyle.MELEE, "opp", null, null, HitDirection.OUTGOING);
		service.clear();
		assertEquals(0, service.getRows().size());
	}
}
