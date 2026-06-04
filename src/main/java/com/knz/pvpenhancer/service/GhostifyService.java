package com.knz.pvpenhancer.service;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	/** Ghosted players' names — the hider matches by name, not object identity. */
	private volatile Set<String> ghostedNames = Collections.emptySet();

	/** Publishes the player→outline-colour snapshot for this tick (pass an immutable-after-publish map). */
	public void update(Map<Player, Color> ghosted)
	{
		Set<String> names = new HashSet<>(ghosted.size());
		for (Player p : ghosted.keySet())
		{
			if (p != null && p.getName() != null)
			{
				names.add(p.getName());
			}
		}
		this.ghosted = ghosted;
		this.ghostedNames = names;
	}

	/**
	 * @return false to hide (ghost) the renderable. Matches by NAME, not object identity: when a
	 * player talks the engine re-draws them through a different {@code Player} instance, which an
	 * identity check would miss (the model would pop back in). The name is stable across instances.
	 */
	public boolean shouldDraw(Renderable renderable, boolean drawingUi)
	{
		if (!(renderable instanceof Player))
		{
			return true;
		}
		String name = ((Player) renderable).getName();
		return name == null || !ghostedNames.contains(name);
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
		ghostedNames = Collections.emptySet();
	}
}
