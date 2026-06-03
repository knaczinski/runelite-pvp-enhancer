package com.knz.pvpenhancer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Seed map of equipped weapon item id → {@link AttackStyle}, used to predict which protection
 * prayer an opponent's weapon calls for.
 *
 * <p><b>Memory-cited seed — extend from live data.</b> Item ids are from memory of the OSRS
 * Wiki; the plugin debug-logs unknown weapon ids seen on a target so the map can grow. Hybrid
 * weapons (e.g. staff of the dead, which autocasts) are mapped to their primary PvP role.
 */
public final class WeaponStyleMap
{
	private static final Map<Integer, AttackStyle> MAP = build();

	private WeaponStyleMap()
	{
	}

	private static Map<Integer, AttackStyle> build()
	{
		Map<Integer, AttackStyle> m = new HashMap<>();

		// ── Melee ──
		put(m, AttackStyle.MELEE,
			4151,  // Abyssal whip
			4587,  // Dragon scimitar
			1305,  // Dragon longsword
			5698,  // Dragon dagger
			1434,  // Dragon mace
			13576, // Dragon warhammer
			13652, // Dragon claws
			11802, // Armadyl godsword
			11804, // Bandos godsword
			11806, // Saradomin godsword
			11808, // Zamorak godsword
			22324, // Ghrazi rapier
			21003, // Elder maul
			23987, // Inquisitor's mace
			12006  // Abyssal tentacle
		);

		// ── Ranged ──
		put(m, AttackStyle.RANGED,
			9185,  // Rune crossbow
			11785, // Armadyl crossbow
			26374, // Zaryte crossbow
			861,   // Magic shortbow
			11235, // Dark bow
			12926, // Toxic blowpipe
			4734,  // Karil's crossbow
			19478, // Light ballista
			19481  // Heavy ballista
		);

		// ── Magic ──
		put(m, AttackStyle.MAGIC,
			11907, // Trident of the seas
			12899, // Trident of the swamp
			21006, // Kodai wand
			6914,  // Master wand
			11791, // Staff of the dead
			22323, // Sanguinesti staff
			4675,  // Ancient staff
			24424  // Volatile nightmare staff
		);

		return m;
	}

	private static void put(Map<Integer, AttackStyle> m, AttackStyle style, int... ids)
	{
		for (int id : ids)
		{
			m.put(id, style);
		}
	}

	/** @return the style for an equipped weapon item id, or {@link AttackStyle#UNKNOWN}. */
	public static AttackStyle styleOf(int weaponItemId)
	{
		return MAP.getOrDefault(weaponItemId, AttackStyle.UNKNOWN);
	}

	public static boolean isKnown(int weaponItemId)
	{
		return MAP.containsKey(weaponItemId);
	}
}
