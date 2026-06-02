package com.knz.pvpenhancer;

/**
 * When the combat-focus feature hides non-involved entities.
 *
 * <p>RuneLite cannot make entities semi-transparent (the render hook is a boolean
 * draw/skip), so "focus" is achieved by hiding the players + NPCs not involved in the
 * relevant fight. Scenery is never touched. The local player is always kept visible.
 */
public enum CombatFocusMode
{
	OFF("Off"),
	SELF("When you fight"),
	ANY_FIGHT("When anyone fights");

	private final String label;

	CombatFocusMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
