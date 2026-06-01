package com.knz.pvpenhancer.combatant;

import com.knz.pvpenhancer.model.AnimationStyleMap;
import com.knz.pvpenhancer.model.AttackEvent;
import com.knz.pvpenhancer.model.AttackStyle;
import net.runelite.api.HeadIcon;

/**
 * Pure translation from a {@link Combatant} to a {@link AttackEvent}. Holds no client or
 * plugin state, so it is unit-tested directly against mocked {@link Combatant}s
 * (see {@code CombatEventFactoryTest}).
 */
public final class CombatEventFactory
{
	private CombatEventFactory()
	{
	}

	/**
	 * Builds an attack event from an attacker, inferring the style from its animation.
	 *
	 * <p>The target's overhead prayer is taken from the target combatant — {@code null}
	 * (left blank by the event) when the target has no prayer up or is an NPC.
	 *
	 * @param attacker the attacking combatant (may be null)
	 * @return the {@link AttackEvent}, or {@code null} if there is no attacker or the
	 * animation is not a catalogued attack animation (idle, walking, unmapped, …).
	 */
	public static AttackEvent fromAttack(Combatant attacker)
	{
		if (attacker == null)
		{
			return null;
		}
		AttackStyle style = AnimationStyleMap.lookup(attacker.getAnimation());
		if (style == null)
		{
			return null;
		}
		Combatant target = attacker.getTarget();
		String targetName = target != null ? target.getName() : "?";
		HeadIcon targetPrayer = target != null ? target.getOverheadPrayer() : null;
		return new AttackEvent(attacker.getName(), targetName, style, targetPrayer);
	}
}
