package com.knz.pvpenhancer.model;

/**
 * Combat attack style inferred from a player's attack animation.
 *
 * <p>Style cannot be read directly from the game state for remote players, so it is
 * inferred by mapping the animation id to a style via {@link AnimationStyleMap}. When
 * an animation is not catalogued the style is {@link #UNKNOWN}.
 */
public enum AttackStyle
{
	MELEE("melee"),
	RANGED("ranged"),
	MAGIC("magic"),
	UNKNOWN("?");

	private final String label;

	AttackStyle(String label)
	{
		this.label = label;
	}

	/**
	 * @return short lower-case label used in the overlay (e.g. "melee").
	 */
	public String getLabel()
	{
		return label;
	}
}
