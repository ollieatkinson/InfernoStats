# Inferno Stats

A plugin to give you in-depth stats about your inferno attempt.

**Wave Tool** chooses where clicking an existing wave row opens its saved spawns. Line of Sight remains the default, and Trainer remains available. Select **Inferno Tips** to use [los.inferno.tips](https://los.inferno.tips/).

With Inferno Tips selected, **Current LoS** opens the player's current tile, living regular monsters and standing pillars when clicked. Clicking a wave row opens the recorded spawns using the existing wave capture flow, with player/pillar state recorded at wave start. Wave rows, timing statistics and the copy-splits action keep their existing layout.

Both actions reuse the coordinate-array URL format (`mager=[[1,5]]`), with optional capture kind, wave, player, pillar and NPC-index metadata for Inferno Tips. The website generates compact IL2 codes when users share a position; the plugin needs no binary encoder or separate wave recorder. Other destinations retain their original URL format. Scene reads use the client thread; pillar scans happen on a wave start or explicit Current LoS click.

This integration supports the existing five regular types: bats, blobs, meleers, rangers and magers. Nibblers, bloblets, Jad and Zuk are not exported; Inferno Tips shows that limitation. Saved Fight Caves and waves 67–69 cannot open in this destination. Current links omit a wave number rather than assume one after mid-wave entry. Attack cooldowns and dig timers are not captured.

The coordinate reader is adapted from [inferno-los-plugin](https://github.com/ollieatkinson/inferno-los-plugin). Its MIT notice is in `src/main/resources/META-INF/licenses/inferno-los-MIT.txt` and ships with the JAR. Mockito is test-only; no runtime dependency is added.
