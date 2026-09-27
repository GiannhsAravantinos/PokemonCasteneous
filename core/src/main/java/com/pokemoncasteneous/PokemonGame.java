package com.pokemoncasteneous;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokemoncasteneous.constants.ColorConstants;
import com.pokemoncasteneous.screens.OverworldScreen;
import com.pokemoncasteneous.utils.ScreenUtils;

public final class PokemonGame extends Game {
    private SpriteBatch batch;
    private final GameState state = new GameState();

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new OverworldScreen(this));
    }

    public SpriteBatch batch() { return batch; }
    public GameState state() { return state; }

    @Override
    public void render() {
        ScreenUtils.clear(ColorConstants.APP_BACKGROUND);
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
    }
}
