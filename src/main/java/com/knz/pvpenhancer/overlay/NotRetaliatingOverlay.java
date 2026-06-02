package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.service.CombatStateService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Prominent warning shown when the local player is in combat but has stopped attacking
 * (disengaged for >= 2 ticks — walked away, looted, or clicked elsewhere).
 *
 * <p>Clears the moment the player re-targets the opponent. The warning panel deliberately
 * uses high-contrast colours so it is noticeable mid-fight without requiring the player
 * to look away from the action.
 */
public class NotRetaliatingOverlay extends OverlayPanel
{
	private static final Color COLOR_WARN = new Color(0xFF6B6B);
	private static final Color COLOR_HINT = new Color(0xFFD93D);

	private final Client client;
	private final PvpEnhancerConfig config;
	private final CombatStateService combatState;

	@Inject
	NotRetaliatingOverlay(Client client, PvpEnhancerConfig config, CombatStateService combatState)
	{
		this.client = client;
		this.config = config;
		this.combatState = combatState;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showNotRetaliating())
		{
			return null;
		}
		if (!combatState.isNotRetaliating(client.getTickCount()))
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(160, 0));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("NOT ATTACKING")
			.color(COLOR_WARN)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Re-click your target")
			.leftColor(COLOR_HINT)
			.build());

		return super.render(graphics);
	}
}
