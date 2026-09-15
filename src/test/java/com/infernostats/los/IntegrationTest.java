package com.infernostats.los;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.controller.WaveHandler;
import com.infernostats.events.WaveStartedEvent;
import com.infernostats.model.*;
import java.nio.file.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IntegrationTest {
    private static class Fixture {
        Client c; WorldView v; Scene scene; Player p; NPC n; SceneCapture capture;
        Fixture() {
        Client c=mock(Client.class);WorldView v=mock(WorldView.class);Scene scene=mock(Scene.class);
        when(c.getTopLevelWorldView()).thenReturn(v);when(c.getWorldView(-1)).thenReturn(v);when(v.getId()).thenReturn(-1);
        when(v.getBaseX()).thenReturn(1000);when(v.getBaseY()).thenReturn(2000);when(v.getSizeX()).thenReturn(104);when(v.getSizeY()).thenReturn(104);
        when(v.isInstance()).thenReturn(true);when(v.getScene()).thenReturn(scene);when(scene.getTiles()).thenReturn(new Tile[4][1][1]);
        int[][][] chunks=new int[4][13][13];
        for(int x=0;x<13;x++)for(int y=0;y<13;y++)chunks[0][x][y]=((2240/8+x)<<14)|((5312/8+y)<<3);
        when(v.getInstanceTemplateChunks()).thenReturn(chunks);
        Player p=mock(Player.class);when(c.getLocalPlayer()).thenReturn(p);when(p.getWorldView()).thenReturn(v);when(p.getWorldLocation()).thenReturn(new WorldPoint(1033,2041,0));
        NPC n=mock(NPC.class);when(n.getId()).thenReturn(7699);when(n.getName()).thenReturn("Jal-Zek");when(n.getIndex()).thenReturn(42);when(n.getWorldView()).thenReturn(v);when(n.getWorldLocation()).thenReturn(new WorldPoint(1037,2038,0));
        NPC dead=mock(NPC.class);when(dead.getId()).thenReturn(7698);when(dead.getName()).thenReturn("Jal-Xil");when(dead.isDead()).thenReturn(true);
        IndexedObjectSet<NPC> npcs=mock(IndexedObjectSet.class);doReturn(npcs).when(v).npcs();
        when(npcs.iterator()).thenAnswer(ignored->Arrays.asList(n,dead).iterator());

            when(c.getGameState()).thenReturn(GameState.LOGGED_IN);
            this.c=c;this.v=v;this.scene=scene;this.p=p;this.n=n;this.capture=new SceneCapture(c);
        }
    }
    @Test public void currentPositionsUseInstanceCoordinatesAndSkipDeadNpcs() throws Exception {
        Fixture f=new Fixture();
        assertTrue(f.capture.canCapture());
        assertEquals(new Point(16,5),f.capture.player());
        List<WaveNpc> before=f.capture.currentNpcs();assertEquals(1,before.size());
        assertEquals(Integer.valueOf(42),before.get(0).getIndex());
        assertEquals(new Point(37,38),before.get(0).getSpawn());
        when(f.n.getWorldLocation()).thenReturn(new WorldPoint(1038,2038,0));
        List<WaveNpc> after=f.capture.currentNpcs();
        assertEquals(new Point(38,38),after.get(0).getSpawn());
        assertEquals(new Point(37,38),before.get(0).getSpawn());
        String url=LosLinks.current(InfernoStatsConfig.URL.INFERNO_TIPS,after,f.capture.player(),f.capture.pillars());
        assertTrue(url.contains("kind=current"));assertFalse(url.contains("wave="));assertFalse(url.contains("IL2"));
        Path file=Path.of("build","fixtures","inferno-tips-current-url.txt");Files.createDirectories(file.getParent());Files.writeString(file,url);
        when(f.c.getLocalPlayer()).thenReturn(null);assertFalse(f.capture.canCapture());assertNull(f.capture.player());
    }
    @Test public void pillarFootprintsAndRotatedNpcChunksAreNormalized() {
        Fixture f=new Fixture();
        Tile tile=mock(Tile.class);GameObject north=mock(GameObject.class);
        when(north.getId()).thenReturn(30354);when(north.getSceneMinLocation()).thenReturn(new Point(34,39));
        when(tile.getGameObjects()).thenReturn(new GameObject[]{north});
        Tile[][][] tiles=new Tile[4][1][1];tiles[0][0][0]=tile;when(f.scene.getTiles()).thenReturn(tiles);
        assertEquals(2,f.capture.pillars());
        when(tile.getGameObjects()).thenReturn(new GameObject[0]);assertEquals(0,f.capture.pillars());
        f.v.getInstanceTemplateChunks()[0][4][5]|=1<<1;
        when(f.n.getName()).thenReturn("Jal-Xil");when(f.n.getWorldLocation()).thenReturn(new WorldPoint(1033,2041,0));
        assertArrayEquals(new int[]{19,5},SceneCapture.grid(f.capture.spawn(f.n)));
        when(f.n.getName()).thenReturn("Jal-Nib");assertNull(f.capture.spawn(f.n));
    }
    @Test public void existingWaveHandlerOwnsSpawnsAndCapturesStartContext() throws Exception {
        Fixture f=new Fixture();InfernoStatsPlugin plugin=mock(InfernoStatsPlugin.class);when(plugin.isInInferno()).thenReturn(true);
        var ctor=WaveHandler.class.getDeclaredConstructor(InfernoStatsPlugin.class,InfernoStatsConfig.class);ctor.setAccessible(true);
        WaveHandler handler=ctor.newInstance(plugin,new InfernoTipsLinkTest.TestConfig());
        var field=WaveHandler.class.getDeclaredField("sceneCapture");field.setAccessible(true);field.set(handler,f.capture);
        EventBus bus=new EventBus();bus.register(handler);
        Wave wave=new Wave(63,0);wave.setLocation(Location.INFERNO);
        bus.post(new WaveStartedEvent(wave));bus.post(new NpcSpawned(f.n));
        assertEquals(1,wave.getWaveNpcs().size());assertEquals(new Point(16,5),wave.getLosPlayer());assertEquals(Integer.valueOf(0),wave.getLosPillars());
        String before=LosLinks.wave(InfernoStatsConfig.URL.INFERNO_TIPS,wave);
        when(f.n.getWorldLocation()).thenReturn(new WorldPoint(1038,2038,0));
        wave.setDuration(3);bus.post(new NpcSpawned(f.n));
        assertEquals(before,LosLinks.wave(InfernoStatsConfig.URL.INFERNO_TIPS,wave));
        when(f.c.getLocalPlayer()).thenReturn(null);
        assertEquals(before,LosLinks.wave(InfernoStatsConfig.URL.INFERNO_TIPS,wave));
        bus.unregister(handler);
    }
}
