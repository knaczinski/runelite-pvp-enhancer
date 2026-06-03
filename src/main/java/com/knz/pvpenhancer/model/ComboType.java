package com.knz.pvpenhancer.model;

/**
 * The type of combo detected. Paired with a {@link ComboTier} in a {@link ComboResult}.
 */
public enum ComboType
{
	/** 5+ worn-equipment slots changed on the same tick. */
	GODLIKE_SWITCH,
	/** 3–4 worn-equipment slots changed on the same tick. */
	EXCELLENT_SWITCH,
	/** 3–4 worn-equipment slots changed across two consecutive ticks. */
	HUMBLE_SWITCH,
	/** Three or more food/potion items consumed on the same tick. */
	TRIPLE_EAT,
	/** A ranged/missile hit and a special-attack hit landing on the opponent on the same tick. */
	SPEC_COMBO,
	/** A ranged hit and a special hit on the opponent across two consecutive ticks. */
	HUMBLE_SPEC_COMBO,
	/** An "Eat"/"Drink" click that produced no consumption (potlocked). */
	COMBO_FAILED
}
