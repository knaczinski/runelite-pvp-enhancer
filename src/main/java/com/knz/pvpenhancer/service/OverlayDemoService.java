package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.ComboResult;
import com.knz.pvpenhancer.model.ComboTier;
import com.knz.pvpenhancer.model.ComboType;
import com.knz.pvpenhancer.model.Debuff;
import com.knz.pvpenhancer.overlay.ComboFeedbackOverlay;
import com.knz.pvpenhancer.overlay.HealOverlay;
import com.knz.pvpenhancer.overlay.HitPredictOverlay;
import com.knz.pvpenhancer.overlay.VengeanceTextOverlay;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry of overlay "demo" scenarios for the developer panel. Each scenario is a
 * named, one-click mock that fires an overlay on the local player so the look/behaviour can be
 * verified without a live fight. This is the single, standardised place that documents — for
 * both code and human audit — what every demoable overlay can be triggered with.
 *
 * <p>Scenarios run on the client thread (overlays mutate client-thread state). v1 covers the
 * floating popups: healing, hit prediction, debuff timers, and combo feedback.
 */
@Singleton
public class OverlayDemoService
{
	/** One named, one-click mock for an overlay. */
	public static final class DemoScenario
	{
		private final String group;
		private final String label;
		private final Runnable action;

		DemoScenario(String group, String label, Runnable action)
		{
			this.group = group;
			this.label = label;
			this.action = action;
		}

		/** Overlay this scenario belongs to (the panel groups by this). */
		public String getGroup()
		{
			return group;
		}

		/** Button label. */
		public String getLabel()
		{
			return label;
		}
	}

	private final Client client;
	private final ClientThread clientThread;
	private final HealOverlay healOverlay;
	private final HitPredictOverlay hitPredictOverlay;
	private final DebuffTrackerService debuffTracker;
	private final ComboFeedbackOverlay comboFeedbackOverlay;
	private final VengeanceTextOverlay vengeanceTextOverlay;
	private final PidGuessService pidGuess;

	private final List<DemoScenario> scenarios = new ArrayList<>();

	@Inject
	OverlayDemoService(Client client, ClientThread clientThread, HealOverlay healOverlay,
		HitPredictOverlay hitPredictOverlay, DebuffTrackerService debuffTracker,
		ComboFeedbackOverlay comboFeedbackOverlay, VengeanceTextOverlay vengeanceTextOverlay,
		PidGuessService pidGuess)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.healOverlay = healOverlay;
		this.hitPredictOverlay = hitPredictOverlay;
		this.debuffTracker = debuffTracker;
		this.comboFeedbackOverlay = comboFeedbackOverlay;
		this.vengeanceTextOverlay = vengeanceTextOverlay;
		this.pidGuess = pidGuess;
		build();
	}

	private void build()
	{
		add("Healing", "Heal +12", () -> healOverlay.addHeal(local(), 12, false));
		add("Healing", "Heal ~30 (est)", () -> healOverlay.addHeal(local(), 30, true));

		add("Hit predict", "Predict 24", () -> hitPredictOverlay.addPrediction(local(), 24, false));
		add("Hit predict", "Predict 40 (cue)", () -> hitPredictOverlay.addPrediction(local(), 40, true));

		add("Debuff timer", "Freeze (Barrage 33t)", () -> debuffTracker.apply(local(), Debuff.FREEZE, 33));
		add("Debuff timer", "Bind (16t)", () -> debuffTracker.apply(local(), Debuff.SNARE, 16));
		add("Debuff timer", "Teleblock (500t)", () -> debuffTracker.apply(local(), Debuff.TELEBLOCK, 500));

		add("Combo popup", "Godlike switch",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.GODLIKE_SWITCH, ComboTier.GODLIKE, "GODLIKE SWITCH")));
		add("Combo popup", "Triple eat",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.TRIPLE_EAT, ComboTier.SUCCESS, "TRIPLE EAT")));
		add("Combo popup", "Spec combo",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.SPEC_COMBO, ComboTier.GODLIKE, "SPEC COMBO")));

		add("PID guess", "PID: you", () -> pidGuess.forceGuess(PidGuessService.Pid.LOCAL));
		add("PID guess", "PID: them", () -> pidGuess.forceGuess(PidGuessService.Pid.OPPONENT));
		add("PID guess", "Swap warning", pidGuess::triggerSwapWarning);
	}

	private void add(String group, String label, Runnable action)
	{
		scenarios.add(new DemoScenario(group, label, action));
	}

	public List<DemoScenario> getScenarios()
	{
		return scenarios;
	}

	/**
	 * Clears all transient debuff timers and floating overlays (heal, hit predict, combo, veng).
	 * Skull resize is managed per game tick, so it is intentionally not cleared here. Runs on the
	 * client thread.
	 */
	public void clearAll()
	{
		clientThread.invoke(() ->
		{
			debuffTracker.clear();
			healOverlay.clear();
			hitPredictOverlay.clear();
			comboFeedbackOverlay.clear();
			vengeanceTextOverlay.clear();
			pidGuess.reset();
		});
	}

	private static final Logger log = LoggerFactory.getLogger(OverlayDemoService.class);

	/**
	 * Dumps (to the client log) which toplevel layout is active + its widget tree, plus the combat
	 * tab — on demand from the dev panel, so it's captured in the exact state with no timing/gating
	 * ambiguity. Used to design the fixed-layout-in-resizable feature + tune the spec-bar resize.
	 */
	public void dumpWidgetTrees()
	{
		clientThread.invoke(() ->
		{
			log.info("=== PvP Enhancer widget dump ===");
			log.info("viewport off=({},{}) size={}x{}", client.getViewportXOffset(), client.getViewportYOffset(),
				client.getViewportWidth(), client.getViewportHeight());
			int[] groups = {548, 161, 164};
			String[] names = {"FIXED(548)", "RESIZABLE_CLASSIC(161)", "RESIZABLE_MODERN(164)"};
			for (int i = 0; i < groups.length; i++)
			{
				Widget root = client.getWidget(groups[i], 0);
				log.info("layout {} -> {}", names[i], root == null ? "absent" : "PRESENT bounds=" + root.getBounds());
				if (root != null)
				{
					dumpTree(root, 0);
				}
			}
			// Combat tab (spec bar) — group 593.
			Widget combatRoot = client.getWidget(593, 0);
			if (combatRoot != null)
			{
				log.info("--- combat tab (593) ---");
				dumpTree(combatRoot, 0);
			}
		});
	}

	private void dumpTree(Widget w, int depth)
	{
		if (w == null || depth > 2)
		{
			return;
		}
		log.info("[d{}] id={} bounds={} oy={} oh={}", depth, w.getId(), w.getBounds(), w.getOriginalY(), w.getOriginalHeight());
		Widget[] kids = w.getChildren();
		if (kids != null)
		{
			for (Widget k : kids)
			{
				dumpTree(k, depth + 1);
			}
		}
	}

	/** Fires a scenario on the client thread (no-op if not logged in). */
	public void trigger(DemoScenario scenario)
	{
		clientThread.invoke(() ->
		{
			if (client.getLocalPlayer() != null)
			{
				scenario.action.run();
			}
		});
	}

	private Actor local()
	{
		return client.getLocalPlayer();
	}
}
