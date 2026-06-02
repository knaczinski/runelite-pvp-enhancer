package com.knz.pvpenhancer;

/**
 * Who the in-game healing numbers are shown for.
 */
public enum HealDisplayMode
{
	OFF("Off"),
	SELF("Yourself"),
	OPPONENTS("Opponents"),
	EVERYONE("Everyone");

	private final String label;

	HealDisplayMode(String label)
	{
		this.label = label;
	}

	public boolean includesLocal()
	{
		return this == SELF || this == EVERYONE;
	}

	public boolean includesOthers()
	{
		return this == OPPONENTS || this == EVERYONE;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
