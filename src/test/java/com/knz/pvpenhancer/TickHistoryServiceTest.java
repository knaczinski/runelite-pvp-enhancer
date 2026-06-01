package com.knz.pvpenhancer;

import com.knz.pvpenhancer.model.EatEvent;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TickHistoryService}: buffer cap, ordering, grouping, and trim.
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
		assertEquals(4, entries.get(1).getTick());
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
	public void groupsMultipleEventsUnderOneTick()
	{
		TickHistoryService service = new TickHistoryService();
		service.addEvent(new EatEvent("p", "a"));
		service.addEvent(new EatEvent("p", "b"));
		service.flushTick(7);

		List<TickEntry> entries = service.getEntries();
		assertEquals(1, entries.size());
		assertEquals(7, entries.get(0).getTick());
		assertEquals(2, entries.get(0).getEvents().size());
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
	public void clearEmptiesBuffer()
	{
		TickHistoryService service = new TickHistoryService();
		service.addEvent(new EatEvent("p", "x"));
		service.flushTick(1);
		service.clear();
		assertTrue(service.getEntries().isEmpty());
	}
}
