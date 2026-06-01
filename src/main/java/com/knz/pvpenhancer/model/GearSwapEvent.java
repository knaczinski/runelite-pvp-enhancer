package com.knz.pvpenhancer.model;

/**
 * An equipment change detected by diffing a player's equipment array between two game
 * ticks. {@code itemId} is the resolved OSRS item id, or -1 when the slot was emptied.
 */
public class GearSwapEvent extends CombatEvent
{
	private final String player;
	private final String slot;
	private final int itemId;
	private final String item;

	public GearSwapEvent(String player, String slot, int itemId, String item)
	{
		this.player = player;
		this.slot = slot;
		this.itemId = itemId;
		this.item = item;
	}

	public String getPlayer()
	{
		return player;
	}

	public String getSlot()
	{
		return slot;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getItem()
	{
		return item;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.GEAR_SWAP;
	}

	@Override
	public String format()
	{
		return player + " equipped " + item + " (" + slot.toLowerCase() + ")";
	}
}
