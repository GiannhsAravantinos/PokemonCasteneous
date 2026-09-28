package com.pokemoncasteneous.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import static com.pokemoncasteneous.constants.ColorConstants.PLAYER_SKIN;
import static com.pokemoncasteneous.constants.ColorConstants.PLAYER_TUNIC;

public final class OverworldTextures implements Disposable {
    private static final int PLAYER_SHEET_SIZE = 128;

    public final Texture grassTile;
    public final Texture pathTile;
    public final Texture treeSprite;
    public final Texture playerSheet;
    public final TextureRegion[][] playerFrames;

    public OverworldTextures() {
        grassTile = loadOptional("grass.png");
        pathTile = loadOptional("dirt.png");
        treeSprite = loadOptional("tree.png");

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

    private static Texture loadOptional(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        if (grassTile != null) grassTile.dispose();
        if (pathTile != null) pathTile.dispose();
        if (treeSprite != null) treeSprite.dispose();
        if (playerSheet != null) playerSheet.dispose();
    }
}
