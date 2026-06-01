package com.knz.pvpenhancer.combatant;

import net.runelite.api.HeadIcon;

/**
 * A fighter the plugin can record events for, abstracting over RuneLite's {@code Player}
 * and {@code NPC}.
 *
 * <p>Both are wrapped behind this one small interface so the detection logic is written
 * once, never branches on the concrete RuneLite type, and is trivially mockable in unit
 * tests (mocking the full {@code Player}/{@code NPC} API is heavy; mocking this is not).
 *
 * <p>Information a given implementation cannot provide is returned blank: e.g. NPCs have
 * no overhead protection prayer, so {@link #getOverheadPrayer()} returns {@code null} for
 * them. Callers must treat {@code null} as "not applicable / leave blank".
 *
 * @see Combatants#of for wrapping a RuneLite actor
 * @see PlayerCombatant
 * @see NpcCombatant
 */
public interface Combatant
{
	/**
	 * @return the display name, tags stripped. Never null (falls back to a placeholder).
	 */
	String getName();

	/**
	 * @return the current animation id (-1 when idle). Used to infer the attack style.
	 */
	int getAnimation();

	/**
	 * @return the overhead protection prayer, or {@code null} when none is active or the
	 * implementation cannot provide it (NPCs always return {@code null}).
	 */
	HeadIcon getOverheadPrayer();

	/**
	 * @return the combatant this one is interacting with (its attack/follow target),
	 * wrapped, or {@code null} when not interacting with anything trackable.
	 */
	Combatant getTarget();

	/**
	 * @return true if this is a player, false if an NPC. Drives the per-type config gate.
	 */
	boolean isPlayer();

	/**
	 * @return true if this combatant is the local (logged-in) player.
	 */
	boolean isLocalPlayer();
}
