package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator.Correlation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;

/**
 * Maintains the row data for the Hit Summary overlay. One row per attack; the "Hit" column
 * is filled when the {@link AttackHitsplatCorrelator} produces a matching correlation.
 *
 * <p>Guice {@code @Singleton} — the plugin and overlay share one instance.
 */
@Singleton
public class HitSummaryService
{
	private static final int DEFAULT_MAX_ROWS = 25;

	private final AtomicInteger nextId = new AtomicInteger(0);
	private final Deque<HitSummaryRow> rows = new ArrayDeque<>();
	private int maxRows = DEFAULT_MAX_ROWS;

	public void setMaxRows(int max)
	{
		this.maxRows = Math.max(1, max);
		trim();
	}

	/**
	 * Adds a new row for an attack. The Hit column starts null and is filled via
	 * {@link #applyCorrelation(Correlation)}.
	 *
	 * @param tickSequence the tick sequence code ("Tick 0042" → 42)
	 * @param player       attacker name
	 * @param style        inferred attack style
	 * @param target       target name
	 * @param targetPrayer short label for the target's overhead prayer at attack time (may be null)
	 * @param offenPrayer  attacker's active offensive prayer label (Piety/Rigour/Augury); null
	 *                     for opponents (not readable by the API)
	 */
	public void addAttack(int tickSequence, String player, AttackStyle style,
		String target, String targetPrayer, String offenPrayer)
	{
		int id = nextId.incrementAndGet();
		rows.addFirst(new HitSummaryRow(id, tickSequence, player, style, target, targetPrayer, offenPrayer));
		trim();
	}

	/**
	 * Fills the Hit column of the earliest unmatched row for the correlated attack.
	 */
	public void applyCorrelation(Correlation correlation)
	{
		for (HitSummaryRow row : rows)
		{
			if (row.hit != null)
			{
				continue; // already matched
			}
			if (row.player.equals(correlation.getAttacker())
				&& row.target.equals(correlation.getTarget())
				&& row.style == correlation.getStyle())
			{
				row.hit = correlation.getAmount();
				return;
			}
		}
	}

	/**
	 * @return a snapshot list of rows, newest first.
	 */
	public List<HitSummaryRow> getRows()
	{
		return new ArrayList<>(rows);
	}

	public void clear()
	{
		rows.clear();
	}

	private void trim()
	{
		while (rows.size() > maxRows)
		{
			rows.removeLast();
		}
	}
}
