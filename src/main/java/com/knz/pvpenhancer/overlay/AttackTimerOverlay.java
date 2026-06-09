package com.knz.pvpenhancer.overlay;

import java.awt.Color;
import java.awt.Dimension;
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
 * Draws, above each tracked actor's head (over the PK skull), a small depleting "cooldown pie"
 * showing how much of the attack cooldown remains until they can attack again — a visual cue the
 * brain reads faster than a number. The plugin feeds the in-scope actors + their
 * {@code [startMs, readyMs]} window each tick via {@link #setTimers}; the wedge shrinks off the
 * wall clock. The wedge colours green→red as it empties.
 */
@Singleton
public class AttackTimerOverlay extends Overlay
{
	private static final int RADIUS = 9;
	private static final int Z_OFFSET = 175; // above the resized skull
	private static final Color DISC_BG = new Color(0, 0, 0, 150);
	private static final Color OUTLINE = new Color(0, 0, 0, 200);

	private Map<Actor, long[]> timers = Collections.emptyMap();

	@Inject
	AttackTimerOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	/** Sets the actors to show, mapped to their {@code [startMs, readyMs]} cooldown window. Client thread. */
	public void setTimers(Map<Actor, long[]> timers)
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
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		long now = System.currentTimeMillis();
		for (Map.Entry<Actor, long[]> e : timers.entrySet())
		{
			Actor actor = e.getKey();
			long[] window = e.getValue();
			if (actor == null || window == null || window[1] <= window[0])
			{
				continue;
			}
			float fraction = (window[1] - now) / (float) (window[1] - window[0]);
			if (fraction <= 0f)
			{
				continue;
			}
			fraction = Math.min(1f, fraction);

			Point loc = actor.getCanvasTextLocation(graphics, "", actor.getLogicalHeight() + Z_OFFSET);
			if (loc == null)
			{
				continue;
			}
			int cx = loc.getX();
			int cy = loc.getY();
			int d = RADIUS * 2;

			graphics.setColor(DISC_BG);
			graphics.fillOval(cx - RADIUS, cy - RADIUS, d, d);

			// Remaining wedge from the top, clockwise; green when fresh, red as it empties.
			graphics.setColor(wedgeColour(fraction));
			graphics.fillArc(cx - RADIUS, cy - RADIUS, d, d, 90, -(int) (fraction * 360));

			graphics.setColor(OUTLINE);
			graphics.drawOval(cx - RADIUS, cy - RADIUS, d, d);
		}
		return null;
	}

	private static Color wedgeColour(float fraction)
	{
		// fraction 1 (full) → green, 0 (empty) → red
		int r = (int) (Math.min(1f, 2f * (1f - fraction)) * 255);
		int g = (int) (Math.min(1f, 2f * fraction) * 255);
		return new Color(r, g, 40, 220);
	}
}
