package com.knz.pvpenhancer.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

/**
 * Boxes prayers in the prayer tab that the plugin asks for — the defensive protection prayer
 * (countering the target's weapon) and/or the offensive prayer (matching your weapon). When any
 * is active it ALSO boxes the prayer-tab button, so you notice the cue even with the inventory
 * tab open (the prayer book hidden).
 *
 * <p>Prayers are located by NAME over {@code InterfaceID.Prayerbook.PRAYER1..PRAYER30}, robust to
 * the child indices being reordered. The plugin gates which prayers to show via config and feeds
 * the names through {@link #setPrayers}.
 */
public class PrayerHighlightOverlay extends Overlay
{
	private static final int PRAYER_SLOTS = 30;
	private static final Color BOX_COLOR = new Color(0xFF, 0xD7, 0x00);
	private static final Color FILL_COLOR = new Color(0xFF, 0xD7, 0x00, 50);

	private final Client client;

	/** Prayer names the plugin wants highlighted this tick (e.g. "Protect from Melee", "Rigour"). */
	private volatile Set<String> prayerNames = Collections.emptySet();

	@Inject
	PrayerHighlightOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(false);
	}

	/** Sets the prayer names to box this tick (empty = nothing). */
	public void setPrayers(Set<String> names)
	{
		this.prayerNames = names == null || names.isEmpty() ? Collections.emptySet() : new HashSet<>(names);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Set<String> wanted = prayerNames;
		if (wanted.isEmpty())
		{
			return null;
		}

		graphics.setStroke(new BasicStroke(2f));

		// Box each requested prayer (only when the prayer book is open / not hidden).
		for (int i = 0; i < PRAYER_SLOTS; i++)
		{
			Widget w = client.getWidget(InterfaceID.Prayerbook.PRAYER1 + i);
			if (w == null || w.isHidden())
			{
				continue;
			}
			String name = w.getName();
			if (name != null && wanted.contains(Text.removeTags(name)))
			{
				box(graphics, w.getBounds());
			}
		}

		// Flag the prayer-tab button so the cue is visible with the inventory tab open.
		box(graphics, prayerTabBounds());
		return null;
	}

	private static void box(Graphics2D graphics, Rectangle bounds)
	{
		if (bounds == null)
		{
			return;
		}
		graphics.setColor(FILL_COLOR);
		graphics.fill(bounds);
		graphics.setColor(BOX_COLOR);
		graphics.draw(bounds);
	}

	/** @return bounds of the visible prayer-tab toggle button, or null. */
	@SuppressWarnings("deprecation") // ComponentID carries the static tab-button ids
	private Rectangle prayerTabBounds()
	{
		int[] tabs = {
			ComponentID.FIXED_VIEWPORT_PRAYER_TAB,
			ComponentID.RESIZABLE_VIEWPORT_PRAYER_TAB,
			ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_PRAYER_TAB,
		};
		for (int id : tabs)
		{
			Widget w = client.getWidget(id);
			if (w != null && !w.isHidden())
			{
				return w.getBounds();
			}
		}
		return null;
	}
}
