package com.knz.pvpenhancer.model;

import net.runelite.api.HeadIcon;

/**
 * An attack thrown by one player at a target. Records the attacker, the target, the
 * inferred {@link AttackStyle}, and the overhead prayer the target had active at the
 * moment the attack animation started (null if none).
 */
public class AttackEvent extends CombatEvent
{
	private final String attacker;
	private final String target;
	private final AttackStyle style;
	private final HeadIcon targetPrayer;

	public AttackEvent(String attacker, String target, AttackStyle style, HeadIcon targetPrayer)
	{
		this.attacker = attacker;
		this.target = target;
		this.style = style;
		this.targetPrayer = targetPrayer;
	}

	public String getAttacker()
	{
		return attacker;
	}

	public String getTarget()
	{
		return target;
	}

	public AttackStyle getStyle()
	{
		return style;
	}

	public HeadIcon getTargetPrayer()
	{
		return targetPrayer;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.COMBAT;
	}

	@Override
	public String format()
	{
		return attacker + " -> " + target + "  " + style.getLabel() + "  on " + prayerLabel(targetPrayer);
	}

	/**
	 * Maps an overhead prayer icon to a short label for the overlay. Returns "no prayer"
	 * when the target had no overhead prayer active.
	 */
	private static String prayerLabel(HeadIcon icon)
	{
		if (icon == null)
		{
			return "no prayer";
		}
		switch (icon)
		{
			case MELEE:
				return "pro melee";
			case RANGED:
				return "pro range";
			case MAGIC:
				return "pro mage";
			default:
				return icon.name().toLowerCase().replace('_', ' ');
		}
	}
}
