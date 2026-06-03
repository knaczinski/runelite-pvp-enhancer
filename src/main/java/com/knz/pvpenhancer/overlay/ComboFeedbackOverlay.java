package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.ComboResult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Transient floating popup that appears in the center of the viewport when a combo is
 * detected. Fades out over {@value #FADE_MS} ms. The color is tier-driven.
 *
 * <p>Shows the most recent combo; a new result replaces any currently fading one.
 * Call {@link #showCombo(ComboResult)} from the plugin when a combo fires.
 */
@Singleton
public class ComboFeedbackOverlay extends Overlay
{
	private static final long FADE_MS = 1500L;
	private static final float FONT_SIZE = 22f;
	private static final int SHADOW_OFFSET = 2;

	private final Client client;
	private final PvpEnhancerConfig config;

	private ComboResult current = null;
	private long shownAtMs = 0L;

	@Inject
	ComboFeedbackOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	/**
	 * Shows a combo popup. Replaces the current popup if one is still visible.
	 */
	public void showCombo(ComboResult result)
	{
		this.current = result;
		this.shownAtMs = System.currentTimeMillis();
	}

	/** Removes the current combo popup immediately. */
	public void clear()
	{
		this.current = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showCombos() || current == null)
		{
			return null;
		}

		long elapsed = System.currentTimeMillis() - shownAtMs;
		if (elapsed >= FADE_MS)
		{
			current = null;
			return null;
		}

		float alpha = Math.max(0f, 1f - (float) elapsed / FADE_MS);
		// Easing: hold near 1.0 for the first half, then fade
		alpha = alpha < 0.5f ? alpha * 2f : 1f;
		alpha = Math.min(1f, alpha);

		String text = current.getLabel();
		Color base = current.getTier().getColor();

		int xOff = client.getViewportXOffset();
		int yOff = client.getViewportYOffset();
		int vw = client.getViewportWidth();
		int vh = client.getViewportHeight();

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, FONT_SIZE));

		FontMetrics fm = graphics.getFontMetrics();
		int textW = fm.stringWidth(text);
		int textH = fm.getAscent();

		int x = xOff + (vw - textW) / 2;
		int y = yOff + vh / 3; // upper third of viewport

		int alphaInt = (int) (alpha * 255);

		// Shadow
		graphics.setColor(new Color(0, 0, 0, Math.min(alphaInt, 200)));
		graphics.drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);

		// Main text
		graphics.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alphaInt));
		graphics.drawString(text, x, y);

		return null;
	}
}
