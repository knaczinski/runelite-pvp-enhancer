package com.knz.pvpenhancer.model;

/**
 * Damage (or a block) applied to an actor. The hitsplat arrives 1-3 ticks after the
 * attack animation depending on weapon and attack type, so it is logged on its own
 * tick rather than correlated with the originating {@link AttackEvent} (see the design
 * doc, "Known limitations").
 */
public class HitsplatEvent extends CombatEvent
{
	private final String target;
	private final int amount;
	private final String type;

	public HitsplatEvent(String target, int amount, String type)
	{
		this.target = target;
		this.amount = amount;
		this.type = type;
	}

	public String getTarget()
	{
		return target;
	}

	public int getAmount()
	{
		return amount;
	}

	public String getType()
	{
		return type;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.COMBAT;
	}

	@Override
	public String format()
	{
		return target + " took " + amount + " (" + type + ")";
	}
}
