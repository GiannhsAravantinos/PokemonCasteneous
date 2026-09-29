package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pokemoncasteneous.utils.IndexUtils;

import java.util.Arrays;
import java.util.List;

public final class OverworldMap {
    private final TileCatalog tileCatalog;
    private final Tile[][] tiles;
    private final int width;
    private final int height;

    public OverworldMap(String fileName) {
        tileCatalog = TileCatalog.load("tiles/tiles.csv");
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) throw new IllegalStateException("Missing overworld map: " + fileName);

        List<String[]> rows = Arrays.stream(file.readString().split("\\R"))
                .map(String::trim)
                .filter(row -> !row.isEmpty() && !row.startsWith("#"))
                .map(row -> row.split("\\|", -1))
                .toList();
        if (rows.isEmpty()) throw new IllegalStateException("Overworld map is empty: " + fileName);

        width = rows.get(0).length;
        height = rows.size();
        if (width == 0) throw new IllegalStateException("Map rows must contain tile IDs: " + fileName);

        tiles = new Tile[width][height];
        IndexUtils.indices(height).forEach(row -> {
            if (rows.get(row.value()).length != width) {
                throw new IllegalStateException("Inconsistent map row width at row " + row.value());
            }
        });
        IndexUtils.cartesianIndices(width, height).forEach(index -> {
            int row = index.y();
            int column = index.x();
            try {
                String encodedId = rows.get(row)[column].trim();
                if (!encodedId.matches("[0-9A-Fa-f]{2}")) {
                    throw new IllegalArgumentException("Expected a two-digit hexadecimal ID");
                }
                int id = Integer.parseInt(encodedId, 16);
                Tile tile = tileCatalog.tileById(id);
                if (tile == null || !tile.isOverworld()) {
                    throw new IllegalArgumentException(String.format("Tile %02X is not defined for the overworld", id));
                }
                tiles[column][height - row - 1] = tile;
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Invalid tile ID at row " + row + ", column " + column, exception);
            }
        });
    }

    public int width() { return width; }
    public int height() { return height; }
    public Tile tileAt(int x, int y) { return tiles[x][y]; }
    public TileCatalog tileCatalog() { return tileCatalog; }
    public Tile tileDefinition(int id) { return tileCatalog.tileById(id); }
}
