package com.knz.pvpenhancer.overlay;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.PrayerNames;
import com.knz.pvpenhancer.service.HitSummaryService;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Hit Summary overlay — one row per attack in tabular format:
 * Tick · Player · Offen.Pray · Attack · Target · Target Prayer · Hit
 *
 * <p>Offen.Pray is only filled for the local player (RuneLite cannot observe opponents'
 * offensive prayers). Hit is filled when the {@link com.knz.pvpenhancer.service.AttackHitsplatCorrelator}
 * supplies a matching hitsplat (may remain "-" for a few ticks). Rows are newest-first.
 */
public class HitSummaryOverlay extends OverlayPanel
{
	private static final Color COLOR_HEADER = Color.YELLOW;
	private static final Color COLOR_ROW_A = Color.WHITE;
	private static final Color COLOR_ROW_B = new Color(0xCCCCCC);
	private static final Color COLOR_HIT_MISS = new Color(0x88AAFF);
	private static final String PENDING = "-";

	private final PvpEnhancerConfig config;
	private final HitSummaryService hitSummary;

	@Inject
	HitSummaryOverlay(PvpEnhancerConfig config, HitSummaryService hitSummary)
	{
		this.config = config;
		this.hitSummary = hitSummary;
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showHitSummary())
		{
			return null;
		}

		List<HitSummaryRow> rows = hitSummary.getRows();
		if (rows.isEmpty())
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(340, 0));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Hit Summary")
			.color(COLOR_HEADER)
			.build());

		// Column header
		panelComponent.getChildren().add(LineComponent.builder()
			.left(header())
			.leftColor(COLOR_HEADER)
			.build());

		boolean alt = false;
		for (HitSummaryRow row : rows)
		{
			Color rowColor = alt ? COLOR_ROW_B : COLOR_ROW_A;
			alt = !alt;
			String hitStr = row.hit != null ? String.valueOf(row.hit) : PENDING;
			Color hitColor = row.hit != null ? damageColor(row.hit) : COLOR_HIT_MISS;

			panelComponent.getChildren().add(LineComponent.builder()
				.left(rowText(row))
				.leftColor(rowColor)
				.right(hitStr)
				.rightColor(hitColor)
				.build());
		}

		return super.render(graphics);
	}

	private static String header()
	{
		return "Tick  Player  OffPray  Atk  Target  TgtPray";
	}

	private static String rowText(HitSummaryRow row)
	{
		return String.format("%04d  %s  %s  %s  %s  %s",
			row.tickSequence,
			truncate(row.player, 8),
			truncate(row.offensivePrayer != null ? row.offensivePrayer : "-", 6),
			row.style.getLabel().substring(0, Math.min(3, row.style.getLabel().length())),
			truncate(row.target, 8),
			truncate(row.targetPrayer != null ? row.targetPrayer : "-", 8));
	}

	private static Color damageColor(int amount)
	{
		if (amount == 0)
		{
			return new Color(0x88AAFF); // blocked — blue
		}
		if (amount >= 30)
		{
			return new Color(0xFF6B6B); // high damage — red
		}
		return Color.WHITE;
	}

	private static String truncate(String s, int max)
	{
		if (s == null)
		{
			return "-";
		}
		return s.length() > max ? s.substring(0, max) : s;
	}
}
