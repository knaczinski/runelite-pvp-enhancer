package com.knz.pvpenhancer.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps OSRS attack animation ids to an {@link AttackStyle}.
 *
 * <p>Style is not directly readable for remote players, so it is inferred from the
 * attack animation. This table is a curated seed of common PvP weapon animations and is
 * deliberately incomplete — when a live fight surfaces an unmapped animation the plugin
 * logs the id at debug level so it can be added here.
 *
 * <p>Animation ids are sourced from the OSRS Wiki (https://oldschool.runescape.wiki/w/Animation).
 * See {@code .ai/game/pvp/pvp-combat-events.md} for the reference table and rationale.
 */
public final class AnimationStyleMap
{
	private static final Map<Integer, AttackStyle> MAP;

	static
	{
		Map<Integer, AttackStyle> m = new HashMap<>();

		// Melee
		m.put(390, AttackStyle.MELEE);   // Punch (unarmed)
		m.put(422, AttackStyle.MELEE);   // Kick (unarmed)
		m.put(423, AttackStyle.MELEE);   // Block (unarmed)
		m.put(1658, AttackStyle.MELEE);  // Whip
		m.put(1711, AttackStyle.MELEE);  // Abyssal whip
		m.put(1979, AttackStyle.MELEE);  // Dragon dagger special (stab x2)
		m.put(7045, AttackStyle.MELEE);  // Dharok's greataxe
		m.put(7054, AttackStyle.MELEE);  // Bandos godsword
		m.put(7055, AttackStyle.MELEE);  // Saradomin godsword
		m.put(7056, AttackStyle.MELEE);  // Zamorak godsword
		m.put(7638, AttackStyle.MELEE);  // Armadyl godsword special

		// Ranged
		m.put(1062, AttackStyle.RANGED); // Shortbow
		m.put(426, AttackStyle.RANGED);  // Crossbow
		m.put(4230, AttackStyle.RANGED); // Karil's crossbow
		m.put(9168, AttackStyle.RANGED); // Twisted bow

		// Magic
		m.put(711, AttackStyle.MAGIC);   // Standard elemental strike/bolt/blast/wave
		m.put(1978, AttackStyle.MAGIC);  // God spells (Claws of Guthix / Saradomin Strike / Flames of Zamorak)
		m.put(811, AttackStyle.MAGIC);   // Ancient spells (Barrage family)

		MAP = Collections.unmodifiableMap(m);
	}

	private AnimationStyleMap()
	{
	}

	/**
	 * @param animationId the actor's current animation id
	 * @return the mapped {@link AttackStyle}, or {@code null} if the animation is not a
	 * catalogued attack animation.
	 */
	public static AttackStyle lookup(int animationId)
	{
		return MAP.get(animationId);
	}

	/**
	 * @return true if the animation id is a catalogued attack animation.
	 */
	public static boolean isKnown(int animationId)
	{
		return MAP.containsKey(animationId);
	}
}
