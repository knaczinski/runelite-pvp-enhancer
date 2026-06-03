package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Re-renders the Vengeance overhead text at a configurable size. The native overhead text has a
 * fixed size that the API does not expose; the plugin captures it, clears the native text, and
 * feeds it here so this overlay can draw a scaled copy with its own fade.
 */
@Singleton
public class VengeanceTextOverlay extends Overlay
{
	private static final long DURATION_MS = 1500L;
	private static final int BASE_FONT = 16;
	private static final Color TEXT_COLOR = new Color(0xFF, 0xFF, 0x00); // native veng text is yellow

	private final PvpEnhancerConfig config;
	private final List<Popup> popups = new ArrayList<>();

	@Inject
	VengeanceTextOverlay(PvpEnhancerConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	/** Queues a scaled overhead-text popup over an actor. */
	public void add(Actor actor, String text)
	{
		if (actor != null && text != null && !text.isEmpty())
		{
			popups.add(new Popup(actor, text, System.currentTimeMillis()));
		}
	}

	public void clear()
	{
		popups.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (popups.isEmpty())
		{
			return null;
		}

		int fontSize = Math.max(6, Math.round(BASE_FONT * config.vengTextSize() / 100f));
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));

		long now = System.currentTimeMillis();
		for (Iterator<Popup> it = popups.iterator(); it.hasNext(); )
		{
			Popup popup = it.next();
			long elapsed = now - popup.startMs;
			if (elapsed >= DURATION_MS)
			{
				it.remove();
				continue;
			}
			Point base = popup.actor.getCanvasTextLocation(graphics, popup.text, popup.actor.getLogicalHeight() + 40);
			if (base == null)
			{
				continue;
			}
			int alpha = (int) ((1f - (float) elapsed / DURATION_MS) * 255);
			graphics.setColor(new Color(0, 0, 0, Math.min(alpha, 200)));
			graphics.drawString(popup.text, base.getX() + 1, base.getY() + 1);
			graphics.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), alpha));
			graphics.drawString(popup.text, base.getX(), base.getY());
		}
		return null;
	}

	private static final class Popup
	{
		private final Actor actor;
		private final String text;
		private final long startMs;

		Popup(Actor actor, String text, long startMs)
		{
			this.actor = actor;
			this.text = text;
			this.startMs = startMs;
		}
	}
}
