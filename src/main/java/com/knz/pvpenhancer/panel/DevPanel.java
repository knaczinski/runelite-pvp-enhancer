package com.knz.pvpenhancer.panel;

import com.knz.pvpenhancer.service.OverlayDemoService;
import com.knz.pvpenhancer.service.OverlayDemoService.DemoScenario;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Developer-only sidebar panel. Lists the overlay mock scenarios from {@link OverlayDemoService},
 * grouped by overlay, each as a one-click button that fires the mock on the local player so its
 * look/behaviour can be checked without a live fight.
 */
public class DevPanel extends PluginPanel
{
	private static final Font LINE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	private final OverlayDemoService demoService;

	@Inject
	DevPanel(OverlayDemoService demoService)
	{
		this.demoService = demoService;
		setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("Overlay mocks");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
		title.setForeground(Color.WHITE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(title);

		JLabel hint = new JLabel("Fires on yourself. Log in first.");
		hint.setFont(LINE_FONT);
		hint.setForeground(new Color(0x9E, 0x9E, 0x9E));
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);
		hint.setBorder(new EmptyBorder(0, 0, 8, 0));
		container.add(hint);

		String currentGroup = null;
		for (DemoScenario scenario : demoService.getScenarios())
		{
			if (!scenario.getGroup().equals(currentGroup))
			{
				currentGroup = scenario.getGroup();
				container.add(sectionHeader(currentGroup));
			}
			container.add(scenarioButton(scenario));
		}

		add(container);
	}

	private JButton scenarioButton(DemoScenario scenario)
	{
		JButton button = new JButton(scenario.getLabel());
		button.setFont(LINE_FONT);
		button.setFocusable(false);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		button.addActionListener(e -> demoService.trigger(scenario));
		return button;
	}

	private static JLabel sectionHeader(String text)
	{
		JLabel header = new JLabel(text);
		header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
		header.setForeground(Color.WHITE);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setBorder(new EmptyBorder(8, 0, 2, 0));
		return header;
	}
}
