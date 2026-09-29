package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
public final class Tile {
    private final int id;
    private final String image;
    private final boolean overworld;
    private final boolean foreground;
    private final int underlayId;

    private Tile(int id, String image, boolean overworld, boolean foreground, int underlayId) {
        this.id = id;
        this.image = image;
        this.overworld = overworld;
        this.foreground = foreground;
        this.underlayId = underlayId;
    }

    public int id() { return id; }
    public String image() { return image; }
    public boolean isOverworld() { return overworld; }
    public boolean isForeground() { return foreground; }
    public int underlayId() { return underlayId; }

    public static Map<Integer, Tile> loadCatalog(String fileName) {
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) throw new IllegalStateException("Missing tile catalog: " + fileName);

        String[] lines = file.readString().split("\\R");
        Map<Integer, Tile> tiles = new HashMap<>();
        boolean foundHeader = false;
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] columns = line.split(",", -1);
            if (!foundHeader) {
                String[] expected = {"id", "image", "isOverworld", "isForeground", "underlayId"};
                if (columns.length != expected.length) throw catalogError(lineNumber, "Invalid CSV header");
                for (int column = 0; column < expected.length; column++) {
                    if (!expected[column].equalsIgnoreCase(columns[column].trim())) {
                        throw catalogError(lineNumber, "Invalid CSV header");
                    }
                }
                foundHeader = true;
                continue;
            }

            if (columns.length != 5) throw catalogError(lineNumber, "Expected five CSV columns");
            try {
                int id = parseId(columns[0]);
                String image = columns[1].trim();
                if (image.isEmpty()) throw new IllegalArgumentException("Image filename is empty");
                boolean isOverworld = parseBoolean(columns[2]);
                boolean isForeground = parseBoolean(columns[3]);
                int underlayId = columns[4].trim().isEmpty() ? -1 : parseId(columns[4]);
                if (isForeground && underlayId < 0) {
                    throw new IllegalArgumentException("Foreground tiles need an underlay ID");
                }
                Tile tile = new Tile(id, image, isOverworld, isForeground, underlayId);
                if (tiles.put(id, tile) != null) throw new IllegalArgumentException("Duplicate tile ID");
            } catch (IllegalArgumentException exception) {
                throw catalogError(lineNumber, exception.getMessage(), exception);
            }
        }
        if (!foundHeader || tiles.isEmpty()) throw new IllegalStateException("Tile catalog is empty: " + fileName);

        for (Tile tile : tiles.values()) {
            if (tile.foreground && !tiles.containsKey(tile.underlayId)) {
                throw new IllegalStateException(String.format("Tile %02X references missing underlay %02X", tile.id, tile.underlayId));
            }
        }
        return Collections.unmodifiableMap(tiles);
    }

    private static int parseId(String value) {
        String id = value.trim();
        if (!id.matches("[0-9A-Fa-f]{2}")) throw new IllegalArgumentException("Tile IDs must be two hexadecimal digits");
        return Integer.parseInt(id, 16);
    }

    private static boolean parseBoolean(String value) {
        String booleanValue = value.trim();
        if (!booleanValue.equalsIgnoreCase("true") && !booleanValue.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Expected true or false");
        }
        return Boolean.parseBoolean(booleanValue);
    }

    private static IllegalStateException catalogError(int lineNumber, String message) {
        return new IllegalStateException("Invalid tile catalog at line " + (lineNumber + 1) + ": " + message);
    }

    private static IllegalStateException catalogError(int lineNumber, String message, Throwable cause) {
        return new IllegalStateException("Invalid tile catalog at line " + (lineNumber + 1) + ": " + message, cause);
    }
}
