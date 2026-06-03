package com.knz.pvpenhancer.service;

import java.util.Collections;
import java.util.Set;
import javax.inject.Singleton;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Renderable;

/**
 * Decides, per rendered entity, whether it should be drawn while combat-focus is active.
 *
 * <p>When active, only the actors in the "involved" set (the fight participants, always
 * including the local player) are drawn; other Players and NPCs are hidden. Scenery,
 * projectiles, etc. are always drawn. The plugin recomputes the involved set + active flag
 * each game tick and a {@code Hooks.RenderableDrawListener} delegates to {@link #shouldDraw}.
 *
 * <p>The involved set is swapped atomically (volatile) so the render thread reads a
 * consistent snapshot; membership is identity-based (Actors).
 */
@Singleton
public class CombatFocusService
{
	private volatile boolean active = false;
	private volatile Set<? extends Renderable> involved = Collections.emptySet();

	/**
	 * Updates the focus state. Pass an identity-based set of the involved actors.
	 */
	public void update(boolean active, Set<? extends Renderable> involved)
	{
		this.involved = involved;
		this.active = active;
	}

	/**
	 * @return false to hide the renderable. Only non-involved Players/NPCs are hidden, and
	 * only while focus is active; everything else always draws.
	 */
	public boolean shouldDraw(Renderable renderable, boolean drawingUi)
	{
		if (!active)
		{
			return true;
		}
		if (renderable instanceof Player || renderable instanceof NPC)
		{
			return involved.contains(renderable);
		}
		return true;
	}

	/** @return true while focus is hiding non-involved entities (the outline overlay gates on this). */
	public boolean isActive()
	{
		return active;
	}

	/** @return true if this entity is currently hidden by focus (a non-involved Player/NPC). */
	public boolean isHidden(Renderable renderable)
	{
		return active && (renderable instanceof Player || renderable instanceof NPC)
			&& !involved.contains(renderable);
	}

	public void clear()
	{
		active = false;
		involved = Collections.emptySet();
	}
}
