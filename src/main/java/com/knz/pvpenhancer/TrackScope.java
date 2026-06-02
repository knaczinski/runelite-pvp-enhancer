package com.knz.pvpenhancer;

/**
 * Which players the tick history and hit summary record events for.
 */
public enum TrackScope
{
	/** Only you and players currently in combat with you (your target + whoever attacks you). */
	SELF_AND_OPPONENTS("Self + opponents"),

	/** All visible players. */
	EVERYONE("Everyone");

	private final String label;

	TrackScope(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
