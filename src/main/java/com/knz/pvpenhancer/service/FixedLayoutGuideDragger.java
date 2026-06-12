package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.overlay.FixedLayoutGuideOverlay;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.MouseAdapter;

/**
 * Lets the user drag the fixed-layout guide (the {@link FixedLayoutGuideOverlay} client outline) to
 * set the pin offset, replacing the Nudge sliders. Dragging requires <b>Alt held</b> so it never
 * hijacks normal clicks inside the 3D scene the guide overlaps. The drag delta is written to
 * {@code frNudgeX/Y}; the live pin and the guide both read those, so they follow the cursor.
 *
 * <p>Registered with the {@code MouseManager} by the plugin. Hit-testing uses the bounds the overlay
 * publishes each render (canvas coords), so no client-thread access is needed here.
 */
@Singleton
public class FixedLayoutGuideDragger extends MouseAdapter
{
	private static final String GROUP = "pvpenhancer";
	private static final int NUDGE_LIMIT = 400;

	private final Client client;
	private final PvpEnhancerConfig config;
	private final ConfigManager configManager;
	private final FixedLayoutGuideOverlay guide;

	private boolean dragging;
	private int startX;
	private int startY;
	private int baseNudgeX;
	private int baseNudgeY;

	@Inject
	FixedLayoutGuideDragger(Client client, PvpEnhancerConfig config, ConfigManager configManager,
		FixedLayoutGuideOverlay guide)
	{
		this.client = client;
		this.config = config;
		this.configManager = configManager;
		this.guide = guide;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.BUTTON1 || !canDrag())
		{
			return e;
		}
		Rectangle handle = guide.getDragBounds();
		if (handle == null || !handle.contains(e.getX(), e.getY()))
		{
			return e;
		}
		dragging = true;
		startX = e.getX();
		startY = e.getY();
		baseNudgeX = config.frNudgeX();
		baseNudgeY = config.frNudgeY();
		e.consume();
		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e)
	{
		if (!dragging)
		{
			return e;
		}
		int nx = clamp(baseNudgeX + (e.getX() - startX));
		int ny = clamp(baseNudgeY + (e.getY() - startY));
		configManager.setConfiguration(GROUP, "frNudgeX", nx);
		configManager.setConfiguration(GROUP, "frNudgeY", ny);
		e.consume();
		return e;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e)
	{
		if (dragging)
		{
			dragging = false;
			e.consume();
		}
		return e;
	}

	private boolean canDrag()
	{
		return config.fixedResizableLayout() && config.frShowGuide()
			&& client.isKeyPressed(KeyCode.KC_ALT);
	}

	private static int clamp(int v)
	{
		return Math.max(-NUDGE_LIMIT, Math.min(NUDGE_LIMIT, v));
	}
}
