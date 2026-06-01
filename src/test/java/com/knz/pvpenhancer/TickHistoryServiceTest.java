package com.knz.pvpenhancer;

import com.knz.pvpenhancer.model.EatEvent;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TickHistoryService}: buffer cap, ordering, per-tick grouping,
 * combo-eat merging, sequence codes, trim, and clear.
 */
public class TickHistoryServiceTest
{
	@Test
	public void capsBufferAndKeepsNewestFirst()
	{
		TickHistoryService service = new TickHistoryService();
		service.setMaxHistory(3);

		for (int tick = 1; tick <= 5; tick++)
		{
			service.addEvent(new EatEvent("p", "food" + tick));
			service.flushTick(tick);
		}

		List<TickEntry> entries = service.getEntries();
		assertEquals(3, entries.size());
		assertEquals(5, entries.get(0).getTick()); // newest first
		assertEquals(3, entries.get(2).getTick()); // oldest retained
	}

	@Test
	public void emptyTickIsNotStored()
	{
		TickHistoryService service = new TickHistoryService();
		service.flushTick(1); // nothing pending
		assertTrue(service.getEntries().isEmpty());
	}

	@Test
	public void differentPlayersStaySeparateLines()
	{
		TickHistoryService service = new TickHistoryService();
		service.addEvent(new EatEvent("p1", "a"));
		service.addEvent(new EatEvent("p2", "b"));
		service.flushTick(7);

		List<TickEntry> entries = service.getEntries();
		assertEquals(1, entries.size());
		assertEquals(2, entries.get(0).getEvents().size()); // not merged across players
	}

	@Test
	public void samePlayerComboEatsMergeIntoOneLine()
	{
		TickHistoryService service = new TickHistoryService();
		service.addEvent(new EatEvent("p", "Shark"));
		service.addEvent(new EatEvent("p", "Karambwan"));
		service.flushTick(2);

		TickEntry entry = service.getEntries().get(0);
		assertEquals(1, entry.getEvents().size());
		String line = entry.getEvents().get(0).format();
		assertEquals("p ate Shark + Karambwan (double eat)", line);
	}

	@Test
	public void assignsSequentialTickCodesNewestFirst()
	{
		TickHistoryService service = new TickHistoryService();
		for (int tick = 100; tick <= 102; tick++)
		{
			service.addEvent(new EatEvent("p", "f"));
			service.flushTick(tick);
		}

		List<TickEntry> entries = service.getEntries();
		assertEquals(3, entries.get(0).getSequence()); // newest = 3rd recorded
		assertEquals(2, entries.get(1).getSequence());
		assertEquals(1, entries.get(2).getSequence());
	}

	@Test
	public void shrinkingMaxHistoryTrimsImmediately()
	{
		TickHistoryService service = new TickHistoryService();
		service.setMaxHistory(10);
		for (int tick = 1; tick <= 8; tick++)
		{
			service.addEvent(new EatEvent("p", "f" + tick));
			service.flushTick(tick);
		}
		assertEquals(8, service.getEntries().size());

		service.setMaxHistory(2);
		assertEquals(2, service.getEntries().size());
		assertEquals(8, service.getEntries().get(0).getTick());
	}

	@Test
	public void clearEmptiesBufferAndResetsSequence()
	{
		TickHistoryService service = new TickHistoryService();
		service.addEvent(new EatEvent("p", "x"));
		service.flushTick(1);
		service.addEvent(new EatEvent("p", "y"));
		service.flushTick(2);

		service.clear();
		assertTrue(service.getEntries().isEmpty());

		// Sequence restarts at 1 after a clear.
		service.addEvent(new EatEvent("p", "z"));
		service.flushTick(99);
		assertEquals(1, service.getEntries().get(0).getSequence());
	}
}
