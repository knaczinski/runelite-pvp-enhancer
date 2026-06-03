package com.knz.pvpenhancer;

/**
 * When to "ghostify" a character category (hide its model, leaving only an outline) — for
 * self, opponents, group, and friends. "In combat" means the character is actively fighting.
 */
public enum GhostifyWhen
{
	NEVER("Never"),
	ALWAYS("Always"),
	IN_COMBAT("In combat"),
	NOT_IN_COMBAT("Not in combat");

	private final String label;

	GhostifyWhen(String label)
	{
		this.label = label;
	}

	/** @return true if a character in this category should be ghosted given its combat state. */
	public boolean shouldGhost(boolean inCombat)
	{
		switch (this)
		{
			case ALWAYS:
				return true;
			case IN_COMBAT:
				return inCombat;
			case NOT_IN_COMBAT:
				return !inCombat;
			default: // NEVER
				return false;
		}
	}

	@Override
	public String toString()
	{
		return label;
	}
}
