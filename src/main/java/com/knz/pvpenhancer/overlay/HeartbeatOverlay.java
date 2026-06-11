package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.service.CombatStateService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Tick-synced "blood" vignette on the viewport edges while the local player is in combat.
 *
 * <p>Pulses once per game tick: peaks at the {@code GameTick} event, then decays linearly
 * over the 600 ms tick window. Rendered as gradient strips from all four viewport edges
 * inward, giving a screen-bleed effect. The plugin calls {@link #recordTick()} on each
 * {@code GameTick} to update the pulse timestamp.
 */
public class HeartbeatOverlay extends Overlay
{
	private static final Color EDGE_COLOR = new Color(180, 0, 0);
	/** One server tick in milliseconds. */
	private static final float TICK_MS = 600f;

	private final Client client;
	private final PvpEnhancerConfig config;
	private final CombatStateService combatState;

	private long lastTickMs = 0L;

	@Inject
	HeartbeatOverlay(Client client, PvpEnhancerConfig config, CombatStateService combatState)
	{
		this.client = client;
		this.config = config;
		this.combatState = combatState;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	/** Called by the plugin on each {@code GameTick} to synchronize the pulse. */
	public void recordTick()
	{
		lastTickMs = System.currentTimeMillis();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showHeartbeat())
		{
			return null;
		}
		if (!combatState.isInCombat(client.getTickCount()))
		{
			return null;
		}
		if (lastTickMs == 0L)
		{
			return null;
		}

		long elapsed = System.currentTimeMillis() - lastTickMs;
		float phase = Math.min(1f, elapsed / TICK_MS);
		float maxAlpha = clamp(config.heartbeatIntensity(), 5, 100) / 100f;
		// Exponential decay: strong at tick start, fades quickly
		float alpha = (float) Math.pow(1.0 - phase, 1.5) * maxAlpha;
		if (alpha <= 0.01f)
		{
			return null;
		}

		int xOff = client.getViewportXOffset();
		int yOff = client.getViewportYOffset();
		int w = client.getViewportWidth();
		int h = client.getViewportHeight();
		int depth = Math.min(clamp(config.heartbeatDepth(), 10, 250), Math.min(w, h) / 3);

		Color peak = new Color(EDGE_COLOR.getRed(), EDGE_COLOR.getGreen(), EDGE_COLOR.getBlue(),
			(int) (alpha * 255));
		Color fade = new Color(EDGE_COLOR.getRed(), EDGE_COLOR.getGreen(), EDGE_COLOR.getBlue(), 0);

		// Top edge
		graphics.setPaint(new GradientPaint(xOff, yOff, peak, xOff, yOff + depth, fade));
		graphics.fillRect(xOff, yOff, w, depth);

		// Bottom edge
		graphics.setPaint(new GradientPaint(xOff, yOff + h, peak, xOff, yOff + h - depth, fade));
		graphics.fillRect(xOff, yOff + h - depth, w, depth);

		// Left edge
		graphics.setPaint(new GradientPaint(xOff, yOff, peak, xOff + depth, yOff, fade));
		graphics.fillRect(xOff, yOff, depth, h);

		// Right edge
		graphics.setPaint(new GradientPaint(xOff + w, yOff, peak, xOff + w - depth, yOff, fade));
		graphics.fillRect(xOff + w - depth, yOff, depth, h);

		return null;
	}

	private static int clamp(int v, int lo, int hi)
	{
		return Math.max(lo, Math.min(hi, v));
	}
}
