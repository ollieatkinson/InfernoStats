package com.infernostats.los;

import com.infernostats.InfernoStatsConfig.URL;
import com.infernostats.model.InfernoNpc;
import com.infernostats.model.Location;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveNpc;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Point;

/** Reuses the existing coordinate-array format for both saved spawns and current positions. */
public final class LosLinks {
    private LosLinks() {}

    public static String wave(URL tool, Wave wave) {
        if (tool == URL.INFERNO_TIPS && (wave.getLocation() != Location.INFERNO || wave.getId() >= 67)) return null;
        return build(tool, wave.getWaveNpcs(), wave.getLosPlayer(), wave.getLosPillars(), wave.getId(), "wave");
    }

    public static String current(URL tool, List<WaveNpc> npcs, Point player, int pillars) {
        return build(tool, npcs, player, pillars, null, "current");
    }

    private static String build(URL tool, List<WaveNpc> npcs, Point player, Integer pillars, Integer wave, String kind) {
        // The wave list can grow on the client thread while the sidebar reads it.
        List<WaveNpc> snapshot = new ArrayList<>(npcs);
        boolean tips = tool == URL.INFERNO_TIPS;
        boolean indices = snapshot.stream().allMatch(n -> n.getIndex() != null);
        StringBuilder url = new StringBuilder(tool.base);
        if (tips) {
            url.append("source=inferno-stats&location=INFERNO&kind=").append(kind).append('&');
            if (wave != null) url.append("wave=").append(wave).append('&');
            if (player != null) parameter(url, "player", Arrays.asList(player.getX(), player.getY()).toString(), true);
            if (pillars != null) parameter(url, "pillars", Arrays.asList((pillars & 1) != 0, (pillars & 2) != 0, (pillars & 4) != 0).toString(), true);
        }
        for (InfernoNpc type : InfernoNpc.values()) {
            List<WaveNpc> group = snapshot.stream().filter(n -> n.getType() == type).collect(Collectors.toList());
            if (group.isEmpty()) continue;
            List<List<Integer>> tiles = group.stream()
                .map(n -> Arrays.asList(n.getSpawn().getX() - 17, 46 - n.getSpawn().getY()))
                .collect(Collectors.toList());
            parameter(url, type.urlParam, tiles.toString(), tips);
            if (tips && indices) parameter(url, type.urlParam + "Ids", group.stream().map(WaveNpc::getIndex).collect(Collectors.toList()).toString(), true);
        }
        return url.append("copyable").toString();
    }

    private static void parameter(StringBuilder url, String name, String value, boolean escape) {
        value = value.replaceAll("\\s", "");
        url.append(name).append('=').append(escape ? URLEncoder.encode(value, StandardCharsets.UTF_8) : value).append('&');
    }
}
