package com.knz.pvpenhancer.panel;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.CombatEvent;
import com.knz.pvpenhancer.model.EventCategory;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.TickEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * RuneLite sidebar panel for the PvP Enhancer. Hosts the data that used to be rendered as
 * on-screen overlays — the tick history and the hit summary — plus a combat status header.
 *
 * <p>Rebuilt on each game tick via {@link #update}. The plugin snapshots the service data
 * on the client thread and hands it here on the Swing EDT, so this panel never touches the
 * live services concurrently.
 */
public class PvpEnhancerPanel extends PluginPanel
{
	private static final Color COLOR_TICK = Color.LIGHT_GRAY;
	private static final Color COLOR_COMBAT = new Color(0xFF6B6B);
	private static final Color COLOR_EATING = new Color(0x6BCB77);
	private static final Color COLOR_GEAR = new Color(0x4D96FF);
	private static final Color COLOR_PRAYER = new Color(0xFFD93D);
	private static final Color COLOR_MUTED = new Color(0x9E, 0x9E, 0x9E);
	private static final Font LINE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	private final PvpEnhancerConfig config;

	private final JLabel statusLabel = new JLabel();
	private final JPanel tickList = new JPanel();
	private final JPanel hitList = new JPanel();

	@Inject
	PvpEnhancerPanel(PvpEnhancerConfig config)
	{
		this.config = config;
		setBorder(new EmptyBorder(8, 8, 8, 8));
		setLayout(new BorderLayout());

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel.setFont(LINE_FONT.deriveFont(Font.BOLD, 12f));
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(statusLabel);
		container.add(verticalGap(8));

		container.add(sectionHeader("Tick History"));
		tickList.setLayout(new BoxLayout(tickList, BoxLayout.Y_AXIS));
		tickList.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tickList.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(tickList);

		container.add(verticalGap(10));

		container.add(sectionHeader("Hit Summary"));
		hitList.setLayout(new BoxLayout(hitList, BoxLayout.Y_AXIS));
		hitList.setBackground(ColorScheme.DARK_GRAY_COLOR);
		hitList.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(hitList);

		add(container, BorderLayout.NORTH);

		renderEmpty();
	}

	/**
	 * Rebuilds the panel from a tick-history + hit-summary snapshot. Must be called on the
	 * Swing EDT (the plugin uses {@code SwingUtilities.invokeLater}).
	 *
	 * @param entries          tick history, newest-first (rendered oldest-first)
	 * @param rows             hit summary rows, newest-first
	 * @param inCombat         current combat state
	 * @param notRetaliating   whether the not-attacking condition is active
	 */
	public void update(List<TickEntry> entries, List<HitSummaryRow> rows,
		boolean inCombat, boolean notRetaliating)
	{
		// Status header
		if (notRetaliating)
		{
			statusLabel.setText("NOT ATTACKING — re-click target");
			statusLabel.setForeground(COLOR_COMBAT);
		}
		else if (inCombat)
		{
			statusLabel.setText("In combat");
			statusLabel.setForeground(COLOR_EATING);
		}
		else
		{
			statusLabel.setText("Idle");
			statusLabel.setForeground(COLOR_MUTED);
		}

		// Tick history (oldest-first), honouring per-category display filters
		tickList.removeAll();
		boolean anyTick = false;
		for (int i = entries.size() - 1; i >= 0; i--)
		{
			TickEntry entry = entries.get(i);
			boolean headerWritten = false;
			for (CombatEvent event : entry.getEvents())
			{
				if (!categoryEnabled(event.getCategory()))
				{
					continue;
				}
				if (!headerWritten)
				{
					tickList.add(line(String.format("Tick %04d", entry.getSequence()), COLOR_TICK));
					headerWritten = true;
				}
				Color c = event.getColor() != null ? event.getColor() : categoryColor(event.getCategory());
				tickList.add(line("  " + event.format(), c));
				anyTick = true;
			}
		}
		if (!anyTick)
		{
			tickList.add(line("No events yet.", COLOR_MUTED));
		}

		// Hit summary (newest-first)
		hitList.removeAll();
		if (!config.showHitSummary() || rows.isEmpty())
		{
			hitList.add(line(config.showHitSummary() ? "No hits yet." : "(disabled)", COLOR_MUTED));
		}
		else
		{
			for (HitSummaryRow row : rows)
			{
				hitList.add(line(formatHitRow(row), COLOR_TICK));
			}
		}

		revalidate();
		repaint();
	}

	private void renderEmpty()
	{
		statusLabel.setText("Idle");
		statusLabel.setForeground(COLOR_MUTED);
		tickList.add(line("No events yet.", COLOR_MUTED));
		hitList.add(line("No hits yet.", COLOR_MUTED));
	}

	private boolean categoryEnabled(EventCategory category)
	{
		switch (category)
		{
			case COMBAT:
				return config.showCombat();
			case EATING:
				return config.showEating();
			case GEAR_SWAP:
				return config.showGearSwap();
			case PRAYER:
				return config.showPrayer();
			case COMBO:
				return config.showCombos();
			default:
				return true;
		}
	}

	private static Color categoryColor(EventCategory category)
	{
		switch (category)
		{
			case COMBAT:
				return COLOR_COMBAT;
			case EATING:
				return COLOR_EATING;
			case GEAR_SWAP:
				return COLOR_GEAR;
			case PRAYER:
				return COLOR_PRAYER;
			default:
				return Color.WHITE;
		}
	}

	/** Compact one-line hit summary suited to the narrow sidebar. */
	private static String formatHitRow(HitSummaryRow row)
	{
		String hit = row.hit != null ? String.valueOf(row.hit) : "-";
		String style = row.style.getLabel();
		String line = String.format("%04d %s→%s %s %s",
			row.tickSequence,
			abbrev(row.player, 6),
			abbrev(row.target, 6),
			style.substring(0, Math.min(3, style.length())),
			hit);
		if (row.targetPrayer != null)
		{
			line += " (" + row.targetPrayer + ")";
		}
		return line;
	}

	private static String abbrev(String s, int max)
	{
		if (s == null)
		{
			return "?";
		}
		return s.length() > max ? s.substring(0, max) : s;
	}

	private static JLabel line(String text, Color color)
	{
		JLabel label = new JLabel(text);
		label.setFont(LINE_FONT);
		label.setForeground(color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static Component verticalGap(int height)
	{
		JPanel gap = new JPanel();
		gap.setBackground(ColorScheme.DARK_GRAY_COLOR);
		gap.setPreferredSize(new Dimension(1, height));
		gap.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		gap.setAlignmentX(Component.LEFT_ALIGNMENT);
		return gap;
	}

	private static JLabel sectionHeader(String text)
	{
		JLabel header = new JLabel(text);
		header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
		header.setForeground(Color.WHITE);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setBorder(new EmptyBorder(0, 0, 4, 0));
		return header;
	}
}
