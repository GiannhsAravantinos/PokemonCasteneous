package com.pokemoncasteneous.assets;

public final class Tile {
    private final int id;
    private final String image;
    private final boolean overworld;
    private final boolean foreground;
    private final int underlayId;

    Tile(int id, String image, boolean overworld, boolean foreground, int underlayId) {
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
}
