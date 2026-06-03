package com.knz.pvpenhancer.service;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.api.Renderable;

/**
 * Decides, per rendered entity, whether it should be "ghostified" — its native model skipped so
 * only an outline (drawn separately by {@code GhostifyOutlineOverlay}) remains.
 *
 * <p>The plugin recomputes, each game tick, the set of players to ghost and their per-category
 * outline colour, and publishes it here as an immutable snapshot (volatile). A
 * {@code Hooks.RenderableDrawListener} delegates to {@link #shouldDraw} on the render thread;
 * the outline overlay reads {@link #getGhosted}. Only players are affected.
 */
@Singleton
public class GhostifyService
{
	private volatile Map<Player, Color> ghosted = Collections.emptyMap();

	/** Publishes the player→outline-colour snapshot for this tick (pass an immutable-after-publish map). */
	public void update(Map<Player, Color> ghosted)
	{
		this.ghosted = ghosted;
	}

	/** @return false to hide (ghost) the renderable: a player in the current ghost set. */
	public boolean shouldDraw(Renderable renderable, boolean drawingUi)
	{
		return !(renderable instanceof Player) || !ghosted.containsKey(renderable);
	}

	/** @return player→outline-colour for everyone currently ghosted (read on the render thread). */
	public Map<Player, Color> getGhosted()
	{
		return ghosted;
	}

	public boolean isActive()
	{
		return !ghosted.isEmpty();
	}

	public void clear()
	{
		ghosted = Collections.emptyMap();
	}
}
