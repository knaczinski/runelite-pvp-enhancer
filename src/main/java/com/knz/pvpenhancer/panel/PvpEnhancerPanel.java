package com.knz.pvpenhancer.panel;

import com.knz.pvpenhancer.PvpEnhancerConfig;
import com.knz.pvpenhancer.model.AttackStyle;
import com.knz.pvpenhancer.model.CombatEvent;
import com.knz.pvpenhancer.model.EventCategory;
import com.knz.pvpenhancer.model.HitDirection;
import com.knz.pvpenhancer.model.HitSummaryRow;
import com.knz.pvpenhancer.model.TickEntry;
import com.knz.pvpenhancer.model.TickLogFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.runelite.api.HeadIcon;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;


/**
 * RuneLite sidebar panel for the PvP Enhancer.
 *
 * <p>Hosts the tick history (scrollable, coloured by category) and the hit summary (a
 * scrollable table coloured green when you attack and red when you are attacked), each with
 * inline config controls. A header has a combat-status label and a button that opens the
 * plugin's config. Rebuilt each tick on the EDT via {@link #update}.
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
	private static final Color COLOR_OUTGOING = new Color(0x6BCB77); // you attack — green
	private static final Color COLOR_INCOMING = new Color(0xFF6B6B); // you are attacked — red
	private static final Font LINE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	private final PvpEnhancerConfig config;
	private final ConfigManager configManager;

	private final JLabel statusLabel = new JLabel();
	private final JButton devButton = new JButton("🛠");
	private final JButton configButton = new JButton("⚙");
	private final JPanel tickList = new JPanel();

	// Programmatically-drawn icons for the hit summary table
	private final Map<AttackStyle, Icon> styleIcons = new HashMap<>();
	private final Map<HeadIcon, Icon> prayerIcons = new HashMap<>();
	private static final int ICON_SIZE = 14;

	// Hit summary table
	private final DefaultTableModel hitModel = new DefaultTableModel(new Object[]{"Atk", "", "Tgt", "", "Hit"}, 0)
	{
		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
	};
	private final JTable hitTable = new JTable(hitModel);
	private final List<HitDirection> rowDirections = new ArrayList<>();

	// Inline config controls (created once)
	private final JSpinner maxTicksSpinner;
	private final JCheckBox combatCheck;
	private final JCheckBox eatingCheck;
	private final JCheckBox gearCheck;
	private final JCheckBox prayerCheck;
	private final JCheckBox comboCheck;
	private final JCheckBox hitSummaryCheck;
	private final JSpinner hitRowsSpinner;

	// Cached snapshot from the last update().
	private List<TickEntry> lastEntries = Collections.emptyList();
	private List<HitSummaryRow> lastRows = Collections.emptyList();
	private boolean lastInCombat;
	private boolean lastNotRetaliating;

	private boolean suppressEvents;
	/** Set by the plugin; opens the RuneLite config for this plugin. */
	private Runnable onOpenConfig;
	/** Set by the plugin; opens the developer panel. */
	private Runnable onOpenDevPanel;

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

		// Header: status + config button
		container.add(buildHeader());
		container.add(gap(8));

		// ── Tick History block ──
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

		tickList.setLayout(new BoxLayout(tickList, BoxLayout.Y_AXIS));
		tickList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.add(scroll(tickList, 240));

		container.add(gap(12));

		// ── Hit Summary block ──
		container.add(sectionHeader("Hit Summary"));

		hitSummaryCheck = makeCheck("Enabled", "showHitSummary", config.showHitSummary());
		hitSummaryCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(hitSummaryCheck);

		hitRowsSpinner = makeIntSpinner(config.hitSummaryRows(), 0, 100, "hitSummaryRows");
		container.add(spinnerRow("Max rows", hitRowsSpinner));
		container.add(gap(4));

		container.add(buildHitTable());

		add(container, BorderLayout.NORTH);

		initIcons();
		rebuild();
	}

	/** Builds small Java2D icons for attack styles and prayers. */
	private void initIcons()
	{
		Color swordColor = new Color(220, 50, 50);
		Color arrowColor = new Color(50, 200, 50);
		Color magicColor = new Color(60, 120, 255);
		Color prayerColor = new Color(120, 200, 255);
		Color retriColor = new Color(255, 160, 40);
		Color redeemColor = new Color(255, 100, 150);
		Color smiteColor = new Color(255, 220, 50);

		styleIcons.put(AttackStyle.MELEE, drawIcon(swordColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2.5f));
			g.drawLine(7, 1, 7, 13);
			g.drawLine(3, 5, 11, 5);
		}));

		styleIcons.put(AttackStyle.RANGED, drawIcon(arrowColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2.5f));
			g.drawLine(2, 12, 12, 2);
			g.drawLine(12, 2, 7, 2);
			g.drawLine(12, 2, 12, 7);
		}));

		styleIcons.put(AttackStyle.MAGIC, drawIcon(magicColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2.5f));
			g.drawLine(7, 1, 13, 7);
			g.drawLine(13, 7, 7, 13);
			g.drawLine(7, 13, 1, 7);
			g.drawLine(1, 7, 7, 1);
		}));

		prayerIcons.put(HeadIcon.MELEE, drawIcon(prayerColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2));
			g.drawLine(4, 2, 10, 2);
			g.drawLine(4, 2, 3, 12);
			g.drawLine(10, 2, 11, 12);
			g.drawLine(3, 12, 11, 12);
		}));

		prayerIcons.put(HeadIcon.RANGED, drawIcon(prayerColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2));
			g.drawLine(2, 12, 12, 2);
			g.drawLine(12, 2, 8, 2);
			g.drawLine(12, 2, 12, 6);
		}));

		prayerIcons.put(HeadIcon.MAGIC, drawIcon(prayerColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2));
			g.drawLine(7, 1, 13, 7);
			g.drawLine(13, 7, 7, 13);
			g.drawLine(7, 13, 1, 7);
			g.drawLine(1, 7, 7, 1);
		}));

		prayerIcons.put(HeadIcon.RETRIBUTION, drawIcon(retriColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2.5f));
			g.drawLine(3, 3, 11, 11);
			g.drawLine(3, 11, 11, 3);
		}));

		prayerIcons.put(HeadIcon.REDEMPTION, drawIcon(redeemColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2));
			g.drawOval(2, 2, 10, 10);
			g.drawLine(6, 7, 6, 10);
			g.drawLine(5, 8, 7, 8);
		}));

		prayerIcons.put(HeadIcon.SMITE, drawIcon(smiteColor, g -> {
			g.setStroke(new java.awt.BasicStroke(2.5f));
			g.drawLine(5, 2, 9, 6);
			g.drawLine(9, 6, 5, 10);
			g.drawLine(5, 10, 9, 14);
		}));
	}

	private static Icon drawIcon(Color color, java.util.function.Consumer<Graphics2D> drawer)
	{
		BufferedImage img = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		drawer.accept(g);
		g.dispose();
		return new ImageIcon(img);
	}

	/** Called by the plugin to wire the config-open action. */
	public void setOnOpenConfig(Runnable onOpenConfig)
	{
		this.onOpenConfig = onOpenConfig;
	}

	/** Called by the plugin to wire the dev-panel-open action. */
	public void setOnOpenDevPanel(Runnable onOpenDevPanel)
	{
		this.onOpenDevPanel = onOpenDevPanel;
	}

	/** Shows or hides the developer (🛠) button in the header. */
	public void setDevButtonVisible(boolean visible)
	{
		devButton.setVisible(visible);
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
		if (lastNotRetaliating)
		{
			statusLabel.setText("NOT ATTACKING");
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
		tickList.revalidate();
		tickList.repaint();

		// Hit summary table
		hitModel.setRowCount(0);
		rowDirections.clear();
		if (config.showHitSummary())
		{
			for (HitSummaryRow row : lastRows)
			{
				hitModel.addRow(new Object[]{
					abbrev(row.player, 6),
					styleIcons.getOrDefault(row.style, null),
					abbrev(row.target, 6),
					row.targetPrayer != null ? prayerIcons.get(row.targetPrayer) : null,
					row.hit != null ? String.valueOf(row.hit) : "-"
				});
				rowDirections.add(row.direction);
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

	private static String abbrev(String s, int max)
	{
		if (s == null)
		{
			return "?";
		}
		return s.length() > max ? s.substring(0, max) : s;
	}

	// ─── UI construction ──────────────────────────────────────────────────────

	private JComponent buildHeader()
	{
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

		statusLabel.setFont(LINE_FONT.deriveFont(Font.BOLD, 12f));
		header.add(statusLabel);
		header.add(Box.createHorizontalGlue());

		devButton.setFont(devButton.getFont().deriveFont(13f));
		devButton.setToolTipText("Open developer panel (overlay mocks)");
		devButton.setFocusable(false);
		devButton.setMargin(new java.awt.Insets(0, 6, 0, 6));
		devButton.setVisible(false);
		devButton.addActionListener(e ->
		{
			if (onOpenDevPanel != null)
			{
				onOpenDevPanel.run();
			}
		});
		header.add(devButton);

		configButton.setFont(configButton.getFont().deriveFont(14f));
		configButton.setToolTipText("Open PvP Enhancer config");
		configButton.setFocusable(false);
		configButton.setMargin(new java.awt.Insets(0, 6, 0, 6));
		configButton.addActionListener(e ->
		{
			if (onOpenConfig != null)
			{
				onOpenConfig.run();
			}
		});
		header.add(configButton);
		return header;
	}

	private JComponent buildHitTable()
	{
		hitTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		hitTable.setFont(LINE_FONT);
		hitTable.setRowSelectionAllowed(false);
		hitTable.setColumnSelectionAllowed(false);
		hitTable.setFocusable(false);
		hitTable.setShowGrid(false);
		hitTable.setRowHeight(16);
		hitTable.getTableHeader().setFont(LINE_FONT);
		hitTable.getTableHeader().setReorderingAllowed(false);
		hitTable.setIntercellSpacing(new Dimension(2, 0));

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column)
			{
				if (value instanceof Icon)
				{
					JLabel label = new JLabel((Icon) value);
					label.setHorizontalAlignment(SwingConstants.CENTER);
					label.setVerticalAlignment(SwingConstants.CENTER);
					HitDirection d = row >= 0 && row < rowDirections.size() ? rowDirections.get(row) : HitDirection.OTHER;
					label.setForeground(d == HitDirection.OUTGOING ? COLOR_OUTGOING
						: d == HitDirection.INCOMING ? COLOR_INCOMING : Color.WHITE);
					label.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					label.setOpaque(true);
					return label;
				}
				Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);
				HitDirection d = row >= 0 && row < rowDirections.size() ? rowDirections.get(row) : HitDirection.OTHER;
				c.setForeground(d == HitDirection.OUTGOING ? COLOR_OUTGOING
					: d == HitDirection.INCOMING ? COLOR_INCOMING : Color.WHITE);
				c.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				return c;
			}
		};
		hitTable.setDefaultRenderer(Object.class, renderer);
		// Icon columns (1, 3) are narrow; Hit column (4) narrow; names take the rest.
		hitTable.getColumnModel().getColumn(1).setMaxWidth(ICON_SIZE + 4);
		hitTable.getColumnModel().getColumn(3).setMaxWidth(ICON_SIZE + 4);
		hitTable.getColumnModel().getColumn(4).setMaxWidth(34);

		JScrollPane sp = new JScrollPane(hitTable);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(200, 170));
		sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
		sp.setAlignmentX(Component.LEFT_ALIGNMENT);
		return sp;
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

	private static JScrollPane scroll(JComponent content, int height)
	{
		JScrollPane sp = new JScrollPane(content);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(200, height));
		sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		sp.setAlignmentX(Component.LEFT_ALIGNMENT);
		sp.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		return sp;
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
		row.add(Box.createHorizontalGlue());
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
