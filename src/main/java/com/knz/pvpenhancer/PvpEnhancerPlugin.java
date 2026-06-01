package com.knz.pvpenhancer;

import com.google.inject.Provides;
import com.knz.pvpenhancer.combatant.CombatEventFactory;
import com.knz.pvpenhancer.combatant.Combatant;
import com.knz.pvpenhancer.combatant.Combatants;
import com.knz.pvpenhancer.model.AnimationStyleMap;
import com.knz.pvpenhancer.model.AttackEvent;
import com.knz.pvpenhancer.model.EatEvent;
import com.knz.pvpenhancer.model.GearSwapEvent;
import com.knz.pvpenhancer.model.HitsplatEvent;
import com.knz.pvpenhancer.overlay.TickHistoryOverlay;
import com.knz.pvpenhancer.service.TickHistoryService;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PvP Enhancer — records a tick-by-tick combat history and renders it as an overlay.
 *
 * <p>This class owns the RuneLite event subscriptions and translates raw game events
 * into {@link com.knz.pvpenhancer.model.CombatEvent}s fed to the {@link TickHistoryService}.
 * The service buffers them; the {@link TickHistoryOverlay} renders them. The plugin is
 * the only component coupled to the live {@link Client}, which keeps the service unit
 * testable.
 */
@PluginDescriptor(
	name = "PvP Enhancer",
	description = "Tick-by-tick PvP combat history overlay (attacks, eating, gear swaps).",
	tags = {"pvp", "combat", "tick", "history", "overlay"}
)
public class PvpEnhancerPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(PvpEnhancerPlugin.class);

	/** Generic eat/drink animation. Eating is detected via the menu click; this is only used to suppress it as an attack. */
	private static final int EAT_ANIMATION = 829;

	/** Equipment-id decode offsets (OSRS Wiki): id >= ITEM_OFFSET is a real item; id-ITEM_OFFSET is the item id. */
	private static final int ITEM_OFFSET = 512;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PvpEnhancerConfig config;

	@Inject
	private TickHistoryService history;

	@Inject
	private TickHistoryOverlay overlay;

	@Inject
	private ItemManager itemManager;

	/** Local player's equipment ids from the previous tick, for gear-swap diffing. */
	private int[] previousEquipment;

	@Provides
	PvpEnhancerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PvpEnhancerConfig.class);
	}

	@Override
	protected void startUp()
	{
		history.clear();
		previousEquipment = null;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		history.clear();
		previousEquipment = null;
	}

	/**
	 * Once per server tick: detect local-player gear swaps, sync the history cap from
	 * config, then seal the tick's accumulated events into the buffer.
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		detectGearSwaps();
		history.setMaxHistory(config.maxHistoryTicks());
		history.flushTick(client.getTickCount());
	}

	/**
	 * Detects an attack: when a tracked player's animation changes to a known attack
	 * animation, records the attacker, target, inferred style, and the target's overhead
	 * prayer at that instant.
	 */
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!config.showCombat())
		{
			return;
		}
		Combatant attacker = Combatants.of(event.getActor(), client.getLocalPlayer());
		if (!isTracked(attacker))
		{
			return;
		}

		int animation = attacker.getAnimation();
		if (animation == EAT_ANIMATION || animation == -1)
		{
			return;
		}

		AttackEvent attack = CombatEventFactory.fromAttack(attacker);
		if (attack != null)
		{
			history.addEvent(attack);
		}
		else if (!AnimationStyleMap.isKnown(animation))
		{
			// Unmapped attack-like animation: log the id so it can be added to
			// AnimationStyleMap (see .ai/game/pvp/pvp-combat-events.md).
			Combatant target = attacker.getTarget();
			if (target != null)
			{
				log.debug("Unmapped animation {} by {} -> {}", animation, attacker.getName(), target.getName());
			}
		}
	}

	/**
	 * Records damage or a block applied to a tracked player.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!config.showCombat())
		{
			return;
		}
		Combatant target = Combatants.of(event.getActor(), client.getLocalPlayer());
		if (!isTracked(target))
		{
			return;
		}

		Hitsplat hitsplat = event.getHitsplat();
		history.addEvent(new HitsplatEvent(target.getName(), hitsplat.getAmount(), hitsplatLabel(hitsplat)));
	}

	/**
	 * Records the local player eating or drinking. The "Eat"/"Drink" menu click fires on
	 * the tick of the click, before the animation, giving the earliest signal.
	 */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!config.showEating())
		{
			return;
		}
		String option = event.getMenuOption();
		if (option == null)
		{
			return;
		}
		if (option.equals("Eat") || option.equals("Drink"))
		{
			String item = Text.removeTags(event.getMenuTarget());
			history.addEvent(new EatEvent(localPlayerName(), item));
		}
	}

	/**
	 * Diffs the local player's equipment against the previous tick and emits a
	 * {@link GearSwapEvent} for each changed slot. Local player only in v1.
	 */
	private void detectGearSwaps()
	{
		if (!config.showGearSwap())
		{
			// Keep the baseline current so re-enabling mid-session does not replay a backlog of swaps.
			previousEquipment = currentEquipment();
			return;
		}

		int[] current = currentEquipment();
		if (current == null)
		{
			return;
		}

		if (previousEquipment != null && previousEquipment.length == current.length)
		{
			String player = localPlayerName();
			KitType[] slots = KitType.values();
			for (int i = 0; i < current.length; i++)
			{
				if (current[i] == previousEquipment[i])
				{
					continue;
				}
				String slot = i < slots.length ? slots[i].name() : ("slot" + i);
				int raw = current[i];
				int itemId = raw >= ITEM_OFFSET ? raw - ITEM_OFFSET : -1;
				String itemName = itemId > 0 ? itemName(itemId) : "(nothing)";
				history.addEvent(new GearSwapEvent(player, slot, itemId, itemName));
			}
		}

		previousEquipment = current;
	}

	private int[] currentEquipment()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return null;
		}
		PlayerComposition composition = local.getPlayerComposition();
		if (composition == null)
		{
			return null;
		}
		int[] ids = composition.getEquipmentIds();
		return ids != null ? ids.clone() : null;
	}

	/**
	 * @return true if events for this combatant should be recorded. Players: always when
	 * trackOpponents is on, otherwise only the local player. NPCs: only when trackNpcs is on.
	 */
	private boolean isTracked(Combatant combatant)
	{
		if (combatant == null)
		{
			return false;
		}
		if (!combatant.isPlayer())
		{
			return config.trackNpcs();
		}
		return config.trackOpponents() || combatant.isLocalPlayer();
	}

	private String localPlayerName()
	{
		Player local = client.getLocalPlayer();
		return local != null ? safeName(local.getName()) : "you";
	}

	private String itemName(int itemId)
	{
		try
		{
			ItemComposition comp = itemManager.getItemComposition(itemId);
			return comp != null ? safeName(comp.getName()) : ("item " + itemId);
		}
		catch (RuntimeException e)
		{
			return "item " + itemId;
		}
	}

	private static String safeName(String name)
	{
		return name != null ? Text.removeTags(name) : "?";
	}

	/**
	 * Coarse hitsplat label. A zero-damage splat is a block; anything else is a hit.
	 * Poison/venom/heal refinement is deferred (see design doc, future extensions).
	 */
	private static String hitsplatLabel(Hitsplat hitsplat)
	{
		return hitsplat.getAmount() == 0 ? "block" : "hit";
	}
}
