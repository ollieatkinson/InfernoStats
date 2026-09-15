// Adapted from ollieatkinson/inferno-los-plugin; MIT license in src/main/resources/META-INF/licenses/inferno-los-MIT.txt.
package com.infernostats.los;

import com.infernostats.model.InfernoNpc;
import com.infernostats.model.WaveNpc;
import net.runelite.api.GameState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

public final class SceneCapture
{
    static final int REGION = 9043;
    static final int[][] PILLARS = {{0, 9}, {17, 7}, {10, 23}};
    private final Client client;
    @Inject SceneCapture(Client client) { this.client = client; }

    public boolean canCapture()
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null) return false;
        LocalPoint local = LocalPoint.fromWorld(client.getLocalPlayer().getWorldView(), client.getLocalPlayer().getWorldLocation());
        return local != null && fits(grid(WorldPoint.fromLocalInstance(client, local)), 1);
    }
    static int[] grid(WorldPoint point)
    {
        if (point == null || point.getRegionID() != REGION) return null;
        return new int[]{point.getRegionX() - 17, 46 - point.getRegionY()};
    }
    static boolean fits(int[] p, int size)
    {
        return p != null && p[0] >= 0 && p[0] + size <= 29 && p[1] < 30 && p[1] - size + 1 >= 0;
    }
    public Point player()
    {
        if (!canCapture()) return null;
        int[] tile = grid(WorldPoint.fromLocalInstance(client, LocalPoint.fromWorld(client.getLocalPlayer().getWorldView(), client.getLocalPlayer().getWorldLocation())));
        return new Point(tile[0], tile[1]);
    }
    private int[] footprint(WorldView view, LocalPoint southwest, int size)
    {
        if (southwest == null) return null;
        int minX = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int dx : new int[]{0, size - 1}) for (int dy : new int[]{0, size - 1})
        {
            LocalPoint corner = LocalPoint.fromScene(southwest.getSceneX() + dx, southwest.getSceneY() + dy, view);
            int[] p = grid(WorldPoint.fromLocalInstance(client, corner, view.getPlane()));
            if (p == null) return null;
            minX = Math.min(minX, p[0]); maxY = Math.max(maxY, p[1]);
        }
        return new int[]{minX, maxY};
    }
    public WorldPoint spawn(NPC npc)
    {
        InfernoNpc type = InfernoNpc.fromName(npc.getName()).orElse(null);
        if (type == null || npc.isDead()) return null;
        int[] p = footprint(npc.getWorldView(), LocalPoint.fromWorld(npc.getWorldView(), npc.getWorldLocation()), type.size);
        return fits(p, type.size) ? WorldPoint.fromRegion(REGION, p[0] + 17, 46 - p[1], 0) : null;
    }
    public int pillars()
    {
        int present = 0;
        WorldView view = client.getTopLevelWorldView();
        if (view == null || view.getScene() == null) return present;
        Tile[][] tiles = view.getScene().getTiles()[view.getPlane()];
        for (Tile[] column : tiles) for (Tile tile : column)
        {
            if (tile == null) continue;
            for (GameObject object : tile.getGameObjects())
            {
                if (object == null || object.getId() < 30353 || object.getId() > 30355) continue;
                Point min = object.getSceneMinLocation();
                int[] p = footprint(view, LocalPoint.fromScene(min.getX(), min.getY(), view), 3);
                for (int i = 0; i < PILLARS.length; i++) if (Arrays.equals(p, PILLARS[i])) present |= 1 << i;
            }
        }
        return present;
    }
    public List<WaveNpc> currentNpcs()
    {
        List<WaveNpc> npcs = new ArrayList<>();
        if (!canCapture()) return npcs;
        for (NPC npc : client.getTopLevelWorldView().npcs())
        {
            WorldPoint point = spawn(npc);
            if (point != null) npcs.add(new WaveNpc(InfernoNpc.fromName(npc.getName()).get(), point, npc.getIndex()));
        }
        return npcs;
    }
}
