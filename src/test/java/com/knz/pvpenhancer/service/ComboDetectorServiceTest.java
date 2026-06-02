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
 * Unit tests for {@link ComboDetectorService}: eat combos, potlock, clean switch, and
 * offensive swap tiers.
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

	@Test
	public void doubleEatOnSameTick()
	{
		detector.onEatClick(0, 385, 1); // shark
		detector.onEatClick(1, 3150, 1); // karambwan

		List<ComboResult> results = flush(100);

		assertContainsType(results, ComboType.DOUBLE_EAT);
	}

	@Test
	public void tripleEatOnSameTick()
	{
		detector.onEatClick(0, 385, 1);
		detector.onEatClick(1, 3150, 1);
		detector.onEatClick(2, 6687, 4); // saradomin brew

		List<ComboResult> results = flush(100);

		assertContainsType(results, ComboType.TRIPLE_EAT);
		assertNotContainsType(results, ComboType.DOUBLE_EAT);
	}

	@Test
	public void potlockDetectedWhenItemUnchangedNextTick()
	{
		// Tick 100: player clicks eat on slot 0 with item 385 qty 1
		detector.onEatClick(0, 385, 1);
		flush(100); // prev = [click on slot0, id=385, qty=1]

		// Tick 101: inventory still has the same item at slot 0 → potlocked
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

		// Tick 101: slot 0 is now empty (item consumed)
		int[] ids = new int[28];
		int[] qtys = new int[28];
		ids[0] = -1;
		qtys[0] = 0;
		List<ComboResult> results = detector.flush(ids, qtys, 101);

		assertNotContainsType(results, ComboType.COMBO_FAILED);
	}

	@Test
	public void cleanSwitchDetectedOnThreeOrMoreSwaps()
	{
		detector.onGearSwapCount(3);
		List<ComboResult> results = flush(100);
		assertContainsType(results, ComboType.CLEAN_SWITCH);
	}

	@Test
	public void lessThanThreeSwapsIsNotCleanSwitch()
	{
		detector.onGearSwapCount(2);
		List<ComboResult> results = flush(100);
		assertNotContainsType(results, ComboType.CLEAN_SWITCH);
	}

	@Test
	public void offensiveSwapGapZeroIsPerfect()
	{
		detector.onWeaponSwap(10);
		detector.onAttack(AttackStyle.MELEE, 10); // same tick = gap 0
		List<ComboResult> results = flush(10);
		ComboResult swap = results.stream().filter(r -> r.getType() == ComboType.OFFENSIVE_SWAP).findFirst().orElse(null);
		assertTrue("should produce offensive swap", swap != null);
		assertEquals(ComboTier.PERFECT, swap.getTier());
	}

	@Test
	public void offensiveSwapGapOneIsGreat()
	{
		detector.onWeaponSwap(10);
		flush(10);
		detector.onAttack(AttackStyle.RANGED, 11);
		List<ComboResult> results = flush(11);
		ComboResult swap = results.stream().filter(r -> r.getType() == ComboType.OFFENSIVE_SWAP).findFirst().orElse(null);
		assertTrue("should produce offensive swap", swap != null);
		assertEquals(ComboTier.GREAT, swap.getTier());
	}

	@Test
	public void offensiveSwapGapThreeProducesNoCombo()
	{
		detector.onWeaponSwap(10);
		flush(10);
		flush(11);
		flush(12);
		detector.onAttack(AttackStyle.MAGIC, 13); // gap = 3
		List<ComboResult> results = flush(13);
		assertNotContainsType(results, ComboType.OFFENSIVE_SWAP);
	}

	@Test
	public void clearResetsAllState()
	{
		detector.onWeaponSwap(5);
		detector.onEatClick(0, 385, 1);
		detector.clear();

		List<ComboResult> results = flush(10);
		assertTrue(results.isEmpty());
	}

	// ─── Helpers ──────────────────────────────────────────────────────────

	private List<ComboResult> flush(int tick)
	{
		return detector.flush(EMPTY_INV_IDS, EMPTY_INV_QTY, tick);
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
