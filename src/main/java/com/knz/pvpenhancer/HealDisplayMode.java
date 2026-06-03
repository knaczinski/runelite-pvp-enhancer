package com.knz.pvpenhancer;

/**
 * Who the in-game healing numbers are shown for. {@code isSelf} and {@code isOpponent} are
 * mutually exclusive (you are never in the opponent set).
 */
public enum HealDisplayMode
{
	OFF("Off"),
	EVERYONE("Everyone"),
	SELF("Only me"),
	OPPONENTS("Only opponents"),
	SELF_AND_OPPONENTS("Self + opponents");

	private final String label;

	HealDisplayMode(String label)
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

	/** True if the local player's own heals are ever shown — gates self-HP tracking. */
	public boolean includesLocal()
	{
		return matches(true, false);
	}

	/** True if any remote player's heals can be shown — gates remote health-ratio tracking. */
	public boolean includesOthers()
	{
		return this == EVERYONE || this == OPPONENTS || this == SELF_AND_OPPONENTS;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
