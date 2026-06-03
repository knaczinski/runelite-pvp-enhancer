package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.AttackStyle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

/**
 * Highlights the protection prayer that counters the current target's equipped-weapon style
 * (predictive). The plugin computes the target's style each tick from {@code WeaponStyleMap}
 * and pushes it via {@link #setTargetStyle}; this overlay boxes the matching prayer widget.
 *
 * <p>The prayer button is located by scanning the prayer book's 30 contiguous child widgets
 * ({@code InterfaceID.Prayerbook.PRAYER1..PRAYER30}) and matching on the widget name, so it is
 * robust against the prayer-book child indices being reordered.
 */
public class PrayerHighlightOverlay extends Overlay
{
	private static final int PRAYER_SLOTS = 30;
	private static final Color BOX_COLOR = new Color(0xFF, 0xD7, 0x00);
	private static final Color FILL_COLOR = new Color(0xFF, 0xD7, 0x00, 50);

	private final Client client;
	private final PvpEnhancerConfig config;

	/** Style of the current target's weapon; the prayer to highlight counters this. */
	private volatile AttackStyle targetStyle = AttackStyle.UNKNOWN;

	@Inject
	PrayerHighlightOverlay(Client client, PvpEnhancerConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(false);
	}

	public void setTargetStyle(AttackStyle style)
	{
		this.targetStyle = style == null ? AttackStyle.UNKNOWN : style;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.prayerHighlight())
		{
			return null;
		}

		String wanted = protectionPrayerName(targetStyle);
		if (wanted == null)
		{
			return null;
		}

		Widget prayer = findPrayerByName(wanted);
		if (prayer == null)
		{
			return null;
		}

		Rectangle bounds = prayer.getBounds();
		if (bounds == null)
		{
			return null;
		}

		graphics.setColor(FILL_COLOR);
		graphics.fill(bounds);
		graphics.setColor(BOX_COLOR);
		graphics.setStroke(new BasicStroke(2f));
		graphics.draw(bounds);
		return null;
	}

	private Widget findPrayerByName(String wanted)
	{
		for (int i = 0; i < PRAYER_SLOTS; i++)
		{
			Widget w = client.getWidget(InterfaceID.Prayerbook.PRAYER1 + i);
			if (w == null || w.isHidden())
			{
				continue;
			}
			String name = w.getName();
			if (name != null && Text.removeTags(name).equalsIgnoreCase(wanted))
			{
				return w;
			}
		}
		return null;
	}

	/** @return the protection-prayer name countering {@code style}, or null if unknown. */
	private static String protectionPrayerName(AttackStyle style)
	{
		switch (style)
		{
			case MELEE:
				return "Protect from Melee";
			case RANGED:
				return "Protect from Missiles";
			case MAGIC:
				return "Protect from Magic";
			default:
				return null;
		}
	}
}
