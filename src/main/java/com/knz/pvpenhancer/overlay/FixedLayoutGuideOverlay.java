package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.util.FixedLayoutGeometry;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Visual-only reference for the fixed-layout-in-resizable feature (B030). Draws the fixed-mode
 * client footprint and scene, plus the inventory / minimap / chat boxes at the exact positions the
 * pin would move them to (viewport centre + each block's fixed offset + Nudge). Lets the user gauge
 * the layout and where clicks would land, without committing to actually moving the live widgets.
 *
 * <p>Anchoring matches {@code applyFixedResizableLayout}: blocks are placed relative to the
 * viewport centre and clamped on-screen, so the guide reflects the real pinned result.
 */
public class FixedLayoutGuideOverlay extends Overlay
{
	private static final Color CLIENT_LINE = new Color(0xFF, 0xFF, 0xFF, 80);
	private static final Color SCENE_LINE = new Color(0x4D, 0x96, 0xFF, 180);
	private static final Color INV_LINE = new Color(0x6B, 0xCB, 0x77, 200);
	private static final Color MM_LINE = new Color(0xFF, 0xD9, 0x3D, 200);
	private static final Color CHAT_LINE = new Color(0xFF, 0x6B, 0x6B, 200);
	private static final Color LABEL = new Color(0xFF, 0xFF, 0xFF, 220);

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
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.fixedResizableLayout() || !config.frShowGuide())
		{
			return null;
		}

		int cx = client.getViewportXOffset() + client.getViewportWidth() / 2 + config.frNudgeX();
		int cy = client.getViewportYOffset() + client.getViewportHeight() / 2 + config.frNudgeY();

		// Fixed scene (3D viewport) centred on the resizable viewport centre.
		int sceneX = cx - FixedLayoutGeometry.SCENE_HALF_X;
		int sceneY = cy - FixedLayoutGeometry.SCENE_HALF_Y;

		// Whole fixed client footprint (scene sits SCENE_INSET inside it).
		graphics.setStroke(DASHED);
		graphics.setColor(CLIENT_LINE);
		graphics.drawRect(sceneX - FixedLayoutGeometry.SCENE_INSET, sceneY - FixedLayoutGeometry.SCENE_INSET,
			FixedLayoutGeometry.CLIENT_W, FixedLayoutGeometry.CLIENT_H);

		graphics.setStroke(SOLID);
		graphics.setColor(SCENE_LINE);
		graphics.drawRect(sceneX, sceneY, FixedLayoutGeometry.SCENE_W, FixedLayoutGeometry.SCENE_H);
		label(graphics, "scene", sceneX + 3, sceneY + 12);

		// Block boxes — only those that are (or would be) pinned.
		if (config.frInventory())
		{
			box(graphics, INV_LINE, "inventory", cx + FixedLayoutGeometry.INV_OFF_X,
				cy + FixedLayoutGeometry.INV_OFF_Y, FixedLayoutGeometry.INV_W, FixedLayoutGeometry.INV_H);
		}
		if (config.frMinimap())
		{
			box(graphics, MM_LINE, "minimap", cx + FixedLayoutGeometry.MM_OFF_X,
				cy + FixedLayoutGeometry.MM_OFF_Y, FixedLayoutGeometry.MM_W, FixedLayoutGeometry.MM_H);
		}
		if (config.frChat())
		{
			box(graphics, CHAT_LINE, "chat", cx + FixedLayoutGeometry.CHAT_OFF_X,
				cy + FixedLayoutGeometry.CHAT_OFF_Y, FixedLayoutGeometry.CHAT_W, FixedLayoutGeometry.CHAT_H);
		}
		return null;
	}

	private void box(Graphics2D g, Color color, String text, int x, int y, int w, int h)
	{
		// Same clamp as the live pin so the guide matches the moved widgets.
		int cw = client.getCanvasWidth();
		int ch = client.getCanvasHeight();
		x = Math.max(0, Math.min(x, cw - w));
		y = Math.max(0, Math.min(y, ch - h));
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
