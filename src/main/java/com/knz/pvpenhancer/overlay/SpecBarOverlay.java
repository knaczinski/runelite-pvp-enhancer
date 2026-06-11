package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * A large, always-readable special-attack bar drawn as a movable overlay (drag to position). Reads
 * the real spec energy ({@code VarPlayer.SPECIAL_ATTACK_PERCENT}, 0–1000 = 0–100%) and fills green,
 * turning brighter at 100%. Replaces the fragile native combat-tab bar resize — it doesn't fight the
 * client's layout, so it never breaks. Sizing comes from config.
 */
public class SpecBarOverlay extends Overlay
{
	private static final Color BG = new Color(0x20, 0x20, 0x20, 220);
	private static final Color BORDER = new Color(0x00, 0x00, 0x00, 220);
	private static final Color FILL = new Color(0x3A, 0xA0, 0x3A);
	private static final Color FILL_FULL = new Color(0x5A, 0xE0, 0x5A);
	private static final Color TEXT = Color.WHITE;
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

	private final Client client;
	private final PvpEnhancerConfig config;

	@Inject
	SpecBarOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(true);     // user drags it where they want; RuneLite persists the location
		setResizable(false);
		setSnappable(true);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.specBar())
		{
			return null;
		}

		int w = clamp(config.specBarWidth(), 60, 400);
		int h = clamp(config.specBarHeight(), 10, 60);

		// 0–1000 → 0..1.
		int raw = client.getVarpValue(VarPlayerID.SA_ENERGY);
		float pct = Math.max(0f, Math.min(1f, raw / 1000f));

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Track.
		graphics.setColor(BG);
		graphics.fillRect(0, 0, w, h);
		// Fill.
		int fillW = Math.round(pct * (w - 2));
		if (fillW > 0)
		{
			graphics.setColor(pct >= 1f ? FILL_FULL : FILL);
			graphics.fillRect(1, 1, fillW, h - 2);
		}
		// Border.
		graphics.setColor(BORDER);
		graphics.drawRect(0, 0, w - 1, h - 1);

		// Centered percentage label.
		graphics.setFont(FONT);
		String label = Math.round(pct * 100) + "%";
		int tw = graphics.getFontMetrics().stringWidth(label);
		int th = graphics.getFontMetrics().getAscent();
		int tx = (w - tw) / 2;
		int ty = (h + th) / 2 - 2;
		graphics.setColor(Color.BLACK);
		graphics.drawString(label, tx + 1, ty + 1);
		graphics.setColor(TEXT);
		graphics.drawString(label, tx, ty);

		return new Dimension(w, h);
	}

	private static int clamp(int v, int lo, int hi)
	{
		return Math.max(lo, Math.min(hi, v));
	}
}
