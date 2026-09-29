package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class OverworldTextures implements Disposable {
    private static final int PLAYER_SHEET_SIZE = 128;

    public final Texture playerSheet;
    public final TextureRegion[][] playerFrames;
    private final Map<Integer, Texture> tileTextures = new HashMap<>();

    public OverworldTextures(TileCatalog tileCatalog) {
        tileCatalog.tilesById().values().stream()
                .filter(Tile::isOverworld)
                .forEach(tile -> tileTextures.put(tile.id(), loadOptional("tiles/" + tile.image())));

        Texture loadedPlayerSheet = loadOptional("player.png");
        boolean sheetMatchesSpecs = loadedPlayerSheet != null
                && loadedPlayerSheet.getWidth() == PLAYER_SHEET_SIZE
                && loadedPlayerSheet.getHeight() == PLAYER_SHEET_SIZE;
        if (!sheetMatchesSpecs) {
            throw new RuntimeException("loadedPlayerSheet aint good");

        }
        playerSheet = loadedPlayerSheet;
        playerFrames = TextureRegion.split(playerSheet, 32, 32);
    }

    public Texture textureFor(Tile tile) {
        return tileTextures.get(tile.id());
    }

    private static Texture loadOptional(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        tileTextures.values().stream().filter(Objects::nonNull).forEach(Texture::dispose);
        if (playerSheet != null) playerSheet.dispose();
    }
}
