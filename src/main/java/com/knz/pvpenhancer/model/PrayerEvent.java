package com.knz.pvpenhancer.model;

import net.runelite.api.HeadIcon;

/**
 * A change to a player's overhead protection prayer, detected by diffing
 * {@code Player.getOverheadIcon()} between ticks. {@code icon} is the new overhead, or
 * {@code null} when the player turned their overhead prayer off.
 *
 * <p>Only the overhead protection prayer is observable for remote players; offensive
 * prayers (Piety, Rigour, …) have no overhead and are out of scope here.
 */
public class PrayerEvent extends CombatEvent
{
	private final String player;
	private final HeadIcon icon;

	public PrayerEvent(String player, HeadIcon icon)
	{
		this.player = player;
		this.icon = icon;
	}

	public String getPlayer()
	{
		return player;
	}

	public HeadIcon getIcon()
	{
		return icon;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.PRAYER;
	}

	@Override
	public String format()
	{
		if (icon == null)
		{
			return player + " prayer off";
		}
		return player + " prayed " + PrayerNames.label(icon);
	}
}
