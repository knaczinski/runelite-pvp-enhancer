package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.ComboResult;
import com.knz.pvpenhancer.model.ComboTier;
import com.knz.pvpenhancer.model.ComboType;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

/**
 * Detects combos for the local player by tracking state across ticks. No Client reference
 * — the plugin feeds it raw data so this class is unit-testable without a running client.
 *
 * <p>Combo taxonomy (S012 redesign):
 * <ul>
 *   <li><b>Gear switch:</b> Godlike (5+ worn slots changed same tick), Excellent (3–4 same
 *       tick), Humble (3–4 spread across two consecutive ticks).</li>
 *   <li><b>Eat:</b> Triple Eat only (3+ consumes same tick; any food/potion).</li>
 *   <li><b>Spec combo:</b> a ranged hit and a special-attack hit on the opponent — same tick =
 *       Spec Combo, across two consecutive ticks = Humble Spec Combo. Heuristic (see
 *       {@link #flush}); the opponent taking 2+ hits while you recently threw a ranged attack
 *       and used a special.</li>
 *   <li><b>Combo failed:</b> an eat/drink click that did not consume (potlock).</li>
 * </ul>
 *
 * <p>Call order on each {@code GameTick}:
 * <ol>
 *   <li>{@link #onEatClick(int, int, int)} — per "Eat"/"Drink" click this tick</li>
 *   <li>{@link #onGearSwapCount(int)} — total worn-slot changes this tick</li>
 *   <li>{@link #onAttack(AttackStyle, int)} — if the local player threw an attack this tick</li>
 *   <li>{@link #onSpecialUsed(int)} — if special-attack energy dropped this tick</li>
 *   <li>{@link #onOpponentHit(int)} — per hitsplat that landed on the opponent this tick</li>
 *   <li>{@link #flush(int[], int[], int)} — tick boundary; returns combos and advances state</li>
 * </ol>
 */
@Singleton
public class ComboDetectorService
{
	/** Max ticks between a ranged attack / special and the spec-combo hit landing. */
	private static final int SPEC_WINDOW_TICKS = 4;

	// Per-tick pending state (set by on* calls, consumed by flush)
	private final List<EatClick> currentEatClicks = new ArrayList<>();
	private int currentGearSwapCount = 0;
	private int currentOpponentHits = 0;

	// Cross-tick state
	private List<EatClick> prevEatClicks = new ArrayList<>();
	private int prevGearSwapCount = 0;
	private int prevOpponentHits = 0;
	private int lastRangedAttackTick = Integer.MIN_VALUE;
	private int lastSpecialTick = Integer.MIN_VALUE;
	private int lastSpecComboTick = Integer.MIN_VALUE;

	/**
	 * Records an "Eat"/"Drink" click on the current tick. Pass the inventory slot and item
	 * data so potlock can be checked against the next tick's inventory.
	 */
	public void onEatClick(int slotIdx, int itemId, int quantity)
	{
		currentEatClicks.add(new EatClick(slotIdx, itemId, quantity));
	}

	/** Records the total number of worn-equipment slot changes this tick. */
	public void onGearSwapCount(int count)
	{
		this.currentGearSwapCount = count;
	}

	/** Records that the local player threw an attack this tick (ranged feeds the spec combo). */
	public void onAttack(AttackStyle style, int tick)
	{
		if (style == AttackStyle.RANGED)
		{
			lastRangedAttackTick = tick;
		}
	}

	/** Records that the local player used a special attack this tick (spec energy dropped). */
	public void onSpecialUsed(int tick)
	{
		lastSpecialTick = tick;
	}

	/** Records one hitsplat landing on the opponent this tick (for the spec combo). */
	public void onOpponentHit(int tick)
	{
		currentOpponentHits++;
	}

