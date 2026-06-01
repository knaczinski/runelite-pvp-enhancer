package com.knz.pvpenhancer.combatant;

import net.runelite.api.HeadIcon;
import net.runelite.api.Player;
import net.runelite.client.util.Text;

/**
 * {@link Combatant} backed by a RuneLite {@link Player}. Provides full information,
 * including the overhead protection prayer.
 */
class PlayerCombatant implements Combatant
{
	private final Player player;
	private final Player localPlayer;

	PlayerCombatant(Player player, Player localPlayer)
	{
		this.player = player;
		this.localPlayer = localPlayer;
	}

	@Override
	public String getName()
	{
		return player.getName() != null ? Text.removeTags(player.getName()) : "?";
	}

	@Override
	public int getAnimation()
	{
		return player.getAnimation();
	}

	@Override
	public HeadIcon getOverheadPrayer()
	{
		return player.getOverheadIcon();
	}

	@Override
	public Combatant getTarget()
	{
		return Combatants.of(player.getInteracting(), localPlayer);
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public boolean isLocalPlayer()
	{
		return player == localPlayer;
	}
}
