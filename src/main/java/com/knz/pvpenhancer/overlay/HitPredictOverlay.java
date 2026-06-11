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
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Floating predicted-damage numbers near the target the local player is hitting.
 *
 * <p>Driven by the Hitpoints XP drop (see {@code XpDamage}): the number appears as the XP is
 * awarded — for ranged/magic, before the projectile lands — so the player can react (e.g.
 * swap to a spec weapon to stack a second hit). Orange to distinguish from green heal numbers.
 */
@Singleton
public class HitPredictOverlay extends Overlay
{
	private static final long DURATION_MS = 1200L;
	private static final int RISE_PX = 24;
	private static final Color PREDICT_COLOR = new Color(0xFF, 0xB0, 0x20);
	private static final Color CRITICAL_COLOR = new Color(0xFF, 0x33, 0x22);
	private static final float CRITICAL_SCALE = 1.7f;
	private static final Font PREDICT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 15);

	private final Client client;
	private final PvpEnhancerConfig config;
	private final List<Prediction> predictions = new ArrayList<>();

	@Inject
	HitPredictOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		// UNDER_WIDGETS: over the native overhead bars/icons but UNDER the game UI (bank, etc.).
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	/**
	 * Queues a predicted-damage popup over the target.
	 *
	 * @param critical render bigger + red (the opponent will be at/under the spec-combo HP cue)
	 */
	public void addPrediction(Actor target, int damage, boolean critical)
	{
		if (target == null || damage <= 0)
		{
			return;
		}
		predictions.add(new Prediction(target, Integer.toString(damage), System.currentTimeMillis(), critical));
	}

	public void clear()
	{
		predictions.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.hitPrediction())
		{
			predictions.clear();
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		long now = System.currentTimeMillis();
		float baseSize = config.hitPredictionSize();
		for (Iterator<Prediction> it = predictions.iterator(); it.hasNext(); )
		{
			Prediction p = it.next();
			long elapsed = now - p.startMs;
			if (elapsed >= DURATION_MS)
			{
				it.remove();
				continue;
			}

			graphics.setFont(PREDICT_FONT.deriveFont(p.critical ? baseSize * CRITICAL_SCALE : baseSize));
			Color colour = p.critical ? CRITICAL_COLOR : PREDICT_COLOR;

			// Anchor over the opponent (default) or over your own character, by the health bar where
			// heal numbers appear — your outgoing damage shown on yourself, like a damage log.
			boolean overSelf = config.hitPredictAnchor() == com.knz.pvpenhancer.HitPredictAnchor.SELF;
			Actor anchor = overSelf ? client.getLocalPlayer() : p.target;
			if (anchor == null)
			{
				continue;
			}
			int zOffset = overSelf ? anchor.getLogicalHeight() : anchor.getLogicalHeight() + 20;
			Point base = anchor.getCanvasTextLocation(graphics, p.text, zOffset);
			if (base == null)
			{
				continue;
			}

			float progress = (float) elapsed / DURATION_MS;
			int alpha = (int) ((1f - progress) * 255);
			int x = base.getX() + (overSelf ? 20 : 0); // mirror the heal overlay's right offset
			int y = base.getY() - (int) (progress * RISE_PX);

			graphics.setColor(new Color(0, 0, 0, Math.min(alpha, 200)));
			graphics.drawString(p.text, x + 1, y + 1);
			graphics.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha));
			graphics.drawString(p.text, x, y);
		}

		return null;
	}

	private static final class Prediction
	{
		private final Actor target;
		private final String text;
		private final long startMs;
		private final boolean critical;

		Prediction(Actor target, String text, long startMs, boolean critical)
		{
			this.target = target;
			this.text = text;
			this.startMs = startMs;
			this.critical = critical;
		}
	}
}
