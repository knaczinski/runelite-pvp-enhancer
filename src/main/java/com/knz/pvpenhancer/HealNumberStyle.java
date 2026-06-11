package com.knz.pvpenhancer;

/**
 * How a healing popup is formatted: just the recovered amount, or a full breakdown showing the HP
 * before, the heal, and the resulting total (e.g. {@code 65 + 25 = 90}).
 */
public enum HealNumberStyle
{
	AMOUNT("Heal only"),
	BREAKDOWN("Before + heal = total");

	private final String label;

	HealNumberStyle(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
