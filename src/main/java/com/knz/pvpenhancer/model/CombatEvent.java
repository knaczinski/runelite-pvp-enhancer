package com.knz.pvpenhancer.model;

/**
 * A single thing that happened during a fight on a specific game tick.
 *
 * <p>Concrete subclasses ({@link AttackEvent}, {@link HitsplatEvent}, {@link EatEvent},
 * {@link GearSwapEvent}) carry their own fields. The base contract is a category (for
 * filtering and colouring) and a one-line display string for the overlay.
 */
public abstract class CombatEvent
{
	/**
	 * @return the category this event belongs to, used for overlay filtering and colour.
	 */
	public abstract EventCategory getCategory();

	/**
	 * @return a single human-readable line describing the event, rendered in the overlay.
	 */
	public abstract String format();

	/**
	 * Optional per-event color override for the overlay. Returns {@code null} to use the
	 * default color for this event's {@link EventCategory}.
	 */
	public java.awt.Color getColor()
	{
		return null;
	}
}
