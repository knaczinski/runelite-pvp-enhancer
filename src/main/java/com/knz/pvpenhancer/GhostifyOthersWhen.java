package com.knz.pvpenhancer;

/**
 * When to "ghostify" the <i>others</i> category (players who are not you, an opponent, group, or
 * a friend). Adds {@link #CANNOT_ATTACK}: ghost players you cannot attack at the current
 * Wilderness level (their combat level is outside your attackable range).
 */
public enum GhostifyOthersWhen
{
	NEVER("Never"),
	ALWAYS("Always"),
	IN_COMBAT("In combat"),
	NOT_IN_COMBAT("Not in combat"),
	CANNOT_ATTACK("Can't attack here"),
	CANNOT_ATTACK_AND_IDLE("Can't attack here + idle");

	private final String label;

	GhostifyOthersWhen(String label)
	{
		this.label = label;
	}

	/**
	 * @param inCombat     whether that player is currently fighting
	 * @param cannotAttack whether you cannot attack them in the current Wilderness level range
	 * @return true if the player should be ghosted
	 */
	public boolean shouldGhost(boolean inCombat, boolean cannotAttack)
	{
		switch (this)
		{
			case ALWAYS:
				return true;
			case IN_COMBAT:
				return inCombat;
			case NOT_IN_COMBAT:
				return !inCombat;
			case CANNOT_ATTACK:
				return cannotAttack;
			case CANNOT_ATTACK_AND_IDLE:
				return cannotAttack && !inCombat;
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
