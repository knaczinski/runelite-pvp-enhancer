package com.knz.pvpenhancer.model;

/**
 * The outcome of a combo detector: the combo that was detected, its quality tier, and a
 * short display label for the floating popup and the tick history.
 */
public class ComboResult
{
	private final ComboType type;
	private final ComboTier tier;
	private final String label;

	public ComboResult(ComboType type, ComboTier tier, String label)
	{
		this.type = type;
		this.tier = tier;
		this.label = label;
	}

	public ComboType getType()
	{
		return type;
	}

	public ComboTier getTier()
	{
		return tier;
	}

	public String getLabel()
	{
		return label;
	}
}
