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
 * <p>Call order on each {@code GameTick}:
 * <ol>
 *   <li>{@link #onEatClick(int, int, int)} — for every "Eat"/"Drink" menu click this tick</li>
 *   <li>{@link #onWeaponSwap(int)} — if the weapon slot changed this tick</li>
 *   <li>{@link #onGearSwapCount(int)} — total worn-slot changes this tick</li>
 *   <li>{@link #onAttack(AttackStyle, int)} — if the local player threw an attack this tick</li>
 *   <li>{@link #flush(int[], int[], int)} — tick boundary; returns detected combos and advances state</li>
 * </ol>
 */
@Singleton
public class ComboDetectorService
{
	private static final int MAX_OFFENSIVE_SWAP_GAP = 2; // ticks; gap > this = no tier

	// Per-tick pending state (set by on* calls, consumed by flush)
	private final List<EatClick> currentEatClicks = new ArrayList<>();
	private int currentGearSwapCount = 0;
	private AttackStyle currentAttackStyle = null;
	private boolean hadWeaponSwapThisTick = false;

	// Cross-tick state
	private List<EatClick> prevEatClicks = new ArrayList<>();
	private int lastWeaponSwapTick = Integer.MIN_VALUE;

	/**
	 * Records an "Eat"/"Drink" click on the current tick. Pass the inventory slot and item
	 * data so potlock can be checked against the next tick's inventory.
	 *
	 * @param slotIdx  inventory slot index (0–27)
	 * @param itemId   item id at that slot when clicked
	 * @param quantity stack count at that slot when clicked
	 */
	public void onEatClick(int slotIdx, int itemId, int quantity)
	{
		currentEatClicks.add(new EatClick(slotIdx, itemId, quantity));
	}

	/**
	 * Records that the local player's weapon slot changed this tick.
	 *
	 * @param tick current server tick
	 */
	public void onWeaponSwap(int tick)
	{
		hadWeaponSwapThisTick = true;
		lastWeaponSwapTick = tick;
	}

	/**
	 * Records the total number of worn-equipment slot changes this tick.
	 */
	public void onGearSwapCount(int count)
	{
		this.currentGearSwapCount = count;
	}

	/**
	 * Records that the local player threw an attack this tick.
	 */
	public void onAttack(AttackStyle style, int tick)
	{
		this.currentAttackStyle = style;
	}

	/**
	 * Advances to the next tick, checks all combo patterns, and returns the results.
	 *
	 * @param inventoryIds       item id at each of the 28 inventory slots (current tick)
	 * @param inventoryQuantities quantity at each slot
	 * @param tick               current server tick
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

		// 2. Eat combos (same-tick multi-consume)
		int eatCount = currentEatClicks.size();
		if (eatCount == 2)
		{
			results.add(new ComboResult(ComboType.DOUBLE_EAT, ComboTier.SUCCESS, "DOUBLE EAT"));
		}
		else if (eatCount >= 3)
		{
			results.add(new ComboResult(ComboType.TRIPLE_EAT, ComboTier.SUCCESS, "TRIPLE EAT"));
		}

		// 3. Clean switch: >= 3 worn slots changed this tick
		if (currentGearSwapCount >= 3)
		{
			results.add(new ComboResult(ComboType.CLEAN_SWITCH, ComboTier.SUCCESS, "CLEAN SWITCH"));
		}

		// 4. Offensive swap→attack tier
		if (currentAttackStyle != null && lastWeaponSwapTick != Integer.MIN_VALUE)
		{
			int gap = tick - lastWeaponSwapTick;
			ComboTier tier = tierForGap(gap);
			if (tier != null)
			{
				String label = tierLabel(tier) + " (" + currentAttackStyle.getLabel() + ")";
				results.add(new ComboResult(ComboType.OFFENSIVE_SWAP, tier, label));
			}
		}

		// Advance state
		prevEatClicks = new ArrayList<>(currentEatClicks);
		currentEatClicks.clear();
		currentGearSwapCount = 0;
		currentAttackStyle = null;
		hadWeaponSwapThisTick = false;

		return results;
	}

	public void clear()
	{
		currentEatClicks.clear();
		prevEatClicks.clear();
		currentGearSwapCount = 0;
		currentAttackStyle = null;
		hadWeaponSwapThisTick = false;
		lastWeaponSwapTick = Integer.MIN_VALUE;
	}

	/**
	 * @return true if the eat click was consumed (item gone from the slot or quantity
	 * decreased). If the item was moved to a different slot (drag), the original slot shows
	 * something different, which is treated as "not same" → not potlocked.
	 */
	private static boolean wasConsumed(EatClick click, int[] ids, int[] quantities)
	{
		if (click.slotIdx < 0 || click.slotIdx >= ids.length)
		{
			return true; // out of range → treat as consumed (defensive)
		}
		// Same item AND same quantity in the same slot → NOT consumed
		return ids[click.slotIdx] != click.itemId
			|| quantities[click.slotIdx] != click.quantity;
	}

	private static ComboTier tierForGap(int gap)
	{
		if (gap < 0 || gap > MAX_OFFENSIVE_SWAP_GAP)
		{
			return null;
		}
		switch (gap)
		{
			case 0: return ComboTier.PERFECT;
			case 1: return ComboTier.GREAT;
			default: return ComboTier.GOOD;
		}
	}

	private static String tierLabel(ComboTier tier)
	{
		switch (tier)
		{
			case PERFECT: return "PERFECT!";
			case GREAT:   return "GREAT";
			default:      return "GOOD";
		}
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
