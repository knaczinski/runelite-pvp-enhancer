package com.knz.pvpenhancer.combatant;

import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;

/**
 * Factory that wraps a RuneLite {@link Actor} in the matching {@link Combatant}
 * implementation. This is the single point that branches on the concrete RuneLite type;
 * everything downstream programs against {@link Combatant}.
 */
public final class Combatants
{
	private Combatants()
	{
	}

	/**
	 * Wraps an actor as a {@link Combatant}.
	 *
	 * @param actor       the RuneLite actor (may be null)
	 * @param localPlayer the local player, used to answer {@link Combatant#isLocalPlayer()}
	 * @return a {@link PlayerCombatant} or {@link NpcCombatant}, or {@code null} if the
	 * actor is null or an unsupported type.
	 */
	public static Combatant of(Actor actor, Player localPlayer)
	{
		if (actor instanceof Player)
		{
			return new PlayerCombatant((Player) actor, localPlayer);
		}
		if (actor instanceof NPC)
		{
			return new NpcCombatant((NPC) actor, localPlayer);
		}
		return null;
	}
}
