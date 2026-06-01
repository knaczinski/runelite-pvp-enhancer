package com.knz.pvpenhancer.combatant;

import com.knz.pvpenhancer.model.AttackEvent;
import com.knz.pvpenhancer.model.AttackStyle;
import net.runelite.api.HeadIcon;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CombatEventFactory}. Because the logic is written against the
 * {@link Combatant} interface (not RuneLite's Player/NPC directly), each combatant is a
 * plain Mockito mock — no live client required.
 */
public class CombatEventFactoryTest
{
	@Test
	public void buildsAttackEventWithPlayerTargetPrayer()
	{
		Combatant target = mock(Combatant.class);
		when(target.getName()).thenReturn("victim");
		when(target.isPlayer()).thenReturn(true);
		when(target.getOverheadPrayer()).thenReturn(HeadIcon.MAGIC);

		Combatant attacker = mock(Combatant.class);
		when(attacker.getName()).thenReturn("attacker");
		when(attacker.getAnimation()).thenReturn(1658); // whip -> MELEE
		when(attacker.getTarget()).thenReturn(target);

		AttackEvent event = CombatEventFactory.fromAttack(attacker);

		assertEquals(AttackStyle.MELEE, event.getStyle());
		assertEquals("attacker", event.getAttacker());
		assertEquals("victim", event.getTarget());
		assertEquals(HeadIcon.MAGIC, event.getTargetPrayer());
		assertTrue(event.format().contains("on Protect Magic"));
	}

	@Test
	public void npcTargetLeavesPrayerBlank()
	{
		// NPCs return null for prayer; the event must leave the prayer clause blank.
		Combatant npc = mock(Combatant.class);
		when(npc.getName()).thenReturn("Chicken");
		when(npc.isPlayer()).thenReturn(false);
		when(npc.getOverheadPrayer()).thenReturn(null);

		Combatant attacker = mock(Combatant.class);
		when(attacker.getName()).thenReturn("you");
		when(attacker.getAnimation()).thenReturn(1658); // whip -> MELEE
		when(attacker.getTarget()).thenReturn(npc);

		AttackEvent event = CombatEventFactory.fromAttack(attacker);

		assertNull(event.getTargetPrayer());
		assertEquals("you -> Chicken  melee", event.format());
		assertFalse(event.format().contains("on "));
	}

	@Test
	public void unmappedAnimationProducesNoEvent()
	{
		Combatant attacker = mock(Combatant.class);
		when(attacker.getAnimation()).thenReturn(987654); // not in AnimationStyleMap

		assertNull(CombatEventFactory.fromAttack(attacker));
	}

	@Test
	public void nullAttackerProducesNoEvent()
	{
		assertNull(CombatEventFactory.fromAttack(null));
	}

	@Test
	public void missingTargetStillBuildsAttack()
	{
		// Attacker mid-swing with no current interaction target.
		Combatant attacker = mock(Combatant.class);
		when(attacker.getName()).thenReturn("you");
		when(attacker.getAnimation()).thenReturn(1658);
		when(attacker.getTarget()).thenReturn(null);

		AttackEvent event = CombatEventFactory.fromAttack(attacker);

		assertEquals("you", event.getAttacker());
		assertEquals("?", event.getTarget());
		assertNull(event.getTargetPrayer());
	}
}
