package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.service.PidGuessService.Pid;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Experimental PID-guess indicator for a 1v1 involving the local player: a small star with a tiny
 * "pid" label, drawn over the head of whoever the noisy contested-hitsplat vote currently favours
 * (green = you, red = them, grey = still computing). Flashes yellow on a likely PID swap.
 *
 * <p>Drawn high above the head (above the overhead-overlay group and the resized PK skull). The
 * plugin pushes the holder actor + guess each tick via {@link #setState}. See the spike doc for why
 * this is best-effort only.
 */
public class PidIndicatorOverlay extends Overlay
{
	private static final Color YOU = new Color(0x6B, 0xCB, 0x77);
	private static final Color THEM = new Color(0xFF, 0x6B, 0x6B);
	private static final Color UNKNOWN = new Color(0xBD, 0xBD, 0xBD);
	private static final Color SWAP = new Color(0xFF, 0xD9, 0x3D);
	private static final Font PID_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);
	/** World-Z height above the model top — clears the resized skull / overhead group. */
	private static final int Z_ABOVE_HEAD = 80;
	private static final int STAR_R = 8;

	private final Client client;
	private final PvpEnhancerConfig config;

	private volatile Actor holder;
	private volatile Pid guess = Pid.UNKNOWN;
	private volatile boolean swapWarning;

	@Inject
	PidIndicatorOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		// UNDER_WIDGETS: over the native overheads but under the game UI (so it doesn't paint over a bank).
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	/**
	 * @param holder      the actor who currently has the guessed PID (drawn over), or null to hide
	 * @param guess       which side leads the vote (colours the star)
	 * @param swapWarning flash the star (the lead just flipped)
	 */
	public void setState(Actor holder, Pid guess, boolean swapWarning)
	{
		this.holder = holder;
		this.guess = guess == null ? Pid.UNKNOWN : guess;
		this.swapWarning = swapWarning;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Actor a = holder;
		if (!config.pidIndicator() || a == null)
		{
			return null;
		}
		Point p = a.getCanvasTextLocation(graphics, "pid", a.getLogicalHeight() + Z_ABOVE_HEAD);
		if (p == null)
		{
			return null;
		}

		boolean flash = swapWarning && (System.currentTimeMillis() / 300 % 2 == 0);
		Color color = flash ? SWAP
			: guess == Pid.LOCAL ? YOU
			: guess == Pid.OPPONENT ? THEM
			: UNKNOWN;

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int cx = p.getX();
		int starCy = p.getY() - STAR_R - 2;
		Polygon star = star(cx, starCy, STAR_R, STAR_R / 2);
		graphics.setColor(new Color(0, 0, 0, 160));
		graphics.setStroke(new java.awt.BasicStroke(2f));
		graphics.draw(star);
		graphics.setColor(color);
		graphics.fill(star);

		// Tiny "pid" label centred under the star.
		graphics.setFont(PID_FONT);
		String label = "pid";
		int tw = graphics.getFontMetrics().stringWidth(label);
		int tx = cx - tw / 2;
		int ty = p.getY() + 8;
		graphics.setColor(Color.BLACK);
		graphics.drawString(label, tx + 1, ty + 1);
		graphics.setColor(color);
		graphics.drawString(label, tx, ty);
		return null;
	}

	/** Builds a 5-point star polygon centred at (cx,cy). */
	private static Polygon star(int cx, int cy, int outer, int inner)
	{
		Polygon poly = new Polygon();
		for (int i = 0; i < 10; i++)
		{
			double r = (i % 2 == 0) ? outer : inner;
			double ang = Math.PI / 2 + i * Math.PI / 5; // point up
			poly.addPoint((int) Math.round(cx + r * Math.cos(ang)), (int) Math.round(cy - r * Math.sin(ang)));
		}
		return poly;
	}
}
