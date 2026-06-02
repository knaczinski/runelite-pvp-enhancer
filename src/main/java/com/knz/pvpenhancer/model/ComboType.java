package com.knz.pvpenhancer.model;

/**
 * The type of combo detected. Paired with a {@link ComboTier} in a {@link ComboResult}.
 */
public enum ComboType
{
	/** Two food/potion items consumed on the same tick. */
	DOUBLE_EAT,
	/** Three food/potion items consumed on the same tick. */
	TRIPLE_EAT,
	/** An "Eat"/"Drink" click that produced no consumption (potlocked). */
	COMBO_FAILED,
	/** Weapon equipped then attack thrown within the last-tick-swap window. */
	OFFENSIVE_SWAP,
	/** Three or more worn-equipment slots changed on the same tick. */
	CLEAN_SWITCH
}
