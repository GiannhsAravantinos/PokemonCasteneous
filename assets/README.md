# Overworld image assets

Place the player sheet in this folder and tile images in `tiles/`. The desktop Gradle run task uses this folder as its working directory. Images use nearest-neighbor filtering so pixel edges remain crisp.

| File | Dimensions | Usage |
| --- | --- | --- |
| `tiles/grass.png` | 32 x 32 | Seamless grass tile, repeated in each grid square |
| `tiles/dirt.png` | 32 x 32 | Seamless dirt path tile, repeated along the cross-path |
| `tiles/tree.png` | 32 x 48 | Transparent tree sprite; trunk centered at bottom, displayed in a 1 x 1.5 square area |
| `tiles/tiles.csv` | CSV | Tile ID, image filename, overworld flag, foreground flag, and underlay ID |
| `player.png` | 128 x 128 | Transparent player sheet, arranged as a 4 x 4 grid of 32 x 32 frames |
| `maps/overworld.map` | 16 x 20 tiles | Overworld layout, pipe-separated two-digit hexadecimal tile IDs |

Tile definitions and image filenames come from `tiles/tiles.csv`. IDs are two-digit hexadecimal values from `00` through `FF`. Set `isOverworld` to `true` for tiles usable in `maps/overworld.map`; foreground tiles specify the ground tile ID in `underlayId`. Map rows run from top to bottom, with positions separated by `|` and ordered left to right.

For `player.png`, rows from top to bottom face down, left, right, and up. The first column is the idle pose; the remaining three columns are walking frames. Keep the character's feet on the same baseline in all frames.
