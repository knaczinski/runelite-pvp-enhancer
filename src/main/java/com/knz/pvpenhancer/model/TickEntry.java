package com.knz.pvpenhancer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All events that occurred on a single game tick.
 *
 * <p>Carries two numbers: a {@link #getSequence() sequence} — a clean 1-based code
 * assigned in recording order, shown to the user as "Tick 0001" — and the raw
 * {@link #getTick() game tick} ({@code client.getTickCount()}) retained for any future
 * timing analysis. A tick can hold many events, by different characters.
 *
 * <p>Immutable once constructed: the event list is defensively copied and wrapped
 * unmodifiable so the overlay can iterate it on the client thread without risk of
 * concurrent mutation.
 */
public class TickEntry
{
	private final int sequence;
	private final int tick;
	private final List<CombatEvent> events;

	public TickEntry(int sequence, int tick, List<CombatEvent> events)
	{
		this.sequence = sequence;
		this.tick = tick;
		this.events = Collections.unmodifiableList(new ArrayList<>(events));
	}

	/**
	 * @return the 1-based recording code for this tick (e.g. 1 renders as "Tick 0001").
	 * Stable for the lifetime of the entry; assigned by the service in flush order.
	 */
	public int getSequence()
	{
		return sequence;
	}

	/**
	 * @return the raw server tick number this entry was recorded on ({@code client.getTickCount()}).
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
