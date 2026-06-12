package com.knz.pvpenhancer.model;

import net.runelite.api.HeadIcon;

/**
 * One row in the Hit Summary overlay — one per attack, filled progressively.
 *
 * <p>{@link #hit} is {@code null} until the {@code AttackHitsplatCorrelator} supplies the
 * matching hitsplat. {@link #offensivePrayer} is non-null only for the local player
 * (RuneLite cannot read opponents' offensive prayers).
 */
public class HitSummaryRow
{
	/** Unique row id within the session, used for correlation lookups. */
	public final int id;

	/** Display tick sequence code (e.g. 42 renders as "0042"). */
	public final int tickSequence;

	public final String player;
	public final AttackStyle style;
	public final String target;

	/** The overhead protection prayer the target had when the attack was thrown. May be null. */
	public final HeadIcon targetPrayer;

	/** The attacker's offensive prayer (Piety/Rigour/Augury). Null for opponents. */
	public final String offensivePrayer;

	/** Direction relative to the local player (drives the row colour). */
	public final HitDirection direction;

	/** Damage amount; null until the hitsplat lands and is correlated. */
	public volatile Integer hit;

	public HitSummaryRow(int id, int tickSequence, String player, AttackStyle style,
		String target, HeadIcon targetPrayer, String offensivePrayer, HitDirection direction)
	{
		this.id = id;
		this.tickSequence = tickSequence;
		this.player = player;
		this.style = style;
		this.target = target;
		this.targetPrayer = targetPrayer;
		this.offensivePrayer = offensivePrayer;
		this.direction = direction;
	}
}
