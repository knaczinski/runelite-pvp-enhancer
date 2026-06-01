package com.knz.pvpenhancer.model;

/**
 * A food or potion consumed by the local player. Detected from the "Eat"/"Drink" menu
 * click, which fires on the tick the player clicks (before the eat animation plays).
 */
public class EatEvent extends CombatEvent
{
	private final String player;
	private final String item;

	public EatEvent(String player, String item)
	{
		this.player = player;
		this.item = item;
	}

	public String getPlayer()
	{
		return player;
	}

	public String getItem()
	{
		return item;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.EATING;
	}

	@Override
	public String format()
	{
		return player + " ate " + item;
	}
}
