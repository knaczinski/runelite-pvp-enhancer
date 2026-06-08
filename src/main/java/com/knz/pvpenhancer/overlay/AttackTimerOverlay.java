package com.knz.pvpenhancer.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws, above each tracked actor's head (over the PK skull), a countdown in seconds (2 decimals)
 * until they can attack again. The plugin feeds the in-scope actors + their ready-at epoch-ms
 * each tick via {@link #setTimers}; the countdown ticks down smoothly off the wall clock.
 */
@Singleton
public class AttackTimerOverlay extends Overlay
{
	private static final Color TEXT_COLOR = new Color(0xFF, 0xE0, 0x60);
	private static final Font TIMER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
	private static final int Z_OFFSET = 150; // above the resized skull

	private Map<Actor, Long> timers = Collections.emptyMap();

	@Inject
	AttackTimerOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	/** Sets the actors to show a countdown for, mapped to their attack-ready epoch-ms. Client thread. */
	public void setTimers(Map<Actor, Long> timers)
	{
		this.timers = timers == null ? Collections.emptyMap() : timers;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (timers.isEmpty())
		{
			return null;
		}
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(TIMER_FONT);
		long now = System.currentTimeMillis();
		for (Map.Entry<Actor, Long> e : timers.entrySet())
		{
			Actor actor = e.getKey();
			if (actor == null)
			{
				continue;
			}
			float remaining = (e.getValue() - now) / 1000f;
			if (remaining <= 0f)
			{
				continue;
			}
			String text = String.format("%.2f", remaining);
			Point loc = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight() + Z_OFFSET);
			if (loc == null)
			{
				continue;
			}
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, loc.getX() + 1, loc.getY() + 1);
			graphics.setColor(TEXT_COLOR);
			graphics.drawString(text, loc.getX(), loc.getY());
		}
		return null;
	}
}
