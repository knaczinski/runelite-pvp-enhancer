package com.knz.pvpenhancer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Seed map of weapon item id → base attack speed in game ticks, for the "ticks until next attack"
 * timer.
 *
 * <p><b>Memory-cited seed — LIVE-VALIDATE.</b> Speeds are from memory of the OSRS Wiki and are the
 * BASE speed (the Rapid/aggressive style shaves a tick — not accounted for, since a remote
 * player's chosen style isn't readable). Unknown weapons fall back to {@link #DEFAULT_TICKS}; the
 * plugin debug-logs unknown ids so the map can grow.
 */
public final class WeaponSpeeds
{
	/** Fallback attack speed (ticks) for an unknown or unarmed weapon. */
	public static final int DEFAULT_TICKS = 4;

	private static final Map<Integer, Integer> MAP = build();

	private WeaponSpeeds()
	{
	}

	private static Map<Integer, Integer> build()
	{
		Map<Integer, Integer> m = new HashMap<>();
		// Melee
		put(m, 4, 4151, 4587, 5698, 13652, 22324, 23987, 12006); // whip, d scim, dds, claws, rapier, inq mace, tentacle
		put(m, 5, 1305, 1434);                                    // dragon longsword, dragon mace
		put(m, 6, 13576, 11802, 11804, 11806, 11808, 21003);      // dwh, AGS, BGS, SGS, ZGS, elder maul
		// Ranged
		put(m, 3, 12926);                                         // toxic blowpipe
		put(m, 4, 861, 4734);                                     // magic shortbow, Karil's
		put(m, 5, 9185);                                          // rune crossbow
		put(m, 6, 11785, 26374);                                  // ACB, ZCB
		put(m, 7, 19478, 19481);                                  // ballistae
		put(m, 9, 11235);                                         // dark bow
		// Magic
		put(m, 4, 11907, 12899, 21006, 6914, 11791, 22323, 24424); // tridents, kodai, master wand, SotD, sang, volatile
		put(m, 5, 4675);                                          // ancient staff
		return m;
	}

	private static void put(Map<Integer, Integer> m, int ticks, int... ids)
	{
		for (int id : ids)
		{
			m.put(id, ticks);
		}
	}

	/** @return base attack speed in ticks for a weapon item id, or {@link #DEFAULT_TICKS}. */
	public static int ticks(int weaponItemId)
	{
		return MAP.getOrDefault(weaponItemId, DEFAULT_TICKS);
	}

	public static boolean isKnown(int weaponItemId)
	{
		return MAP.containsKey(weaponItemId);
	}
}
