package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pokemoncasteneous.utils.IndexUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class TileCatalog {
    private static final String[] CSV_HEADER = {"id", "image", "isOverworld", "isForeground", "underlayId"};

    private final Map<Integer, Tile> tilesById;

    private TileCatalog(Map<Integer, Tile> tilesById) {
        this.tilesById = Collections.unmodifiableMap(new HashMap<>(tilesById));
    }

    public static TileCatalog load(String fileName) {
        FileHandle file = Gdx.files.internal(fileName);
        if (!file.exists()) throw new IllegalStateException("Missing tile catalog: " + fileName);

        Map<Integer, Tile> tiles = new HashMap<>();
        String[] sourceLines = file.readString().split("\\R");
        IndexedLine[] contentLines = IndexUtils.indices(sourceLines.length)
                .map(index -> new IndexedLine(index.value() + 1, sourceLines[index.value()].trim()))
                .filter(line -> !line.text().isEmpty() && !line.text().startsWith("#"))
                .toArray(IndexedLine[]::new);
        if (contentLines.length == 0) throw new IllegalStateException("Tile catalog is empty: " + fileName);

        String[] header = contentLines[0].text().split(",", -1);
        if (header.length != CSV_HEADER.length) throw catalogError(contentLines[0], "Invalid CSV header");
        IndexUtils.indices(CSV_HEADER.length).forEach(index -> {
            int column = index.value();
            if (!CSV_HEADER[column].equalsIgnoreCase(header[column].trim())) {
                throw catalogError(contentLines[0], "Invalid CSV header");
            }
        });

        Stream.of(contentLines).skip(1).forEach(line -> {
            String[] columns = line.text().split(",", -1);
            if (columns.length != CSV_HEADER.length) throw catalogError(line, "Expected five CSV columns");
            try {
                int id = parseId(columns[0]);
                String image = columns[1].trim();
                if (image.isEmpty()) throw new IllegalArgumentException("Image filename is empty");
                boolean isOverworld = parseBoolean(columns[2]);
                boolean isForeground = parseBoolean(columns[3]);
                int underlayId = columns[4].trim().isEmpty() ? -1 : parseId(columns[4]);
                if (isForeground && (!isOverworld || underlayId < 0)) {
                    throw new IllegalArgumentException("Foreground overworld tiles need an underlay ID");
                }
                Tile tile = new Tile(id, image, isOverworld, isForeground, underlayId);
                if (tiles.put(id, tile) != null) throw new IllegalArgumentException("Duplicate tile ID");
            } catch (IllegalArgumentException exception) {
                throw catalogError(line, exception.getMessage(), exception);
            }
        });

        tiles.values().stream().filter(Tile::isForeground).forEach(tile -> {
            Tile underlay = tiles.get(tile.underlayId());
            if (underlay == null || !underlay.isOverworld()) {
                throw new IllegalStateException(String.format(
                        "Tile %02X references an invalid overworld underlay %02X", tile.id(), tile.underlayId()));
            }
        });
        return new TileCatalog(tiles);
    }

    public Tile tileById(int id) { return tilesById.get(id); }
    public Map<Integer, Tile> tilesById() { return tilesById; }

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

    private static IllegalStateException catalogError(IndexedLine line, String message) {
        return new IllegalStateException("Invalid tile catalog at line " + line.number() + ": " + message);
    }

    private static IllegalStateException catalogError(IndexedLine line, String message, Throwable cause) {
        return new IllegalStateException("Invalid tile catalog at line " + line.number() + ": " + message, cause);
    }

    private record IndexedLine(int number, String text) { }
}
