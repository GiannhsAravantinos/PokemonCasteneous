package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OverworldMap {
    private final Map<Integer, Tile> tileCatalog;
    private final Tile[][] tiles;
    private final int width;
    private final int height;

    public OverworldMap(String fileName) {
        tileCatalog = Tile.loadCatalog("tiles/tiles.csv");
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) throw new IllegalStateException("Missing overworld map: " + fileName);

        String[] lines = file.readString().split("\\R");
        List<String[]> rows = new ArrayList<>();
        for (String line : lines) {
            String row = line.trim();
            if (!row.isEmpty() && !row.startsWith("#")) rows.add(row.split("\\|", -1));
        }
        if (rows.isEmpty()) throw new IllegalStateException("Overworld map is empty: " + fileName);

        width = rows.get(0).length;
        height = rows.size();
        if (width == 0) throw new IllegalStateException("Map rows must contain tile IDs: " + fileName);

        tiles = new Tile[width][height];
        for (int row = 0; row < height; row++) {
            String[] encodedRow = rows.get(row);
            if (encodedRow.length != width) {
                throw new IllegalStateException("Inconsistent map row width at row " + row);
            }
            for (int column = 0; column < width; column++) {
                try {
                    String encodedId = encodedRow[column].trim();
                    if (!encodedId.matches("[0-9A-Fa-f]{2}")) {
                        throw new IllegalArgumentException("Expected a two-digit hexadecimal ID");
                    }
                    int id = Integer.parseInt(encodedId, 16);
                    Tile tile = tileCatalog.get(id);
                    if (tile == null || !tile.isOverworld()) {
                        throw new IllegalArgumentException(String.format("Tile %02X is not defined for the overworld", id));
                    }
                    tiles[column][height - row - 1] = tile;
                } catch (IllegalArgumentException exception) {
                    throw new IllegalStateException("Invalid tile ID at row " + row + ", column " + column, exception);
                }
            }
        }
    }

    public int width() { return width; }
    public int height() { return height; }
    public Tile tileAt(int x, int y) { return tiles[x][y]; }
    public Map<Integer, Tile> tileCatalog() { return tileCatalog; }
    public Tile tileDefinition(int id) { return tileCatalog.get(id); }
}
