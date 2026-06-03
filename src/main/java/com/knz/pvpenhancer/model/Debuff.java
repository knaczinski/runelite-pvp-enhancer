package com.knz.pvpenhancer.model;

import java.awt.Color;

/**
 * A movement/teleport debuff that can be tracked with a countdown timer.
 */
public enum Debuff
{
	FREEZE("Freeze", new Color(0x66, 0xCC, 0xFF), "Ice_Barrage.png"),
	SNARE("Bind", new Color(0x99, 0xDD, 0x44), "Entangle.png"),
	TELEBLOCK("TB", new Color(0xFF, 0x99, 0x33), "Tele_Block.png");

	private final String label;
	private final Color color;
	private final String iconFile;

	Debuff(String label, Color color, String iconFile)
	{
		this.label = label;
		this.color = color;
		this.iconFile = iconFile;
	}

	public String getLabel()
	{
		return label;
	}

	public Color getColor()
	{
		return color;
	}

	/** OSRS-wiki spell-icon file under {@code /com/knz/pvpenhancer/icons/}. */
	public String getIconFile()
	{
		return iconFile;
	}
}
