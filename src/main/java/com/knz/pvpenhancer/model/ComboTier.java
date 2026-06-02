package com.knz.pvpenhancer.model;

import java.awt.Color;

/**
 * Quality tier of a detected combo, used for the popup color and the tick-history label.
 */
public enum ComboTier
{
	/** Equip + attack on the same tick (gap = 0) — zero reaction window for opponent. */
	PERFECT(new Color(0xFFD700)),  // gold

	/** Gap = 1 tick between equip and attack. */
	GREAT(new Color(0x6BCB77)),    // green

	/** Gap = 2 ticks between equip and attack. */
	GOOD(new Color(0x4D96FF)),     // blue

	/** Successful eat combo or clean switch (no timing grade needed). */
	SUCCESS(new Color(0x6BCB77)),  // green

	/** Combo failed (potlock / fumble). */
	FAILED(new Color(0xFF6B6B));   // red

	private final Color color;

	ComboTier(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}
}
