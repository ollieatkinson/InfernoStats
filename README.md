# Inferno Stats

A plugin to give you in-depth stats about your inferno attempt.

The **Wave Tool** setting chooses the destination used by the existing wave links and the **Spawn LoS** buttons. Line of Sight remains the default, and Trainer remains available. Select **Inferno Tips** to use [los.inferno.tips](https://los.inferno.tips/):

- **Current LoS** captures your current player tile, living supported NPCs and standing pillars when clicked. It is available inside the Inferno and stays above the scrolling wave list.
- **Spawn LoS** opens the recorded wave start, including the player tile and pillars at that time, NPC spawn tiles and NPC indices. Those captures remain available after leaving until a new run or plugin restart. A wave without a recorded start is disabled; it never opens a randomly generated wave.

Inferno Tips receives compact `#IL2-…` share codes. The existing destinations retain their original URL format. Switching the setting updates the links for waves already in the sidebar. The existing Copy Wave Splits action still copies timing statistics.

Captures include nibblers, bloblets, Jad and Jad healers as well as the five regular monster types. Fight Caves is not supported by Inferno Tips capture. Zuk, his shield and Zuk healers are not simulated; supported adds are marked with a warning. Current captures describe positions, not observed attack cooldowns or dig timers. Capture is local and opening the browser is always a user action.

Capture and encoding code is adapted from [inferno-los-plugin](https://github.com/ollieatkinson/inferno-los-plugin); its MIT notice is in `licenses/inferno-los-MIT.txt` and included in the JAR. Mockito is used only by tests; no runtime dependency is added.
