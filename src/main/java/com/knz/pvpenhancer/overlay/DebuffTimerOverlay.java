package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.model.Debuff;
import com.knz.pvpenhancer.service.DebuffTrackerService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

/**
 * Draws freeze/snare/teleblock timers to the right of each affected actor's health bar, stacked
 * under the heal popup. Each active debuff shows its OSRS-wiki spell icon plus a countdown in
 * seconds; several debuffs on one actor stack downward.
 */
public class DebuffTimerOverlay extends Overlay
{
	private static final String ICON_PATH = "/com/knz/pvpenhancer/icons/";
	private static final int ICON_SIZE = 16;
	private static final int ROW_HEIGHT = 18;
	private static final int RIGHT_OFFSET = 20;   // px right of the health bar
	private static final int TOP_OFFSET = 14;     // px below the heal row
	private static final Font TIMER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

	private final DebuffTrackerService tracker;
	private final Map<Debuff, BufferedImage> icons = new EnumMap<>(Debuff.class);

	@Inject
	DebuffTimerOverlay(DebuffTrackerService tracker)
	{
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		// UNDER_WIDGETS: over native overheads, under the game UI (no painting over an open bank).
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
		for (Debuff debuff : Debuff.values())
		{
			icons.put(debuff, ImageUtil.loadImageResource(getClass(), ICON_PATH + debuff.getIconFile()));
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<Actor, EnumMap<Debuff, Integer>> active = tracker.getActive();
		if (active.isEmpty())
		{
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(TIMER_FONT);

		for (Map.Entry<Actor, EnumMap<Debuff, Integer>> entry : active.entrySet())
		{
			Actor actor = entry.getKey();
			if (actor == null)
			{
				continue;
			}
			Point anchor = actor.getCanvasTextLocation(graphics, "", actor.getLogicalHeight());
			if (anchor == null)
			{
				continue;
			}

			int x = anchor.getX() + RIGHT_OFFSET;
			int y = anchor.getY() + TOP_OFFSET;
			for (Map.Entry<Debuff, Integer> d : entry.getValue().entrySet())
			{
				drawRow(graphics, d.getKey(), d.getValue(), x, y);
				y += ROW_HEIGHT;
			}
		}
		return null;
	}

	private void drawRow(Graphics2D graphics, Debuff debuff, int ticksRemaining, int x, int y)
	{
		BufferedImage icon = icons.get(debuff);
		if (icon != null)
		{
			graphics.drawImage(icon, x, y - ICON_SIZE + 2, ICON_SIZE, ICON_SIZE, null);
		}
		int seconds = (int) Math.ceil(ticksRemaining * 0.6);
		String text = seconds + "s";
		int textX = x + ICON_SIZE + 3;
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, textX + 1, y + 1);
		graphics.setColor(debuff.getColor());
		graphics.drawString(text, textX, y);
	}
}
