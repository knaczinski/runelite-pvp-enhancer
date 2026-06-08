package com.knz.pvpenhancer.service;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Actor;

/**
 * Tracks, per actor, the wall-clock time at which they can attack again — driving the
 * countdown-to-next-attack timer. Time is kept in epoch-ms (≈ tick × 600ms) so the overlay can
 * show a smooth 2-decimal seconds countdown.
 *
 * <p>An attack sets the timer to now + weaponSpeed; eating/drinking pushes it back by the eat
 * delay (3 ticks) since a consume delays your next attack. All access is on the client thread.
 */
@Singleton
public class AttackCooldownService
{
	private static final long TICK_MS = 600L;
	private static final long EAT_DELAY_TICKS = 3L;

	private final Map<Actor, Long> readyAtMs = new HashMap<>();

	/** Records an attack: the actor can attack again in {@code speedTicks} ticks. */
	public void recordAttack(Actor actor, int speedTicks)
	{
		if (actor != null && speedTicks > 0)
		{
			readyAtMs.put(actor, System.currentTimeMillis() + speedTicks * TICK_MS);
		}
	}

	/** Records a consume (eat/drink): delays the next attack by the eat delay, never shortening it. */
	public void recordConsume(Actor actor)
	{
		if (actor != null)
		{
			readyAtMs.merge(actor, System.currentTimeMillis() + EAT_DELAY_TICKS * TICK_MS, Math::max);
		}
	}

	/** @return epoch-ms when the actor can attack again, or null if untracked. */
	public Long readyAt(Actor actor)
	{
		return readyAtMs.get(actor);
	}

	public void remove(Actor actor)
	{
		readyAtMs.remove(actor);
	}

	/** Drops timers that finished a while ago (and any null actors). */
	public void prune()
	{
		long cutoff = System.currentTimeMillis() - 1000L;
		readyAtMs.entrySet().removeIf(e -> e.getKey() == null || e.getValue() < cutoff);
	}

	public void clear()
	{
		readyAtMs.clear();
	}
}
