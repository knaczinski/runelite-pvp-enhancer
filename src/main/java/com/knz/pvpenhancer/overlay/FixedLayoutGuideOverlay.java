package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.util.FixedLayoutGeometry;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * The fixed-layout guide (B030): a movable overlay outlining the fixed-mode client (scene +
 * inventory / minimap / chat boxes). Because it is a real RuneLite movable overlay, it gets the
 * standard Alt-drag affordance (yellow outline) for free; drag it to position the fixed layout. Its
 * on-screen top-left is the fixed-client origin — the plugin pins the live UI blocks to it via
 * {@link #clientTopLeft()}.
 *
 * <p>It always reports its size (so it stays positioned + draggable) while the feature is enabled,
 * but only draws the outlines when "Show guide" is on.
 */
public class FixedLayoutGuideOverlay extends Overlay
{
	private static final Color CLIENT_LINE = new Color(0xFF, 0xFF, 0xFF, 110);
	private static final Color SCENE_LINE = new Color(0x4D, 0x96, 0xFF, 190);
	private static final Color INV_LINE = new Color(0x6B, 0xCB, 0x77, 210);
	private static final Color MM_LINE = new Color(0xFF, 0xD9, 0x3D, 210);
	private static final Color CHAT_LINE = new Color(0xFF, 0x6B, 0x6B, 210);
	private static final Color LABEL = new Color(0xFF, 0xFF, 0xFF, 230);

	private static final Stroke SOLID = new BasicStroke(1.5f);
	private static final Stroke DASHED = new BasicStroke(1f, BasicStroke.CAP_BUTT,
		BasicStroke.JOIN_MITER, 4f, new float[]{4f, 4f}, 0f);

	private final Client client;
	private final PvpEnhancerConfig config;

	@Inject
	FixedLayoutGuideOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DETACHED);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(true);   // standard Alt-drag (yellow outline) handled by RuneLite
		setSnappable(false);
		setResizable(false);
	}

	/** @return the guide's current on-screen top-left = the fixed-client origin, or null if unplaced. */
	public Point clientTopLeft()
	{
		java.awt.Rectangle b = getBounds();
		return (b == null || b.width == 0) ? null : new Point(b.x, b.y);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.fixedResizableLayout())
		{
			return null;
		}

		// First placement: centre the fixed client on the canvas so it starts somewhere sensible.
		if (getPreferredLocation() == null)
		{
			int x = Math.max(0, (client.getCanvasWidth() - FixedLayoutGeometry.CLIENT_W) / 2);
			int y = Math.max(0, (client.getCanvasHeight() - FixedLayoutGeometry.CLIENT_H) / 2);
			setPreferredLocation(new Point(x, y));
		}

		// Only paint when the guide is shown; still report size so it stays positioned + draggable.
		if (config.frShowGuide())
		{
			drawGuide(graphics);
		}
		return new Dimension(FixedLayoutGeometry.CLIENT_W, FixedLayoutGeometry.CLIENT_H);
	}

	/** Draws everything relative to the overlay origin (0,0) = the fixed-client top-left. */
	private void drawGuide(Graphics2D graphics)
	{
		graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
			java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);

		// Client footprint (the drag handle).
		graphics.setStroke(DASHED);
		graphics.setColor(CLIENT_LINE);
		graphics.drawRect(0, 0, FixedLayoutGeometry.CLIENT_W - 1, FixedLayoutGeometry.CLIENT_H - 1);

		// Fixed scene.
		graphics.setStroke(SOLID);
		graphics.setColor(SCENE_LINE);
		graphics.drawRect(FixedLayoutGeometry.SCENE_INSET, FixedLayoutGeometry.SCENE_INSET,
			FixedLayoutGeometry.SCENE_W, FixedLayoutGeometry.SCENE_H);
		label(graphics, "scene", FixedLayoutGeometry.SCENE_INSET + 3, FixedLayoutGeometry.SCENE_INSET + 12);

		if (config.frInventory())
		{
			box(graphics, INV_LINE, "inventory", FixedLayoutGeometry.INV_FX, FixedLayoutGeometry.INV_FY,
				FixedLayoutGeometry.INV_W, FixedLayoutGeometry.INV_H);
		}
		if (config.frMinimap())
		{
			box(graphics, MM_LINE, "minimap", FixedLayoutGeometry.MM_FX, FixedLayoutGeometry.MM_FY,
				FixedLayoutGeometry.MM_W, FixedLayoutGeometry.MM_H);
		}
		if (config.frChat())
		{
			box(graphics, CHAT_LINE, "chat", FixedLayoutGeometry.CHAT_FX, FixedLayoutGeometry.CHAT_FY,
				FixedLayoutGeometry.CHAT_W, FixedLayoutGeometry.CHAT_H);
		}
	}

	private void box(Graphics2D g, Color color, String text, int x, int y, int w, int h)
	{
		g.setStroke(SOLID);
		g.setColor(color);
		g.drawRect(x, y, w, h);
		label(g, text, x + 3, y + 12);
	}

	private void label(Graphics2D g, String text, int x, int y)
	{
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + 1);
		g.setColor(LABEL);
		g.drawString(text, x, y);
	}
}
