package com.knz.pvpenhancer.service;

import javax.inject.Singleton;

/**
 * Single source of truth for whether the local player is "in combat", shared by the
 * heartbeat and not-retaliating overlays.
 *
 * <p>In combat is true when <em>either</em> the local player had combat activity (dealt or
 * took a hit, or threw an attack) within the last {@link #setCombatWindow(int) window}
 * ticks, <em>or</em> the local player is currently interacting with another player.
 *
 * <p>Fed by the plugin (it owns the {@code Client}); this class holds no game state, which
 * keeps it unit testable. Guice {@code @Singleton} so overlays share one instance.
 */
@Singleton
public class CombatStateService
{
	static final int DEFAULT_COMBAT_WINDOW_TICKS = 8;

	private int combatWindowTicks = DEFAULT_COMBAT_WINDOW_TICKS;
	private int lastActivityTick = Integer.MIN_VALUE;
	private boolean interactingWithPlayer;

	/**
	 * Sets how many ticks after the last combat activity the player is still considered in
	 * combat. Clamped to at least 1.
	 */
	public void setCombatWindow(int ticks)
	{
		this.combatWindowTicks = Math.max(1, ticks);
	}

	/**
	 * Stamps combat activity (a hit involving the local player, or an attack thrown) on the
	 * given tick.
	 */
	public void recordCombatActivity(int tick)
	{
		this.lastActivityTick = tick;
	}

	/**
	 * Reports whether the local player is interacting with another player this tick.
	 */
	public void setInteractingWithPlayer(boolean interacting)
	{
		this.interactingWithPlayer = interacting;
	}

	public boolean isInteractingWithPlayer()
	{
		return interactingWithPlayer;
	}

	/**
	 * @param currentTick the current server tick ({@code client.getTickCount()})
	 * @return true if in combat: interacting with a player, or recent combat activity within
	 * the window.
	 */
	public boolean isInCombat(int currentTick)
	{
		if (interactingWithPlayer)
		{
			return true;
		}
		if (lastActivityTick == Integer.MIN_VALUE)
		{
			return false;
		}
		int elapsed = currentTick - lastActivityTick;
		return elapsed >= 0 && elapsed <= combatWindowTicks;
	}

	/**
	 * Resets all state. Called on plugin start/stop and logout.
	 */
	public void clear()
	{
		lastActivityTick = Integer.MIN_VALUE;
		interactingWithPlayer = false;
	}
}
