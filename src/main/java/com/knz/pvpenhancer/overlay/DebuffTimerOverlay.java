package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.service.DebuffTrackerService;
import com.knz.pvpenhancer.service.DebuffTrackerService.ActiveDebuff;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws a freeze/snare/teleblock countdown over each affected actor's head, coloured per
 * {@link com.knz.pvpenhancer.model.Debuff}. The timer text shows the remaining game ticks; the
 * plugin counts them down via {@link DebuffTrackerService#tick()}.
 */
public class DebuffTimerOverlay extends Overlay
{
	private static final Font TIMER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	private final DebuffTrackerService tracker;

	@Inject
	DebuffTimerOverlay(DebuffTrackerService tracker)
	{
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<Actor, ActiveDebuff> active = tracker.getActive();
		if (active.isEmpty())
		{
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(TIMER_FONT);

		for (Map.Entry<Actor, ActiveDebuff> e : active.entrySet())
		{
			Actor actor = e.getKey();
			ActiveDebuff debuff = e.getValue();
			if (actor == null)
			{
				continue;
			}
			String text = debuff.debuff.getLabel() + " " + debuff.ticksRemaining;
			// Above the head, clear of the heal popup (+50) so they don't overlap.
			Point loc = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight() + 80);
			if (loc == null)
			{
				continue;
			}
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, loc.getX() + 1, loc.getY() + 1);
			graphics.setColor(debuff.debuff.getColor());
			graphics.drawString(text, loc.getX(), loc.getY());
		}
		return null;
	}
}
