package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.Debuff;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Actor;

/**
 * Tracks active freeze/snare/teleblock timers per actor. An actor can carry several debuffs at
 * once (e.g. frozen AND teleblocked), each with its own countdown. The plugin applies a debuff
 * when it sees the matching spot-anim and calls {@link #tick()} once per game tick to count down.
 *
 * <p>All access is on the client thread (event handlers + overlay render), so no
 * synchronization is needed.
 */
@Singleton
public class DebuffTrackerService
{
	private final Map<Actor, EnumMap<Debuff, Integer>> active = new HashMap<>();

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
		EnumMap<Debuff, Integer> debuffs = active.computeIfAbsent(actor, a -> new EnumMap<>(Debuff.class));
		debuffs.merge(debuff, durationTicks, Math::max);
	}

	/** Counts every timer down by one tick and drops expired debuffs / vanished actors. */
	public void tick()
	{
		for (Iterator<Map.Entry<Actor, EnumMap<Debuff, Integer>>> it = active.entrySet().iterator(); it.hasNext(); )
		{
			Map.Entry<Actor, EnumMap<Debuff, Integer>> entry = it.next();
			if (entry.getKey() == null)
			{
				it.remove();
				continue;
			}
			EnumMap<Debuff, Integer> debuffs = entry.getValue();
			debuffs.replaceAll((d, ticks) -> ticks - 1);
			debuffs.values().removeIf(ticks -> ticks <= 0);
			if (debuffs.isEmpty())
			{
				it.remove();
			}
		}
	}

	/** @return live map of actor → (debuff → ticks remaining). Read-only use on the render thread. */
	public Map<Actor, EnumMap<Debuff, Integer>> getActive()
	{
		return active;
	}

	public void clear()
	{
		active.clear();
	}
}
