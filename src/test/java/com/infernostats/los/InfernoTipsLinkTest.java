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
    public static class TestConfig implements InfernoStatsConfig {
        @Override public long tzhaarDuration() { return 0; }
        @Override public com.infernostats.controller.TimerHandler.TimerState tzhaarTimerState() {
            return com.infernostats.controller.TimerHandler.TimerState.STOPPED;
        }
        @Override public void tzhaarDuration(long duration) {}
        @Override public void tzhaarTimerState(com.infernostats.controller.TimerHandler.TimerState state) {}
    }
    @Test public void configRoutesExistingWaveRowsWithoutChangingLegacyUrls() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Wave wave=new Wave(63,0);wave.setLocation(Location.INFERNO);
            wave.getWaveNpcs().add(new WaveNpc(InfernoNpc.MAGER,new WorldPoint(2258,5353,0),42));
            wave.setLosPlayer(new net.runelite.api.Point(16,5));wave.setLosPillars(5);
            InfernoStatsConfig.URL[] selected={InfernoStatsConfig.URL.LINE_OF_SIGHT};
            InfernoStatsConfig config=new TestConfig(){@Override public URL url(){return selected[0];}};
            WaveStatsPanel panel=new WaveStatsPanel(config,wave);
            String legacy="https://infernostats.github.io/inferno.html?mager=[[1,5]]&copyable";
            assertEquals(legacy,panel.generateURL());
            selected[0]=InfernoStatsConfig.URL.INFERNO_TIPS;
            String url=panel.generateURL();
            assertTrue(url.contains("kind=wave&wave=63&"));assertTrue(url.contains("mager=%5B%5B1%2C5%5D%5D&"));
            assertTrue(url.contains("magerIds=%5B42%5D&"));assertFalse(url.contains("IL2"));
            try {Path file=Path.of("build","fixtures","inferno-tips-spawn-url.txt");Files.createDirectories(file.getParent());Files.writeString(file,url);}
            catch(java.io.IOException e){throw new AssertionError(e);}
            selected[0]=InfernoStatsConfig.URL.TRAINER;
            assertEquals("https://www.infernotrainer.com/?mager=[[1,5]]&copyable",panel.generateURL());
            selected[0]=InfernoStatsConfig.URL.LINE_OF_SIGHT;assertEquals(legacy,panel.generateURL());
        });
    }
    @Test public void unsupportedWaveLinksAreUnavailableOnlyForInfernoTips() {
        Wave jad=new Wave(67,0);jad.setLocation(Location.INFERNO);
        assertNull(LosLinks.wave(InfernoStatsConfig.URL.INFERNO_TIPS,jad));
        Wave caves=new Wave(1,0);caves.setLocation(Location.FIGHT_CAVES);
        assertNull(LosLinks.wave(InfernoStatsConfig.URL.INFERNO_TIPS,caves));
        assertNotNull(LosLinks.wave(InfernoStatsConfig.URL.LINE_OF_SIGHT,caves));
    }
}
