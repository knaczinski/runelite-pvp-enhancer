package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Flashes the opponent's outline red↔yellow when the local player is in combat but has
 * stopped attacking it (disengaged for &gt;= 2 ticks — walked away, looted, clicked elsewhere).
 *
 * <p>The plugin decides who the opponent is and whether the warning is active, then pushes the
 * actor via {@link #setOpponent}; a null opponent means "no warning". The flash alternates each
 * game tick so it reads as a blink without a separate animation timer.
 */
public class NotRetaliatingOverlay extends Overlay
{
	private static final Color FLASH_A = new Color(0xFF, 0x3B, 0x3B);       // red
	private static final Color FLASH_B = new Color(0xFF, 0xD9, 0x3D);       // yellow
	private static final Color FILL_A = new Color(0xFF, 0x3B, 0x3B, 60);
	private static final Color FILL_B = new Color(0xFF, 0xD9, 0x3D, 60);
	private static final java.awt.BasicStroke STROKE = new java.awt.BasicStroke(2f);

	private final Client client;
	private final PvpEnhancerConfig config;

	/** Opponent to flash; null = no warning. Set by the plugin each tick. */
	private volatile Actor opponent;

	@Inject
	NotRetaliatingOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	/** @param opponent the actor to flash, or null to clear the warning. */
	public void setOpponent(Actor opponent)
	{
		this.opponent = opponent;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Actor target = opponent;
		if (!config.showNotRetaliating() || target == null)
		{
			return null;
		}

		Shape hull = target.getConvexHull();
		if (hull == null)
		{
			return null;
		}

		boolean phase = (client.getTickCount() & 1) == 0;
		graphics.setColor(phase ? FILL_A : FILL_B);
		graphics.fill(hull);
		graphics.setColor(phase ? FLASH_A : FLASH_B);
		graphics.setStroke(STROKE);
		graphics.draw(hull);
		return null;
	}
}
