package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.AttackStyle;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Best-effort correlation of an attack with the hitsplat it produces.
 *
 * <p>In OSRS the hitsplat lands 1–3 ticks after the attack animation (melee is near-instant,
 * ranged/magic travel). Attacks are queued; the next hitsplat on the same target within the
 * style's delay window claims the earliest matching attack. Stale attacks expire. Accurate
 * in 1v1, approximate in chaotic multi-combat.
 *
 * <p>Pure and stateful (an internal pending queue) — unit tested without a client.
 */
public class AttackHitsplatCorrelator
{
	/** Hard cap on how long an unmatched attack stays pending, in ticks. */
	private static final int MAX_PENDING_TICKS = 3;

	private final Deque<PendingAttack> pending = new ArrayDeque<>();

	/**
	 * Queues an attack to be matched against a future hitsplat.
	 */
	public void recordAttack(String attacker, String target, AttackStyle style, int tick)
	{
		expire(tick);
		pending.addLast(new PendingAttack(attacker, target, style, tick));
	}

	/**
	 * Offers a hitsplat for correlation.
	 *
	 * @return the matched {@link Correlation}, or {@code null} if no pending attack fits
	 * (target mismatch or outside the style's delay window).
	 */
	public Correlation recordHitsplat(String target, int amount, int tick)
	{
		expire(tick);
		for (Iterator<PendingAttack> it = pending.iterator(); it.hasNext(); )
		{
			PendingAttack attack = it.next();
			int delay = tick - attack.tick;
			if (attack.target.equals(target) && delay >= 0 && delay <= windowFor(attack.style))
			{
				it.remove();
				return new Correlation(attack.attacker, attack.target, attack.style, attack.tick, amount, tick);
			}
		}
		return null;
	}

	public void clear()
	{
		pending.clear();
	}

	private void expire(int currentTick)
	{
		pending.removeIf(a -> currentTick - a.tick > MAX_PENDING_TICKS);
	}

	/** Max ticks between attack and its hitsplat for a given style. */
	private static int windowFor(AttackStyle style)
	{
		if (style == AttackStyle.MELEE)
		{
			return 1;
		}
		return MAX_PENDING_TICKS; // ranged / magic / unknown — allow projectile travel
	}

	private static final class PendingAttack
	{
		private final String attacker;
		private final String target;
		private final AttackStyle style;
		private final int tick;

		PendingAttack(String attacker, String target, AttackStyle style, int tick)
		{
			this.attacker = attacker;
			this.target = target;
			this.style = style;
			this.tick = tick;
		}
	}

	/** Result of a successful attack→hitsplat match. */
	public static final class Correlation
	{
		private final String attacker;
		private final String target;
		private final AttackStyle style;
		private final int attackTick;
		private final int amount;
		private final int hitTick;

		Correlation(String attacker, String target, AttackStyle style, int attackTick, int amount, int hitTick)
		{
			this.attacker = attacker;
			this.target = target;
			this.style = style;
			this.attackTick = attackTick;
			this.amount = amount;
			this.hitTick = hitTick;
		}

		public String getAttacker()
		{
			return attacker;
		}

		public String getTarget()
		{
			return target;
		}

		public AttackStyle getStyle()
		{
			return style;
		}

		public int getAttackTick()
		{
			return attackTick;
		}

		public int getAmount()
		{
			return amount;
		}

		public int getHitTick()
		{
			return hitTick;
		}
	}
}
