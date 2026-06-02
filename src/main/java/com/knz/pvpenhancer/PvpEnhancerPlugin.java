package com.knz.pvpenhancer;

import com.google.inject.Provides;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import com.knz.pvpenhancer.combatant.CombatEventFactory;
import com.knz.pvpenhancer.combatant.Combatant;
import com.knz.pvpenhancer.combatant.Combatants;
import com.knz.pvpenhancer.model.AnimationStyleMap;
import com.knz.pvpenhancer.model.AttackEvent;
import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.ComboEvent;
import com.knz.pvpenhancer.model.ComboResult;
import com.knz.pvpenhancer.model.EatEvent;
import com.knz.pvpenhancer.model.GearSwapEvent;
import com.knz.pvpenhancer.model.HealMath;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.HitsplatEvent;
import com.knz.pvpenhancer.model.HitsplatLabels;
import com.knz.pvpenhancer.model.PrayerEvent;
import com.knz.pvpenhancer.model.PrayerNames;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.overlay.ComboFeedbackOverlay;
import com.knz.pvpenhancer.overlay.HealOverlay;
import com.knz.pvpenhancer.overlay.HeartbeatOverlay;
import com.knz.pvpenhancer.overlay.NotRetaliatingOverlay;
import com.knz.pvpenhancer.panel.PvpEnhancerPanel;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator.Correlation;
import com.knz.pvpenhancer.service.CombatStateService;
import com.knz.pvpenhancer.service.ComboDetectorService;
import com.knz.pvpenhancer.service.HitSummaryService;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.HeadIcon;
import net.runelite.api.Hitsplat;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PvP Enhancer — tick-by-tick combat history, heartbeat, hit summary, combos, and
 * not-retaliating indicator.
 *
 * <p>This class owns all RuneLite event subscriptions and is the only component
 * coupled to the live {@link Client}. It translates raw events into model objects and
 * feeds them to the pure services (TickHistoryService, CombatStateService,
 * HitSummaryService, ComboDetectorService, AttackHitsplatCorrelator).
 *
 * <p>Config split: <b>Tracking</b> toggles gate what is <em>recorded</em>; <b>Overlay</b>
 * and feature-section toggles are display-only filters applied in the overlays.
 */
