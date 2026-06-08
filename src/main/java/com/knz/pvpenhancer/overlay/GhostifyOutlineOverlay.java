package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.service.GhostifyService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Draws the outline ("highlighter" half of ghostify) around each ghosted player, in that
 * player's category colour. The plugin's {@code Hooks.RenderableDrawListener} skips the native
 * model; {@link ModelOutlineRenderer} reads the geometry from memory regardless, so the player
 * shows only as a coloured contour.
 */
public class GhostifyOutlineOverlay extends Overlay
{
	private static final int OUTLINE_WIDTH = 2;
	private static final int OUTLINE_FEATHER = 4;

	private final GhostifyService ghostify;
	private final ModelOutlineRenderer outlineRenderer;

	@Inject
	GhostifyOutlineOverlay(GhostifyService ghostify, ModelOutlineRenderer outlineRenderer)
	{
		this.ghostify = ghostify;
		this.outlineRenderer = outlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		// UNDER_WIDGETS: outlines over native overheads, under the game UI.
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setMovable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<Player, Color> ghosted = ghostify.getGhosted();
		for (Map.Entry<Player, Color> e : ghosted.entrySet())
		{
			if (e.getKey() != null)
			{
				outlineRenderer.drawOutline(e.getKey(), OUTLINE_WIDTH, e.getValue(), OUTLINE_FEATHER);
			}
		}
		return null;
	}
}
