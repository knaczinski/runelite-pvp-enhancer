package com.knz.pvpenhancer.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Seed map of spot-anim (graphic) id → {@link Debuff} + base duration in game ticks.
 *
 * <p><b>Memory-cited seed — LIVE-VALIDATE.</b> The graphic ids and durations below are from
 * memory of the OSRS Wiki and must be confirmed in a live fight (HT). The plugin debug-logs
 * unknown spot-anims observed on tracked players so missing ids can be harvested and added.
 *
 * <p>Durations are the full (no-protection) values. Protection prayer / partial freezes halve
 * them, but the API does not expose that reliably, so the timer is a best-effort upper bound.
 */
public final class SpotanimDebuffs
{
	/** A debuff plus its base duration in ticks. */
	public static final class Entry
	{
		public final Debuff debuff;
		public final int durationTicks;

		Entry(Debuff debuff, int durationTicks)
		{
			this.debuff = debuff;
			this.durationTicks = durationTicks;
		}
	}

	private static final Map<Integer, Entry> MAP = build();

	private SpotanimDebuffs()
	{
	}

	private static Map<Integer, Entry> build()
	{
		Map<Integer, Entry> m = new HashMap<>();
		// Ancient Magicks ice spells — freeze (durations: Rush 5s, Burst 10s, Blitz 15s, Barrage 20s).
		m.put(361, new Entry(Debuff.FREEZE, 8));   // Ice Rush
		m.put(363, new Entry(Debuff.FREEZE, 16));  // Ice Burst
		m.put(367, new Entry(Debuff.FREEZE, 24));  // Ice Blitz
		m.put(369, new Entry(Debuff.FREEZE, 33));  // Ice Barrage
		// Bind family — snare/root (Bind 5s, Snare 10s, Entangle 15s).
		m.put(177, new Entry(Debuff.SNARE, 8));    // Bind
		m.put(178, new Entry(Debuff.SNARE, 16));   // Snare
		m.put(179, new Entry(Debuff.SNARE, 25));   // Entangle
		// Teleblock — ~5 minutes (500 ticks).
		m.put(345, new Entry(Debuff.TELEBLOCK, 500));
		return Collections.unmodifiableMap(m);
	}

	/** @return the seeded entry for a spot-anim id, or null if unknown. */
	public static Entry lookup(int spotanimId)
	{
		return MAP.get(spotanimId);
	}

	public static boolean isKnown(int spotanimId)
	{
		return MAP.containsKey(spotanimId);
	}
}
