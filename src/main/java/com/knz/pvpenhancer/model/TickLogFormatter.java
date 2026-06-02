package com.knz.pvpenhancer.model;

import java.util.List;

/**
 * Formats the tick history into a plain-text dump for clipboard export / post-fight review.
 * Pure and stateless. Renders oldest-first (matching the on-screen order), every event of
 * every tick — no category filtering, so the export is a complete record.
 */
public final class TickLogFormatter
{
	private TickLogFormatter()
	{
	}

	/**
	 * @param entries tick history newest-first (as the service stores it)
	 * @return a multi-line text dump, oldest tick first; empty string if there is nothing
	 */
	public static String format(List<TickEntry> entries)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = entries.size() - 1; i >= 0; i--)
		{
			TickEntry entry = entries.get(i);
			sb.append(String.format("Tick %04d", entry.getSequence())).append(System.lineSeparator());
			for (CombatEvent event : entry.getEvents())
			{
				sb.append("  ").append(event.format()).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}
}
