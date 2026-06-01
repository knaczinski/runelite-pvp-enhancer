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
import com.knz.pvpenhancer.model.PrayerEvent;
import com.knz.pvpenhancer.overlay.TickHistoryOverlay;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.HeadIcon;
import net.runelite.api.Hitsplat;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
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
 * <p>This class owns the RuneLite event subscriptions and translates raw game events into
 * {@link com.knz.pvpenhancer.model.CombatEvent}s fed to the {@link TickHistoryService}.
 * It is the only component coupled to the live {@link Client}, which keeps the service
 * unit testable.
 *
 * <p>Config split: <b>Tracking</b> toggles (trackOpponents/trackNpcs) gate what is
 * recorded; the <b>Overlay</b> {@code show*} toggles are display filters applied by the
 * overlay, so everything tracked is always recorded and can be revealed retroactively.
 */
@PluginDescriptor(
	name = "PvP Enhancer",
	description = "Tick-by-tick PvP combat history overlay (attacks, eating, gear swaps, prayers).",
	tags = {"pvp", "combat", "tick", "history", "overlay", "prayer"}
)
public class PvpEnhancerPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(PvpEnhancerPlugin.class);

	/** Generic eat/drink animation. Eating is detected via the menu click; this only suppresses it as an attack. */
	private static final int EAT_ANIMATION = 829;

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

	/** Local player's worn item ids from the previous tick, for gear-swap diffing. */
	private Map<EquipmentInventorySlot, Integer> previousEquipment;

	/** Each tracked player's overhead prayer from the previous tick, keyed by name. */
	private final Map<String, HeadIcon> previousOverheads = new HashMap<>();

	@Provides
	PvpEnhancerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PvpEnhancerConfig.class);
	}

	@Override
	protected void startUp()
	{
		history.clear();
		resetDiffState();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		history.clear();
		resetDiffState();
	}

	private void resetDiffState()
	{
		previousEquipment = null;
		previousOverheads.clear();
	}

	/**
	 * Once per server tick: detect local-player gear swaps and overhead prayer changes,
	 * sync the history cap from config, then seal the tick's accumulated events.
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		detectGearSwaps();
		detectPrayerChanges();
		history.setMaxHistory(config.maxHistoryTicks());
		history.flushTick(client.getTickCount());
	}

	/**
	 * Records an attack when a tracked player's animation changes to a known attack
	 * animation. Display filtering by category happens in the overlay, not here.
	 */
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
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
	 * Records damage or a block applied to a tracked combatant.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
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
	 * the click tick, before the animation. Same-tick consumes merge into a combo eat at
	 * flush time.
	 */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = event.getMenuOption();
		if (option == null)
		{
			return;
		}
		if (option.equals("Eat") || option.equals("Drink"))
		{
			history.addEvent(new EatEvent(localPlayerName(), Text.removeTags(event.getMenuTarget())));
		}
	}

	/**
	 * Diffs the local player's worn equipment against the previous tick and emits a
	 * {@link GearSwapEvent} per changed slot. Reads the equipment item container, so item
	 * ids — and therefore names — are exact (no appearance-id decoding). Local player only.
	 */
	private void detectGearSwaps()
	{
		Map<EquipmentInventorySlot, Integer> current = snapshotEquipment();
		if (current == null)
		{
			return;
		}
		if (previousEquipment != null)
		{
			String player = localPlayerName();
			for (Map.Entry<EquipmentInventorySlot, Integer> entry : current.entrySet())
			{
				int currentId = entry.getValue();
				int previousId = previousEquipment.getOrDefault(entry.getKey(), -1);
				if (currentId != previousId)
				{
					String itemName = currentId > 0 ? itemName(currentId) : "(nothing)";
					history.addEvent(new GearSwapEvent(player, entry.getKey().name(), currentId, itemName));
				}
			}
		}
		previousEquipment = current;
	}

	private Map<EquipmentInventorySlot, Integer> snapshotEquipment()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null)
		{
			return null;
		}
		Map<EquipmentInventorySlot, Integer> snapshot = new EnumMap<>(EquipmentInventorySlot.class);
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			Item item = equipment.getItem(slot.getSlotIdx());
			snapshot.put(slot, item != null ? item.getId() : -1);
		}
		return snapshot;
	}

	/**
	 * Diffs each tracked player's overhead protection prayer against the previous tick and
	 * emits a {@link PrayerEvent} on change. Newly seen players set a baseline silently.
	 */
	private void detectPrayerChanges()
	{
		Player localPlayer = client.getLocalPlayer();
		Map<String, HeadIcon> current = new HashMap<>();
		for (Player player : client.getTopLevelWorldView().players())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}
			Combatant combatant = Combatants.of(player, localPlayer);
			if (!isTracked(combatant))
			{
				continue;
			}
			String name = combatant.getName();
			HeadIcon icon = combatant.getOverheadPrayer();
			current.put(name, icon);
			if (previousOverheads.containsKey(name) && !Objects.equals(previousOverheads.get(name), icon))
			{
				history.addEvent(new PrayerEvent(name, icon));
			}
		}
		previousOverheads.clear();
		previousOverheads.putAll(current);
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
		return local != null && local.getName() != null ? Text.removeTags(local.getName()) : "you";
	}

	private String itemName(int itemId)
	{
		try
		{
			ItemComposition comp = itemManager.getItemComposition(itemId);
			return comp != null ? Text.removeTags(comp.getName()) : ("item " + itemId);
		}
		catch (RuntimeException e)
		{
			return "item " + itemId;
		}
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
