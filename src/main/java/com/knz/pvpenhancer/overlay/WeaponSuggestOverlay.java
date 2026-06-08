package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.WeaponStyleMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Boxes inventory slots holding a weapon of a requested {@link AttackStyle} — the "weapon from
 * prayer" half of the offensive-prayer highlighter (your active offensive prayer → suggested
 * weapon). The plugin pushes the desired style via {@link #setStyle}; weapons are recognised via
 * {@code WeaponStyleMap}. All matching weapons are boxed.
 */
public class WeaponSuggestOverlay extends Overlay
{
	private static final Color BOX_COLOR = new Color(0x66, 0xCC, 0xFF);
	private static final Color FILL_COLOR = new Color(0x66, 0xCC, 0xFF, 60);

	private final Client client;

	private volatile AttackStyle style = AttackStyle.UNKNOWN;

	@Inject
	WeaponSuggestOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(false);
	}

	/** Sets the weapon style to highlight in the inventory ({@code UNKNOWN} = nothing). */
	public void setStyle(AttackStyle style)
	{
		this.style = style == null ? AttackStyle.UNKNOWN : style;
	}

	@Override
	@SuppressWarnings("deprecation") // ComponentID.INVENTORY_CONTAINER
	public Dimension render(Graphics2D graphics)
	{
		AttackStyle want = style;
		if (want == AttackStyle.UNKNOWN)
		{
			return null;
		}
		Widget inv = client.getWidget(ComponentID.INVENTORY_CONTAINER);
		if (inv == null || inv.isHidden())
		{
			return null;
		}
		Widget[] items = inv.getDynamicChildren();
		if (items == null)
		{
			return null;
		}
		graphics.setStroke(new BasicStroke(2f));
		for (Widget item : items)
		{
			if (item == null || item.getItemId() <= 0)
			{
				continue;
			}
			if (WeaponStyleMap.styleOf(item.getItemId()) == want)
			{
				Rectangle b = item.getBounds();
				if (b != null)
				{
					graphics.setColor(FILL_COLOR);
					graphics.fill(b);
					graphics.setColor(BOX_COLOR);
					graphics.draw(b);
				}
			}
		}
		return null;
	}
}
