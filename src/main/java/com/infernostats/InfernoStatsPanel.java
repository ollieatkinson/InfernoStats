package com.infernostats;

import com.infernostats.model.Wave;
import com.infernostats.view.*;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Getter(AccessLevel.PACKAGE)
public class InfernoStatsPanel extends PluginPanel {
	private WaveStatsPanel waveStatsPanel;

	private final TitlePanel titlePanel = new TitlePanel();
	private final WaveSplitsPanel waveSplitsPanel = new WaveSplitsPanel();
	private final WaveListPanel waveListPanel = new WaveListPanel();
	private final WaveListContainer waveListContainer = new WaveListContainer(waveListPanel);
	private final JButton currentLos = new JButton("Current LoS");

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	private InfernoStatsPanel(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		this.plugin = plugin;
		this.config = config;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		currentLos.setEnabled(false);
		currentLos.setVisible(config.url() == InfernoStatsConfig.URL.INFERNO_TIPS);
		currentLos.setToolTipText("Enter the Inferno to capture current positions");
		currentLos.addActionListener(e -> plugin.openCurrentLos());
		add(currentLos, 0);
		add(titlePanel, BorderLayout.NORTH, 1);
		add(waveSplitsPanel, BorderLayout.CENTER, 2);
		add(waveListContainer, BorderLayout.SOUTH, 3);

		waveSplitsPanel.setWaves(this.plugin.getWaves());
	}

	void refreshLinks(boolean canCapture) {
		SwingUtilities.invokeLater(() -> {
			currentLos.setVisible(config.url() == InfernoStatsConfig.URL.INFERNO_TIPS);
			currentLos.setEnabled(canCapture);
			currentLos.setToolTipText(canCapture ? "Open your current player, monster and pillar positions"
				: "Enter the Inferno to capture current positions");
			for (Component child : waveListPanel.getComponents()) ((WaveStatsPanel) child).update();
			revalidate();
			repaint();
		});
	}

	void updateCurrentLos(boolean canCapture) {
		SwingUtilities.invokeLater(() -> {
			currentLos.setEnabled(canCapture);
			currentLos.setToolTipText(canCapture ? "Open your current player, monster and pillar positions"
				: "Enter the Inferno to capture current positions");
		});
	}

	void AddWave(Wave wave) {
		SwingUtilities.invokeLater(() ->
		{
			waveStatsPanel = new WaveStatsPanel(config, wave);
			waveListPanel.add(waveStatsPanel, 0);

			updateUI();
		});
	}

	void UpdateWave() {
		if (waveStatsPanel == null)
			return;

		SwingUtilities.invokeLater(() -> waveStatsPanel.update());
	}

	public void ClearWaves() {
		SwingUtilities.invokeLater(() -> {
			waveStatsPanel = null;
			waveListPanel.removeAll();
			waveListPanel.revalidate();
			waveListPanel.repaint();
		});
	}
}
