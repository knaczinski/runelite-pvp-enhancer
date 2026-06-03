package com.knz.pvpenhancer.model;

import java.awt.Color;

/**
 * Quality tier of a detected combo, used for the popup color and the tick-history label.
 */
public enum ComboTier
{
	/** Top-tier execution (Godlike 5+-way switch, or a same-tick spec combo). */
	GODLIKE(new Color(0xC7, 0x7D, 0xFF)), // purple

	/** Strong execution (Excellent 3–4-way same-tick switch). */
	EXCELLENT(new Color(0xFFD700)),       // gold

	/** Sloppy-but-landed (switch or spec combo spread over two ticks). */
	HUMBLE(new Color(0x4D96FF)),          // blue

	/** Successful combo with no timing grade (triple eat). */
	SUCCESS(new Color(0x6BCB77)),         // green

	/** Combo failed (potlock / fumble). */
	FAILED(new Color(0xFF6B6B));          // red

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
