package com.infernostats.view;

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
    @Test public void exportsTheExistingSpawnFormatWithWaveAndLocation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Wave wave = new Wave(63, 0);
            wave.setLocation(Location.INFERNO);
            wave.getWaveNpcs().add(new WaveNpc(InfernoNpc.MAGER, new WorldPoint(2258, 5353, 0)));
            InfernoStatsConfig config = new TestConfig() {
                @Override public URL url() { return URL.INFERNO_TIPS; }
            };
            String url = new WaveStatsPanel(config, wave).generateURL();
            assertEquals("https://los.inferno.tips/?source=inferno-stats&wave=63&location=INFERNO&mager=%5B%5B1%2C5%5D%5D&copyable", url);
            try {
                Path fixture = Path.of("build", "fixtures", "inferno-tips-url.txt");
                Files.createDirectories(fixture.getParent());
                Files.writeString(fixture, url);
            } catch (java.io.IOException e) { throw new AssertionError(e); }
            // Existing users retain the same default website and URL format.
            InfernoStatsConfig original = new TestConfig() {};
            assertEquals(InfernoStatsConfig.URL.LINE_OF_SIGHT, original.url());
            assertEquals("https://infernostats.github.io/inferno.html?mager=[[1,5]]&copyable",
                new WaveStatsPanel(original, wave).generateURL());
        });
    }
}
