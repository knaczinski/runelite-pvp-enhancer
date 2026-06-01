package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.CombatEvent;
import com.knz.pvpenhancer.model.TickEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.inject.Singleton;

/**
 * Stateful, tick-bucketed combat event buffer. Single source of truth for the overlay.
 *
 * <p>Events arriving from the plugin's {@code @Subscribe} handlers during a tick are
 * accumulated in {@code pending}. On the next {@code GameTick} the plugin calls
 * {@link #flushTick(int)}, which seals the pending events into an immutable
 * {@link TickEntry} pushed to the front of the buffer (newest first). The buffer is
 * capped at {@link #setMaxHistory(int)} entries.
 *
 * <p>Guice-scoped {@code @Singleton} so the plugin and the overlay share one instance.
 * All access is on the client thread; no synchronisation is required.
 */
@Singleton
public class TickHistoryService
{
	private static final int DEFAULT_MAX_HISTORY = 20;

	private final Deque<TickEntry> entries = new ArrayDeque<>();
	private final List<CombatEvent> pending = new ArrayList<>();
	private int maxHistory = DEFAULT_MAX_HISTORY;

	/**
	 * Sets the maximum number of tick entries retained and trims immediately if the new
	 * cap is smaller. Values below 1 are clamped to 1.
	 */
	public void setMaxHistory(int maxHistory)
	{
		this.maxHistory = Math.max(1, maxHistory);
		trim();
	}

	/**
	 * Queues an event for the current (not-yet-flushed) tick. Null events are ignored.
	 */
	public void addEvent(CombatEvent event)
	{
		if (event != null)
		{
			pending.add(event);
		}
	}

	/**
	 * Seals all events queued since the last flush into a {@link TickEntry} for the given
	 * tick and pushes it to the front of the buffer. A tick with no events is not stored.
	 *
	 * @param tickNumber the server tick number ({@code client.getTickCount()})
	 */
	public void flushTick(int tickNumber)
	{
		if (pending.isEmpty())
		{
			return;
		}
		entries.addFirst(new TickEntry(tickNumber, pending));
		pending.clear();
		trim();
	}

	/**
	 * @return a snapshot of the buffer, newest tick first. Safe to iterate without
	 * affecting the live buffer.
	 */
	public List<TickEntry> getEntries()
	{
		return new ArrayList<>(entries);
	}

	/**
	 * Clears all history and any pending events. Called on plugin start/stop and on logout.
	 */
	public void clear()
	{
		entries.clear();
		pending.clear();
	}

	private void trim()
	{
		while (entries.size() > maxHistory)
		{
			entries.removeLast();
		}
	}
}
