package com.infernostats;

import com.infernostats.controller.TimerHandler;
import com.infernostats.model.Location;
import com.infernostats.model.Wave;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LosPanelTest
{
    @Test public void currentActionFollowsConfigurationAndPreservesWaveRows() throws Exception
    {
        InfernoStatsPlugin plugin = mock(InfernoStatsPlugin.class);
        when(plugin.getWaves()).thenReturn(new ArrayList<>());
        InfernoStatsConfig.URL[] destination = {InfernoStatsConfig.URL.INFERNO_TIPS};
        InfernoStatsConfig config = new InfernoStatsConfig() {
            @Override public long tzhaarDuration() { return 0; }
            @Override public void tzhaarDuration(long value) {}
            @Override public TimerHandler.TimerState tzhaarTimerState() { return TimerHandler.TimerState.STOPPED; }
            @Override public void tzhaarTimerState(TimerHandler.TimerState state) {}
            @Override public URL url() { return destination[0]; }
        };
        Constructor<InfernoStatsPanel> constructor = InfernoStatsPanel.class.getDeclaredConstructor(InfernoStatsPlugin.class, InfernoStatsConfig.class);
        constructor.setAccessible(true);
        InfernoStatsPanel[] holder = new InfernoStatsPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            try { holder[0] = constructor.newInstance(plugin, config); }
            catch (Exception e) { throw new AssertionError(e); }
            assertFalse(holder[0].getCurrentLos().isEnabled());
        });
        InfernoStatsPanel panel = holder[0];
        for (int i = 1; i <= 66; i++) {
            Wave wave = new Wave(i, 0); wave.setLocation(Location.INFERNO);
            panel.AddWave(wave);
        }
        panel.refreshLinks(true);
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(panel.getCurrentLos().isVisible());
            assertTrue(panel.getCurrentLos().isEnabled());
            panel.getCurrentLos().doClick();
            verify(plugin).openCurrentLos();
            panel.getWrappedPanel().setSize(240, 650);
            layout(panel.getWrappedPanel());
            layout(panel.getWrappedPanel());
            assertTrue(panel.getCurrentLos().getHeight() >= 20);
            assertEquals(66, panel.getWaveListPanel().getComponentCount());

            assertFalse(SwingUtilities.isDescendingFrom(panel.getCurrentLos(), panel.getWaveListContainer()));
            assertFalse(java.util.Arrays.stream(((java.awt.Container)panel.getWaveListPanel().getComponent(0)).getComponents()).anyMatch(c -> c instanceof javax.swing.JButton));

            BufferedImage image = new BufferedImage(240, 650, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image.createGraphics();
            panel.getWrappedPanel().printAll(graphics); graphics.dispose();
            try {
                Path output = Path.of("build", "fixtures", "los-panel.png");
                Files.createDirectories(output.getParent()); ImageIO.write(image, "png", output.toFile());
            } catch (Exception e) { throw new AssertionError(e); }
        });
        destination[0] = InfernoStatsConfig.URL.LINE_OF_SIGHT;
        panel.refreshLinks(false);
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(panel.getCurrentLos().isVisible());
            assertFalse(panel.getCurrentLos().isEnabled());
        });
    }

    private static void layout(java.awt.Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) if (child instanceof java.awt.Container) layout((java.awt.Container) child);
    }
}
