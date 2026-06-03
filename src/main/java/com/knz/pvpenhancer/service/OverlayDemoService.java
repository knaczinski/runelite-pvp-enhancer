package com.knz.pvpenhancer.service;

import com.knz.pvpenhancer.model.ComboResult;
import com.knz.pvpenhancer.model.ComboTier;
import com.knz.pvpenhancer.model.ComboType;
import com.knz.pvpenhancer.model.Debuff;
import com.knz.pvpenhancer.overlay.ComboFeedbackOverlay;
import com.knz.pvpenhancer.overlay.HealOverlay;
import com.knz.pvpenhancer.overlay.HitPredictOverlay;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

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

	private final List<DemoScenario> scenarios = new ArrayList<>();

	@Inject
	OverlayDemoService(Client client, ClientThread clientThread, HealOverlay healOverlay,
		HitPredictOverlay hitPredictOverlay, DebuffTrackerService debuffTracker,
		ComboFeedbackOverlay comboFeedbackOverlay)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.healOverlay = healOverlay;
		this.hitPredictOverlay = hitPredictOverlay;
		this.debuffTracker = debuffTracker;
		this.comboFeedbackOverlay = comboFeedbackOverlay;
		build();
	}

	private void build()
	{
		add("Healing", "Heal +12", () -> healOverlay.addHeal(local(), 12, false));
		add("Healing", "Heal ~30 (est)", () -> healOverlay.addHeal(local(), 30, true));

		add("Hit predict", "Predict 24", () -> hitPredictOverlay.addPrediction(local(), 24));
		add("Hit predict", "Predict 40", () -> hitPredictOverlay.addPrediction(local(), 40));

		add("Debuff timer", "Freeze (Barrage 33t)", () -> debuffTracker.apply(local(), Debuff.FREEZE, 33));
		add("Debuff timer", "Bind (16t)", () -> debuffTracker.apply(local(), Debuff.SNARE, 16));
		add("Debuff timer", "Teleblock (500t)", () -> debuffTracker.apply(local(), Debuff.TELEBLOCK, 500));

		add("Combo popup", "Godlike switch",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.GODLIKE_SWITCH, ComboTier.GODLIKE, "GODLIKE SWITCH")));
		add("Combo popup", "Triple eat",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.TRIPLE_EAT, ComboTier.SUCCESS, "TRIPLE EAT")));
		add("Combo popup", "Spec combo",
			() -> comboFeedbackOverlay.showCombo(new ComboResult(ComboType.SPEC_COMBO, ComboTier.GODLIKE, "SPEC COMBO")));
	}

	private void add(String group, String label, Runnable action)
	{
		scenarios.add(new DemoScenario(group, label, action));
	}

	public List<DemoScenario> getScenarios()
	{
		return scenarios;
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
