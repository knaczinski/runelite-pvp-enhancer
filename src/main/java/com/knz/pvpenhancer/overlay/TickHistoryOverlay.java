package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.CombatEvent;
import com.knz.pvpenhancer.model.EventCategory;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Renders the {@link TickHistoryService} buffer as a side panel: a title, then each tick
 * (newest first) shown as a "Tick N" header followed by one colour-coded line per event.
 * Read-only — it never mutates the service. Honours the per-category visibility toggles.
 */
public class TickHistoryOverlay extends OverlayPanel
{
	private static final Color COLOR_TICK = Color.LIGHT_GRAY;
	private static final Color COLOR_COMBAT = new Color(0xFF6B6B);
	private static final Color COLOR_EATING = new Color(0x6BCB77);
	private static final Color COLOR_GEAR = new Color(0x4D96FF);

	private final PvpEnhancerConfig config;
	private final TickHistoryService history;

	@Inject
	TickHistoryOverlay(PvpEnhancerConfig config, TickHistoryService history)
	{
		this.config = config;
		this.history = history;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<TickEntry> entries = history.getEntries();
		if (entries.isEmpty())
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(230, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("PvP Tick History")
			.color(Color.WHITE)
			.build());

		// getEntries() is newest-first; render oldest-first so ticks read top-to-bottom
		// in chronological order (Tick 0001, 0002, 0003 ...).
		for (int i = entries.size() - 1; i >= 0; i--)
		{
			TickEntry entry = entries.get(i);
			boolean headerWritten = false;
			for (CombatEvent event : entry.getEvents())
			{
				if (!isCategoryEnabled(event.getCategory()))
				{
					continue;
				}
				if (!headerWritten)
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left(String.format("Tick %04d", entry.getSequence()))
						.leftColor(COLOR_TICK)
						.build());
					headerWritten = true;
				}
				panelComponent.getChildren().add(LineComponent.builder()
					.left(event.format())
					.leftColor(categoryColor(event.getCategory()))
					.build());
			}
		}

		return super.render(graphics);
	}

	private boolean isCategoryEnabled(EventCategory category)
	{
		switch (category)
		{
			case COMBAT:
				return config.showCombat();
			case EATING:
				return config.showEating();
			case GEAR_SWAP:
				return config.showGearSwap();
			default:
				return true;
		}
	}

	private static Color categoryColor(EventCategory category)
	{
		switch (category)
		{
			case COMBAT:
				return COLOR_COMBAT;
			case EATING:
				return COLOR_EATING;
			case GEAR_SWAP:
				return COLOR_GEAR;
			default:
				return Color.WHITE;
		}
	}
}
