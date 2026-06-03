package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.service.PidGuessService;
import com.knz.pvpenhancer.service.PidGuessService.Pid;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Experimental PID-guess indicator for a 1v1 involving the local player. Shows which side the
 * noisy contested-hitsplat vote currently favours ({@code PID: YOU / THEM / ?}) and flashes a
 * swap warning when the lead flips. Fed by {@link PidGuessService}; see the spike doc for why this
 * is best-effort only.
 */
public class PidIndicatorOverlay extends OverlayPanel
{
	private static final Color YOU = new Color(0x6B, 0xCB, 0x77);
	private static final Color THEM = new Color(0xFF, 0x6B, 0x6B);
	private static final Color UNKNOWN = new Color(0xBD, 0xBD, 0xBD);
	private static final Color SWAP = new Color(0xFF, 0xD9, 0x3D);

	private final PvpEnhancerConfig config;
	private final PidGuessService pid;

	@Inject
	PidIndicatorOverlay(PvpEnhancerConfig config, PidGuessService pid)
	{
		this.config = config;
		this.pid = pid;
		setPosition(OverlayPosition.TOP_CENTER);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.pidIndicator() || !pid.isActive())
		{
			return null;
		}

		Pid guess = pid.getGuess();
		String who = guess == Pid.LOCAL ? "YOU" : guess == Pid.OPPONENT ? "THEM" : "?";
		Color color = guess == Pid.LOCAL ? YOU : guess == Pid.OPPONENT ? THEM : UNKNOWN;

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("PID: " + who + " (exp)")
			.color(color)
			.build());

		if (pid.getConfidence() > 0)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("confidence")
				.right(String.valueOf(pid.getConfidence()))
				.build());
		}

		if (pid.isSwapWarningActive())
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("PID SWAP?")
				.color(SWAP)
				.build());
		}

		panelComponent.setPreferredSize(new Dimension(120, 0));
		return super.render(graphics);
	}
}
