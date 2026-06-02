package com.knz.pvpenhancer.model;

/**
 * Direction of a hit-summary row relative to the local player, used to colour the row:
 * green when you attack someone, red when someone attacks you, default otherwise.
 */
public enum HitDirection
{
	/** The local player is the attacker. */
	OUTGOING,
	/** The local player is the target. */
	INCOMING,
	/** Neither party is the local player. */
	OTHER
}
