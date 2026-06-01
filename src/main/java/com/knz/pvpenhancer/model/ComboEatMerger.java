package com.knz.pvpenhancer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collapses multiple {@link EatEvent}s by the same player within one tick into a single
 * combo-eat event (e.g. shark + karambwan eaten on the same tick → one "ate A + B
 * (double eat)" line).
 *
 * <p>Pure and stateless: operates on one tick's event list and returns a new list. Only
 * same-player eats are merged; events of other types and other players keep their order.
 */
public final class ComboEatMerger
{
	private ComboEatMerger()
	{
	}

	/**
	 * @param events the raw events recorded on a tick, in insertion order
	 * @return a new list where each player's eats are merged into one {@link EatEvent} at
	 * the position of that player's first eat; all other events are preserved in order.
	 */
	public static List<CombatEvent> merge(List<CombatEvent> events)
	{
		// First pass: gather each player's eaten items, in first-seen player order.
		Map<String, List<String>> itemsByPlayer = new LinkedHashMap<>();
		for (CombatEvent event : events)
		{
			if (event instanceof EatEvent)
			{
				EatEvent eat = (EatEvent) event;
				itemsByPlayer.computeIfAbsent(eat.getPlayer(), k -> new ArrayList<>()).addAll(eat.getItems());
			}
		}

		// Second pass: rebuild, emitting one merged eat at each player's first eat.
		List<CombatEvent> result = new ArrayList<>(events.size());
		Set<String> emitted = new HashSet<>();
		for (CombatEvent event : events)
		{
			if (!(event instanceof EatEvent))
			{
				result.add(event);
				continue;
			}
			String player = ((EatEvent) event).getPlayer();
			if (!emitted.add(player))
			{
				continue; // already merged into this player's first eat
			}
			List<String> items = itemsByPlayer.get(player);
			result.add(items.size() == 1 ? event : new EatEvent(player, items));
		}
		return result;
	}
}
