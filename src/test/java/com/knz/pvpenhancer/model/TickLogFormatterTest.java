package com.knz.pvpenhancer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TickLogFormatter}.
 */
public class TickLogFormatterTest
{
	@Test
	public void emptyHistoryIsEmptyString()
	{
		assertEquals("", TickLogFormatter.format(Collections.emptyList()));
	}

	@Test
	public void rendersOldestFirstWithIndentedEvents()
	{
		// Service stores newest-first; entry seq 2 is newest, seq 1 is oldest.
		TickEntry newest = new TickEntry(2, 102, Arrays.asList(new EatEvent("p", "Shark")));
		TickEntry oldest = new TickEntry(1, 101, Arrays.asList(new EatEvent("p", "Karambwan")));
		List<TickEntry> entries = new ArrayList<>(Arrays.asList(newest, oldest));

		String out = TickLogFormatter.format(entries);
		String nl = System.lineSeparator();
		String expected =
			"Tick 0001" + nl
			+ "  p ate Karambwan" + nl
			+ "Tick 0002" + nl
			+ "  p ate Shark" + nl;

		assertEquals(expected, out);
	}

	@Test
	public void includesAllEventsOnATick()
	{
		TickEntry entry = new TickEntry(5, 200, Arrays.asList(
			new GearSwapEvent("p", "WEAPON", 4151, "Abyssal whip"),
			new EatEvent("p", "Shark")));
		String out = TickLogFormatter.format(Collections.singletonList(entry));
		assertTrue(out.contains("Tick 0005"));
		assertTrue(out.contains("equipped Abyssal whip"));
		assertTrue(out.contains("ate Shark"));
	}
}