	/**
	 * Advances to the next tick, checks all combo patterns, and returns the results.
	 *
	 * @param inventoryIds        item id at each of the 28 inventory slots (current tick)
	 * @param inventoryQuantities quantity at each slot
	 * @param tick                current server tick
	 * @return list of detected {@link ComboResult}s for this tick (may be empty)
	 */
	public List<ComboResult> flush(int[] inventoryIds, int[] inventoryQuantities, int tick)
	{
		List<ComboResult> results = new ArrayList<>();

		// 1. Potlock check: did the previous tick's eat clicks NOT consume?
		for (EatClick prev : prevEatClicks)
		{
			if (!wasConsumed(prev, inventoryIds, inventoryQuantities))
			{
				results.add(new ComboResult(ComboType.COMBO_FAILED, ComboTier.FAILED, "COMBO FAILED"));
				break; // one fail event per tick is enough
			}
		}

		// 2. Triple eat (3+ same tick; any food/potion)
		if (currentEatClicks.size() >= 3)
		{
			results.add(new ComboResult(ComboType.TRIPLE_EAT, ComboTier.SUCCESS, "TRIPLE EAT"));
		}

		// 3. Gear switch tiers
		if (currentGearSwapCount >= 5)
		{
			results.add(new ComboResult(ComboType.GODLIKE_SWITCH, ComboTier.GODLIKE, "GODLIKE SWITCH"));
		}
		else if (currentGearSwapCount >= 3)
		{
			results.add(new ComboResult(ComboType.EXCELLENT_SWITCH, ComboTier.EXCELLENT, "EXCELLENT SWITCH"));
		}
		else if (currentGearSwapCount >= 1 && prevGearSwapCount >= 1 && prevGearSwapCount < 3)
		{
			int spread = currentGearSwapCount + prevGearSwapCount;
			if (spread >= 3 && spread <= 4)
			{
				results.add(new ComboResult(ComboType.HUMBLE_SWITCH, ComboTier.HUMBLE, "HUMBLE SWITCH"));
			}
		}

		// 4. Spec combo (ranged hit + special hit on the opponent)
		boolean rangedRecent = withinSpecWindow(lastRangedAttackTick, tick);
		boolean specRecent = withinSpecWindow(lastSpecialTick, tick);
		if (rangedRecent && specRecent && tick != lastSpecComboTick && (tick - 1) != lastSpecComboTick)
		{
			if (currentOpponentHits >= 2)
			{
				results.add(new ComboResult(ComboType.SPEC_COMBO, ComboTier.GODLIKE, "SPEC COMBO"));
				markSpecComboFired(tick);
			}
			else if (currentOpponentHits >= 1 && prevOpponentHits >= 1)
			{
				results.add(new ComboResult(ComboType.HUMBLE_SPEC_COMBO, ComboTier.HUMBLE, "HUMBLE SPEC COMBO"));
				markSpecComboFired(tick);
			}
		}

		// Advance state
		prevEatClicks = new ArrayList<>(currentEatClicks);
		currentEatClicks.clear();
		prevGearSwapCount = currentGearSwapCount;
		currentGearSwapCount = 0;
		prevOpponentHits = currentOpponentHits;
		currentOpponentHits = 0;

		return results;
	}

	public void clear()
	{
		currentEatClicks.clear();
		prevEatClicks.clear();
		currentGearSwapCount = 0;
		prevGearSwapCount = 0;
		currentOpponentHits = 0;
		prevOpponentHits = 0;
		lastRangedAttackTick = Integer.MIN_VALUE;
		lastSpecialTick = Integer.MIN_VALUE;
		lastSpecComboTick = Integer.MIN_VALUE;
	}

	private void markSpecComboFired(int tick)
	{
		lastSpecComboTick = tick;
		lastRangedAttackTick = Integer.MIN_VALUE;
		lastSpecialTick = Integer.MIN_VALUE;
	}

	private static boolean withinSpecWindow(int markTick, int now)
	{
		if (markTick == Integer.MIN_VALUE)
		{
			return false;
		}
		int gap = now - markTick;
		return gap >= 0 && gap <= SPEC_WINDOW_TICKS;
	}

	/**
	 * @return true if the eat click was consumed (item gone from the slot or quantity
	 * decreased). If the item moved to a different slot (drag), the original slot shows
	 * something different, treated as "not same" → not potlocked.
	 */
	private static boolean wasConsumed(EatClick click, int[] ids, int[] quantities)
	{
		if (click.slotIdx < 0 || click.slotIdx >= ids.length)
		{
			return true; // out of range → treat as consumed (defensive)
		}
		return ids[click.slotIdx] != click.itemId
			|| quantities[click.slotIdx] != click.quantity;
	}

	private static final class EatClick
	{
		final int slotIdx;
		final int itemId;
		final int quantity;

		EatClick(int slotIdx, int itemId, int quantity)
		{
			this.slotIdx = slotIdx;
			this.quantity = quantity;
			this.itemId = itemId;
		}
	}
}
