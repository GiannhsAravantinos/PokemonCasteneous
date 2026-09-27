# Overworld image assets

Place these PNG files directly in this `assets` folder. The desktop Gradle run task uses this folder as its working directory. Images use nearest-neighbor filtering so pixel edges remain crisp.

| File | Dimensions | Usage |
| --- | --- | --- |
| `grass.png` | 32 x 32 | Seamless grass tile, repeated in each grid square |
| `dirt.png` | 32 x 32 | Seamless dirt path tile, repeated along the cross-path |
| `tree.png` | 32 x 48 | Transparent tree sprite; trunk centered at bottom, displayed in a 1 x 1.5 square area |
| `player.png` | 128 x 128 | Transparent player sheet, arranged as a 4 x 4 grid of 32 x 32 frames |

For `player.png`, rows from top to bottom face down, left, right, and up. The first column is the idle pose; the remaining three columns are walking frames. Keep the character's feet on the same baseline in all frames.
