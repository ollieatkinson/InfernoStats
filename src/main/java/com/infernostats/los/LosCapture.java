package com.infernostats.los;

import com.infernostats.events.WaveStartedEvent;
import com.infernostats.model.Location;
import com.infernostats.model.Wave;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

/** All scene reads and recorder changes run on the client thread. */
public final class LosCapture
{
    private final Client client;
    private final SceneCapture scene;
    private final WaveRecorder recorder = new WaveRecorder();
    private Wave activeWave;

    @Inject LosCapture(Client client, SceneCapture scene)
    {
        this.client = client;
        this.scene = scene;
    }

    public boolean canCapture()
    {
        return client.getGameState() == GameState.LOGGED_IN && SceneCapture.fits(scene.player(), 1);
    }

    public String currentUrl()
    {
        if (!canCapture()) return null;
        return scene.current(recorder.wave(), recorder.warnings()).toUrl("https://los.inferno.tips/");
    }

    @Subscribe(priority = 1)
    public void onWaveStartedEvent(WaveStartedEvent event)
    {
        if (event.getWave().getLocation() != Location.INFERNO || !canCapture()) return;
        activeWave = event.getWave();
        recorder.waveStarted(activeWave.getId(), client.getTickCount(), scene.player(), scene.pillars());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        if (!canCapture()) return;
        Snapshot.Mob mob = scene.mob(event.getNpc());
        if (mob != null) recorder.spawned(client.getTickCount(), mob);
    }

    @Subscribe(priority = 1)
    public void onGameTick(GameTick event)
    {
        if (!canCapture()) { leave(); return; }
        Snapshot snapshot = recorder.endTick(client.getTickCount());
        if (snapshot != null && activeWave != null) activeWave.setLosSnapshot(snapshot);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        // Loading can occur between waves. Only clear pending captures on an actual exit.
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
            leave();
    }

    private void leave()
    {
        activeWave = null;
        recorder.leave();
    }

    public void clear()
    {
        activeWave = null;
        recorder.clear();
    }
}
