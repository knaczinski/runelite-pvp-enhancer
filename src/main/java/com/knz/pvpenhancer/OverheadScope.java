package com.knz.pvpenhancer;

/**
 * Which actors a "resize overhead element" feature (Vengeance text, PK skull) applies to.
 * {@code isSelf} and {@code isOpponent} are mutually exclusive (the local player is never in the
 * opponent set).
 */
public enum OverheadScope
{
	OFF("Off"),
	EVERYONE("Everyone"),
	SELF("Me"),
	OPPONENTS("Opponents"),
	SELF_AND_OPPONENTS("Self + opp"),
	OTHERS("Others");

	private final String label;

	OverheadScope(String label)
	{
		this.label = label;
	}

	/** @return true if a player with the given relationship to you is in this scope. */
	public boolean matches(boolean isSelf, boolean isOpponent)
	{
		switch (this)
		{
			case EVERYONE:
				return true;
			case SELF:
				return isSelf;
			case OPPONENTS:
				return isOpponent && !isSelf;
			case SELF_AND_OPPONENTS:
				return isSelf || isOpponent;
			case OTHERS:
				return !isSelf && !isOpponent;
			default: // OFF
				return false;
		}
	}

	@Override
	public String toString()
	{
		return label;
	}
}
