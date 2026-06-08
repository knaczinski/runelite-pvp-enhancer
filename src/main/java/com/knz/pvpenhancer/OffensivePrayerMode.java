package com.knz.pvpenhancer;

/**
 * How the offensive-prayer highlighter works, to match different combat flows:
 * <ul>
 *   <li>{@link #PRAYER_FROM_WEAPON} — you equip a weapon, the plugin highlights the matching
 *       offensive prayer (Piety / Rigour / Augury) in the prayer tab.</li>
 *   <li>{@link #WEAPON_FROM_PRAYER} — you turn on an offensive prayer, the plugin highlights a
 *       weapon of the matching style in your inventory.</li>
 * </ul>
 */
public enum OffensivePrayerMode
{
	OFF("Off"),
	PRAYER_FROM_WEAPON("Prayer from weapon"),
	WEAPON_FROM_PRAYER("Weapon from prayer");

	private final String label;

	OffensivePrayerMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
