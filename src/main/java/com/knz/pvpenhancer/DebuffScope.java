package com.knz.pvpenhancer;

/**
 * Which players the freeze/snare/teleblock timers are shown for.
 */
public enum DebuffScope
{
	OFF("Off"),
	OPPONENTS("Self + opponents"),
	ALL("All players");

	private final String label;

	DebuffScope(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
