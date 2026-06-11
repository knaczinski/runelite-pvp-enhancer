package com.knz.pvpenhancer;

/**
 * When the floating combo feedback popup (e.g. "GODLIKE SWITCH", "TRIPLE EAT") is shown.
 * Detection always runs while combos are enabled; this only gates the on-screen popup.
 */
public enum ComboPopups
{
	NEVER("Never"),
	IN_COMBAT("In combat"),
	ALWAYS("Always");

	private final String label;

	ComboPopups(String label)
	{
		this.label = label;
	}

	/** @param inCombat whether the local player is currently in combat. */
	public boolean shows(boolean inCombat)
	{
		return this == ALWAYS || (this == IN_COMBAT && inCombat);
	}

	@Override
	public String toString()
	{
		return label;
	}
}
