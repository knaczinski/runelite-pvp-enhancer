package com.knz.pvpenhancer.service;

import java.util.Objects;
import javax.inject.Singleton;

/**
 * Experimental PID (player processing order) guesser for a 1v1 involving the local player.
 *
 * <p>PID is not exposed by the API (see {@code docs/pid-indicator-spike.md}). This accumulates a
 * <b>noisy vote</b> from contested same-tick hitsplats: on a tick where both you and the single
 * opponent take a hit, the side whose hit is applied first scores a point. The leading side is the
 * current guess; when the lead flips, a "PID swap?" warning is raised (the server likely
 * reshuffled). This is explicitly experimental — never certainty.
 *
 * <p>Active only for a clean 1v1 the local player is part of; the tally resets when the opponent
 * changes or the fight ends. All access is on the client thread.
 */
@Singleton
public class PidGuessService
{
	public enum Pid { UNKNOWN, LOCAL, OPPONENT }

	private static final long SWAP_WARN_MS = 4000L;

	private String opponentName;
	private boolean active;
	private int localFirst;
	private int opponentFirst;
	private Pid guess = Pid.UNKNOWN;
	private Pid lastDefinite = Pid.UNKNOWN; // last non-UNKNOWN guess, for swap detection across ties
	private long swapWarnUntil;

	/**
	 * Declares the current 1v1 opponent (null = not a tracked 1v1). Changing opponent resets the
	 * tally; a null opponent deactivates the indicator but keeps no stale guess.
	 */
	public void setFight(String opponentName)
	{
		if (!Objects.equals(opponentName, this.opponentName))
		{
			resetTally();
			this.opponentName = opponentName;
		}
		this.active = opponentName != null;
		if (opponentName == null)
		{
			guess = Pid.UNKNOWN;
		}
	}

	/** Records one contested tick: true if the local player's hit was applied before the opponent's. */
	public void recordContest(boolean localWasFirst)
	{
		if (!active)
		{
			return;
		}
		if (localWasFirst)
		{
			localFirst++;
		}
		else
		{
			opponentFirst++;
		}
		Pid next = localFirst > opponentFirst ? Pid.LOCAL
			: opponentFirst > localFirst ? Pid.OPPONENT : Pid.UNKNOWN;
		// Warn when the leading side flips, even if it passed through a tie in between.
		if (next != Pid.UNKNOWN)
		{
			if (lastDefinite != Pid.UNKNOWN && next != lastDefinite)
			{
				swapWarnUntil = System.currentTimeMillis() + SWAP_WARN_MS;
			}
			lastDefinite = next;
		}
		guess = next;
	}

	public boolean isActive()
	{
		return active;
	}

	public Pid getGuess()
	{
		return guess;
	}

	/** Difference between the two tallies — a rough confidence for display. */
	public int getConfidence()
	{
		return Math.abs(localFirst - opponentFirst);
	}

	/** @return true while a swap warning is still fresh (for the overlay flash). */
	public boolean isSwapWarningActive()
	{
		return System.currentTimeMillis() < swapWarnUntil;
	}

	// ─── Dev-panel mock hooks ────────────────────────────────────────────────

	/** Forces a guess and activates the indicator (dev panel mock). */
	public void forceGuess(Pid pid)
	{
		this.active = true;
		this.guess = pid;
	}

	/** Triggers the swap-warning flash (dev panel mock). */
	public void triggerSwapWarning()
	{
		this.active = true;
		this.swapWarnUntil = System.currentTimeMillis() + SWAP_WARN_MS;
	}

	public void reset()
	{
		opponentName = null;
		active = false;
		resetTally();
		swapWarnUntil = 0L;
	}

	private void resetTally()
	{
		localFirst = 0;
		opponentFirst = 0;
		guess = Pid.UNKNOWN;
		lastDefinite = Pid.UNKNOWN;
	}
}
