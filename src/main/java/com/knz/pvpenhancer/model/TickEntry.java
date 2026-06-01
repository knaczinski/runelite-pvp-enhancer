package com.knz.pvpenhancer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All events that occurred on a single game tick. Immutable once constructed: the event
 * list is defensively copied and wrapped unmodifiable so the overlay can iterate it on
 * the client thread without risk of concurrent mutation.
 */
public class TickEntry
{
	private final int tick;
	private final List<CombatEvent> events;

	public TickEntry(int tick, List<CombatEvent> events)
	{
		this.tick = tick;
		this.events = Collections.unmodifiableList(new ArrayList<>(events));
	}

	/**
	 * @return the server tick number this entry was recorded on ({@code client.getTickCount()}).
	 */
	public int getTick()
	{
		return tick;
	}

	/**
	 * @return an unmodifiable list of the events recorded on this tick, in insertion order.
	 */
	public List<CombatEvent> getEvents()
	{
		return events;
	}
}
