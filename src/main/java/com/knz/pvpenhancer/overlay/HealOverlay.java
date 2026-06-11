package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.HealDisplayMode;
import com.knz.pvpenhancer.HealNumberStyle;
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
 * Renders floating "+N" healing numbers near the health bar of the actor who healed. Green
 * text that rises and fades over {@value #DURATION_MS} ms. Remote-player heals are estimates
 * (prefixed "~") because the API only exposes their health ratio, not real HP.
 *
 * <p>The plugin detects heals on each game tick and calls {@link #addHeal}. Both detection
 * and rendering run on the client thread, so the popup list needs no synchronization.
 */
@Singleton
public class HealOverlay extends Overlay
{
	private static final long DURATION_MS = 1500L;
	private static final int RISE_PX = 28;
	private static final int HBAR_RIGHT_OFFSET = 20; // px right of the health bar
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
		// UNDER_WIDGETS: over the native health bar / overhead icons but UNDER the game UI (so it
		// doesn't paint over an open bank/interface). ABOVE_SCENE drew under the natives.
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	/** Convenience: heal with no before/after context (breakdown falls back to amount-only). */
	public void addHeal(Actor actor, int amount, boolean estimate)
	{
		addHeal(actor, amount, estimate, -1, -1);
	}

	/**
	 * Queues a healing popup over the given actor.
	 *
	 * @param actor    the healed actor (its live position is used each frame)
	 * @param amount   HP healed
	 * @param estimate true if the amount is an estimate (remote player) — shown with "~"
	 * @param beforeHp HP before the heal, or -1 if unknown (disables breakdown)
	 * @param afterHp  HP after the heal, or -1 if unknown (disables breakdown)
	 */
	public void addHeal(Actor actor, int amount, boolean estimate, int beforeHp, int afterHp)
	{
		if (actor == null || amount <= 0)
		{
			return;
		}
		popups.add(new HealPopup(actor, amount, estimate, beforeHp, afterHp, System.currentTimeMillis()));
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
		graphics.setFont(HEAL_FONT.deriveFont((float) config.healSize()));

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

			// Anchor to the right of the health bar (clear of the skull / overhead icons).
			Point base = popup.actor.getCanvasTextLocation(graphics, "", popup.actor.getLogicalHeight());
			if (base == null)
			{
				continue;
			}

			float progress = (float) elapsed / DURATION_MS;
			int alpha = (int) ((1f - progress) * 255);
			int x = base.getX() + HBAR_RIGHT_OFFSET;
			int y = base.getY() - (int) (progress * RISE_PX);

			String text = popup.format(config.healNumberStyle());
			graphics.setColor(new Color(0, 0, 0, Math.min(alpha, 200)));
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(new Color(HEAL_COLOR.getRed(), HEAL_COLOR.getGreen(), HEAL_COLOR.getBlue(), alpha));
			graphics.drawString(text, x, y);
		}

		return null;
	}

	private static final class HealPopup
	{
		private final Actor actor;
		private final int amount;
		private final boolean estimate;
		private final int beforeHp;
		private final int afterHp;
		private final long startMs;

		HealPopup(Actor actor, int amount, boolean estimate, int beforeHp, int afterHp, long startMs)
		{
			this.actor = actor;
			this.amount = amount;
			this.estimate = estimate;
			this.beforeHp = beforeHp;
			this.afterHp = afterHp;
			this.startMs = startMs;
		}

		/** "+25" / "~25", or "65 + 25 = 90" / "~65 + 25 = ~90" when breakdown data is present. */
		String format(HealNumberStyle style)
		{
			if (style == HealNumberStyle.BREAKDOWN && beforeHp >= 0 && afterHp >= 0)
			{
				String pfx = estimate ? "~" : "";
				return pfx + beforeHp + " + " + amount + " = " + pfx + afterHp;
			}
			return (estimate ? "~" : "+") + amount;
		}
	}
}
