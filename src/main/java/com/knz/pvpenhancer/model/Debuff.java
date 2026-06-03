package com.knz.pvpenhancer.model;

import java.awt.Color;

/**
 * A movement/teleport debuff that can be tracked with a countdown timer.
 */
public enum Debuff
{
	FREEZE("Freeze", new Color(0x66, 0xCC, 0xFF)),
	SNARE("Bind", new Color(0x99, 0xDD, 0x44)),
	TELEBLOCK("TB", new Color(0xFF, 0x99, 0x33));

	private final String label;
	private final Color color;

	Debuff(String label, Color color)
	{
		this.label = label;
		this.color = color;
	}

	public String getLabel()
	{
		return label;
	}

	public Color getColor()
	{
		return color;
	}
}
