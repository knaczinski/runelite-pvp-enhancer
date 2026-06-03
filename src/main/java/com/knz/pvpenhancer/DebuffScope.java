package com.knz.pvpenhancer;

/**
 * Which players the freeze/snare/teleblock timers are shown for. {@code isSelf} and
 * {@code isOpponent} are mutually exclusive (you are never in the opponent set).
 */
public enum DebuffScope
{
	OFF("Off"),
	EVERYONE("Everyone"),
	SELF("Only me"),
	OPPONENTS("Only opponents"),
	SELF_AND_OPPONENTS("Self + opponents");

	private final String label;

	DebuffScope(String label)
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
