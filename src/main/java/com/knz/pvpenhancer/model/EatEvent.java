package com.knz.pvpenhancer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One or more foods/potions consumed by a player on a single tick. A single click is one
 * item; a combo (e.g. shark + karambwan, or food + potion sip on the same tick) carries
 * several items and renders as "ate A + B (double eat)".
 *
 * <p>Same-player eats within a tick are merged into one {@code EatEvent} by
 * {@link ComboEatMerger} at flush time.
 */
public class EatEvent extends CombatEvent
{
	private final String player;
	private final List<String> items;

	public EatEvent(String player, String item)
	{
		this(player, Collections.singletonList(item));
	}

	public EatEvent(String player, List<String> items)
	{
		this.player = player;
		this.items = Collections.unmodifiableList(new ArrayList<>(items));
	}

	public String getPlayer()
	{
		return player;
	}

	/**
	 * @return the items consumed on this tick, in click order. Size 1 for a normal eat,
	 * 2+ for a combo eat.
	 */
	public List<String> getItems()
	{
		return items;
	}

	@Override
	public EventCategory getCategory()
	{
		return EventCategory.EATING;
	}

	@Override
	public String format()
	{
		if (items.size() == 1)
		{
			return player + " ate " + items.get(0);
		}
		return player + " ate " + String.join(" + ", items) + " (" + comboLabel(items.size()) + ")";
	}

	/**
	 * @return a label for an n-item combo eat: "double eat", "triple eat", else "Nx eat".
	 */
	private static String comboLabel(int count)
	{
		switch (count)
		{
			case 2:
				return "double eat";
			case 3:
				return "triple eat";
			default:
				return count + "x eat";
		}
	}
}
