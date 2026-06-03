package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.service.CombatFocusService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Draws a thin outline around the entities that combat focus has hidden, so a hidden
 * player/NPC remains visible as a ghost contour rather than vanishing entirely.
 *
 * <p>This is the "highlighter" half of the EntityHider + Highlighter approach: the
 * {@code Hooks.RenderableDrawListener} (in the plugin) skips the native model draw, and
 * {@link ModelOutlineRenderer} — which reads the model geometry straight from memory regardless
 * of whether it was drawn this frame — paints just the edges in a 2D overlay pass on top.
 */
public class FocusOutlineOverlay extends Overlay
{
	private static final Color OUTLINE_COLOR = new Color(0xC8, 0xC8, 0xC8, 200);
	private static final int OUTLINE_WIDTH = 2;
	private static final int OUTLINE_FEATHER = 4;

	private final Client client;
	private final CombatFocusService combatFocus;
	private final ModelOutlineRenderer outlineRenderer;

	@Inject
	FocusOutlineOverlay(Client client, CombatFocusService combatFocus, ModelOutlineRenderer outlineRenderer)
	{
		this.client = client;
		this.combatFocus = combatFocus;
		this.outlineRenderer = outlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setMovable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!combatFocus.isActive())
		{
			return null;
		}

		for (Player player : client.getTopLevelWorldView().players())
		{
			if (player != null && combatFocus.isHidden(player))
			{
				outlineRenderer.drawOutline(player, OUTLINE_WIDTH, OUTLINE_COLOR, OUTLINE_FEATHER);
			}
		}
		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (npc != null && combatFocus.isHidden(npc))
			{
				outlineRenderer.drawOutline(npc, OUTLINE_WIDTH, OUTLINE_COLOR, OUTLINE_FEATHER);
			}
		}
		return null;
	}
}
