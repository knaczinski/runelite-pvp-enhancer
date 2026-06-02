package com.knz.pvpenhancer.panel;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.CombatEvent;
import com.knz.pvpenhancer.model.EventCategory;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.model.TickLogFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * RuneLite sidebar panel for the PvP Enhancer.
 *
 * <p>Hosts the data that used to be on-screen overlays (tick history + hit summary) plus a
 * combat status header. Each block also carries its own inline config controls — the
 * tick-history filters/depth and the hit-summary toggle/row-cap live here, not in the
 * RuneLite config panel (those items are {@code hidden = true} in the config). The controls
 * read/write the config via {@link ConfigManager}, so everything stays persisted and in sync.
 *
 * <p>Rebuilt each game tick: the plugin snapshots service data on the client thread and
 * calls {@link #update} on the Swing EDT. Interactive controls are created once and never
 * rebuilt.
 */
public class PvpEnhancerPanel extends PluginPanel
{
	private static final String GROUP = "pvpenhancer";

	private static final Color COLOR_TICK = Color.LIGHT_GRAY;
	private static final Color COLOR_COMBAT = new Color(0xFF6B6B);
	private static final Color COLOR_EATING = new Color(0x6BCB77);
	private static final Color COLOR_GEAR = new Color(0x4D96FF);
	private static final Color COLOR_PRAYER = new Color(0xFFD93D);
	private static final Color COLOR_MUTED = new Color(0x9E, 0x9E, 0x9E);
	private static final Font LINE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	private final PvpEnhancerConfig config;
	private final ConfigManager configManager;

	private final JLabel statusLabel = new JLabel();
	private final JPanel tickList = new JPanel();
	private final JPanel hitList = new JPanel();

	// Inline config controls (created once)
	private final JSpinner maxTicksSpinner;
	private final JCheckBox combatCheck;
	private final JCheckBox eatingCheck;
	private final JCheckBox gearCheck;
	private final JCheckBox prayerCheck;
	private final JCheckBox comboCheck;
	private final JCheckBox hitSummaryCheck;
	private final JSpinner hitRowsSpinner;

	// Cached snapshot from the last update(), so a control toggle re-renders instantly.
	private List<TickEntry> lastEntries = Collections.emptyList();
	private List<HitSummaryRow> lastRows = Collections.emptyList();
	private boolean lastInCombat;
	private boolean lastNotRetaliating;

	/** Guards programmatic control updates from re-triggering their own listeners. */
	private boolean suppressEvents;

	@Inject
	PvpEnhancerPanel(PvpEnhancerConfig config, ConfigManager configManager)
	{
		this.config = config;
		this.configManager = configManager;
		setBorder(new EmptyBorder(8, 8, 8, 8));
		setLayout(new BorderLayout());

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel.setFont(LINE_FONT.deriveFont(Font.BOLD, 12f));
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(statusLabel);
		container.add(gap(8));

		// ── Tick History block: header + inline controls + list ──
		container.add(sectionHeader("Tick History"));

		maxTicksSpinner = makeIntSpinner(config.maxHistoryTicks(), 0, 5000, "maxHistoryTicks");
		container.add(spinnerRow("Max ticks", maxTicksSpinner));

		combatCheck = makeCheck("Combat", "showCombat", config.showCombat());
		eatingCheck = makeCheck("Eat", "showEating", config.showEating());
		gearCheck = makeCheck("Gear", "showGearSwap", config.showGearSwap());
		prayerCheck = makeCheck("Prayer", "showPrayer", config.showPrayer());
		comboCheck = makeCheck("Combo", "showCombos", config.showCombos());
		JPanel filters = new JPanel(new GridLayout(0, 2, 0, 0));
		filters.setBackground(ColorScheme.DARK_GRAY_COLOR);
		filters.setAlignmentX(Component.LEFT_ALIGNMENT);
		filters.add(combatCheck);
		filters.add(eatingCheck);
		filters.add(gearCheck);
		filters.add(prayerCheck);
		filters.add(comboCheck);
		container.add(filters);

		container.add(makeCopyButton());
		container.add(gap(4));

		configList(tickList);
		container.add(tickList);

		container.add(gap(12));

		// ── Hit Summary block: header + inline controls + list ──
		container.add(sectionHeader("Hit Summary"));

		hitSummaryCheck = makeCheck("Enabled", "showHitSummary", config.showHitSummary());
		hitSummaryCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(hitSummaryCheck);

		hitRowsSpinner = makeIntSpinner(config.hitSummaryRows(), 0, 100, "hitSummaryRows");
		container.add(spinnerRow("Max rows", hitRowsSpinner));
		container.add(gap(4));

		configList(hitList);
		container.add(hitList);

		add(container, BorderLayout.NORTH);

		rebuild();
	}

	/**
	 * Rebuilds the panel from a tick-history + hit-summary snapshot. Must run on the EDT.
	 */
	public void update(List<TickEntry> entries, List<HitSummaryRow> rows,
		boolean inCombat, boolean notRetaliating)
	{
		this.lastEntries = entries;
		this.lastRows = rows;
		this.lastInCombat = inCombat;
		this.lastNotRetaliating = notRetaliating;
		syncControls();
		rebuild();
	}

	// ─── Rendering ──────────────────────────────────────────────────────────

	private void rebuild()
	{
		// Status header
		if (lastNotRetaliating)
		{
			statusLabel.setText("NOT ATTACKING — re-click target");
			statusLabel.setForeground(COLOR_COMBAT);
		}
		else if (lastInCombat)
		{
			statusLabel.setText("In combat");
			statusLabel.setForeground(COLOR_EATING);
		}
		else
		{
			statusLabel.setText("Idle");
			statusLabel.setForeground(COLOR_MUTED);
		}

		// Tick history, oldest-first, honouring the inline category filters
		tickList.removeAll();
		boolean anyTick = false;
		for (int i = lastEntries.size() - 1; i >= 0; i--)
		{
			TickEntry entry = lastEntries.get(i);
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

		// Hit summary, newest-first
		hitList.removeAll();
		if (!config.showHitSummary())
		{
			hitList.add(line("(disabled)", COLOR_MUTED));
		}
		else if (lastRows.isEmpty())
		{
			hitList.add(line("No hits yet.", COLOR_MUTED));
		}
		else
		{
			for (HitSummaryRow row : lastRows)
			{
				hitList.add(line(formatHitRow(row), COLOR_TICK));
			}
		}

		revalidate();
		repaint();
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

	private static String formatHitRow(HitSummaryRow row)
	{
		String hit = row.hit != null ? String.valueOf(row.hit) : "-";
		String style = row.style.getLabel();
		String text = String.format("%04d %s→%s %s %s",
			row.tickSequence,
			abbrev(row.player, 6),
			abbrev(row.target, 6),
			style.substring(0, Math.min(3, style.length())),
			hit);
		if (row.targetPrayer != null)
		{
			text += " (" + row.targetPrayer + ")";
		}
		return text;
	}

	private static String abbrev(String s, int max)
	{
		if (s == null)
		{
			return "?";
		}
		return s.length() > max ? s.substring(0, max) : s;
	}

	/** "Copy log" button — copies the full tick history to the system clipboard. */
	private JComponent makeCopyButton()
	{
		JButton button = new JButton("Copy log");
		button.setFont(LINE_FONT);
		button.setFocusable(false);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		button.addActionListener(e ->
		{
			String log = TickLogFormatter.format(lastEntries);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(log), null);
			button.setText("Copied!");
			Timer revert = new Timer(1200, ev -> button.setText("Copy log"));
			revert.setRepeats(false);
			revert.start();
		});
		return button;
	}

	// ─── Inline control factories ───────────────────────────────────────────

	private JCheckBox makeCheck(String label, String key, boolean initial)
	{
		JCheckBox cb = new JCheckBox(label, initial);
		cb.setFont(LINE_FONT);
		cb.setForeground(Color.WHITE);
		cb.setBackground(ColorScheme.DARK_GRAY_COLOR);
		cb.setFocusable(false);
		cb.addActionListener(e ->
		{
			if (suppressEvents)
			{
				return;
			}
			configManager.setConfiguration(GROUP, key, cb.isSelected());
			rebuild();
		});
		return cb;
	}

	private JSpinner makeIntSpinner(int value, int min, int max, String key)
	{
		int clamped = Math.max(min, Math.min(max, value));
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(clamped, min, max, 1));
		spinner.setMaximumSize(new Dimension(70, 22));
		spinner.setPreferredSize(new Dimension(70, 22));
		spinner.addChangeListener(e ->
		{
			if (suppressEvents)
			{
				return;
			}
			configManager.setConfiguration(GROUP, key, spinner.getValue());
			rebuild();
		});
		return spinner;
	}

	/** Re-reads the config into the controls (e.g. if changed elsewhere). Guarded. */
	private void syncControls()
	{
		suppressEvents = true;
		combatCheck.setSelected(config.showCombat());
		eatingCheck.setSelected(config.showEating());
		gearCheck.setSelected(config.showGearSwap());
		prayerCheck.setSelected(config.showPrayer());
		comboCheck.setSelected(config.showCombos());
		hitSummaryCheck.setSelected(config.showHitSummary());
		maxTicksSpinner.setValue(clamp(config.maxHistoryTicks(), 0, 5000));
		hitRowsSpinner.setValue(clamp(config.hitSummaryRows(), 0, 100));
		suppressEvents = false;
	}

	// ─── Small UI helpers ─────────────────────────────────────────────────────

	private static int clamp(int v, int min, int max)
	{
		return Math.max(min, Math.min(max, v));
	}

	private static void configList(JPanel list)
	{
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.setBackground(ColorScheme.DARK_GRAY_COLOR);
		list.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	private static JComponent spinnerRow(String label, JSpinner spinner)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel l = new JLabel(label + ": ");
		l.setFont(LINE_FONT);
		l.setForeground(COLOR_MUTED);
		row.add(l);
		row.add(spinner);
		row.add(javax.swing.Box.createHorizontalGlue());
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		return row;
	}

	private static JLabel line(String text, Color color)
	{
		JLabel label = new JLabel(text);
		label.setFont(LINE_FONT);
		label.setForeground(color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static Component gap(int height)
	{
		JPanel g = new JPanel();
		g.setBackground(ColorScheme.DARK_GRAY_COLOR);
		g.setPreferredSize(new Dimension(1, height));
		g.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		g.setAlignmentX(Component.LEFT_ALIGNMENT);
		return g;
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
