package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

/**
 * Re-renders the PK skull at a configurable size. The native skull size is not exposed by the
 * API, so the plugin hides the native skull (setSkullIcon(-1)) on in-scope players and feeds
 * them here; this overlay draws a scaled skull sprite above their head. Only the regular skull
 * is handled (other skull variants are left native).
 */
@Singleton
public class SkullResizeOverlay extends Overlay
{
	/** Native on-screen size of the regular skull (the wiki sprite is 25×25), so 100% ≈ native. */
	private static final int BASE_SIZE = 25;

	private final PvpEnhancerConfig config;
	private final BufferedImage skull;
	private final List<Player> targets = new ArrayList<>();

	@Inject
	SkullResizeOverlay(PvpEnhancerConfig config)
	{
		this.config = config;
		this.skull = ImageUtil.loadImageResource(getClass(), "/com/knz/pvpenhancer/icons/Skull.png");
		setPosition(OverlayPosition.DYNAMIC);
		// ABOVE_WIDGETS so the redrawn skull paints OVER the native health bar / overhead-prayer
		// icons (which the client draws after ABOVE_SCENE overlays, hiding the skull otherwise).
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(false);
	}

	/** Sets the players whose (hidden) native skull should be re-drawn scaled. Client thread. */
	public void setTargets(Collection<Player> players)
	{
		targets.clear();
		targets.addAll(players);
	}

	public void clear()
	{
		targets.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (skull == null || targets.isEmpty())
		{
			return null;
		}
		int size = Math.max(6, Math.round(BASE_SIZE * config.skullSize() / 100f));
		for (Player p : targets)
		{
			if (p == null)
			{
				continue;
			}
			// Centre horizontally over the head. When the HP bar or an overhead-prayer icon is
			// showing, sit ABOVE them so they don't cover the skull; otherwise sit just over the head.
			boolean hasBarOrPrayer = p.getHealthRatio() >= 0 || p.getOverheadIcon() != null;
			int zOffset = p.getLogicalHeight() + (hasBarOrPrayer ? 100 : 25);
			Point anchor = p.getCanvasTextLocation(graphics, "", zOffset);
			if (anchor == null)
			{
				continue;
			}
			graphics.drawImage(skull, anchor.getX() - size / 2, anchor.getY() - size, size, size, null);
		}
		return null;
	}
}
