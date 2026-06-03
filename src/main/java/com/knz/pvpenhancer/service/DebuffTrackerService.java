package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.Debuff;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Actor;

/**
 * Tracks active freeze/snare/teleblock timers per actor. The plugin applies a debuff when it
 * sees the matching spot-anim and calls {@link #tick()} once per game tick to count down.
 *
 * <p>All access is on the client thread (event handlers + overlay render), so no
 * synchronization is needed.
 */
@Singleton
public class DebuffTrackerService
{
	/** A live timer: the debuff and how many ticks remain. */
	public static final class ActiveDebuff
	{
		public final Debuff debuff;
		public int ticksRemaining;

		ActiveDebuff(Debuff debuff, int ticksRemaining)
		{
			this.debuff = debuff;
			this.ticksRemaining = ticksRemaining;
		}
	}

	private final Map<Actor, ActiveDebuff> active = new HashMap<>();

	/**
	 * Applies (or refreshes) a debuff on an actor. A re-application only extends the timer, never
	 * shortens it — re-casting the same freeze on an already-frozen target does not reset it.
	 */
	public void apply(Actor actor, Debuff debuff, int durationTicks)
	{
		if (actor == null || durationTicks <= 0)
		{
			return;
		}
		ActiveDebuff existing = active.get(actor);
		if (existing != null && existing.debuff == debuff && existing.ticksRemaining >= durationTicks)
		{
			return; // keep the longer running timer
		}
		active.put(actor, new ActiveDebuff(debuff, durationTicks));
	}

	/** Counts every timer down by one tick and drops expired / vanished actors. */
	public void tick()
	{
		for (Iterator<Map.Entry<Actor, ActiveDebuff>> it = active.entrySet().iterator(); it.hasNext(); )
		{
			Map.Entry<Actor, ActiveDebuff> e = it.next();
			if (e.getKey() == null)
			{
				it.remove();
				continue;
			}
			if (--e.getValue().ticksRemaining <= 0)
			{
				it.remove();
			}
		}
	}

	public Map<Actor, ActiveDebuff> getActive()
	{
		return active;
	}

	public void clear()
	{
		active.clear();
	}
}
