package com.knz.pvpenhancer;

import com.google.inject.Provides;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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
import com.knz.pvpenhancer.model.Debuff;
import com.knz.pvpenhancer.model.EatEvent;
import com.knz.pvpenhancer.model.GearSwapEvent;
import com.knz.pvpenhancer.model.HealMath;
import com.knz.pvpenhancer.model.HitDirection;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.HitsplatEvent;
import com.knz.pvpenhancer.model.HitsplatLabels;
import com.knz.pvpenhancer.model.PrayerEvent;
import com.knz.pvpenhancer.model.PrayerNames;
import com.knz.pvpenhancer.model.SpotanimDebuffs;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.model.WeaponStyleMap;
import com.knz.pvpenhancer.model.XpDamage;
import com.knz.pvpenhancer.overlay.ComboFeedbackOverlay;
import com.knz.pvpenhancer.overlay.DebuffTimerOverlay;
import com.knz.pvpenhancer.overlay.HealOverlay;
import com.knz.pvpenhancer.overlay.HeartbeatOverlay;
import com.knz.pvpenhancer.overlay.HitPredictOverlay;
import com.knz.pvpenhancer.overlay.NotRetaliatingOverlay;
import com.knz.pvpenhancer.overlay.PrayerHighlightOverlay;
import com.knz.pvpenhancer.overlay.SkullResizeOverlay;
import com.knz.pvpenhancer.overlay.VengeanceTextOverlay;
import com.knz.pvpenhancer.panel.DevPanel;
import com.knz.pvpenhancer.panel.PvpEnhancerPanel;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator;
import com.knz.pvpenhancer.service.AttackHitsplatCorrelator.Correlation;
import com.knz.pvpenhancer.service.CombatFocusService;
import com.knz.pvpenhancer.service.CombatStateService;
import com.knz.pvpenhancer.service.ComboDetectorService;
import com.knz.pvpenhancer.service.DebuffTrackerService;
import com.knz.pvpenhancer.service.HitSummaryService;
import com.knz.pvpenhancer.service.TickHistoryService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.HeadIcon;
import net.runelite.api.Hitsplat;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SkullIcon;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
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
	@Inject private HitPredictOverlay hitPredictOverlay;
	@Inject private DebuffTimerOverlay debuffTimerOverlay;
	@Inject private PrayerHighlightOverlay prayerHighlightOverlay;
	@Inject private VengeanceTextOverlay vengeanceTextOverlay;
	@Inject private SkullResizeOverlay skullResizeOverlay;

	@Inject private DebuffTrackerService debuffTracker;
	@Inject private CombatFocusService combatFocus;
	@Inject private Hooks hooks;
	@SuppressWarnings("deprecation") // RenderableDrawListener is the API EntityHider uses; RenderCallback is newer
	private Hooks.RenderableDrawListener focusListener;

	@Inject private ClientToolbar clientToolbar;
	@Inject private PvpEnhancerPanel panel;
	@Inject private DevPanel devPanel;
	@Inject private EventBus eventBus;
	private NavigationButton navButton;
	private NavigationButton devNavButton;
	private boolean devNavAdded;

	/**
	 * Non-rendered overlay used purely to carry this plugin's reference so the panel's config
	 * button can open the RuneLite config via {@link OverlayMenuClicked}. ConfigPlugin resolves
	 * the target config from {@code overlay.getPlugin()}, so this must be constructed with
	 * {@code super(this)}. It is never added to the OverlayManager.
	 */
	private final Overlay configAnchor = new Overlay(this)
	{
		@Override
		public Dimension render(Graphics2D graphics)
		{
			return null;
		}
	};

	private final OverlayMenuEntry configMenuEntry =
		new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "PvP Enhancer");

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

	/** Local player's total Hitpoints XP last seen (-1 = not yet baselined), for hit prediction. */
	private int previousHpXp = -1;

	/** Per-actor tick until which they stay "involved" in a fight (combat-focus persistence). */
	private final Map<Actor, Integer> focusInvolvedUntil = new HashMap<>();

	/** Names of players currently fighting you (target + attackers), for SELF_AND_OPPONENTS scope. */
	private final Set<String> currentOpponents = new HashSet<>();

	/** The actor the local player is fighting (last targeted while in combat); flashed when not retaliating. */
	private Actor combatOpponent;

	/** Local player's special-attack energy last tick (-1 = not baselined), for spec-combo detection. */
	private int previousSpecialEnergy = -1;

	/** Players whose native skull we hid (setSkullIcon(-1)) → their original skull id, for restore. */
	private final Map<Player, Integer> hiddenSkulls = new HashMap<>();

	/** Players whose native Vengeance overhead text we keep clearing → epoch-ms to stop clearing. */
	private final Map<Player, Long> vengClearUntil = new HashMap<>();

	/** How long (ms) to keep clearing a player's native veng text after first seen (covers its lifetime). */
	private static final long VENG_CLEAR_MS = 3000L;

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
		overlayManager.add(hitPredictOverlay);
		overlayManager.add(debuffTimerOverlay);
		overlayManager.add(prayerHighlightOverlay);
		overlayManager.add(vengeanceTextOverlay);
		overlayManager.add(skullResizeOverlay);

		panel.setOnOpenConfig(() -> eventBus.post(new OverlayMenuClicked(configMenuEntry, configAnchor)));
		panel.setOnOpenDevPanel(() ->
		{
			if (devNavButton != null)
			{
				clientToolbar.openPanel(devNavButton);
			}
		});

		navButton = NavigationButton.builder()
			.tooltip("PvP Enhancer")
			.icon(buildIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		devNavButton = NavigationButton.builder()
			.tooltip("PvP Enhancer — Developer")
			.icon(buildDevIcon())
			.priority(8)
			.panel(devPanel)
			.build();

		applyDevMode(config.developerMode());
		registerFocusListener();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(heartbeatOverlay);
		overlayManager.remove(notRetaliatingOverlay);
		overlayManager.remove(comboFeedbackOverlay);
		overlayManager.remove(healOverlay);
		overlayManager.remove(hitPredictOverlay);
		overlayManager.remove(debuffTimerOverlay);
		overlayManager.remove(prayerHighlightOverlay);
		overlayManager.remove(vengeanceTextOverlay);
		overlayManager.remove(skullResizeOverlay);
		restoreAllSkulls();
		unregisterFocusListener();
		clientToolbar.removeNavigation(navButton);
		if (devNavAdded)
		{
			clientToolbar.removeNavigation(devNavButton);
			devNavAdded = false;
		}
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
		hitPredictOverlay.clear();
		debuffTracker.clear();
		vengeanceTextOverlay.clear();
		vengClearUntil.clear();
		restoreAllSkulls();
		skullResizeOverlay.clear();
		combatFocus.clear();
		focusInvolvedUntil.clear();
		currentOpponents.clear();
		combatOpponent = null;
		previousSpecialEnergy = -1;
		notRetaliatingOverlay.setOpponent(null);
		previousEquipment = null;
		previousOverheads.clear();
		previousLocalHp = -1;
		previousHealthRatio.clear();
		previousHpXp = -1;
	}

	@SuppressWarnings("deprecation") // EntityHider uses the same hook; the RenderCallback replacement is newer
	private void registerFocusListener()
	{
		focusListener = combatFocus::shouldDraw;
		hooks.registerRenderableDrawListener(focusListener);
	}

	@SuppressWarnings("deprecation")
	private void unregisterFocusListener()
	{
		if (focusListener != null)
		{
			hooks.unregisterRenderableDrawListener(focusListener);
			focusListener = null;
		}
	}

	/**
	 * Recomputes which actors are involved in the current fight and updates the focus
	 * service so the render hook hides everyone else.
	 *
	 * <p>Involvement is persistent: an actor stamped as engaged stays involved for
	 * {@code combatFocusTimeout} ticks after their last attack/interaction. This keeps your
	 * target (and other participants) visible while they eat, pause, or stop fighting back,
	 * and turns focus off only once everyone has been quiet for the timeout. The local
	 * player is always kept visible while focus is active.
	 */
	private void detectCombatFocus(int tick)
	{
		CombatFocusMode mode = config.combatFocusMode();
		if (mode == CombatFocusMode.OFF)
		{
			focusInvolvedUntil.clear();
			combatFocus.clear();
			return;
		}

		int until = tick + Math.max(1, config.combatFocusTimeout());
		Player local = client.getLocalPlayer();

		// Stamp actors engaged THIS tick.
		if (mode == CombatFocusMode.SELF)
		{
			if (local != null)
			{
				Actor target = local.getInteracting();
				if (target != null)
				{
					focusInvolvedUntil.put(target, until); // who you are fighting
				}
				for (Player p : client.getTopLevelWorldView().players())
				{
					if (p != null && p.getInteracting() == local)
					{
						focusInvolvedUntil.put(p, until); // who is fighting you
					}
				}
			}
		}
		else // ANY_FIGHT
		{
			// PvP-only: a player counts as fighting only when interacting with ANOTHER PLAYER.
			// Counting any interaction (PvE, following) made bystanders who teleport in stay
			// visible — they were "involved" just by interacting with an NPC/each other.
			for (Player p : client.getTopLevelWorldView().players())
			{
				if (p == null)
				{
					continue;
				}
				Actor target = p.getInteracting();
				if (target instanceof Player)
				{
					focusInvolvedUntil.put(p, until);
					focusInvolvedUntil.put(target, until);
				}
			}
		}

		// Drop expired, then build the involved set from what remains.
		focusInvolvedUntil.values().removeIf(expiry -> expiry < tick);
		Set<Actor> involved = new HashSet<>(focusInvolvedUntil.keySet());

		// In SELF mode focus is only active while YOU are in a fight (you stay involved via
		// your target/attacker). If nothing is involved, focus is off.
		boolean active = !involved.isEmpty();
		if (active && local != null)
		{
			involved.add(local); // never hide yourself
		}
		else
		{
			involved = java.util.Collections.emptySet();
		}
		combatFocus.update(active, involved);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("pvpenhancer".equals(event.getGroup()) && "developerMode".equals(event.getKey()))
		{
			applyDevMode(config.developerMode());
		}
	}

	/** Adds/removes the developer sidebar panel + the panel's dev button to match the config toggle. */
	private void applyDevMode(boolean enabled)
	{
		if (enabled && !devNavAdded)
		{
			clientToolbar.addNavigation(devNavButton);
			devNavAdded = true;
		}
		else if (!enabled && devNavAdded)
		{
			clientToolbar.removeNavigation(devNavButton);
			devNavAdded = false;
		}
		panel.setDevButtonVisible(enabled);
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

	/** Builds the developer-panel navigation icon (a purple wrench-ish glyph) in code. */
	private static BufferedImage buildDevIcon()
	{
		int size = 24;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(0xC7, 0x7D, 0xFF));
		g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(6, 18, 16, 8);          // handle
		g.fillOval(13, 4, 7, 7);           // head
		g.fillOval(4, 15, 6, 6);           // grip
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

		// 1. Update combat engagement + current opponent (any actor, incl. NPCs for testing)
		updateCombatOpponent(local, tick);
		updateCurrentOpponents(local);

		// 2. Inventory snapshot for combo-failed detection
		int[] invIds = inventoryIds();
		int[] invQty = inventoryQuantities();

		// 3. Detect gear swaps (also feeds combo detector)
		detectGearSwaps(tick);

		// 4. Detect prayer changes
		detectPrayerChanges();

		// 4b. Detect healing (overlay only — near the healer's health bar)
		detectHeals();

		// 4b2. Vengeance overhead text resize (replaces the native text with a scaled copy)
		detectVengeance(local);

		// 4b3. PK skull resize (hides the native skull, redraws scaled)
		detectSkullResize(local);

		// 4c. Combat focus — hide non-involved entities
		detectCombatFocus(tick);

		// 4d. Count down debuff timers + update the predictive prayer highlight
		debuffTracker.tick();
		updatePrayerHighlight(local);

		// 4e. Flash the opponent if in combat but not attacking it
		boolean notRetaliating = config.showNotRetaliating() && combatState.isNotRetaliating(tick);
		notRetaliatingOverlay.setOpponent(notRetaliating ? combatOpponent : null);

		// 5. Detect combos (before flush so ComboEvents land in this tick)
		if (config.showCombos())
		{
			detectSpecialUse(tick);
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
		if (animation == -1)
		{
			return;
		}
		if (animation == EAT_ANIMATION)
		{
			// Opponent eating: the local player's eating is captured more precisely via the
			// menu click (with the item name), so only emit here for OTHER players to avoid
			// double-counting. The item is unknown for remote players, hence a generic label.
			if (!attacker.isLocalPlayer())
			{
				history.addEvent(new EatEvent(attacker.getName(), "food"));
			}
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

			String localName = localPlayerName();
			HitDirection direction = attacker.isLocalPlayer() ? HitDirection.OUTGOING
				: localName.equals(attack.getTarget()) ? HitDirection.INCOMING : HitDirection.OTHER;

			hitSummary.addAttack(
				history.getLastSequence(),
				attack.getAttacker(),
				attack.getStyle(),
				attack.getTarget(),
				attack.getTargetPrayer() != null ? PrayerNames.label(attack.getTargetPrayer()) : null,
				offenPray,
				direction
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

		// Spec-combo: count hitsplats landing on the opponent this tick.
		if (config.showCombos() && actor != null && actor == combatOpponent)
		{
			comboDetector.onOpponentHit(tick);
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

	/**
	 * Predicts the local player's outgoing damage from the Hitpoints XP drop. The XP is
	 * granted at attack time — before ranged/magic projectiles land — so the number appears
	 * on the target ahead of the hitsplat.
	 */
	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.HITPOINTS)
		{
			return;
		}
		int xp = event.getXp();
		if (previousHpXp >= 0 && xp > previousHpXp)
		{
			// Gaining Hitpoints XP means the local player dealt damage — so this also marks
			// "in combat" (fixes combat state/focus triggering only when you TAKE a hit).
			combatState.recordCombatActivity(client.getTickCount());

			if (config.hitPrediction())
			{
				int damage = XpDamage.fromHitpointsXp(xp - previousHpXp);
				Player local = client.getLocalPlayer();
				Actor target = local != null ? local.getInteracting() : null;
				if (damage > 0 && target != null)
				{
					hitPredictOverlay.addPrediction(target, damage);
				}
			}
		}
		previousHpXp = xp;
	}

	/**
	 * While in combat, de-prioritises the ground-item "Take" option so a left-click walks
	 * instead of picking up (which would break your attack). "Take" remains on right-click.
	 */
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.swapPickupInCombat() || !combatState.isInCombat(client.getTickCount()))
		{
			return;
		}
		MenuEntry entry = event.getMenuEntry();
		if ("Take".equals(entry.getOption()) && isGroundItemAction(entry.getType()))
		{
			entry.setDeprioritized(true);
		}
	}

	private static boolean isGroundItemAction(MenuAction type)
	{
		switch (type)
		{
			case GROUND_ITEM_FIRST_OPTION:
			case GROUND_ITEM_SECOND_OPTION:
			case GROUND_ITEM_THIRD_OPTION:
			case GROUND_ITEM_FOURTH_OPTION:
			case GROUND_ITEM_FIFTH_OPTION:
				return true;
			default:
				return false;
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

			for (Map.Entry<EquipmentInventorySlot, Integer> entry : current.entrySet())
			{
				int currentId = entry.getValue();
				int previousId = previousEquipment.getOrDefault(entry.getKey(), -1);
				if (currentId != previousId)
				{
					swapCount++;
					String itemName = currentId > 0 ? itemName(currentId) : "(nothing)";
					history.addEvent(new GearSwapEvent(player, entry.getKey().name(), currentId, itemName));
				}
			}

			if (config.showCombos())
			{
				comboDetector.onGearSwapCount(swapCount);
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
				// Only show this remote player's heal if they are in scope (opponent vs everyone).
				if (prev != null && ratio > prev && mode.matches(false, currentOpponents.contains(name)))
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

	/**
	 * Detects freeze/snare/teleblock by matching the spot-anim applied to a tracked player against
	 * the {@link SpotanimDebuffs} seed. Unknown spot-anims are debug-logged for live collection.
	 */
	@Subscribe
	@SuppressWarnings("deprecation") // Actor#getGraphic returns the current spot-anim id (seed-collection path)
	public void onGraphicChanged(GraphicChanged event)
	{
		if (config.debuffTimers() == DebuffScope.OFF)
		{
			return;
		}
		Actor actor = event.getActor();
		if (!(actor instanceof Player))
		{
			return;
		}
		int spotanim = actor.getGraphic();
		if (spotanim == -1)
		{
			return;
		}
		SpotanimDebuffs.Entry entry = SpotanimDebuffs.lookup(spotanim);
		if (entry == null)
		{
			log.debug("Unknown spot-anim {} on {}", spotanim, actor.getName());
			return;
		}

		// The seeded ids are CAST graphics, which play on the caster — so the debuff lands on
		// whoever the caster is targeting (the sufferer), not on the caster. Fall back to the
		// bearer when there is no interaction target.
		Actor sufferer = actor.getInteracting() != null ? actor.getInteracting() : actor;
		if (!isInDebuffScope(sufferer))
		{
			return;
		}

		// Teleblock is halved (~2.5 min) when the target had Protect from Magic up as it landed.
		int duration = entry.durationTicks;
		if (entry.debuff == Debuff.TELEBLOCK && sufferer instanceof Player
			&& ((Player) sufferer).getOverheadIcon() == HeadIcon.MAGIC)
		{
			duration = entry.durationTicks / 2;
		}

		log.debug("Debuff {} (spot-anim {}, {}t) by {} -> {}", entry.debuff, spotanim,
			duration, actor.getName(), sufferer.getName());
		debuffTracker.apply(sufferer, entry.debuff, duration);
	}

	/**
	 * Re-suppresses the native overheads we replace (skull, Vengeance text) on every client frame.
	 * The client re-applies them on each appearance update — far more often than the game tick —
	 * so suppressing only once per game tick lets them blink back (the flicker seen in combat).
	 */
	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (config.skullScope() != OverheadScope.OFF && !hiddenSkulls.isEmpty())
		{
			for (Player p : hiddenSkulls.keySet())
			{
				if (p != null && p.getSkullIcon() != SkullIcon.NONE)
				{
					p.setSkullIcon(SkullIcon.NONE);
				}
			}
		}

		if (!vengClearUntil.isEmpty())
		{
			long now = System.currentTimeMillis();
			for (Iterator<Map.Entry<Player, Long>> it = vengClearUntil.entrySet().iterator(); it.hasNext(); )
			{
				Map.Entry<Player, Long> e = it.next();
				if (e.getKey() == null || e.getValue() < now)
				{
					it.remove();
					continue;
				}
				String text = e.getKey().getOverheadText();
				if (text != null && text.toLowerCase().contains("vengeance"))
				{
					e.getKey().setOverheadText("");
				}
			}
		}
	}

	/** Drops debuff timers + skull state for a player who leaves the scene (e.g. teleports away). */
	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		Player p = event.getPlayer();
		debuffTracker.remove(p);
		hiddenSkulls.remove(p);
		vengClearUntil.remove(p);
	}

	/** Drops debuff timers for an NPC that leaves the scene. */
	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		debuffTracker.remove(event.getNpc());
	}

	// ─── Helpers ─────────────────────────────────────────────────────────

	/**
	 * Tracks the local player's combat engagement and current opponent. {@code engaged} is true
	 * whenever you are targeting any actor (player or NPC); the opponent reference is the last
	 * actor you targeted and is kept while in combat so the not-retaliating flash knows whom to
	 * highlight after you stop attacking. Works for NPCs too (e.g. testing on a guard).
	 */
	private void updateCombatOpponent(Player local, int tick)
	{
		if (local == null)
		{
			combatState.setEngaged(false, tick);
			combatOpponent = null;
			return;
		}
		Actor interacting = local.getInteracting();
		if (interacting != null)
		{
			combatOpponent = interacting;
			combatState.setEngaged(true, tick);
		}
		else
		{
			combatState.setEngaged(false, tick);
		}
		// Forget the opponent once combat has fully lapsed.
		if (!combatState.isInCombat(tick))
		{
			combatOpponent = null;
		}
	}

	/**
	 * Feeds the combo detector a "special used" event when the local player's special-attack
	 * energy drops between ticks. A drop = a special was just performed (e.g. AGS, MSB spec).
	 */
	private void detectSpecialUse(int tick)
	{
		int energy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (previousSpecialEnergy >= 0 && energy < previousSpecialEnergy)
		{
			comboDetector.onSpecialUsed(tick);
		}
		previousSpecialEnergy = energy;
	}

	/**
	 * Detects Vengeance overhead text on in-scope players, queues a scaled copy, and clears the
	 * native text (the only way to "resize" it — the native size is not exposed by the API).
	 */
	private void detectVengeance(Player local)
	{
		OverheadScope scope = config.vengTextScope();
		if (scope == OverheadScope.OFF)
		{
			return;
		}
		for (Player p : client.getTopLevelWorldView().players())
		{
			if (p == null)
			{
				continue;
			}
			String text = p.getOverheadText();
			if (text == null || !text.toLowerCase().contains("vengeance"))
			{
				continue;
			}
			if (!isInOverheadScope(p, local, scope))
			{
				continue;
			}
			if (vengClearUntil.containsKey(p))
			{
				p.setOverheadText(""); // already handling this cast; just keep it cleared
				continue;
			}
			vengeanceTextOverlay.add(p, text);
			vengClearUntil.put(p, System.currentTimeMillis() + VENG_CLEAR_MS);
			p.setOverheadText(""); // hide the native text; we draw a scaled copy
		}
	}

	/**
	 * Resizes the regular PK skull: hides the native skull on in-scope players and feeds them to
	 * {@link SkullResizeOverlay} to be re-drawn scaled. Players that drop out of scope (or when the
	 * feature is off) have their original skull restored. Only {@link SkullIcon#SKULL} is handled.
	 *
	 * <p>Hacky by nature (we mutate other players' client-side skull state); if a skull naturally
	 * expired while hidden we cannot tell, so a stale skull could briefly show — acceptable in
	 * active PvP where skulls persist far longer than a fight.
	 */
	private void detectSkullResize(Player local)
	{
		OverheadScope scope = config.skullScope();
		List<Player> draw = new ArrayList<>();
		if (scope != OverheadScope.OFF)
		{
			for (Player p : client.getTopLevelWorldView().players())
			{
				if (p == null || !isInOverheadScope(p, local, scope))
				{
					continue;
				}
				int icon = p.getSkullIcon();
				if (icon == SkullIcon.SKULL)
				{
					hiddenSkulls.putIfAbsent(p, icon);
					p.setSkullIcon(SkullIcon.NONE);
					draw.add(p);
				}
				else if (icon == SkullIcon.NONE && hiddenSkulls.containsKey(p))
				{
					draw.add(p); // we already hid it; keep drawing while in scope
				}
			}
		}

		// Restore any previously-hidden player we are no longer drawing.
		for (Iterator<Map.Entry<Player, Integer>> it = hiddenSkulls.entrySet().iterator(); it.hasNext(); )
		{
			Map.Entry<Player, Integer> e = it.next();
			if (!draw.contains(e.getKey()))
			{
				if (e.getKey() != null)
				{
					e.getKey().setSkullIcon(e.getValue());
				}
				it.remove();
			}
		}
		skullResizeOverlay.setTargets(draw);
	}

	/** Restores every skull we hid back to its original id. */
	private void restoreAllSkulls()
	{
		for (Map.Entry<Player, Integer> e : hiddenSkulls.entrySet())
		{
			if (e.getKey() != null)
			{
				e.getKey().setSkullIcon(e.getValue());
			}
		}
		hiddenSkulls.clear();
	}

	/** Scope test for overhead-element features (Vengeance text, PK skull). */
	private boolean isInOverheadScope(Player p, Player local, OverheadScope scope)
	{
		boolean isSelf = p == local;
		boolean isOpponent = !isSelf && p.getName() != null
			&& currentOpponents.contains(Text.removeTags(p.getName()));
		return scope.matches(isSelf, isOpponent);
	}

	/** Recomputes the set of players currently fighting the local player (target + attackers). */
	private void updateCurrentOpponents(Player local)
	{
		currentOpponents.clear();
		if (local == null)
		{
			return;
		}
		Actor target = local.getInteracting();
		if (target instanceof Player && target.getName() != null)
		{
			currentOpponents.add(Text.removeTags(target.getName()));
		}
		for (Player p : client.getTopLevelWorldView().players())
		{
			if (p != null && p.getName() != null && p.getInteracting() == local)
			{
				currentOpponents.add(Text.removeTags(p.getName()));
			}
		}
	}

	/** Pushes the current target's equipped-weapon style to the predictive prayer highlight. */
	private void updatePrayerHighlight(Player local)
	{
		if (!config.prayerHighlight() || local == null || !(local.getInteracting() instanceof Player))
		{
			prayerHighlightOverlay.setTargetStyle(AttackStyle.UNKNOWN);
			return;
		}
		prayerHighlightOverlay.setTargetStyle(weaponStyleOf((Player) local.getInteracting()));
	}

	/** Resolves a player's equipped-weapon style, logging unknown weapon ids for live collection. */
	private AttackStyle weaponStyleOf(Player player)
	{
		PlayerComposition comp = player.getPlayerComposition();
		if (comp == null)
		{
			return AttackStyle.UNKNOWN;
		}
		int weaponItemId = comp.getEquipmentId(KitType.WEAPON);
		if (weaponItemId <= 0)
		{
			return AttackStyle.UNKNOWN; // unarmed / non-item slot
		}
		AttackStyle style = WeaponStyleMap.styleOf(weaponItemId);
		if (style == AttackStyle.UNKNOWN)
		{
			log.debug("Unknown weapon id {} on {}", weaponItemId, player.getName());
		}
		return style;
	}

	/** True if debuff timers should be tracked for this actor under the configured scope. */
	private boolean isInDebuffScope(Actor actor)
	{
		boolean isSelf = actor == client.getLocalPlayer();
		boolean isOpponent = !isSelf && actor.getName() != null
			&& currentOpponents.contains(Text.removeTags(actor.getName()));
		return config.debuffTimers().matches(isSelf, isOpponent);
	}

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
		if (combatant.isLocalPlayer())
		{
			return true;
		}
		if (config.trackScope() == TrackScope.EVERYONE)
		{
			return true;
		}
		// SELF_AND_OPPONENTS: only players currently in combat with you.
		return currentOpponents.contains(combatant.getName());
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
