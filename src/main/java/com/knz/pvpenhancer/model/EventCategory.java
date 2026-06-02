package com.knz.pvpenhancer.model;

/**
 * Category a {@link CombatEvent} belongs to. Used by the overlay to colour-code
 * entries and to honour the per-category visibility toggles in the plugin config.
 */
public enum EventCategory
{
	COMBAT,
	EATING,
	GEAR_SWAP,
	PRAYER,
	COMBO
}