@PluginDescriptor(
	name = "PvP Enhancer",
	description = "Tick-history, heartbeat, hit summary, combos, and combat indicators.",
	tags = {"pvp", "combat", "tick", "history", "overlay", "prayer", "combo", "heartbeat"}
)
public class PvpEnhancerPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(PvpEnhancerPlugin.class);

	/** Animation id of the eat/drink action — excluded from attack style inference. */
	private static final int EAT_ANIMATION = 829;

	// ─── Injected services ────────────────────────────────────────────────

	@Inject private Client client;
	@Inject private OverlayManager overlayManager;
	@Inject private PvpEnhancerConfig config;
	@Inject private ItemManager itemManager;

	@Inject private TickHistoryService history;
	@Inject private CombatStateService combatState;
	@Inject private AttackHitsplatCorrelator correlator;
	@Inject private HitSummaryService hitSummary;
	@Inject private ComboDetectorService comboDetector;

	@Inject private HeartbeatOverlay heartbeatOverlay;
	@Inject private NotRetaliatingOverlay notRetaliatingOverlay;
	@Inject private ComboFeedbackOverlay comboFeedbackOverlay;
	@Inject private HealOverlay healOverlay;

	@Inject private ClientToolbar clientToolbar;
	@Inject private PvpEnhancerPanel panel;
	private NavigationButton navButton;

	// ─── Diff state ──────────────────────────────────────────────────────

	/** Local player's worn item ids from the previous tick, for gear-swap diffing. */
	private Map<EquipmentInventorySlot, Integer> previousEquipment;

	/** Each tracked player's overhead prayer from the previous tick, keyed by name. */
	private final Map<String, HeadIcon> previousOverheads = new HashMap<>();

	/** Assumed max HP for estimating remote players' heal amounts from their health ratio. */
	private static final int ASSUMED_MAX_HP = 99;

	/** Local player's exact HP last tick (-1 = not tracked). */
	private int previousLocalHp = -1;

	/** Each tracked player's health ratio last tick, keyed by name (remote heal detection). */
	private final Map<String, Integer> previousHealthRatio = new HashMap<>();

	// ─── Lifecycle ───────────────────────────────────────────────────────

	@Provides
	PvpEnhancerConfig provideConfig(ConfigManager cm)
	{
		return cm.getConfig(PvpEnhancerConfig.class);
	}

	@Override
	protected void startUp()
	{
		resetState();
		// Screen-view effects/alerts stay as overlays; the data lives in the sidebar panel.
		overlayManager.add(heartbeatOverlay);
		overlayManager.add(notRetaliatingOverlay);
		overlayManager.add(comboFeedbackOverlay);
		overlayManager.add(healOverlay);

		navButton = NavigationButton.builder()
			.tooltip("PvP Enhancer")
			.icon(buildIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(heartbeatOverlay);
		overlayManager.remove(notRetaliatingOverlay);
		overlayManager.remove(comboFeedbackOverlay);
		overlayManager.remove(healOverlay);
		clientToolbar.removeNavigation(navButton);
		resetState();
	}

	private void resetState()
	{
		history.clear();
		combatState.clear();
		correlator.clear();
		hitSummary.clear();
		comboDetector.clear();
		healOverlay.clear();
		previousEquipment = null;
		previousOverheads.clear();
		previousLocalHp = -1;
		previousHealthRatio.clear();
	}

	/** Snapshots service data on the client thread and rebuilds the sidebar panel on the EDT. */
	private void refreshPanel()
	{
		List<TickEntry> entries = history.getEntries();
		List<HitSummaryRow> rows = hitSummary.getRows();
		int tick = client.getTickCount();
		boolean inCombat = combatState.isInCombat(tick);
		boolean notRetaliating = combatState.isNotRetaliating(tick);
		SwingUtilities.invokeLater(() -> panel.update(entries, rows, inCombat, notRetaliating));
	}

	/** Builds the sidebar navigation icon (crossed swords) in code — no image resource needed. */
	private static BufferedImage buildIcon()
	{
		int size = 24;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(new Color(0xC8, 0x32, 0x32));
		g.drawLine(5, 5, 19, 19);
		g.setColor(new Color(0xDD, 0xDD, 0xDD));
		g.drawLine(19, 5, 5, 19);
		g.dispose();
		return img;
	}

	// ─── GameTick ────────────────────────────────────────────────────────

	/**
	 * Tick boundary: update combat state, detect gear/prayer/gear/combo changes, flush,
	 * then pulse the heartbeat overlay.
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		int tick = client.getTickCount();
		Player local = client.getLocalPlayer();

		// 1. Update combat engagement state
		boolean engaged = local != null && local.getInteracting() instanceof Player;
		combatState.setEngaged(engaged, tick);

		// 2. Inventory snapshot for combo-failed detection
		int[] invIds = inventoryIds();
		int[] invQty = inventoryQuantities();

		// 3. Detect gear swaps (also feeds combo detector)
		detectGearSwaps(tick);

		// 4. Detect prayer changes
		detectPrayerChanges();

		// 4b. Detect healing (overlay only — near the healer's health bar)
		detectHeals();

		// 5. Detect combos (before flush so ComboEvents land in this tick)
		if (config.showCombos())
		{
			List<ComboResult> combos = comboDetector.flush(invIds, invQty, tick);
			for (ComboResult combo : combos)
			{
				history.addEvent(new ComboEvent(combo));
				comboFeedbackOverlay.showCombo(combo);
			}
		}

		// 6. Flush tick history
		history.setMaxHistory(config.maxHistoryTicks());
		hitSummary.setMaxRows(config.hitSummaryRows());
		history.flushTick(tick);

		// 7. Heartbeat pulse + sidebar panel refresh
		heartbeatOverlay.recordTick();
		refreshPanel();
	}

	// ─── AnimationChanged ────────────────────────────────────────────────

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

			// Feed correlator + hit summary
			int tick = client.getTickCount();
			correlator.recordAttack(attack.getAttacker(), attack.getTarget(), attack.getStyle(), tick);

			// Offensive-prayer check for local player only
			String offenPray = null;
			if (attacker.isLocalPlayer())
			{
				offenPray = activeOffensivePrayer();
				combatState.recordCombatActivity(tick);
				// Feed combo detector: local player attack
				comboDetector.onAttack(attack.getStyle(), tick);
			}

			hitSummary.addAttack(
				history.getLastSequence(),
				attack.getAttacker(),
				attack.getStyle(),
				attack.getTarget(),
				attack.getTargetPrayer() != null ? PrayerNames.label(attack.getTargetPrayer()) : null,
				offenPray
			);
		}
		else if (!AnimationStyleMap.isKnown(animation))
		{
			Combatant target = attacker.getTarget();
			if (target != null)
			{
				log.debug("Unmapped animation {} by {} -> {}", animation, attacker.getName(), target.getName());
			}
		}
	}

	// ─── HitsplatApplied ─────────────────────────────────────────────────

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		int tick = client.getTickCount();
		Actor actor = event.getActor();

		if (actor == client.getLocalPlayer())
		{
			combatState.recordCombatActivity(tick);
		}

		Combatant target = Combatants.of(actor, client.getLocalPlayer());
		if (!isTracked(target))
		{
			return;
		}

		Hitsplat hitsplat = event.getHitsplat();
		history.addEvent(new HitsplatEvent(target.getName(), hitsplat.getAmount(), hitsplatLabel(hitsplat)));

		// Try to correlate with a pending attack
		Correlation corr = correlator.recordHitsplat(target.getName(), hitsplat.getAmount(), tick);
		if (corr != null)
		{
			hitSummary.applyCorrelation(corr);
		}
	}

	// ─── MenuOptionClicked ───────────────────────────────────────────────

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
			// param0 = inventory slot index; see MenuOptionClicked Javadoc
			int actionParam = event.getParam0();
			int itemId = event.getItemId();
			int qty = inventoryQuantityAt(actionParam);

			String itemName = Text.removeTags(event.getMenuTarget());
			history.addEvent(new EatEvent(localPlayerName(), itemName));

			if (config.showCombos())
			{
				comboDetector.onEatClick(actionParam, itemId, qty);
			}
		}
	}

	// ─── Gear swap detection ─────────────────────────────────────────────

	/**
	 * Diffs the local player's worn-equipment container against the previous tick. Emits
	 * {@link GearSwapEvent}s and feeds the combo detector.
	 */
	private void detectGearSwaps(int tick)
	{
		Map<EquipmentInventorySlot, Integer> current = snapshotEquipment();
		if (current == null)
		{
			previousEquipment = null;
			return;
		}

		if (previousEquipment != null)
		{
			String player = localPlayerName();
			int swapCount = 0;
			boolean weaponSwapped = false;

			for (Map.Entry<EquipmentInventorySlot, Integer> entry : current.entrySet())
			{
				int currentId = entry.getValue();
				int previousId = previousEquipment.getOrDefault(entry.getKey(), -1);
				if (currentId != previousId)
				{
					swapCount++;
					String itemName = currentId > 0 ? itemName(currentId) : "(nothing)";
					history.addEvent(new GearSwapEvent(player, entry.getKey().name(), currentId, itemName));
					if (entry.getKey() == EquipmentInventorySlot.WEAPON)
					{
						weaponSwapped = true;
					}
				}
			}

			if (config.showCombos())
			{
				comboDetector.onGearSwapCount(swapCount);
				if (weaponSwapped)
				{
					comboDetector.onWeaponSwap(tick);
				}
			}
		}

		previousEquipment = current;
	}

	private Map<EquipmentInventorySlot, Integer> snapshotEquipment()
	{
		ItemContainer eq = client.getItemContainer(InventoryID.WORN);
		if (eq == null)
		{
			return null;
		}
		Map<EquipmentInventorySlot, Integer> snapshot = new EnumMap<>(EquipmentInventorySlot.class);
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			Item item = eq.getItem(slot.getSlotIdx());
			snapshot.put(slot, item != null ? item.getId() : -1);
		}
		return snapshot;
	}

	// ─── Prayer change detection ─────────────────────────────────────────

	/**
	 * Diffs each tracked player's overhead prayer; emits {@link PrayerEvent} on change.
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
			Combatant c = Combatants.of(player, localPlayer);
			if (!isTracked(c))
			{
				continue;
			}
			String name = c.getName();
			HeadIcon icon = c.getOverheadPrayer();
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
	 * Detects HP recovery and shows a floating number near the healer's health bar. Local
	 * healing is exact (Hitpoints skill delta); remote players only expose a health ratio,
	 * so their amounts are estimated ("~") via {@link HealMath}. Scope is the heal config.
	 */
	private void detectHeals()
	{
		HealDisplayMode mode = config.healDisplayMode();

		// Local player — exact HP delta
		if (mode.includesLocal())
		{
			Player local = client.getLocalPlayer();
			if (local == null)
			{
				previousLocalHp = -1;
			}
			else
			{
				int hp = client.getBoostedSkillLevel(Skill.HITPOINTS);
				if (previousLocalHp >= 0 && hp - previousLocalHp >= 2) // skip +1 natural regen
				{
					healOverlay.addHeal(local, hp - previousLocalHp, false);
				}
				previousLocalHp = hp;
			}
		}
		else
		{
			previousLocalHp = -1;
		}

		// Remote players — estimate from health-ratio increase
		if (mode.includesOthers())
		{
			Player local = client.getLocalPlayer();
			Map<String, Integer> current = new HashMap<>();
			for (Player player : client.getTopLevelWorldView().players())
			{
				if (player == null || player == local || player.getName() == null)
				{
					continue;
				}
				int ratio = player.getHealthRatio();
				int scale = player.getHealthScale();
				if (ratio < 0 || scale <= 0)
				{
					continue;
				}
				String name = Text.removeTags(player.getName());
				Integer prev = previousHealthRatio.get(name);
				if (prev != null && ratio > prev)
				{
					int estimate = HealMath.estimateRemoteHeal(prev, ratio, scale, ASSUMED_MAX_HP);
					if (estimate >= 1)
					{
						healOverlay.addHeal(player, estimate, true);
					}
				}
				current.put(name, ratio);
			}
			previousHealthRatio.clear();
			previousHealthRatio.putAll(current);
		}
		else
		{
			previousHealthRatio.clear();
		}
	}

	// ─── Helpers ─────────────────────────────────────────────────────────

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
	 * Returns the label for the local player's currently active offensive prayer, or null.
	 * isPrayerActive(Prayer) is deprecated (does not handle eagle-eye/mystic-vigour) but
	 * no replacement is available for these prayers yet — suppress the warning.
	 */
	@SuppressWarnings("deprecation")
	private String activeOffensivePrayer()
	{
		if (client.isPrayerActive(Prayer.AUGURY))   return "Augury";
		if (client.isPrayerActive(Prayer.RIGOUR))   return "Rigour";
		if (client.isPrayerActive(Prayer.PIETY))    return "Piety";
		if (client.isPrayerActive(Prayer.CHIVALRY)) return "Chivalry";
		return null;
	}

	/** Snapshots all 28 inventory slot item ids. Returns empty array if container unavailable. */
	private int[] inventoryIds()
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		if (inv == null)
		{
			return new int[28];
		}
		int[] ids = new int[28];
		for (int i = 0; i < 28; i++)
		{
			Item item = inv.getItem(i);
			ids[i] = item != null ? item.getId() : -1;
		}
		return ids;
	}

	/** Snapshots all 28 inventory slot quantities. */
	private int[] inventoryQuantities()
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		if (inv == null)
		{
			return new int[28];
		}
		int[] qtys = new int[28];
		for (int i = 0; i < 28; i++)
		{
			Item item = inv.getItem(i);
			qtys[i] = item != null ? item.getQuantity() : 0;
		}
		return qtys;
	}

	/** Returns the item quantity at a specific inventory slot. */
	private int inventoryQuantityAt(int slot)
	{
		if (slot < 0 || slot >= 28)
		{
			return 0;
		}
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		if (inv == null)
		{
			return 0;
		}
		Item item = inv.getItem(slot);
		return item != null ? item.getQuantity() : 0;
	}

	private static String hitsplatLabel(Hitsplat hitsplat)
	{
		return HitsplatLabels.label(hitsplat.getHitsplatType(), hitsplat.getAmount());
	}
}
