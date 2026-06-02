package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.HealDisplayMode;
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
import net.runelite.api.Actor;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Renders floating "+N" healing numbers near the health bar of the actor who healed. Green
 * text that rises and fades over {@value #DURATION_MS} ms. Remote-player heals are estimates
 * (prefixed "~") because the API only exposes their health ratio, not real HP.
 *
 * <p>The plugin detects heals on each game tick and calls {@link #addHeal}. Both detection
 * and rendering run on the client thread, so the popup list needs no synchronization.
 */
public class HealOverlay extends Overlay
{
	private static final long DURATION_MS = 1500L;
	private static final int RISE_PX = 28;
	private static final Color HEAL_COLOR = new Color(0x33, 0xDD, 0x33);
	private static final Font HEAL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	private final net.runelite.api.Client client;
	private final PvpEnhancerConfig config;
	private final List<HealPopup> popups = new ArrayList<>();

	@Inject
	HealOverlay(net.runelite.api.Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	/**
	 * Queues a healing popup over the given actor.
	 *
	 * @param actor    the healed actor (its live position is used each frame)
	 * @param amount   HP healed
	 * @param estimate true if the amount is an estimate (remote player) — shown with "~"
	 */
	public void addHeal(Actor actor, int amount, boolean estimate)
	{
		if (actor == null || amount <= 0)
		{
			return;
		}
		String text = (estimate ? "~" : "+") + amount;
		popups.add(new HealPopup(actor, text, System.currentTimeMillis()));
	}

	public void clear()
	{
		popups.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.healDisplayMode() == HealDisplayMode.OFF)
		{
			popups.clear();
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(HEAL_FONT);

		long now = System.currentTimeMillis();
		for (Iterator<HealPopup> it = popups.iterator(); it.hasNext(); )
		{
			HealPopup popup = it.next();
			long elapsed = now - popup.startMs;
			if (elapsed >= DURATION_MS)
			{
				it.remove();
				continue;
			}

			// Raise above the head + skull/overhead-prayer icons so they don't cover the number.
			Point base = popup.actor.getCanvasTextLocation(graphics, popup.text, popup.actor.getLogicalHeight() + 50);
			if (base == null)
			{
				continue;
			}

			float progress = (float) elapsed / DURATION_MS;
			int alpha = (int) ((1f - progress) * 255);
			int y = base.getY() - (int) (progress * RISE_PX);

			graphics.setColor(new Color(0, 0, 0, Math.min(alpha, 200)));
			graphics.drawString(popup.text, base.getX() + 1, y + 1);
			graphics.setColor(new Color(HEAL_COLOR.getRed(), HEAL_COLOR.getGreen(), HEAL_COLOR.getBlue(), alpha));
			graphics.drawString(popup.text, base.getX(), y);
		}

		return null;
	}

	private static final class HealPopup
	{
		private final Actor actor;
		private final String text;
		private final long startMs;

		HealPopup(Actor actor, String text, long startMs)
		{
			this.actor = actor;
			this.text = text;
			this.startMs = startMs;
		}
	}
}
