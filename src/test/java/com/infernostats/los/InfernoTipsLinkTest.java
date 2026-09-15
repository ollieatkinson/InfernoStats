package com.infernostats.los;

import com.infernostats.view.WaveStatsPanel;
import java.util.Collections;
import static org.junit.Assert.*;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.model.InfernoNpc;
import com.infernostats.model.Location;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveNpc;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class InfernoTipsLinkTest {
    private static class TestConfig implements InfernoStatsConfig {
        @Override public long tzhaarDuration() { return 0; }
        @Override public com.infernostats.controller.TimerHandler.TimerState tzhaarTimerState() {
            return com.infernostats.controller.TimerHandler.TimerState.STOPPED;
        }
        @Override public void tzhaarDuration(long duration) {}
        @Override public void tzhaarTimerState(com.infernostats.controller.TimerHandler.TimerState state) {}
    }
    @Test public void configRoutesBothNewAndPreviouslyRenderedWaves() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Wave wave = new Wave(63, 0);
            wave.setLocation(Location.INFERNO);
            wave.getWaveNpcs().add(new WaveNpc(InfernoNpc.MAGER, new WorldPoint(2258, 5353, 0)));
            InfernoStatsConfig.URL[] selected = {InfernoStatsConfig.URL.LINE_OF_SIGHT};
            InfernoStatsConfig config = new TestConfig() {
                @Override public URL url() { return selected[0]; }
            };
            WaveStatsPanel panel = new WaveStatsPanel(config, wave);
            String legacy = "https://infernostats.github.io/inferno.html?mager=[[1,5]]&copyable";
            assertEquals(InfernoStatsConfig.URL.LINE_OF_SIGHT, new TestConfig() {}.url());
            assertEquals(legacy, panel.generateURL());
            selected[0] = InfernoStatsConfig.URL.INFERNO_TIPS;
            panel.update();
            assertNull(panel.generateURL()); // Never fall back to a random wave or guessed player/pillars.
            assertFalse(panel.getSpawnLos().isEnabled());
            Snapshot captured = new Snapshot("wave", 63, new int[]{16, 5}, new boolean[]{true, false, true},
                Collections.singletonList(new Snapshot.Mob(42, NpcKind.MAGER, 1, 5)), Collections.emptyList());
            wave.setLosSnapshot(captured);
            panel.update();
            assertTrue(panel.getSpawnLos().isEnabled());
            String url = panel.generateURL();
            assertTrue(url.startsWith("https://los.inferno.tips/#IL2-"));
            assertEquals(captured.toUrl(config.url().base), url);
            try {
                Path fixture = Path.of("build", "fixtures", "inferno-tips-spawn-url.txt");
                Files.createDirectories(fixture.getParent());
                Files.writeString(fixture, url);
            } catch (java.io.IOException e) { throw new AssertionError(e); }
            selected[0] = InfernoStatsConfig.URL.TRAINER;
            assertEquals("https://www.infernotrainer.com/?mager=[[1,5]]&copyable", panel.generateURL());
            selected[0] = InfernoStatsConfig.URL.LINE_OF_SIGHT;
            assertEquals(legacy, panel.generateURL());
        });
    }
}
