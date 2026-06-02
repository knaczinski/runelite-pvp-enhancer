package com.knz.pvpenhancer.model;

import java.awt.Color;

/**
 * A combo result logged as a tick-history event. The color is tier-driven, overriding the
 * default category color so each tier renders distinctly in the overlay.
 */
public class ComboEvent extends CombatEvent
{
	private final ComboResult result;

	public ComboEvent(ComboResult result)
	{
		this.result = result;
	}

	public ComboResult getResult()
	{
		return result;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.COMBO;
	}

	@Override
	public String format()
	{
		return result.getLabel();
	}

	/** Overrides the category default — each tier has its own colour. */
	@Override
	public Color getColor()
	{
		return result.getTier().getColor();
	}
}
