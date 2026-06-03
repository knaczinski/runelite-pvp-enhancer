package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.service.PidGuessService.Pid;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PidGuessServiceTest
{
	@Test
	public void inactiveUntilFightSet()
	{
		PidGuessService s = new PidGuessService();
		assertFalse(s.isActive());
		s.recordContest(true); // ignored while inactive
		assertEquals(Pid.UNKNOWN, s.getGuess());
	}

	@Test
	public void leadingSideBecomesGuess()
	{
		PidGuessService s = new PidGuessService();
		s.setFight("opp");
		s.recordContest(true);
		s.recordContest(true);
		s.recordContest(false);
		assertEquals(Pid.LOCAL, s.getGuess());
		assertEquals(1, s.getConfidence());
	}

	@Test
	public void flipRaisesSwapWarning()
	{
		PidGuessService s = new PidGuessService();
		s.setFight("opp");
		s.recordContest(true); // LOCAL leads
		assertEquals(Pid.LOCAL, s.getGuess());
		assertFalse(s.isSwapWarningActive());
		s.recordContest(false); // tie
		s.recordContest(false); // OPPONENT overtakes -> swap
		assertEquals(Pid.OPPONENT, s.getGuess());
		assertTrue(s.isSwapWarningActive());
	}

	@Test
	public void changingOpponentResetsTally()
	{
		PidGuessService s = new PidGuessService();
		s.setFight("opp1");
		s.recordContest(true);
		s.setFight("opp2");
		assertEquals(Pid.UNKNOWN, s.getGuess());
		assertEquals(0, s.getConfidence());
	}

	@Test
	public void nullFightDeactivates()
	{
		PidGuessService s = new PidGuessService();
		s.setFight("opp");
		s.recordContest(true);
		s.setFight(null);
		assertFalse(s.isActive());
		assertEquals(Pid.UNKNOWN, s.getGuess());
	}
}
