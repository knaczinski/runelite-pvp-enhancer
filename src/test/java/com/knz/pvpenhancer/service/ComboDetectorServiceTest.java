package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.ComboResult;
import com.knz.pvpenhancer.model.ComboTier;
import com.knz.pvpenhancer.model.ComboType;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ComboDetectorService}: triple eat, potlock, gear switch tiers
 * (Godlike / Excellent / Humble), and the spec combo (same-tick + humble).
 */
public class ComboDetectorServiceTest
{
	private ComboDetectorService detector;
	private static final int[] EMPTY_INV_IDS = new int[28];
	private static final int[] EMPTY_INV_QTY = new int[28];

	@Before
	public void setUp()
	{
		detector = new ComboDetectorService();
	}

	// ─── Eat ────────────────────────────────────────────────────────────────

	@Test
	public void tripleEatOnSameTick()
	{
		detector.onEatClick(0, 385, 1);
		detector.onEatClick(1, 3150, 1);
		detector.onEatClick(2, 6687, 4);

		assertContainsType(flush(100), ComboType.TRIPLE_EAT);
	}

	@Test
	public void doubleEatIsNotACombo()
	{
		detector.onEatClick(0, 385, 1);
		detector.onEatClick(1, 3150, 1);

		assertTrue("double eat must not produce any combo", flush(100).isEmpty());
	}

	@Test
	public void potlockDetectedWhenItemUnchangedNextTick()
	{
		detector.onEatClick(0, 385, 1);
		flush(100);

		int[] ids = new int[28];
		int[] qtys = new int[28];
		ids[0] = 385;
		qtys[0] = 1;
		List<ComboResult> results = detector.flush(ids, qtys, 101);

		assertContainsType(results, ComboType.COMBO_FAILED);
		assertEquals(ComboTier.FAILED, results.stream()
			.filter(r -> r.getType() == ComboType.COMBO_FAILED)
			.findFirst().orElseThrow().getTier());
	}

	@Test
	public void noFalsePositiveWhenItemConsumed()
	{
		detector.onEatClick(0, 385, 1);
		flush(100);

		int[] ids = new int[28];
		int[] qtys = new int[28];
		ids[0] = -1;
		List<ComboResult> results = detector.flush(ids, qtys, 101);

		assertNotContainsType(results, ComboType.COMBO_FAILED);
	}

	// ─── Gear switch ──────────────────────────────────────────────────────────

	@Test
	public void godlikeSwitchOnFivePlusSlots()
	{
		detector.onGearSwapCount(5);
		List<ComboResult> results = flush(100);
		assertContainsType(results, ComboType.GODLIKE_SWITCH);
		assertEquals(ComboTier.GODLIKE, typeOf(results, ComboType.GODLIKE_SWITCH).getTier());
	}

	@Test
	public void excellentSwitchOnThreeOrFourSlots()
	{
		detector.onGearSwapCount(3);
		assertContainsType(flush(100), ComboType.EXCELLENT_SWITCH);

		detector.onGearSwapCount(4);
		assertContainsType(flush(101), ComboType.EXCELLENT_SWITCH);
	}

	@Test
	public void humbleSwitchWhenSpreadOverTwoConsecutiveTicks()
	{
		detector.onGearSwapCount(2);
		assertTrue(flush(100).isEmpty()); // 2-way alone is nothing

		detector.onGearSwapCount(2);
		List<ComboResult> results = flush(101); // 2 + 2 = 4 over two ticks
		assertContainsType(results, ComboType.HUMBLE_SWITCH);
		assertNotContainsType(results, ComboType.EXCELLENT_SWITCH);
	}

	@Test
	public void twoWaySwitchIsNotACombo()
	{
		detector.onGearSwapCount(2);
		assertTrue(flush(100).isEmpty());
	}

	// ─── Spec combo ───────────────────────────────────────────────────────────

	@Test
	public void specComboWhenRangedPlusSpecialAndTwoHitsSameTick()
	{
		detector.onAttack(AttackStyle.RANGED, 50);
		detector.onSpecialUsed(50);
		detector.onOpponentHit(50);
		detector.onOpponentHit(50);

		List<ComboResult> results = flush(50);
		assertContainsType(results, ComboType.SPEC_COMBO);
		assertEquals(ComboTier.GODLIKE, typeOf(results, ComboType.SPEC_COMBO).getTier());
	}

	@Test
	public void humbleSpecComboWhenHitsSpanTwoTicks()
	{
		detector.onAttack(AttackStyle.RANGED, 50);
		detector.onSpecialUsed(50);
		detector.onOpponentHit(50);
		assertNotContainsType(flush(50), ComboType.SPEC_COMBO); // only one hit this tick

		detector.onOpponentHit(51);
		assertContainsType(flush(51), ComboType.HUMBLE_SPEC_COMBO);
	}

	@Test
	public void noSpecComboWithoutSpecial()
	{
		detector.onAttack(AttackStyle.RANGED, 50);
		detector.onOpponentHit(50);
		detector.onOpponentHit(50);
		assertNotContainsType(flush(50), ComboType.SPEC_COMBO);
	}

	@Test
	public void clearResetsAllState()
	{
		detector.onGearSwapCount(5);
		detector.onEatClick(0, 385, 1);
		detector.clear();

		assertTrue(flush(10).isEmpty());
	}

	// ─── Helpers ──────────────────────────────────────────────────────────────

	private List<ComboResult> flush(int tick)
	{
		return detector.flush(EMPTY_INV_IDS, EMPTY_INV_QTY, tick);
	}

	private static ComboResult typeOf(List<ComboResult> results, ComboType type)
	{
		return results.stream().filter(r -> r.getType() == type).findFirst().orElseThrow();
	}

	private static void assertContainsType(List<ComboResult> results, ComboType type)
	{
		assertTrue("Expected combo type " + type,
			results.stream().anyMatch(r -> r.getType() == type));
	}

	private static void assertNotContainsType(List<ComboResult> results, ComboType type)
	{
		assertFalse("Unexpected combo type " + type,
			results.stream().anyMatch(r -> r.getType() == type));
	}
}
