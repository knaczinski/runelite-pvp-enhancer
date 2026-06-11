package com.knz.pvpenhancer;

/**
 * Where the predicted-damage number is drawn: over the opponent you are hitting, or over your own
 * character (next to the health bar, where heal numbers appear).
 */
public enum HitPredictAnchor
{
	OPPONENT("Over opponent"),
	SELF("Over me (heal spot)");

	private final String label;

	HitPredictAnchor(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
