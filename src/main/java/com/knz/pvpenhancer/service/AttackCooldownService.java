package com.knz.pvpenhancer.service;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Actor;

/**
 * Tracks, per actor, the current attack-cooldown window as {@code [startMs, readyMs]} in wall-clock
 * epoch-ms (≈ tick × 600ms), so the overlay can draw a smooth depleting cooldown indicator.
 *
 * <p>An attack opens a window of length weaponSpeed; eating/drinking opens a fresh 3-tick window
 * when that pushes the ready time later. All access is on the client thread.
 */
@Singleton
public class AttackCooldownService
{
	private static final long TICK_MS = 600L;
	private static final long EAT_DELAY_TICKS = 3L;

	/** actor → [startMs, readyMs]. */
	private final Map<Actor, long[]> windows = new HashMap<>();

	/** Records an attack: opens a window of {@code speedTicks} from now. */
	public void recordAttack(Actor actor, int speedTicks)
	{
		if (actor != null && speedTicks > 0)
		{
			long now = System.currentTimeMillis();
			windows.put(actor, new long[]{now, now + speedTicks * TICK_MS});
		}
	}

	/** Records a consume (eat/drink): opens a fresh 3-tick window when it pushes the ready time later. */
	public void recordConsume(Actor actor)
	{
		if (actor == null)
		{
			return;
		}
		long now = System.currentTimeMillis();
		long ready = now + EAT_DELAY_TICKS * TICK_MS;
		long[] existing = windows.get(actor);
		if (existing == null || ready > existing[1])
		{
			windows.put(actor, new long[]{now, ready});
		}
	}

	/** @return [startMs, readyMs] for the actor, or null. */
	public long[] window(Actor actor)
	{
		return windows.get(actor);
	}

	public void remove(Actor actor)
	{
		windows.remove(actor);
	}

	/** Drops windows that finished a while ago (and any null actors). */
	public void prune()
	{
		long cutoff = System.currentTimeMillis() - 500L;
		windows.entrySet().removeIf(e -> e.getKey() == null || e.getValue()[1] < cutoff);
	}

	public void clear()
	{
		windows.clear();
	}
}
