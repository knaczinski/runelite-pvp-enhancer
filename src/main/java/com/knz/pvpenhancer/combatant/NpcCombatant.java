package com.knz.pvpenhancer.combatant;

import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.client.util.Text;

/**
 * {@link Combatant} backed by a RuneLite {@link NPC}.
 *
 * <p>NPCs are a testing aid: they let a developer exercise combat/hitsplat detection
 * without a second player. Information NPCs do not have is returned blank —
 * {@link #getOverheadPrayer()} is always {@code null} (NPCs have no overhead protection
 * prayer in the player sense), and {@link #isLocalPlayer()} is always false.
 */
class NpcCombatant implements Combatant
{
	private final NPC npc;
	private final net.runelite.api.Player localPlayer;

	NpcCombatant(NPC npc, net.runelite.api.Player localPlayer)
	{
		this.npc = npc;
		this.localPlayer = localPlayer;
	}

	@Override
	public String getName()
	{
		return npc.getName() != null ? Text.removeTags(npc.getName()) : "?";
	}

	@Override
	public int getAnimation()
	{
		return npc.getAnimation();
	}

	/**
	 * @return always {@code null}. NPCs have no overhead protection prayer to report;
	 * callers leave the field blank.
	 */
	@Override
	public HeadIcon getOverheadPrayer()
	{
		return null;
	}

	@Override
	public Combatant getTarget()
	{
		return Combatants.of(npc.getInteracting(), localPlayer);
	}

	@Override
	public boolean isPlayer()
	{
		return false;
	}

	@Override
	public boolean isLocalPlayer()
	{
		return false;
	}
}
